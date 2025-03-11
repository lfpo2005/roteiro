package dev.luisoliveira.roteiro.service;

import org.json.JSONArray;
import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import lombok.extern.slf4j.Slf4j;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Base64;

/**
 * Serviço para geração de imagens usando a API do Replicate.
 */
@Service
@Slf4j
public class ReplicateService {

    @Value("${replicate.api.key}")
    private String REPLICATE_API_KEY;

//    @Value("${replicate.model:stability-ai/sdxl:39ed52f2a78e934b3ba6e2a89f5b1c712de7dfea535525255b1aa35c5565e08b}")
//    private String MODEL;

    private static final String PREDICTIONS_URL = "https://api.replicate.com/v1/models/black-forest-labs/flux-schnell/predictions";
    private static final int MAX_RETRIES = 30;
    private static final int RETRY_DELAY_MS = 2000;

    /**
     * Gera uma imagem baseada no prompt fornecido
     *
     * @param prompt Prompt detalhado para geração da imagem
     * @param processId ID do processo para armazenamento
     * @param title Título da oração (usado para nome do arquivo)
     * @param outputDir Diretório onde a imagem será salva
     * @return Caminho do arquivo da imagem gerada
     * @throws IOException Em caso de erro na geração ou download
     */
    public String generateImage(String prompt, String processId, String title, String outputDir) throws IOException {
        log.info("Iniciando geração de imagem para o processo: {}", processId);
        log.debug("Prompt para imagem: {}", prompt);

        // Criar a predição (job) no Replicate
        String predictionId = createPrediction(prompt);
        log.info("Prediction criada com ID: {}", predictionId);

        // Esperar pela conclusão da geração
        String imageUrl = waitForPredictionCompletion(predictionId);
        log.info("Imagem gerada com sucesso, URL: {}", imageUrl);

        // Baixar e salvar a imagem
        String imagePath = downloadAndSaveImage(imageUrl, processId, title, outputDir);
        log.info("Imagem salva com sucesso em: {}", imagePath);

        return imagePath;
    }

    /**
     * Cria uma nova predição (job) no Replicate
     *
     * @param prompt Prompt para geração da imagem
     * @return ID da predição criada
     * @throws IOException Em caso de erro na comunicação com a API
     */
    private String createPrediction(String prompt) throws IOException {
        HttpURLConnection connection = null;
        try {
            // Configuração da conexão
            URL url = new URL(PREDICTIONS_URL);
            connection = (HttpURLConnection) url.openConnection();
            connection.setRequestMethod("POST");
            connection.setRequestProperty("Content-Type", "application/json");
            connection.setRequestProperty("Authorization", "Bearer " + REPLICATE_API_KEY);
            connection.setDoOutput(true);

            // Criar o corpo da requisição
            JSONObject requestBody = new JSONObject();

            // Configurar o modelo a ser usado
           // requestBody.put("version", MODEL);

            // Configurar os inputs para o modelo flux-schnell
            JSONObject input = new JSONObject();
            input.put("prompt", prompt);
            input.put("go_fast", true);  // Ativar modo rápido
            input.put("megapixels", "1");  // Resolução
            input.put("num_outputs", 1);  // Uma imagem por geração
            input.put("aspect_ratio", "16:9");  // Proporção ideal para capas
            input.put("output_format", "jpg");  // Formato de saída
            input.put("output_quality", 80);  // Qualidade boa, mas econômica
            input.put("num_inference_steps", 4);  // Mínimo de passos para economizar

            requestBody.put("input", input);

            // Debug log - imprimir o corpo da requisição
            log.debug("Corpo da requisição: {}", requestBody.toString());

            // Enviar a requisição
            try (OutputStream os = connection.getOutputStream()) {
                byte[] inputBytes = requestBody.toString().getBytes(StandardCharsets.UTF_8);
                os.write(inputBytes, 0, inputBytes.length);
            }

            // Verificar o código de resposta
            int responseCode = connection.getResponseCode();
            if (responseCode != HttpURLConnection.HTTP_CREATED) { // 201 Created
                log.error("Erro na requisição ao Replicate: Status code {}", responseCode);

                // Ler a mensagem de erro
                StringBuilder errorResponse = new StringBuilder();
                try (BufferedReader br = new BufferedReader(
                        new InputStreamReader(connection.getErrorStream(), StandardCharsets.UTF_8))) {
                    String responseLine;
                    while ((responseLine = br.readLine()) != null) {
                        errorResponse.append(responseLine.trim());
                    }
                }

                throw new IOException("Erro na API do Replicate: " + responseCode + " - " + errorResponse.toString());
            }

            // Ler a resposta
            StringBuilder response = new StringBuilder();
            try (BufferedReader br = new BufferedReader(
                    new InputStreamReader(connection.getInputStream(), StandardCharsets.UTF_8))) {
                String responseLine;
                while ((responseLine = br.readLine()) != null) {
                    response.append(responseLine.trim());
                }
            }

            // Extrair o ID da predição da resposta
            JSONObject responseJson = new JSONObject(response.toString());
            String predictionId = responseJson.getString("id");

            return predictionId;
        } finally {
            if (connection != null) {
                connection.disconnect();
            }
        }
    }

    /**
     * Aguarda a conclusão da geração da imagem, verificando periodicamente o status
     *
     * @param predictionId ID da predição a ser verificada
     * @return URL da imagem gerada
     * @throws IOException Em caso de erro na comunicação com a API
     */
    private String waitForPredictionCompletion(String predictionId) throws IOException {
        String predictionUrl = PREDICTIONS_URL + "/" + predictionId;

        for (int attempt = 0; attempt < MAX_RETRIES; attempt++) {
            HttpURLConnection connection = null;
            try {
                // Configuração da conexão
                URL url = new URL(predictionUrl);
                connection = (HttpURLConnection) url.openConnection();
                connection.setRequestMethod("POST");
                connection.setRequestProperty("Authorization", "Bearer " + REPLICATE_API_KEY);

                // Verificar o código de resposta
                int responseCode = connection.getResponseCode();
                if (responseCode != HttpURLConnection.HTTP_OK) { // 200
                    log.error("Erro ao verificar status da predição: Status code {}", responseCode);
                    throw new IOException("Erro ao verificar status da predição: " + responseCode);
                }

                // Ler a resposta
                StringBuilder response = new StringBuilder();
                try (BufferedReader br = new BufferedReader(
                        new InputStreamReader(connection.getInputStream(), StandardCharsets.UTF_8))) {
                    String responseLine;
                    while ((responseLine = br.readLine()) != null) {
                        response.append(responseLine.trim());
                    }
                }

                // Verificar o status da predição
                JSONObject responseJson = new JSONObject(response.toString());
                String status = responseJson.getString("status");

                log.debug("Status da predição {}: {}", predictionId, status);

                if ("succeeded".equals(status)) {
                    // A predição foi concluída com sucesso
                    JSONArray output = responseJson.getJSONArray("output");
                    return output.getString(0);  // Retorna a URL da primeira imagem gerada
                } else if ("failed".equals(status) || "canceled".equals(status)) {
                    // A predição falhou ou foi cancelada
                    String error = responseJson.has("error") ? responseJson.getString("error") : "Unknown error";
                    throw new IOException("A predição falhou ou foi cancelada: " + error);
                }

                // A predição ainda está em andamento, aguardar e tentar novamente
                log.debug("Predição em andamento, tentando novamente em {}ms...", RETRY_DELAY_MS);
                Thread.sleep(RETRY_DELAY_MS);

            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                throw new IOException("A operação foi interrompida", e);
            } finally {
                if (connection != null) {
                    connection.disconnect();
                }
            }
        }

        throw new IOException("Número máximo de tentativas excedido ao aguardar a conclusão da predição");
    }

    /**
     * Baixa e salva a imagem gerada
     *
     * @param imageUrl URL da imagem a ser baixada
     * @param processId ID do processo
     * @param title Título para o nome do arquivo
     * @param outputDir Diretório de saída
     * @return Caminho do arquivo salvo
     * @throws IOException Em caso de erro no download ou gravação do arquivo
     */
    private String downloadAndSaveImage(String imageUrl, String processId, String title, String outputDir) throws IOException {
        HttpURLConnection connection = null;
        try {
            // Configuração da conexão
            URL url = new URL(imageUrl);
            connection = (HttpURLConnection) url.openConnection();
            connection.setRequestMethod("GET");

            // Verificar o código de resposta
            int responseCode = connection.getResponseCode();
            if (responseCode != HttpURLConnection.HTTP_OK) { // 200
                log.error("Erro ao baixar imagem: Status code {}", responseCode);
                throw new IOException("Erro ao baixar imagem: " + responseCode);
            }

            // Criar nome de arquivo seguro baseado no título
            String safeTitle = title.replaceAll("[^a-zA-Z0-9]", "_")
                    .replaceAll("_+", "_");

            // Criar o diretório se não existir
            Path processDir = Paths.get(outputDir, processId);
            if (!Files.exists(processDir)) {
                Files.createDirectories(processDir);
            }

            // Caminho completo do arquivo
            String fileName = safeTitle + "_cover.jpg";
            Path filePath = processDir.resolve(fileName);

            // Baixar e salvar a imagem
            Files.copy(connection.getInputStream(), filePath);

            return filePath.toString();

        } finally {
            if (connection != null) {
                connection.disconnect();
            }
        }
    }
}
package dev.luisoliveira.roteiro.service;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import lombok.RequiredArgsConstructor;
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

import org.json.JSONObject;

/**
 * Serviço para conversão de texto para áudio usando a API da ElevenLabs.
 */
@Service
@Slf4j
@RequiredArgsConstructor
public class ElevenLabsService {

    @Value("${elevenlabs.api.key}")
    private String ELEVENLABS_API_KEY;

    @Value("${elevenlabs.voice.id:21m00Tcm4TlvDq8ikWAM}")
    private String DEFAULT_VOICE_ID;

    @Value("${elevenlabs.voice.david_trailer:TxGEqnHWrfWFTfGW9XjX}")
    private String DAVID_TRAILER_VOICE_ID;// Rachel voice ID por padrão

    @Value("${file.output.path:./gerados}")
    private String outputPath;

    private static final String TEXT_TO_SPEECH_URL = "https://api.elevenlabs.io/v1/text-to-speech/";
    private static final String GET_VOICES_URL = "https://api.elevenlabs.io/v1/voices";

    /**
     * Converte o texto da oração para áudio usando a API da ElevenLabs.
     *
     * @param text Texto para conversão
     * @param processId ID do processo
     * @param fileName Nome do arquivo sem extensão
     * @param voiceId ID da voz a ser usada (opcional)
     * @return Caminho do arquivo de áudio gerado
     * @throws IOException Em caso de erro na API ou ao salvar o arquivo
     */
    public String convertTextToSpeech(String text, String processId, String fileName, String voiceId) throws IOException {
        if (voiceId == null || voiceId.trim().isEmpty()) {
            voiceId = DAVID_TRAILER_VOICE_ID;
        }

        log.info("Iniciando conversão de texto para áudio com a ElevenLabs para processo: {}", processId);
        log.debug("Usando voz ID: {}", voiceId);

        HttpURLConnection connection = null;
        try {
            // Configuração da conexão
            URL url = new URL(TEXT_TO_SPEECH_URL + voiceId);
            connection = (HttpURLConnection) url.openConnection();
            connection.setRequestMethod("POST");
            connection.setRequestProperty("Content-Type", "application/json");
            connection.setRequestProperty("xi-api-key", ELEVENLABS_API_KEY);
            connection.setDoOutput(true);

            // Dividir o texto em pedaços menores se necessário
            // A API da ElevenLabs tem um limite de caracteres (pode variar com o plano)
            String[] textChunks = splitTextIntoChunks(text, 5000); // Tamanho máximo seguro

            if (textChunks.length == 1) {
                // Se tivermos apenas um pedaço, enviamos normalmente
                byte[] audioData = convertSingleChunk(textChunks[0], connection);
                return saveAudioToFile(audioData, processId, fileName);
            } else {
                // Se tivermos múltiplos pedaços, processamos cada um e concatenamos os resultados
                log.info("Texto dividido em {} pedaços para processamento", textChunks.length);
                return processMultipleChunks(textChunks, processId, fileName, voiceId);
            }
        } catch (Exception e) {
            log.error("Erro ao converter texto para áudio: {}", e.getMessage(), e);
            throw new IOException("Falha na conversão de texto para áudio: " + e.getMessage(), e);
        } finally {
            if (connection != null) {
                connection.disconnect();
            }
        }
    }

    /**
     * Converte um único trecho de texto para áudio.
     */
    private byte[] convertSingleChunk(String text, HttpURLConnection connection) throws IOException {
        // Criar o corpo da requisição
        JSONObject requestBody = new JSONObject();
        requestBody.put("text", text);

        // Podemos adicionar configurações de voz
        JSONObject voiceSettings = new JSONObject();
        voiceSettings.put("stability", 0.5);
        voiceSettings.put("similarity_boost", 0.75);
        requestBody.put("voice_settings", voiceSettings);

        // Modelo a ser usado
        requestBody.put("model_id", "eleven_multilingual_v2");

        // Enviar a requisição
        try (OutputStream os = connection.getOutputStream()) {
            byte[] input = requestBody.toString().getBytes(StandardCharsets.UTF_8);
            os.write(input, 0, input.length);
        }

        // Verificar o código de resposta
        int responseCode = connection.getResponseCode();
        if (responseCode != HttpURLConnection.HTTP_OK) { // 200
            StringBuilder errorResponse = new StringBuilder();
            try (BufferedReader br = new BufferedReader(
                    new InputStreamReader(connection.getErrorStream(), StandardCharsets.UTF_8))) {
                String responseLine;
                while ((responseLine = br.readLine()) != null) {
                    errorResponse.append(responseLine.trim());
                }
            }

            log.error("Erro na API da ElevenLabs: {} - {}", responseCode, errorResponse.toString());
            throw new IOException("Erro na API da ElevenLabs: " + responseCode + " - " + errorResponse.toString());
        }

        // Ler a resposta (dados de áudio)
        return connection.getInputStream().readAllBytes();
    }

    /**
     * Processa múltiplos trechos de texto e combina os resultados.
     */
    private String processMultipleChunks(String[] textChunks, String processId, String fileName, String voiceId) throws IOException {
        // Criar um diretório temporário para os arquivos de áudio parciais
        Path tempDir = createTempDirectory(processId);

        try {
            // Processar cada trecho e salvar como arquivo temporário
            Path[] tempFiles = new Path[textChunks.length];

            for (int i = 0; i < textChunks.length; i++) {
                log.info("Processando trecho {}/{}", i + 1, textChunks.length);

                HttpURLConnection connection = null;
                try {
                    // Configuração da conexão
                    URL url = new URL(TEXT_TO_SPEECH_URL + voiceId);
                    connection = (HttpURLConnection) url.openConnection();
                    connection.setRequestMethod("POST");
                    connection.setRequestProperty("Content-Type", "application/json");
                    connection.setRequestProperty("xi-api-key", ELEVENLABS_API_KEY);
                    connection.setDoOutput(true);

                    // Converter o trecho
                    byte[] audioData = convertSingleChunk(textChunks[i], connection);

                    // Salvar em arquivo temporário
                    String tempFileName = "temp_" + i + ".mp3";
                    Path tempFile = tempDir.resolve(tempFileName);
                    Files.write(tempFile, audioData);
                    tempFiles[i] = tempFile;

                } finally {
                    if (connection != null) {
                        connection.disconnect();
                    }
                }
            }

            // Combinar os arquivos de áudio usando ffmpeg ou outra ferramenta
            String outputFile = combineAudioFiles(tempFiles, processId, fileName);

            return outputFile;
        } finally {
            // Limpar arquivos temporários
            cleanupTempFiles(tempDir);
        }
    }

    /**
     * Cria um diretório temporário para os arquivos de áudio parciais.
     */
    private Path createTempDirectory(String processId) throws IOException {
        Path tempDir = Paths.get(outputPath, processId, "temp_audio");
        if (!Files.exists(tempDir)) {
            Files.createDirectories(tempDir);
        }
        return tempDir;
    }

    /**
     * Combina múltiplos arquivos de áudio em um único arquivo.
     * Usa FFmpeg se disponível, ou uma solução alternativa.
     */
    private String combineAudioFiles(Path[] audioFiles, String processId, String fileName) throws IOException {
        // Criar o diretório de saída se não existir
        Path processDir = Paths.get(outputPath, processId);
        if (!Files.exists(processDir)) {
            Files.createDirectories(processDir);
        }

        // Caminho do arquivo de saída
        String outputFileName = fileName + ".mp3";
        Path outputPath = processDir.resolve(outputFileName);

        // Tentar usar FFmpeg se disponível
        try {
            // Criar uma lista de arquivos para o FFmpeg
            StringBuilder ffmpegCommand = new StringBuilder();
            ffmpegCommand.append("ffmpeg");

            // Adicionar cada arquivo como input
            for (Path file : audioFiles) {
                ffmpegCommand.append(" -i ").append(file.toString());
            }

            // Adicionar filtro para concatenar
            ffmpegCommand.append(" -filter_complex \"");
            for (int i = 0; i < audioFiles.length; i++) {
                ffmpegCommand.append("[").append(i).append(":0]");
            }
            ffmpegCommand.append("concat=n=").append(audioFiles.length).append(":v=0:a=1[out]\" -map \"[out]\" ");

            // Adicionar output file
            ffmpegCommand.append(outputPath.toString());

            // Executar o comando
            Process process = Runtime.getRuntime().exec(ffmpegCommand.toString());
            int exitCode = process.waitFor();

            if (exitCode == 0) {
                log.info("Arquivos de áudio combinados com sucesso usando FFmpeg");
                return outputPath.toString();
            } else {
                log.warn("Falha ao combinar arquivos com FFmpeg (código {}), tentando método alternativo", exitCode);
                // Continuar com método alternativo
            }
        } catch (Exception e) {
            log.warn("FFmpeg não disponível ou erro: {}", e.getMessage());
            // Continuar com método alternativo
        }

        // Metodo alternativo: concatenar os bytes diretamente
        log.info("Usando método alternativo para combinar arquivos de áudio");

        try (OutputStream out = Files.newOutputStream(outputPath)) {
            // Para MP3, precisamos manter os headers do primeiro arquivo
            // e depois concatenar apenas os frames de áudio dos arquivos subsequentes
            // Esta é uma simplificação e pode não funcionar perfeitamente para todos os casos

            for (Path file : audioFiles) {
                byte[] fileData = Files.readAllBytes(file);
                out.write(fileData);
            }
        }

        log.info("Arquivos de áudio combinados com sucesso usando método alternativo");
        return outputPath.toString();
    }

    /**
     * Limpa os arquivos temporários após o processamento.
     */
    private void cleanupTempFiles(Path tempDir) {
        try {
            if (Files.exists(tempDir)) {
                Files.walk(tempDir)
                        .sorted(java.util.Comparator.reverseOrder())
                        .map(Path::toFile)
                        .forEach(file -> {
                            if (!file.delete()) {
                                log.warn("Não foi possível excluir arquivo temporário: {}", file.getAbsolutePath());
                            }
                        });
            }
        } catch (IOException e) {
            log.warn("Erro ao limpar arquivos temporários: {}", e.getMessage());
        }
    }

    /**
     * Salva os dados de áudio em um arquivo.
     *
     * @param audioData Dados de áudio em bytes
     * @param processId ID do processo
     * @param fileName Nome do arquivo sem extensão
     * @return Caminho do arquivo salvo
     * @throws IOException Em caso de erro ao salvar o arquivo
     */
    private String saveAudioToFile(byte[] audioData, String processId, String fileName) throws IOException {
        // Criar o diretório se não existir
        Path processDir = Paths.get(outputPath, processId);
        if (!Files.exists(processDir)) {
            Files.createDirectories(processDir);
        }

        // Salvar o arquivo de áudio
        String outputFileName = fileName + ".mp3";
        Path filePath = processDir.resolve(outputFileName);
        Files.write(filePath, audioData);

        log.info("Áudio salvo com sucesso em: {}", filePath);
        return filePath.toString();
    }

    /**
     * Divide o texto em pedaços menores para processamento.
     * Tenta dividir nos pontos finais para manter a coerência.
     *
     * @param text Texto a ser dividido
     * @param maxChunkSize Tamanho máximo de cada pedaço
     * @return Array de strings com os pedaços de texto
     */
    private String[] splitTextIntoChunks(String text, int maxChunkSize) {
        if (text.length() <= maxChunkSize) {
            return new String[] { text };
        }

        // Dividir o texto em parágrafos
        String[] paragraphs = text.split("\n\n");

        // Agrupar parágrafos em chunks sem exceder o tamanho máximo
        java.util.List<String> chunks = new java.util.ArrayList<>();
        StringBuilder currentChunk = new StringBuilder();

        for (String paragraph : paragraphs) {
            // Se o parágrafo sozinho exceder o tamanho máximo, precisamos dividir o parágrafo
            if (paragraph.length() > maxChunkSize) {
                // Se já temos conteúdo no chunk atual, finalizamos ele primeiro
                if (currentChunk.length() > 0) {
                    chunks.add(currentChunk.toString());
                    currentChunk = new StringBuilder();
                }

                // Dividir o parágrafo em sentenças
                String[] sentences = paragraph.split("(?<=[.!?])\\s+");

                for (String sentence : sentences) {
                    // Se a sentença sozinha exceder o tamanho máximo, dividimos por tamanho fixo
                    if (sentence.length() > maxChunkSize) {
                        int start = 0;
                        while (start < sentence.length()) {
                            int end = Math.min(start + maxChunkSize, sentence.length());
                            chunks.add(sentence.substring(start, end));
                            start = end;
                        }
                    } else if (currentChunk.length() + sentence.length() > maxChunkSize) {
                        // Se adicionar esta sentença exceder o tamanho máximo, finalizamos o chunk atual
                        chunks.add(currentChunk.toString());
                        currentChunk = new StringBuilder(sentence);
                    } else {
                        // Caso contrário, adicionamos a sentença ao chunk atual
                        if (currentChunk.length() > 0) {
                            currentChunk.append(" ");
                        }
                        currentChunk.append(sentence);
                    }
                }
            } else if (currentChunk.length() + paragraph.length() + 2 > maxChunkSize) {
                // Se adicionar este parágrafo exceder o tamanho máximo, finalizamos o chunk atual
                chunks.add(currentChunk.toString());
                currentChunk = new StringBuilder(paragraph);
            } else {
                // Caso contrário, adicionamos o parágrafo ao chunk atual
                if (currentChunk.length() > 0) {
                    currentChunk.append("\n\n");
                }
                currentChunk.append(paragraph);
            }
        }

        // Adicionar o último chunk se houver conteúdo
        if (currentChunk.length() > 0) {
            chunks.add(currentChunk.toString());
        }

        return chunks.toArray(new String[0]);
    }

    /**
     * Obtém a lista de vozes disponíveis na ElevenLabs.
     *
     * @return JSONObject contendo a lista de vozes
     * @throws IOException Em caso de erro na API
     */
    public JSONObject getAvailableVoices() throws IOException {
        HttpURLConnection connection = null;
        try {
            // Configuração da conexão
            URL url = new URL(GET_VOICES_URL);
            connection = (HttpURLConnection) url.openConnection();
            connection.setRequestMethod("GET");
            connection.setRequestProperty("xi-api-key", ELEVENLABS_API_KEY);

            // Verificar o código de resposta
            int responseCode = connection.getResponseCode();
            if (responseCode != HttpURLConnection.HTTP_OK) { // 200
                log.error("Erro ao obter vozes disponíveis: Status code {}", responseCode);
                throw new IOException("Erro ao obter vozes disponíveis: " + responseCode);
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

            // Converter a resposta para JSONObject
            return new JSONObject(response.toString());

        } finally {
            if (connection != null) {
                connection.disconnect();
            }
        }
    }

    /**
     * Converte texto para fala e salva como arquivo de áudio.
     * Método simplificado que usa valores padrão.
     *
     * @param text Texto para conversão
     * @param processId ID do processo
     * @param fileName Nome do arquivo sem extensão
     * @return Caminho do arquivo de áudio gerado
     * @throws IOException Em caso de erro
     */
    public String convertTextToSpeech(String text, String processId, String fileName) throws IOException {
        return convertTextToSpeech(text, processId, fileName, null);
    }
}
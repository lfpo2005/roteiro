package dev.luisoliveira.roteiro.service;

import lombok.extern.slf4j.Slf4j;
import org.json.JSONArray;
import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;

@Slf4j
@Service
public class OpenAIService {

    @Value("${openai.api.key}")
    private String OPENAI_API_KEY;

    @Value("${openai.model:gpt-4}")
    private String MODEL;

    private static final String COMPLETIONS_URL = "https://api.openai.com/v1/chat/completions";

    public List<String> generateTitles(String prompt) {
        String response = callGpt(prompt);
        return parseTitlesFromResponse(response);
    }

    public String generateOracao(String prompt) {
        return callGpt(prompt);
    }

    public String generateDescription(String prompt) {
        return callGpt(prompt);
    }

    private String callGpt(String prompt) {
        log.info("Iniciando requisição ao OpenAI...");
        HttpURLConnection connection = null;
        try {
            // Configuração da conexão
            URL url = new URL(COMPLETIONS_URL);
            connection = (HttpURLConnection) url.openConnection();
            connection.setRequestMethod("POST");
            connection.setRequestProperty("Content-Type", "application/json");
            connection.setRequestProperty("Authorization", "Bearer " + OPENAI_API_KEY);
            connection.setDoOutput(true);

            // Criar o corpo da requisição
            JSONObject requestBody = new JSONObject();
            requestBody.put("model", MODEL);

            JSONArray messages = new JSONArray();

            JSONObject systemMessage = new JSONObject();
            systemMessage.put("role", "system");
            systemMessage.put("content", "Você é um assistente especializado em criar conteúdo religioso para YouTube.");
            messages.put(systemMessage);

            JSONObject userMessage = new JSONObject();
            userMessage.put("role", "user");
            userMessage.put("content", prompt);
            messages.put(userMessage);

            requestBody.put("messages", messages);
            requestBody.put("temperature", 0.7);

            // Enviar a requisição
            try (OutputStream os = connection.getOutputStream()) {
                byte[] input = requestBody.toString().getBytes(StandardCharsets.UTF_8);
                os.write(input, 0, input.length);
            }

            // Verificar o código de resposta
            int responseCode = connection.getResponseCode();
            if (responseCode != HttpURLConnection.HTTP_OK) { // 200
                log.error("Erro na requisição: Status code {}", responseCode);
                throw new RuntimeException("Erro na API do OpenAI: " + responseCode);
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

            // Processar a resposta JSON
            JSONObject responseJson = new JSONObject(response.toString());
            String content = responseJson.getJSONArray("choices")
                    .getJSONObject(0)
                    .getJSONObject("message")
                    .getString("content");

            log.info("Requisição concluída com sucesso");
            return content;

        } catch (IOException e) {
            log.error("Erro ao processar a requisição", e);
            throw new RuntimeException("Erro ao processar a requisição para OpenAI", e);
        } finally {
            if (connection != null) {
                connection.disconnect();
            }
        }
    }

    private List<String> parseTitlesFromResponse(String response) {
        List<String> titles = new ArrayList<>();
        String[] lines = response.split("\n");

        for (String line : lines) {
            line = line.trim();
            if (line.startsWith("-") || line.startsWith("*") || line.matches("^\\d+\\..*")) {
                // Remove prefixos de lista como "- ", "* ", "1. "
                titles.add(line.replaceFirst("^[-*\\d]+[.\\s]+", "").trim());
            }
        }

        return titles;
    }
}
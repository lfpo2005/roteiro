package dev.luisoliveira.roteiro.service;

import lombok.extern.slf4j.Slf4j;
import org.apache.hc.client5.http.classic.methods.HttpPost;
import org.apache.hc.client5.http.impl.classic.CloseableHttpClient;
import org.apache.hc.client5.http.impl.classic.CloseableHttpResponse;
import org.apache.hc.client5.http.impl.classic.HttpClients;
import org.apache.hc.core5.http.ContentType;
import org.apache.hc.core5.http.io.entity.StringEntity;
import org.json.JSONArray;
import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.io.IOException;
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
        try (CloseableHttpClient client = HttpClients.createDefault()) {
            HttpPost post = new HttpPost(COMPLETIONS_URL);

            // Configura os headers
            post.setHeader("Authorization", "Bearer " + OPENAI_API_KEY);
            post.setHeader("Content-Type", "application/json");

            // Configura o corpo da requisição
            JSONObject json = new JSONObject();
            json.put("model", MODEL);

            // Adiciona mensagem do sistema
            JSONArray messages = new JSONArray();
            JSONObject systemMessage = new JSONObject();
            systemMessage.put("role", "system");
            systemMessage.put("content", "Você é um assistente especializado em criar conteúdo religioso para YouTube.");
            messages.put(systemMessage);

            // Adiciona mensagem do usuário
            JSONObject userMessage = new JSONObject();
            userMessage.put("role", "user");
            userMessage.put("content", prompt);
            messages.put(userMessage);

            json.put("messages", messages);
            json.put("temperature", 0.7);

            post.setEntity(new StringEntity(json.toString(), ContentType.APPLICATION_JSON));

            try (CloseableHttpResponse response = client.execute(post)) {
                int statusCode = response.getCode();
                if (statusCode != 200) {
                    log.error("Erro na requisição: Status code {}", statusCode);
                    throw new RuntimeException("Erro na API do OpenAI: " + statusCode);
                }

                String result = new String(response.getEntity().getContent().readAllBytes(), StandardCharsets.UTF_8);
                JSONObject responseJson = new JSONObject(result);

                String content = responseJson.getJSONArray("choices")
                        .getJSONObject(0)
                        .getJSONObject("message")
                        .getString("content");

                log.info("Requisição concluída com sucesso");

                return content;
            }
        } catch (IOException e) {
            log.error("Erro ao processar a requisição", e);
            throw new RuntimeException("Erro ao processar a requisição para OpenAI", e);
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
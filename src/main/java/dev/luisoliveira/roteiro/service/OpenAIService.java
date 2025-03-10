package dev.luisoliveira.roteiro.service;

import lombok.extern.slf4j.Slf4j;
import org.json.JSONArray;
import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Slf4j
@Service
public class OpenAIService {

    private final RestTemplate restTemplate;

    @Value("${openai.api.key}")
    private String OPENAI_API_KEY;

    @Value("${openai.model:gpt-4}")
    private String MODEL;

    private static final String COMPLETIONS_URL = "https://api.openai.com/v1/chat/completions";

    public OpenAIService() {
        this.restTemplate = new RestTemplate();
    }

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
        try {
            // Configurar headers
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            headers.set("Authorization", "Bearer " + OPENAI_API_KEY);

            // Criar objeto para corpo da requisição
            Map<String, Object> systemMessage = new HashMap<>();
            systemMessage.put("role", "system");
            systemMessage.put("content", "Você é um assistente especializado em criar conteúdo religioso para YouTube.");

            Map<String, Object> userMessage = new HashMap<>();
            userMessage.put("role", "user");
            userMessage.put("content", prompt);

            List<Map<String, Object>> messages = new ArrayList<>();
            messages.add(systemMessage);
            messages.add(userMessage);

            Map<String, Object> requestBody = new HashMap<>();
            requestBody.put("model", MODEL);
            requestBody.put("messages", messages);
            requestBody.put("temperature", 0.7);

            // Criar entidade HTTP
            HttpEntity<Map<String, Object>> entity = new HttpEntity<>(requestBody, headers);

            // Fazer a requisição
            ResponseEntity<String> response = restTemplate.postForEntity(COMPLETIONS_URL, entity, String.class);

            log.info("Resposta da API OpenAI: status code {}", response.getStatusCode());

            if (response.getStatusCode().is2xxSuccessful() && response.getBody() != null) {
                JSONObject responseJson = new JSONObject(response.getBody());

                String content = responseJson.getJSONArray("choices")
                        .getJSONObject(0)
                        .getJSONObject("message")
                        .getString("content");

                log.info("Requisição concluída com sucesso");
                return content;
            } else {
                log.error("Erro na requisição: Status code {}", response.getStatusCode());
                throw new RuntimeException("Erro na API do OpenAI: " + response.getStatusCode());
            }
        } catch (Exception e) {
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
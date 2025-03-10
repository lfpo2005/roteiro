package dev.luisoliveira.roteiro.service;

import com.theokanning.openai.completion.chat.ChatCompletionRequest;
import com.theokanning.openai.completion.chat.ChatMessage;
import com.theokanning.openai.service.OpenAiService;
import org.springframework.stereotype.Service;
import lombok.RequiredArgsConstructor;

import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
public class OpenAIService {

    private final OpenAiService openAiService;
    private static final String GPT_MODEL = "gpt-4";

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

    public String generateImagePrompt(String prompt) {
        return callGpt(prompt);
    }

    private String callGpt(String prompt) {
        List<ChatMessage> messages = new ArrayList<>();
        messages.add(new ChatMessage("system", "Você é um assistente especializado em criar conteúdo religioso para YouTube."));
        messages.add(new ChatMessage("user", prompt));

        ChatCompletionRequest request = ChatCompletionRequest.builder()
                .model(GPT_MODEL)
                .messages(messages)
                .temperature(0.7)
                .build();

        return openAiService.createChatCompletion(request)
                .getChoices().get(0).getMessage().getContent();
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
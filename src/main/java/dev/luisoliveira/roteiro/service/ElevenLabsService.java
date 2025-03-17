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
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.web.client.RestTemplate;

import com.fasterxml.jackson.databind.ObjectMapper;

import java.util.HashMap;
import java.util.Map;

/**
 * Serviço para integração com a API ElevenLabs para geração de áudio (versão
 * MongoDB)
 */
@Service
@Slf4j
public class ElevenLabsService {

    private final RestTemplate restTemplate;

    @Value("${elevenlabs.api.key}")
    private String apiKey;

    @Value("${elevenlabs.voice.david_trailer:ZQe5CZNOzWyzPSCn5a3c}")
    private String voiceId;

    @Value("${elevenlabs.api.url:https://api.elevenlabs.io/v1}")
    private String apiUrl;

    @Value("${file.output.path:./gerados}")
    private String outputPath;

    public ElevenLabsService(RestTemplate restTemplate) {
        this.restTemplate = restTemplate;
        log.info("ElevenLabsService inicializado");
    }

    /**
     * Gera áudio a partir de texto usando a API ElevenLabs
     * 
     * @param text Texto para conversão em áudio
     * @return Array de bytes contendo o áudio gerado
     */
    public byte[] generateSpeech(String text) {
        try {
            log.info("Gerando áudio para texto (tamanho: {} caracteres)", text.length());

            // URL para a API ElevenLabs
            String url = apiUrl + "/text-to-speech/" + voiceId;

            // Configurar os headers da requisição
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            headers.set("xi-api-key", apiKey);

            // Corpo da requisição
            Map<String, Object> requestBody = new HashMap<>();
            requestBody.put("text", text);

            // Configurações de estabilidade e similaridade de voz
            Map<String, Object> voiceSettings = new HashMap<>();
            voiceSettings.put("stability", 0.5);
            voiceSettings.put("similarity_boost", 0.75);
            requestBody.put("voice_settings", voiceSettings);

            // Criar a entidade HTTP
            HttpEntity<Map<String, Object>> requestEntity = new HttpEntity<>(requestBody, headers);

            // Fazer a requisição POST e retornar o áudio como bytes
            byte[] audioData = restTemplate.postForObject(url, requestEntity, byte[].class);
            log.info("Áudio gerado com sucesso: {} bytes", audioData != null ? audioData.length : 0);

            return audioData;
        } catch (Exception e) {
            log.error("Erro ao gerar áudio com ElevenLabs: {}", e.getMessage(), e);
            throw new RuntimeException("Falha ao gerar áudio: " + e.getMessage(), e);
        }
    }

    /**
     * Gera áudio a partir de texto e salva em arquivo
     * 
     * @param text      Texto para gerar áudio
     * @param processId ID do processo para nomear o arquivo
     * @return Caminho do arquivo de áudio gerado
     * @throws IOException Se ocorrer erro ao salvar o arquivo
     */
    public String generateSpeech(String text, String processId) throws IOException {
        log.info("Gerando áudio para texto de {} caracteres, processo: {}", text.length(), processId);

        // Gerar o áudio usando o método existente
        byte[] audioData = generateSpeech(text);

        // Criar diretório de saída se não existir
        Path outputDir = Paths.get(outputPath);
        if (!Files.exists(outputDir)) {
            Files.createDirectories(outputDir);
        }

        // Salvar o áudio em arquivo
        String fileName = "audio_" + processId + ".mp3";
        Path audioFile = outputDir.resolve(fileName);
        Files.write(audioFile, audioData);

        log.info("Áudio salvo em: {}", audioFile.toString());
        return audioFile.toString();
    }
}
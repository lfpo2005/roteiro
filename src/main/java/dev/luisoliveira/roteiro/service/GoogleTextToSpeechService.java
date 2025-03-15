package dev.luisoliveira.roteiro.service;

import com.google.cloud.texttospeech.v1.*;
import com.google.protobuf.ByteString;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import lombok.extern.slf4j.Slf4j;

import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.Map;

/**
 * Serviço para geração de áudio usando a API Text-to-Speech do Google Cloud.
 */
@Service
@Slf4j
public class GoogleTextToSpeechService {

    @Value("${google.tts.enabled:false}")
    private boolean enabled;

    // Mapeamento de idiomas para vozes específicas
    private static final Map<String, VoiceConfig> VOICE_CONFIGS = new HashMap<>();

    static {
        // Configurações para espanhol (padrão)
        VOICE_CONFIGS.put("es", new VoiceConfig(
                "es-ES", "es-ES-Wavenet-B", SsmlVoiceGender.MALE));

        // Configurações para espanhol latino/mexicano
        VOICE_CONFIGS.put("es-MX", new VoiceConfig(
                "es-US", "es-US-Neural2-A", SsmlVoiceGender.FEMALE));

        // Configurações para português
        VOICE_CONFIGS.put("pt", new VoiceConfig(
                "pt-BR", "pt-BR-Neural2-B", SsmlVoiceGender.MALE));

        VOICE_CONFIGS.put("pt-BR", new VoiceConfig(
                "pt-BR", "pt-BR-Neural2-B", SsmlVoiceGender.MALE));

        // Configurações para inglês
        VOICE_CONFIGS.put("en", new VoiceConfig(
                "en-US", "en-US-Neural2-D", SsmlVoiceGender.MALE));
    }

    /**
     * Gera um arquivo de áudio a partir de um texto.
     *
     * @param text Texto a ser convertido em áudio
     * @param outputFilePath Caminho onde o arquivo de áudio será salvo
     * @param idioma Idioma do texto (es, es-MX, pt, pt-BR, en)
     * @return Caminho do arquivo de áudio gerado
     * @throws IOException Em caso de erro na geração ou gravação do arquivo
     */
    public String generateAudio(String text, String outputFilePath, String idioma) throws IOException {
        if (!enabled) {
            log.warn("Google Text-to-Speech está desabilitado. Defina google.tts.enabled=true para ativá-lo.");
            throw new IllegalStateException("Google Text-to-Speech está desabilitado");
        }

        try (TextToSpeechClient textToSpeechClient = TextToSpeechClient.create()) {
            // Selecionando a configuração de voz apropriada para o idioma
            VoiceConfig voiceConfig = VOICE_CONFIGS.getOrDefault(idioma, VOICE_CONFIGS.get("es"));

            // Configure a entrada
            SynthesisInput input = SynthesisInput.newBuilder()
                    .setText(text)
                    .build();

            // Configure a voz
            VoiceSelectionParams voice = VoiceSelectionParams.newBuilder()
                    .setLanguageCode(voiceConfig.languageCode)
                    .setName(voiceConfig.voiceName)
                    .setSsmlGender(voiceConfig.gender)
                    .build();

            // Configure o áudio
            AudioConfig audioConfig = AudioConfig.newBuilder()
                    .setAudioEncoding(AudioEncoding.MP3)
                    .setSpeakingRate(0.95) // Velocidade da fala (0.5 a 2.0)
                    .setPitch(0) // Pitch da voz (-20.0 a 20.0)
                    .setVolumeGainDb(1) // Volume (-96.0 a 16.0)
                    .build();

            // Realizar a chamada à API TTS
            SynthesizeSpeechResponse response = textToSpeechClient.synthesizeSpeech(
                    input, voice, audioConfig);

            // Obter o conteúdo do áudio da resposta
            ByteString audioContents = response.getAudioContent();

            // Garantir que o diretório de saída exista
            Path outputPath = Paths.get(outputFilePath);
            Files.createDirectories(outputPath.getParent());

            // Escrever o conteúdo do áudio no arquivo
            try (OutputStream out = new FileOutputStream(outputFilePath)) {
                out.write(audioContents.toByteArray());
            }

            log.info("Áudio gerado com sucesso e salvo em: {}", outputFilePath);
            return outputFilePath;
        } catch (Exception e) {
            log.error("Erro ao gerar áudio: {}", e.getMessage(), e);
            throw new IOException("Erro ao gerar áudio: " + e.getMessage(), e);
        }
    }

    /**
     * Classe interna para configurações de voz
     */
    private static class VoiceConfig {
        final String languageCode;
        final String voiceName;
        final SsmlVoiceGender gender;

        VoiceConfig(String languageCode, String voiceName, SsmlVoiceGender gender) {
            this.languageCode = languageCode;
            this.voiceName = voiceName;
            this.gender = gender;
        }
    }
}
package dev.luisoliveira.roteiro.service;

import dev.luisoliveira.roteiro.event.AudioGeneratedEvent;
import dev.luisoliveira.roteiro.event.DescriptionGeneratedEvent;
import dev.luisoliveira.roteiro.util.FileUtils;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Service;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;

/**
 * Serviço para geração de áudio a partir do texto da oração.
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class AudioGenerationService {

    private final GoogleTextToSpeechService textToSpeechService;
    private final EventBusService eventBusService;
    private final ProcessTrackingService processTrackingService;

    @Value("${file.output.path:./gerados}")
    private String outputBasePath;

    @Value("${audio.generation.enabled:false}")
    private boolean audioGenerationEnabled;

    /**
     * Método que escuta eventos de descrição gerada e gera áudio
     * baseado no conteúdo da oração.
     */
    @EventListener
    public void handleDescriptionGeneratedEvent(DescriptionGeneratedEvent event) {
        try {
            String processId = event.getProcessId();

            // Verificar se a geração de áudio está habilitada para este processo
            if (!processTrackingService.deveGerarAudio(processId)) {
                log.info("Geração de áudio desabilitada para o processo: {}", processId);
                return;
            }

            // Verificar configuração global de áudio
            if (!audioGenerationEnabled) {
                log.info("Geração de áudio está desabilitada globalmente.");
                return;
            }

            // Atualizar status
            processTrackingService.updateStatus(
                    processId,
                    "Gerando áudio da oração...",
                    92
            );

            // Obter dados necessários
            String titulo = event.getTitle();
            String oracaoContent = event.getOracaoContent();
            String shortContent = event.getShortContent();
            String idioma = processTrackingService.getIdioma(processId);

            log.info("Gerando áudio para oração completa e versão curta no idioma: {}", idioma);

            // Criar diretório para áudios
            Path processDir = Paths.get(outputBasePath, processId);
            String safeTitle = FileUtils.createSafeFileName(titulo);

            // Gerar áudio da oração completa
            String fullAudioPath = processDir.resolve(safeTitle + "_full.mp3").toString();
            textToSpeechService.generateAudio(oracaoContent, fullAudioPath, idioma);
            log.info("Áudio da oração completa gerado com sucesso: {}", fullAudioPath);

            // Gerar áudio da versão curta
            String shortAudioPath = processDir.resolve(safeTitle + "_short.mp3").toString();
            textToSpeechService.generateAudio(shortContent, shortAudioPath, idioma);
            log.info("Áudio da versão curta gerado com sucesso: {}", shortAudioPath);

            // Atualizar status
            processTrackingService.updateStatus(
                    processId,
                    "Áudio gerado com sucesso",
                    94
            );

            // Publicar evento notificando que o áudio foi gerado
            eventBusService.publish(new AudioGeneratedEvent(
                    processId,
                    event.getTitle(),
                    fullAudioPath,
                    shortAudioPath
            ));

        } catch (Exception e) {
            // Lidar com erros
            log.error("Erro ao gerar áudio: {}", e.getMessage(), e);
            processTrackingService.updateStatus(
                    event.getProcessId(),
                    "Aviso: Não foi possível gerar o áudio: " + e.getMessage(),
                    93  // Ainda continua o processo, apenas sem áudio
            );
        }
    }
}
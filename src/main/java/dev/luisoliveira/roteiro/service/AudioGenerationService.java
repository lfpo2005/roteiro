package dev.luisoliveira.roteiro.service;

import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Service;

import dev.luisoliveira.roteiro.event.AudioGenerationEvent;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

/**
 * Serviço para geração de áudio (versão transitória para MongoDB)
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class AudioGenerationService {

    private final ProcessTrackingService processTrackingService;
    private final ElevenLabsService elevenLabsService;
    private final FileStorageService fileStorageService;

    /**
     * Manipula o evento de geração de áudio
     * 
     * @param event Evento de geração de áudio
     */
    @EventListener
    public void handleAudioGenerationEvent(AudioGenerationEvent event) {
        String processId = event.getProcessId();
        log.info("Iniciando geração de áudio para processo: {}", processId);
        processTrackingService.updateStatus(processId, "Gerando áudio", 85);

        try {
            // Gera e salva o áudio para a versão completa
            String fullContent = processTrackingService.getOracaoContent(processId);
            if (fullContent != null && !fullContent.isEmpty()) {
                byte[] fullAudioData = elevenLabsService.generateSpeech(fullContent);
                String fullAudioId = fileStorageService.saveAudio(fullAudioData);

                // Gera e salva o áudio para a versão curta, se existir
                String shortContent = processTrackingService.getShortContent(processId);
                String shortAudioId = null;
                if (shortContent != null && !shortContent.isEmpty() &&
                        Boolean.TRUE.equals(processTrackingService.getGerarVersaoShort(processId))) {
                    byte[] shortAudioData = elevenLabsService.generateSpeech(shortContent);
                    shortAudioId = fileStorageService.saveAudio(shortAudioData);
                }

                // Armazena os IDs dos áudios no serviço de rastreamento
                processTrackingService.storeAudioIds(processId, fullAudioId, shortAudioId);

                log.info("Áudio gerado com sucesso para processo: {}", processId);
                processTrackingService.updateStatus(processId, "Áudio gerado", 95);
            } else {
                log.warn("Conteúdo da oração não disponível para geração de áudio: {}", processId);
                processTrackingService.updateStatus(processId, "Áudio não gerado - conteúdo indisponível", 95);
            }
        } catch (Exception e) {
            log.error("Erro ao gerar áudio para processo {}: {}", processId, e.getMessage(), e);
            processTrackingService.updateStatus(processId, "Erro na geração de áudio: " + e.getMessage(), 95);
        } finally {
            // Finaliza o processo independentemente do resultado da geração de áudio
            processTrackingService.updateStatus(processId, "Concluído", 100);
        }
    }
}
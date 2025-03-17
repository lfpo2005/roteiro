package dev.luisoliveira.roteiro.service;

import dev.luisoliveira.roteiro.event.AudioGenerationEvent;
import dev.luisoliveira.roteiro.event.ContentCompletedEvent;
import dev.luisoliveira.roteiro.model.PrayerContent;
import dev.luisoliveira.roteiro.repository.PrayerContentRepository;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Service;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import java.util.Optional;

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
    private final PrayerContentRepository prayerContentRepository;
    private final ApplicationEventPublisher eventPublisher;

    @Value("${audio.generation.enabled:false}")
    private boolean audioGenerationEnabled;

    /**
     * Manipula o evento de geração de áudio
     * 
     * @param event Evento de geração de áudio
     */
    @EventListener
    public void handleAudioGenerationEvent(AudioGenerationEvent event) {
        String processId = event.getProcessId();
        log.info("Recebido evento AudioGenerationEvent para processId: {}", processId);

        if (!audioGenerationEnabled) {
            log.warn("Geração de áudio está desabilitada nas configurações. Pulando geração para processo: {}",
                    processId);
            processTrackingService.updateStatus(processId, "Geração de áudio desabilitada", 100);
            return;
        }

        try {
            // Atualizar status
            processTrackingService.updateStatus(processId, "Gerando áudio...", 80);

            // Obter conteúdo da oração
            String oracaoContent = processTrackingService.getOracaoContent(processId);
            if (oracaoContent == null || oracaoContent.isEmpty()) {
                throw new RuntimeException("Conteúdo da oração não encontrado para o processo: " + processId);
            }

            // Gerar áudio
            log.info("Iniciando geração de áudio para oração (tamanho: {} caracteres)", oracaoContent.length());
            String audioFilePath = elevenLabsService.generateSpeech(oracaoContent, processId);
            log.info("Áudio gerado com sucesso: {}", audioFilePath);

            // Armazenar ID do áudio
            processTrackingService.storeAudioIds(processId, audioFilePath, null);

            // Atualizar a oração no MongoDB com a URL do áudio
            String oracaoId = processTrackingService.getOracaoId(processId);
            if (oracaoId != null) {
                log.info("Atualizando oração no MongoDB com URL do áudio. ID da oração: {}", oracaoId);
                Optional<PrayerContent> oracaoOpt = prayerContentRepository.findById(oracaoId);
                if (oracaoOpt.isPresent()) {
                    PrayerContent oracao = oracaoOpt.get();
                    oracao.setAudioUrl(audioFilePath);
                    prayerContentRepository.save(oracao);
                    log.info("Oração atualizada com URL do áudio: {}", audioFilePath);
                } else {
                    log.warn("Oração não encontrada no MongoDB com ID: {}", oracaoId);
                }
            } else {
                log.warn("ID da oração não encontrado para o processo: {}", processId);
            }

            // Atualizar status
            processTrackingService.updateStatus(processId, "Áudio gerado com sucesso", 100);
            log.info("Processo de geração de áudio concluído com sucesso para processo: {}", processId);

            // Publicar evento de conclusão para notificação via WebSocket
            String title = processTrackingService.getTitulo(processId);
            eventPublisher.publishEvent(new ContentCompletedEvent(processId, title, audioFilePath));

        } catch (Exception e) {
            log.error("Erro ao gerar áudio: {}", e.getMessage(), e);
            processTrackingService.updateStatus(processId, "Erro ao gerar áudio: " + e.getMessage(), 0);
        }
    }
}
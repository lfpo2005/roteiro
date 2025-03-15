package dev.luisoliveira.roteiro.service;

import dev.luisoliveira.roteiro.event.AudioGeneratedEvent;
import dev.luisoliveira.roteiro.event.DescriptionGeneratedEvent;
import dev.luisoliveira.roteiro.util.FileUtils;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Service;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import java.io.IOException;

/**
 * Serviço para geração de áudio das orações usando a API da ElevenLabs.
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class AudioGenerationService {

    private final ElevenLabsService elevenLabsService;
    private final EventBusService eventBusService;
    private final ProcessTrackingService processTrackingService;

    @EventListener
    public void handleDescriptionGeneratedEvent(DescriptionGeneratedEvent event) {
        String processId = event.getProcessId();

        // Verificar se o áudio deve ser gerado para este processo
        if (!processTrackingService.deveGerarAudio(processId)) {
            log.info("Geração de áudio desativada para o processo: {}", processId);
            return;
        }

        try {
            String title = event.getTitle();
            String oracaoContent = event.getOracaoContent();
            String shortContent = event.getShortContent();

            // Atualizar status
            processTrackingService.updateStatus(
                    processId,
                    "Gerando áudio da oração...",
                    88
            );

            log.info("Iniciando geração de áudio para o processo: {}", processId);

            // Criar nome de arquivo seguro baseado no título
            String safeTitle = FileUtils.createSafeFileName(title);

            // Gerar áudio para a oração completa
            String fullAudioPath = elevenLabsService.convertTextToSpeech(
                    oracaoContent,
                    processId,
                    safeTitle
            );
            log.info("Áudio da oração completa gerado com sucesso: {}", fullAudioPath);

            // Verificar se o conteúdo short é realmente diferente do conteúdo original
            // Se for igual, significa que não houve geração de versão short personalizada
            boolean isShortDifferent = !shortContent.equals(oracaoContent);
            String shortAudioPath = null;

            // Somente gerar áudio da versão short se houve geração de short personalizado
            if (isShortDifferent) {
                log.info("Gerando áudio para versão short personalizada");
                shortAudioPath = elevenLabsService.convertTextToSpeech(
                        shortContent,
                        processId,
                        safeTitle + "_short"
                );
                log.info("Áudio da versão curta gerado com sucesso: {}", shortAudioPath);
            } else {
                log.info("Pulando geração de áudio short pois não houve geração de versão short personalizada");
            }

            // Atualizar status
            processTrackingService.updateStatus(
                    processId,
                    "Áudio gerado com sucesso",
                    90
            );

            // Armazenar caminhos dos arquivos de áudio
            processTrackingService.storeAudioPaths(processId, fullAudioPath, shortAudioPath);

            // Publicar evento com os caminhos dos arquivos de áudio
            eventBusService.publish(new AudioGeneratedEvent(
                    processId,
                    title,
                    fullAudioPath,
                    shortAudioPath
            ));

        } catch (IOException e) {
            log.error("Erro ao gerar áudio: {}", e.getMessage(), e);
            processTrackingService.updateStatus(
                    event.getProcessId(),
                    "Aviso: Não foi possível gerar o áudio: " + e.getMessage(),
                    89  // Continua o processo mesmo sem áudio
            );
        }
    }
}
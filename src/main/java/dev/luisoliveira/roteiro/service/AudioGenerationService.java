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

    /**
     * Escuta o evento de descrição gerada para gerar o áudio da oração.
     */
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

            // Gerar áudio para a versão curta da oração
            String shortAudioPath = elevenLabsService.convertTextToSpeech(
                    shortContent,
                    processId,
                    safeTitle + "_short"
            );
            log.info("Áudio da versão curta gerado com sucesso: {}", shortAudioPath);

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

            // Além do evento, continuar o fluxo normal com a geração de imagem
            // Isso garante compatibilidade com o fluxo existente

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
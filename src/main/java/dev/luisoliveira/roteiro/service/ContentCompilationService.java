package dev.luisoliveira.roteiro.service;

import dev.luisoliveira.roteiro.event.ContentCompletedEvent;
import dev.luisoliveira.roteiro.event.DescriptionGeneratedEvent;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Service;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Service
@RequiredArgsConstructor
@Slf4j
public class ContentCompilationService {

    private final FileStorageService fileStorageService;
    private final EventBusService eventBusService;
    private final ProcessTrackingService processTrackingService;

    @EventListener
    public void handleDescriptionGeneratedEvent(DescriptionGeneratedEvent event) {
        try {
            String processId = event.getProcessId();

            // Atualizar status
            processTrackingService.updateStatus(
                    processId,
                    "Compilando conteúdo final...",
                    95
            );

            log.info("Compilando conteúdo final para processo: {}", processId);

            // Obter a lista de títulos alternativos (todos os títulos gerados)
            java.util.List<String> allTitles = processTrackingService.getTitles(processId);

            // Salvar o conteúdo compilado em arquivos (txt e srt)
            String outputPath = fileStorageService.saveOracaoFile(
                    processId,
                    event.getTitle(),
                    event.getOracaoContent(),
                    event.getShortContent(),
                    event.getDescriptionContent(),
                    allTitles
            );

            // Armazenar o caminho do resultado
            processTrackingService.storeResult(processId, outputPath);

            // Atualizar status final
            processTrackingService.updateStatus(
                    processId,
                    "Processo concluído com sucesso! Arquivos gerados em: " + outputPath,
                    100
            );

            log.info("Conteúdo compilado com sucesso. Arquivos gerados em: {}", outputPath);

            // Publicar evento de conclusão
            eventBusService.publish(new ContentCompletedEvent(
                    processId,
                    event.getTitle(),
                    outputPath
            ));
        } catch (Exception e) {
            // Lidar com erros
            log.error("Erro ao compilar conteúdo: {}", e.getMessage(), e);
            processTrackingService.updateStatus(
                    event.getProcessId(),
                    "Erro ao compilar conteúdo: " + e.getMessage(),
                    0
            );
        }
    }
}
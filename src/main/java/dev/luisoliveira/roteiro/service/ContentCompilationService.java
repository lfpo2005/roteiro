package dev.luisoliveira.roteiro.service;

import dev.luisoliveira.roteiro.event.ContentCompletedEvent;
import dev.luisoliveira.roteiro.event.DescriptionGeneratedEvent;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Service;
import lombok.RequiredArgsConstructor;


@Service
@RequiredArgsConstructor
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

            // Salvar o conteúdo compilado em um arquivo
            String filePath = fileStorageService.saveOracaoFile(
                    processId,
                    event.getTitle(),
                    event.getOracaoContent(),
                    event.getShortContent(),
                    event.getDescriptionContent()
            );

            // Armazenar o caminho do resultado
            processTrackingService.storeResult(processId, filePath);

            // Atualizar status final
            processTrackingService.updateStatus(
                    processId,
                    "Processo concluído com sucesso! Arquivo gerado: " + filePath,
                    100
            );

            // Publicar evento de conclusão
            eventBusService.publish(new ContentCompletedEvent(
                    processId,
                    event.getTitle(),
                    filePath
            ));
        } catch (Exception e) {
            // Lidar com erros
            processTrackingService.updateStatus(
                    event.getProcessId(),
                    "Erro ao compilar conteúdo: " + e.getMessage(),
                    0
            );
        }
    }
}

package dev.luisoliveira.roteiro.service;

import dev.luisoliveira.roteiro.event.AudioGeneratedEvent;
import dev.luisoliveira.roteiro.event.ContentCompletedEvent;
import dev.luisoliveira.roteiro.event.DescriptionGeneratedEvent;
import dev.luisoliveira.roteiro.event.ImagePromptGeneratedEvent;
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

    /**
     * Escuta o evento de imagem gerada para compilar o conteúdo final.
     * Se a geração de imagem for bem-sucedida, esse método será chamado.
     */
    @EventListener
    public void handleImagePromptGeneratedEvent(ImagePromptGeneratedEvent event) {
        try {
            String processId = event.getProcessId();

            // Atualizar status
            processTrackingService.updateStatus(
                    processId,
                    "Compilando conteúdo final...",
                    95
            );

            log.info("Compilando conteúdo final para processo: {}", processId);

            // Buscar os dados necessários do processo
            String title = event.getTitle();
            String oracaoContent = processTrackingService.getOracaoContent(processId);
            String shortContent = processTrackingService.getShortContent(processId);
            String descriptionContent = processTrackingService.getDescriptionContent(processId);

            // Verificar se temos todos os dados necessários
            if (oracaoContent == null || shortContent == null || descriptionContent == null) {
                throw new IllegalStateException("Dados incompletos para compilação do conteúdo");
            }

            // Obter a lista de títulos alternativos (todos os títulos gerados)
            java.util.List<String> allTitles = processTrackingService.getTitles(processId);

            // Salvar o conteúdo compilado em arquivos (txt e srt)
            String outputPath = fileStorageService.saveOracaoFile(
                    processId,
                    title,
                    oracaoContent,
                    shortContent,
                    descriptionContent,
                    allTitles,
                    event.getImagePath() // Novo parâmetro: caminho da imagem gerada
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
                    title,
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

    /**
     * Fallback para quando a geração de imagem falha.
     * Este método garante que o processo continue mesmo se a imagem não for gerada.
     */
    @EventListener
    public void handleDescriptionGeneratedEvent(DescriptionGeneratedEvent event) {
        // Verificar se a imagem já está sendo processada
        if (processTrackingService.isImageBeingProcessed(event.getProcessId())) {
            // Imagem já está sendo processada, não precisa fazer nada
            log.debug("Imagem sendo processada para {}, pulando compilação direta", event.getProcessId());
            return;
        }

        try {
            String processId = event.getProcessId();

            // Atualizar status
            processTrackingService.updateStatus(
                    processId,
                    "Compilando conteúdo final (sem imagem)...",
                    95
            );

            log.info("Compilando conteúdo final (sem imagem) para processo: {}", processId);

            // Obter a lista de títulos alternativos (todos os títulos gerados)
            java.util.List<String> allTitles = processTrackingService.getTitles(processId);

            // Salvar o conteúdo compilado em arquivos (txt e srt)
            String outputPath = fileStorageService.saveOracaoFile(
                    processId,
                    event.getTitle(),
                    event.getOracaoContent(),
                    event.getShortContent(),
                    event.getDescriptionContent(),
                    allTitles,
                    null  // Sem imagem
            );

            // Armazenar o caminho do resultado
            processTrackingService.storeResult(processId, outputPath);

            // Atualizar status final
            processTrackingService.updateStatus(
                    processId,
                    "Processo concluído com sucesso (sem imagem)! Arquivos gerados em: " + outputPath,
                    100
            );

            log.info("Conteúdo compilado com sucesso (sem imagem). Arquivos gerados em: {}", outputPath);

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

    /**
     * Handler para o evento de áudio gerado.
     * Este método compila o conteúdo final incluindo os arquivos de áudio.
     */
    @EventListener
    public void handleAudioGeneratedEvent(AudioGeneratedEvent event) {
        try {
            String processId = event.getProcessId();

            // Atualizar status
            processTrackingService.updateStatus(
                    processId,
                    "Áudios gerados, compilando conteúdo final...",
                    95
            );

            log.info("Compilando conteúdo final com áudios para processo: {}", processId);

            // Armazenar os caminhos dos áudios
            processTrackingService.storeAudioPaths(processId, event.getFullAudioPath(), event.getShortAudioPath());

            // Buscar os dados necessários do processo
            String title = event.getTitle();
            String oracaoContent = processTrackingService.getOracaoContent(processId);
            String shortContent = processTrackingService.getShortContent(processId);
            String descriptionContent = processTrackingService.getDescriptionContent(processId);

            // Verificar se temos todos os dados necessários
            if (oracaoContent == null || shortContent == null || descriptionContent == null) {
                throw new IllegalStateException("Dados incompletos para compilação do conteúdo");
            }

            // Obter a lista de títulos alternativos (todos os títulos gerados)
            java.util.List<String> allTitles = processTrackingService.getTitles(processId);

            // Salvar o conteúdo compilado em arquivos (txt e srt)
            String outputPath = fileStorageService.saveOracaoFile(
                    processId,
                    title,
                    oracaoContent,
                    shortContent,
                    descriptionContent,
                    allTitles,
                    null, // Não temos caminho de imagem neste fluxo
                    event.getFullAudioPath(),
                    event.getShortAudioPath()
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
                    title,
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
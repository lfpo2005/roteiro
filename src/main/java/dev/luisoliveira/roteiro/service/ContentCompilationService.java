package dev.luisoliveira.roteiro.service;

import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;

import dev.luisoliveira.roteiro.dto.TitleCompletionRequest;
import dev.luisoliveira.roteiro.event.AudioGenerationEvent;
import dev.luisoliveira.roteiro.event.ContentCompilationCompletedEvent;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

/**
 * Serviço para compilação de conteúdo de orações (versão transitória para
 * MongoDB)
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class ContentCompilationService {

        private final ApplicationEventPublisher eventPublisher;
        private final ProcessTrackingService processTrackingService;
        private final FileStorageService fileStorageService;

        /**
         * Compila o conteúdo da oração completa, versão curta e descrição
         * 
         * @param processId ID do processo
         */
        public void compileContent(String processId) {
                log.info("Iniciando compilação de conteúdo para processo: {}", processId);
                processTrackingService.updateStatus(processId, "Compilando conteúdo", 70);

                // Obtém o conteúdo da oração do serviço de rastreamento
                String oracaoContent = processTrackingService.getOracaoContent(processId);
                String shortContent = processTrackingService.getShortContent(processId);
                String descriptionContent = processTrackingService.getDescriptionContent(processId);
                String title = processTrackingService.getTitulo(processId);

                // Formata e salva o conteúdo da oração
                String formattedContent = formatPrayerContent(title, oracaoContent, shortContent, descriptionContent);

                // Salva o conteúdo como string e obtém um ID único
                String contentId = fileStorageService.saveOracaoFile(processId, formattedContent);
                log.info("Conteúdo compilado e salvo com ID: {}", contentId);

                // Armazena o resultado (ID do conteúdo)
                processTrackingService.storeResult(processId, contentId);

                // Publica evento de conclusão da compilação
                processTrackingService.updateStatus(processId, "Conteúdo compilado", 80);
                eventPublisher.publishEvent(new ContentCompilationCompletedEvent(this, processId, contentId));

                // Se o áudio deve ser gerado, publica o evento para iniciar a geração
                if (processTrackingService.deveGerarAudio(processId)) {
                        log.info("Iniciando geração de áudio para processo: {}", processId);
                        eventPublisher.publishEvent(new AudioGenerationEvent(this, processId));
                } else {
                        log.info("Geração de áudio não solicitada para processo: {}", processId);
                        processTrackingService.updateStatus(processId, "Concluído", 100);
                }
        }

        /**
         * Completa o título com base na primeira geração de conteúdo
         * 
         * @param processId ID do processo
         * @param request   Requisição com sugestões de título
         */
        public void completeTitleGeneration(String processId, TitleCompletionRequest request) {
                log.info("Completando geração de título para processo: {}", processId);

                if (request.getSelectedTitle() != null && !request.getSelectedTitle().isEmpty()) {
                        String selectedTitle = request.getSelectedTitle();
                        log.info("Título selecionado: {}", selectedTitle);
                        processTrackingService.setTitulo(processId, selectedTitle);
                } else if (request.getTitles() != null && !request.getTitles().isEmpty()) {
                        // Se nenhum título foi selecionado, mas há sugestões, use o primeiro
                        String firstTitle = request.getTitles().get(0);
                        log.info("Nenhum título selecionado, usando o primeiro: {}", firstTitle);
                        processTrackingService.setTitulo(processId, firstTitle);
                }

                // Salva a lista de títulos sugeridos
                if (request.getTitles() != null) {
                        processTrackingService.saveTitles(processId, request.getTitles());
                }
        }

        /**
         * Formata o conteúdo da oração para exibição
         * 
         * @param title              Título da oração
         * @param fullContent        Conteúdo completo da oração
         * @param shortContent       Conteúdo da versão curta
         * @param descriptionContent Conteúdo da descrição
         * @return Conteúdo formatado
         */
        private String formatPrayerContent(String title, String fullContent, String shortContent,
                        String descriptionContent) {
                StringBuilder formattedContent = new StringBuilder();

                // Adiciona o título
                if (title != null && !title.trim().isEmpty()) {
                        formattedContent.append("# ").append(title).append("\n\n");
                }

                // Adiciona a descrição
                if (descriptionContent != null && !descriptionContent.trim().isEmpty()) {
                        formattedContent.append("## Descrição\n\n").append(descriptionContent).append("\n\n");
                }

                // Adiciona o conteúdo completo
                formattedContent.append("## Oração Completa\n\n").append(fullContent).append("\n\n");

                // Adiciona a versão curta, se disponível
                if (shortContent != null && !shortContent.trim().isEmpty()) {
                        formattedContent.append("## Versão Curta\n\n").append(shortContent).append("\n\n");
                }

                return formattedContent.toString();
        }
}
package dev.luisoliveira.roteiro.service;

import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;

import dev.luisoliveira.roteiro.dto.TitleCompletionRequest;
import dev.luisoliveira.roteiro.event.AudioGenerationEvent;
import dev.luisoliveira.roteiro.event.ContentCompilationCompletedEvent;
import dev.luisoliveira.roteiro.event.ContentCompletedEvent;
import dev.luisoliveira.roteiro.model.PrayerContent;
import dev.luisoliveira.roteiro.repository.PrayerContentRepository;
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
        private final PrayerContentRepository prayerContentRepository;

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

                // Verifica se já existe uma oração salva no MongoDB para este processo
                String oracaoId = processTrackingService.getOracaoId(processId);
                if (oracaoId == null) {
                        // Se não existir, cria uma nova oração no MongoDB
                        log.info("Criando nova oração no MongoDB para o processo: {}", processId);
                        PrayerContent oracao = new PrayerContent(oracaoContent);
                        oracao.setTitle(title);
                        oracao.setProcessId(processId);
                        oracao.setShortContent(shortContent);
                        oracao.setDescription(descriptionContent);

                        // Adicionar outros metadados disponíveis
                        oracao.setTheme(processTrackingService.getTema(processId));
                        oracao.setStyle(processTrackingService.getEstiloOracao(processId));
                        oracao.setDuration(processTrackingService.getDuracao(processId));
                        oracao.setLanguage(processTrackingService.getIdioma(processId));

                        oracao = prayerContentRepository.save(oracao);
                        oracaoId = oracao.getId();
                        processTrackingService.storeOracaoId(processId, oracaoId);
                        log.info("Oração salva no MongoDB com ID: {}", oracaoId);
                } else {
                        // Se existir, atualiza a oração existente
                        log.info("Atualizando oração existente no MongoDB. ID: {}", oracaoId);
                        prayerContentRepository.findById(oracaoId).ifPresent(oracao -> {
                                oracao.setTexto(oracaoContent);
                                oracao.setTitle(title);
                                oracao.setShortContent(shortContent);
                                oracao.setDescription(descriptionContent);

                                // Atualizar outros metadados se necessário
                                if (oracao.getTheme() == null) {
                                        oracao.setTheme(processTrackingService.getTema(processId));
                                }
                                if (oracao.getStyle() == null) {
                                        oracao.setStyle(processTrackingService.getEstiloOracao(processId));
                                }
                                if (oracao.getDuration() == null) {
                                        oracao.setDuration(processTrackingService.getDuracao(processId));
                                }
                                if (oracao.getLanguage() == null) {
                                        oracao.setLanguage(processTrackingService.getIdioma(processId));
                                }

                                prayerContentRepository.save(oracao);
                                log.info("Oração atualizada no MongoDB");
                        });
                }

                // Armazena o resultado (ID do conteúdo)
                processTrackingService.storeResult(processId, contentId);

                // Publica evento de conclusão da compilação
                processTrackingService.updateStatus(processId, "Conteúdo compilado", 80);
                eventPublisher.publishEvent(new ContentCompilationCompletedEvent(this, processId, contentId));

                // Publicar também um ContentCompletedEvent para notificação via WebSocket
                eventPublisher.publishEvent(new ContentCompletedEvent(processId, title, contentId));

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
                StringBuilder content = new StringBuilder();

                // Adicionar título
                if (title != null && !title.isEmpty()) {
                        content.append("# ").append(title).append("\n\n");
                }

                // Adicionar conteúdo completo
                if (fullContent != null && !fullContent.isEmpty()) {
                        content.append("## Oração Completa\n\n").append(fullContent).append("\n\n");
                }

                // Adicionar versão curta, se existir
                if (shortContent != null && !shortContent.isEmpty()) {
                        content.append("## Versão Curta\n\n").append(shortContent).append("\n\n");
                }

                // Adicionar descrição, se existir
                if (descriptionContent != null && !descriptionContent.isEmpty()) {
                        content.append("## Descrição\n\n").append(descriptionContent).append("\n\n");
                }

                return content.toString();
        }
}
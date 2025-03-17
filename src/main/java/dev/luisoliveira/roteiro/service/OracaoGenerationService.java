package dev.luisoliveira.roteiro.service;

import dev.luisoliveira.roteiro.event.OracaoGeneratedEvent;
import dev.luisoliveira.roteiro.event.TitleSelectedEvent;
import dev.luisoliveira.roteiro.model.PrayerContent;
import dev.luisoliveira.roteiro.repository.PrayerContentRepository;
import dev.luisoliveira.roteiro.util.PromptBuilder;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Service;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Service
@RequiredArgsConstructor
@Slf4j
public class OracaoGenerationService {

        private final OpenAIService openAIService;
        private final EventBusService eventBusService;
        private final ProcessTrackingService processTrackingService;
        private final PrayerContentRepository prayerContentRepository;

        @EventListener
        public void handleTitleSelectedEvent(TitleSelectedEvent event) {
                log.info("Recebido evento TitleSelectedEvent para processId: {} com título: {}",
                                event.getProcessId(), event.getSelectedTitle());

                try {
                        String processId = event.getProcessId();
                        String selectedTitle = event.getSelectedTitle();

                        // Atualizar status
                        processTrackingService.updateStatus(
                                        processId,
                                        "Gerando oração para o título selecionado...",
                                        50);
                        log.info("Status atualizado para 'Gerando oração...' (50%)");

                        // Recuperar informações do processo
                        String tema = processTrackingService.getTema(processId);
                        String estiloOracao = processTrackingService.getEstiloOracao(processId);
                        String duracao = processTrackingService.getDuracao(processId);
                        String idioma = processTrackingService.getIdioma(processId);

                        log.info("Recuperadas informações do processo: tema={}, estilo={}, duracao={}, idioma={}",
                                        tema, estiloOracao, duracao, idioma);

                        if (tema == null || estiloOracao == null || duracao == null) {
                                log.error("Informações incompletas para o processo: {}", processId);
                                throw new RuntimeException("Informações incompletas para o processo: " + processId);
                        }

                        // Construir prompt otimizado para oração
                        log.info("Construindo prompt para oração no idioma: {}", idioma);
                        String prompt = PromptBuilder.buildOracaoPrompt(
                                        tema,
                                        estiloOracao,
                                        duracao,
                                        selectedTitle,
                                        idioma);
                        log.debug("Prompt construído: {}", prompt);

                        // Chamar OpenAI API
                        log.info("Iniciando chamada à API OpenAI para gerar oração...");
                        String oracaoContent = openAIService.generateOracao(prompt);
                        processTrackingService.setOracaoContent(processId, oracaoContent);
                        log.info("Oração gerada com sucesso (tamanho: {} caracteres)", oracaoContent.length());

                        // Salvar a oração no MongoDB
                        log.info("Salvando oração no MongoDB...");
                        PrayerContent oracao = new PrayerContent(oracaoContent);
                        oracao.setTitle(selectedTitle);
                        oracao.setTheme(tema);
                        oracao.setStyle(estiloOracao);
                        oracao.setDuration(duracao);
                        oracao.setLanguage(idioma);
                        oracao.setProcessId(processId);

                        oracao = prayerContentRepository.save(oracao);
                        log.info("Oração salva no MongoDB com ID: {}", oracao.getId());

                        // Armazenar o ID da oração no processo
                        processTrackingService.storeOracaoId(processId, oracao.getId());

                        // Atualizar status
                        processTrackingService.updateStatus(
                                        processId,
                                        "Oração gerada com sucesso",
                                        70);
                        log.info("Status atualizado para 'Oração gerada com sucesso' (70%)");

                        // Publicar evento com resultado
                        log.info("Publicando evento OracaoGeneratedEvent...");
                        eventBusService.publish(new OracaoGeneratedEvent(
                                        processId,
                                        selectedTitle,
                                        oracaoContent));
                        log.info("Evento OracaoGeneratedEvent publicado com sucesso");

                } catch (Exception e) {
                        // Lidar com erros
                        log.error("Erro ao gerar oração: {}", e.getMessage(), e);
                        processTrackingService.updateStatus(
                                        event.getProcessId(),
                                        "Erro ao gerar oração: " + e.getMessage(),
                                        0);
                }
        }
}
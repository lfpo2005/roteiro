package dev.luisoliveira.roteiro.service;

import dev.luisoliveira.roteiro.event.DescriptionGeneratedEvent;
import dev.luisoliveira.roteiro.event.ShortGeneratedEvent;
import dev.luisoliveira.roteiro.util.PromptBuilder;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Service;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Service
@RequiredArgsConstructor
@Slf4j
public class DescriptionGenerationService {

    private final OpenAIService openAIService;
    private final EventBusService eventBusService;
    private final ProcessTrackingService processTrackingService;

    @EventListener
    public void handleShortGeneratedEvent(ShortGeneratedEvent event) {
        try {
            String processId = event.getProcessId();

            // Atualizar status
            processTrackingService.updateStatus(
                    processId,
                    "Gerando descrição para YouTube e TikTok...",
                    85
            );

            // Obter o idioma do processo
            String idioma = processTrackingService.getIdioma(processId);
            log.info("Gerando descrição para YouTube e TikTok no idioma: {}", idioma);

            // Construir prompt otimizado para descrição
            String prompt = PromptBuilder.buildDescriptionPrompt(
                    event.getTitle(),
                    event.getOracaoContent(),
                    idioma
            );

            // Chamar OpenAI API
            log.info("Iniciando geração da descrição no idioma: {}", idioma);
            String descriptionContent = openAIService.generateDescription(prompt);
            log.info("Descrição gerada com sucesso: {} caracteres", descriptionContent.length());

            // Atualizar status
            processTrackingService.updateStatus(
                    processId,
                    "Descrição gerada com sucesso",
                    90
            );

            // Publicar evento com resultado
            eventBusService.publish(new DescriptionGeneratedEvent(
                    processId,
                    event.getTitle(),
                    event.getOracaoContent(),
                    event.getShortContent(),
                    descriptionContent
            ));
        } catch (Exception e) {
            // Lidar com erros
            log.error("Erro ao gerar descrição: {}", e.getMessage(), e);
            processTrackingService.updateStatus(
                    event.getProcessId(),
                    "Erro ao gerar descrição: " + e.getMessage(),
                    0
            );
        }
    }
}
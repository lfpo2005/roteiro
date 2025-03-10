package dev.luisoliveira.roteiro.service;

import dev.luisoliveira.roteiro.event.DescriptionGeneratedEvent;
import dev.luisoliveira.roteiro.event.ShortGeneratedEvent;
import dev.luisoliveira.roteiro.util.PromptBuilder;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Service;
import lombok.RequiredArgsConstructor;


@Service
@RequiredArgsConstructor
public class DescriptionGenerationService {

    private final OpenAIService openAIService;
    private final EventBusService eventBusService;
    private final ProcessTrackingService processTrackingService;

    @EventListener
    public void handleShortGeneratedEvent(ShortGeneratedEvent event) {
        try {
            String processId = event.getProcessId(); // Correção aqui

            // Atualizar status
            processTrackingService.updateStatus(
                    processId,
                    "Gerando descrição para YouTube e TikTok...",
                    85
            );

            // Construir prompt otimizado para descrição
            String prompt = PromptBuilder.buildDescriptionPrompt(
                    event.getTitle(),
                    event.getOracaoContent()
            );

            // Chamar OpenAI API
            String descriptionContent = openAIService.generateDescription(prompt);

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
            processTrackingService.updateStatus(
                    event.getProcessId(),
                    "Erro ao gerar descrição: " + e.getMessage(),
                    0
            );
        }
    }
}
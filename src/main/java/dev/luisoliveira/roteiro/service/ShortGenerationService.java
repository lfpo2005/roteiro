package dev.luisoliveira.roteiro.service;

import dev.luisoliveira.roteiro.event.OracaoGeneratedEvent;
import dev.luisoliveira.roteiro.event.ShortGeneratedEvent;
import dev.luisoliveira.roteiro.util.PromptBuilder;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Service;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class ShortGenerationService {

    private final OpenAIService openAIService;
    private final EventBusService eventBusService;
    private final ProcessTrackingService processTrackingService;

    @EventListener
    public void handleOracaoGeneratedEvent(OracaoGeneratedEvent event) {
        try {
            String processId = event.getProcessId();

            // Atualizar status
            processTrackingService.updateStatus(
                    processId,
                    "Gerando versão short da oração...",
                    75
            );

            // Construir prompt otimizado para short
            String prompt = PromptBuilder.buildShortPrompt(
                    event.getOracaoContent(),
                    event.getTitle()
            );

            // Chamar OpenAI API
            String shortContent = openAIService.generateOracao(prompt);

            // Atualizar status
            processTrackingService.updateStatus(
                    processId,
                    "Versão short gerada com sucesso",
                    80
            );

            // Publicar evento com resultado
            eventBusService.publish(new ShortGeneratedEvent(
                    processId,
                    event.getTitle(),
                    event.getOracaoContent(),
                    shortContent
            ));
        } catch (Exception e) {
            // Lidar com erros
            processTrackingService.updateStatus(
                    event.getProcessId(),
                    "Erro ao gerar versão short: " + e.getMessage(),
                    0
            );
        }
    }
}

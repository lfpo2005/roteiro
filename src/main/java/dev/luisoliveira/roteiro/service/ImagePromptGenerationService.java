package dev.luisoliveira.roteiro.service;

import dev.luisoliveira.roteiro.event.DescriptionGeneratedEvent;
import dev.luisoliveira.roteiro.event.ImagePromptGeneratedEvent;
import dev.luisoliveira.roteiro.util.PromptBuilder;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Service;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class ImagePromptGenerationService {

    private final OpenAIService openAIService;
    private final EventBusService eventBusService;
    private final ProcessTrackingService processTrackingService;

    @EventListener
    public void handleDescriptionGeneratedEvent(DescriptionGeneratedEvent event) {
        try {
            String processId = event.getProcessId();

            // Atualizar status
            processTrackingService.updateStatus(
                    processId,
                    "Gerando prompt para imagem de miniatura...",
                    92
            );

            // Construir prompt otimizado para geração de imagem
            String prompt = PromptBuilder.buildImagePromptPrompt(
                    event.getTitle(),
                    event.getOracaoContent()
            );

            // Chamar OpenAI API
            String imagePrompt = openAIService.generateImagePrompt(prompt);

            // Atualizar status
            processTrackingService.updateStatus(
                    processId,
                    "Prompt para imagem gerado com sucesso",
                    94
            );

            // Publicar evento com resultado
            eventBusService.publish(new ImagePromptGeneratedEvent(
                    processId,
                    event.getTitle(),
                    imagePrompt
            ));
        } catch (Exception e) {
            // Lidar com erros
            processTrackingService.updateStatus(
                    event.getProcessId(),
                    "Erro ao gerar prompt para imagem: " + e.getMessage(),
                    0
            );
        }
    }
}
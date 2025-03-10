package dev.luisoliveira.roteiro.service;

import dev.luisoliveira.roteiro.event.ContentInitiatedEvent;
import dev.luisoliveira.roteiro.event.TitleSelectedEvent;
import dev.luisoliveira.roteiro.util.PromptBuilder;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Service;
import lombok.RequiredArgsConstructor;

import java.util.List;

@Service
@RequiredArgsConstructor
public class TitleGenerationService {

    private final OpenAIService openAIService;
    private final EventBusService eventBusService;
    private final ProcessTrackingService processTrackingService;

    @EventListener
    public void handleContentInitiatedEvent(ContentInitiatedEvent event) {
        try {
            // Atualizar status
            processTrackingService.updateStatus(
                    event.getProcessId(),
                    "Gerando títulos...",
                    20
            );

            // Construir prompt otimizado
            String prompt = PromptBuilder.buildTitlePrompt(
                    event.getTema(),
                    event.getEstiloOracao()
            );

            // Chamar OpenAI API
            List<String> titles = openAIService.generateTitles(prompt);

            // Armazenar títulos
            processTrackingService.storeTitles(event.getProcessId(), titles);

            // Atualizar status
            processTrackingService.updateStatus(
                    event.getProcessId(),
                    "Títulos gerados com sucesso",
                    40
            );

            // Publicar evento com resultados
            eventBusService.publish(new TitleSelectedEvent(
                    event.getProcessId(),
                    titles,
                    event.getTema(),
                    event.getEstiloOracao(),
                    event.getDuracao(),
                    event.getTipoOracao()
            ));
        } catch (Exception e) {
            // Lidar com erros
            processTrackingService.updateStatus(
                    event.getProcessId(),
                    "Erro ao gerar títulos: " + e.getMessage(),
                    0
            );
        }
    }
}
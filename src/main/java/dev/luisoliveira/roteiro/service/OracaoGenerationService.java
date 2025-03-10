package dev.luisoliveira.roteiro.service;

import dev.luisoliveira.roteiro.dto.ProcessStatus;
import dev.luisoliveira.roteiro.event.OracaoGeneratedEvent;
import dev.luisoliveira.roteiro.event.TitleSelectedEvent;
import dev.luisoliveira.roteiro.util.PromptBuilder;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Service;
import lombok.RequiredArgsConstructor;

import java.util.List;

@Service
@RequiredArgsConstructor
public class OracaoGenerationService {

    private final OpenAIService openAIService;
    private final EventBusService eventBusService;
    private final ProcessTrackingService processTrackingService;

    @EventListener
    public void handleTitleSelectedEvent(TitleSelectedEvent event) {
        try {
            String processId = event.getProcessId();
            String selectedTitle = event.getSelectedTitle();

            // Atualizar status
            processTrackingService.updateStatus(
                    processId,
                    "Gerando oração para o título selecionado...",
                    50
            );

            // Recuperar informações do processo
            ProcessStatus status = processTrackingService.getStatus(processId);
            if (status == null) {
                throw new RuntimeException("Processo não encontrado: " + processId);
            }

            // Construir prompt otimizado para oração
            String prompt = PromptBuilder.buildOracaoPrompt(
                    status.getTema(),
                    status.getEstiloOracao(),
                    status.getDuracao(),
                    selectedTitle
            );

            // Chamar OpenAI API
            String oracaoContent = openAIService.generateOracao(prompt);

            // Atualizar status
            processTrackingService.updateStatus(
                    processId,
                    "Oração gerada com sucesso",
                    70
            );

            // Publicar evento com resultado
            eventBusService.publish(new OracaoGeneratedEvent(
                    processId,
                    selectedTitle,
                    oracaoContent
            ));
        } catch (Exception e) {
            // Lidar com erros
            processTrackingService.updateStatus(
                    event.getProcessId(),
                    "Erro ao gerar oração: " + e.getMessage(),
                    0
            );
        }
    }
}

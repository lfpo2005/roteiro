package dev.luisoliveira.roteiro.service;

import dev.luisoliveira.roteiro.event.ContentInitiatedEvent;
import dev.luisoliveira.roteiro.event.TitleSelectedEvent;
import dev.luisoliveira.roteiro.util.PromptBuilder;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Service;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class TitleGenerationService {

    private final OpenAIService openAIService;
    private final EventBusService eventBusService;
    private final ProcessTrackingService processTrackingService;

    @EventListener
    public void handleContentInitiatedEvent(ContentInitiatedEvent event) {
        try {
            String processId = event.getProcessId();
            String idioma = event.getIdioma();

            // Verificar se um título já foi fornecido
            if (event.hasTitulo()) {
                String titulo = event.getTitulo();
                log.info("Título fornecido diretamente para o processo {}: {}", processId, titulo);

                // Armazenar o título fornecido (como único título)
                List<String> titles = new ArrayList<>();
                titles.add(titulo);
                processTrackingService.storeTitles(processId, titles);

                // Atualizar status
                processTrackingService.updateStatus(
                        processId,
                        "Usando título fornecido: " + titulo,
                        40
                );

                // Pular a geração de títulos e ir direto para o próximo passo
                log.info("Pulando geração de títulos e usando título fornecido: {}", titulo);

                // Publicar evento de título selecionado diretamente
                eventBusService.publish(new TitleSelectedEvent(
                        processId,
                        titulo
                ));

                return; // Sair do método, pois não precisamos gerar títulos
            }

            // Se não tem título, continua o fluxo normal
            // Atualizar status
            processTrackingService.updateStatus(
                    processId,
                    "Gerando títulos...",
                    20
            );

            log.info("Gerando títulos para o processo {} no idioma {}", processId, idioma);

            // Obter observações se houver
            String observacoes = event.getObservacoes();
            boolean hasObservacoes = event.hasObservacoes();

            // Construir prompt otimizado com suporte ao idioma
            String prompt = PromptBuilder.buildTitlePrompt(
                    event.getTema(),
                    event.getEstiloOracao(),
                    idioma,
                    hasObservacoes,
                    observacoes
            );

            // Chamar OpenAI API
            List<String> titles = openAIService.generateTitles(prompt);

            // Armazenar títulos
            processTrackingService.storeTitles(processId, titles);

            // Atualizar status
            processTrackingService.updateStatus(
                    processId,
                    "Títulos gerados com sucesso",
                    40
            );

            log.info("Gerados {} títulos com sucesso para o processo {}", titles.size(), processId);

            // Selecionar automaticamente o melhor título
            String selectedTitle = selectBestTitle(titles, event.getTema(), event.getEstiloOracao());

            log.info("Título selecionado automaticamente: {}", selectedTitle);

            // Atualizar status
            processTrackingService.updateStatus(
                    processId,
                    "Título selecionado: " + selectedTitle,
                    45
            );

            // Publicar evento com o título selecionado
            eventBusService.publish(new TitleSelectedEvent(
                    processId,
                    selectedTitle
            ));
        } catch (Exception e) {
            // Lidar com erros
            log.error("Erro ao processar títulos: {}", e.getMessage(), e);
            processTrackingService.updateStatus(
                    event.getProcessId(),
                    "Erro ao processar títulos: " + e.getMessage(),
                    0
            );
        }
    }

    /**
     * Seleciona o melhor título baseado em critérios predefinidos
     * @param titles Lista de títulos gerados
     * @param tema Tema da oração
     * @param estiloOracao Estilo da oração
     * @return O título selecionado
     */
    private String selectBestTitle(List<String> titles, String tema, String estiloOracao) {
        if (titles == null || titles.isEmpty()) {
            throw new IllegalArgumentException("Nenhum título foi gerado");
        }

        // Por padrão, seleciona o primeiro título
        // Em uma implementação mais avançada, você pode adicionar aqui
        // critérios para analisar e escolher o melhor título

        // Exemplo de critérios que podem ser implementados:
        // 1. Preferir títulos com palavras-chave específicas
        // 2. Preferir títulos com comprimento ideal
        // 3. Avaliar impacto emocional usando análise de sentimento
        // 4. Verificar se contém hashtags importantes

        // Por enquanto, simplesmente retorna o primeiro título
        return titles.get(0);
    }
}
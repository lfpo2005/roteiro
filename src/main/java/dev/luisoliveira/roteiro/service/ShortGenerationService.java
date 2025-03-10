package dev.luisoliveira.roteiro.service;

import dev.luisoliveira.roteiro.event.OracaoGeneratedEvent;
import dev.luisoliveira.roteiro.event.ShortGeneratedEvent;
import dev.luisoliveira.roteiro.util.PromptBuilder;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Service;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Service
@RequiredArgsConstructor
@Slf4j
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

            // Obter o idioma do processo
            String idioma = processTrackingService.getIdioma(processId);
            log.info("Gerando versão short da oração no idioma: {}", idioma);

            // Construir prompt otimizado para short
            String prompt = PromptBuilder.buildShortPrompt(
                    event.getOracaoContent(),
                    event.getTitle(),
                    idioma
            );

            // Chamar OpenAI API
            log.info("Iniciando geração da versão short no idioma: {}", idioma);
            String shortContent = openAIService.generateOracao(prompt);

            // Verificar se o conteúdo está no idioma correto
            if (necessitaCorrecaoIdioma(shortContent, idioma)) {
                log.warn("Conteúdo short não parece estar no idioma correto ({}), tentando regenerar...", idioma);

                String idiomaNome;
                if ("pt".equalsIgnoreCase(idioma) || "pt-BR".equalsIgnoreCase(idioma)) {
                    idiomaNome = "português";
                } else if ("en".equalsIgnoreCase(idioma)) {
                    idiomaNome = "inglés";
                } else {
                    idiomaNome = "español";
                }

                String correctedPrompt = "Por favor, crea una versión corta (30-60 segundos) en " + idiomaNome +
                        " de la siguiente oración. Es MUY IMPORTANTE que sea COMPLETAMENTE en " + idiomaNome +
                        ":\n\nTítulo: \"" + event.getTitle() + "\"\n\n" +
                        "Contenido original:\n" + event.getOracaoContent() + "\n\n" +
                        "La versión corta debe mantener la esencia principal, incluir un versículo bíblico y " +
                        "terminar con \"En el nombre de Jesús, Amén.\"";

                shortContent = openAIService.generateOracao(correctedPrompt);
            }

            // Atualizar status
            processTrackingService.updateStatus(
                    processId,
                    "Versão short gerada com sucesso",
                    80
            );

            log.info("Versão short gerada com sucesso: {} caracteres", shortContent.length());

            // Publicar evento com resultado
            eventBusService.publish(new ShortGeneratedEvent(
                    processId,
                    event.getTitle(),
                    event.getOracaoContent(),
                    shortContent
            ));
        } catch (Exception e) {
            // Lidar com erros
            log.error("Erro ao gerar versão short: {}", e.getMessage(), e);
            processTrackingService.updateStatus(
                    event.getProcessId(),
                    "Erro ao gerar versão short: " + e.getMessage(),
                    0
            );
        }
    }

    /**
     * Método para verificar se o conteúdo gerado precisa de correção de idioma
     */
    private boolean necessitaCorrecaoIdioma(String text, String idioma) {
        if (text == null || text.isEmpty()) {
            return false;
        }

        // Verificação para português
        if (("pt".equalsIgnoreCase(idioma) || "pt-BR".equalsIgnoreCase(idioma))) {
            // Palavras típicas do português que não costumam aparecer em espanhol ou inglês
            String[] palavrasPortugues = {"em", "não", "você", "para", "são", "também", "muito", "obrigado", "está"};

            // Se o texto deve estar em português, verificamos se não há palavras espanholas
            String[] palavrasEspanhol = {"el", "la", "los", "las", "es", "en", "con", "por", "para", "su"};

            return !contemPalavras(text, palavrasPortugues) && contemPalavras(text, palavrasEspanhol);
        }

        // Verificação para inglês
        if ("en".equalsIgnoreCase(idioma)) {
            // Palavras típicas do inglês
            String[] palavrasIngles = {"the", "and", "for", "with", "your", "our", "that", "this", "from", "have"};

            // Se o texto deve estar em inglês, verificamos se não há palavras espanholas
            String[] palavrasEspanhol = {"el", "la", "los", "las", "es", "en", "con", "por", "para", "su"};

            return !contemPalavras(text, palavrasIngles) && contemPalavras(text, palavrasEspanhol);
        }

        // Verificação para espanhol (padrão)
        // Palavras típicas do espanhol
        String[] palavrasEspanhol = {"el", "la", "los", "las", "es", "en", "con", "por", "para", "su"};

        // Palavras típicas do português
        String[] palavrasPortugues = {"em", "não", "você", "para", "são", "também", "muito", "obrigado", "está"};

        return !contemPalavras(text, palavrasEspanhol) && contemPalavras(text, palavrasPortugues);
    }

    /**
     * Verifica se o texto contém pelo menos algumas das palavras da lista
     */
    private boolean contemPalavras(String texto, String[] palavras) {
        texto = texto.toLowerCase();
        int contador = 0;
        int minimo = Math.min(3, palavras.length); // Pelo menos 3 palavras ou todas se forem menos que 3

        for (String palavra : palavras) {
            if (texto.contains(" " + palavra + " ") ||
                    texto.startsWith(palavra + " ") ||
                    texto.endsWith(" " + palavra) ||
                    texto.equals(palavra)) {
                contador++;
                if (contador >= minimo) {
                    return true;
                }
            }
        }
        return false;
    }
}
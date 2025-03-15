package dev.luisoliveira.roteiro.service;

import dev.luisoliveira.roteiro.event.DescriptionGeneratedEvent;
import dev.luisoliveira.roteiro.event.ImagePromptGeneratedEvent;
import dev.luisoliveira.roteiro.util.PromptBuilder;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Service;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import java.io.IOException;

/**
 * Serviço para geração de prompts de imagem e imagens para as orações.
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class ImagePromptGenerationService {

    private final OpenAIService openAIService;
    private final ReplicateService replicateService;
    private final EventBusService eventBusService;
    private final ProcessTrackingService processTrackingService;

    @Value("${file.output.path:./gerados}")
    private String outputPath;

    /**
     * Método que escuta eventos de descrição gerada e gera um prompt de imagem
     * baseado no conteúdo da oração e no título.
     */
    @EventListener
    public void handleDescriptionGeneratedEvent(DescriptionGeneratedEvent event) {
        try {
            String processId = event.getProcessId();

            // Verificar se a geração de imagem está habilitada para este processo
            if (!processTrackingService.deveGerarImagem(processId)) {
                log.info("Geração de imagem desabilitada para o processo: {}", processId);
                // Marcar como não processando imagem e sair do método
                processTrackingService.setImageBeingProcessed(processId, false);
                return;
            }

            processTrackingService.setImageBeingProcessed(processId, true);

            // Atualizar status
            processTrackingService.updateStatus(
                    processId,
                    "Gerando capa para a oração...",
                    92
            );

            // Obter o idioma do processo
            String idioma = processTrackingService.getIdioma(processId);
            log.info("Gerando prompt para imagem de capa no idioma: {}", idioma);

            // Construir prompt otimizado para geração de imagem
            String imagePrompt = PromptBuilder.buildImagePromptPrompt(
                    event.getTitle(),
                    event.getOracaoContent(),
                    idioma
            );

            // Chamar OpenAI API para gerar o prompt detalhado para a imagem
            log.info("Gerando prompt para capa via OpenAI...");
            String generatedImagePrompt = openAIService.generateDescription(imagePrompt);
            log.info("Prompt de imagem gerado com sucesso: {} caracteres", generatedImagePrompt.length());

            // Melhorar o prompt para o Replicate
            String enhancedPrompt = enhancePromptForReplicate(generatedImagePrompt, event.getTitle());

            // Gerar a imagem com o Replicate
            log.info("Gerando imagem via Replicate...");
            String imagePath = replicateService.generateImage(
                    enhancedPrompt,
                    processId,
                    event.getTitle(),
                    outputPath
            );

            // Atualizar status
            processTrackingService.updateStatus(
                    processId,
                    "Capa gerada com sucesso",
                    94
            );

            log.info("Capa gerada com sucesso e salva em: {}", imagePath);

            // Publicar evento notificando que a imagem foi gerada
            // (Não é necessário para o fluxo atual, mas pode ser útil para extensões futuras)
            eventBusService.publish(new ImagePromptGeneratedEvent(
                    processId,
                    event.getTitle(),
                    imagePath
            ));

        } catch (Exception e) {
            // Lidar com erros
            log.error("Erro ao gerar capa: {}", e.getMessage(), e);
            processTrackingService.updateStatus(
                    event.getProcessId(),
                    "Aviso: Não foi possível gerar a capa: " + e.getMessage(),
                    93  // Ainda continua o processo, apenas sem imagem
            );

            // Marcar que o processamento da imagem terminou (com erro)
            processTrackingService.setImageBeingProcessed(event.getProcessId(), false);

            // Não propagar a exceção para permitir que o fluxo continue mesmo sem a imagem
        }
    }

    /**
     * Melhora o prompt para obter melhores resultados com o modelo flux-schnell
     */
    private String enhancePromptForReplicate(String basePrompt, String title) {
        // Limpar hashtags e emojis do título para usar em parte do prompt
        String cleanTitle = title.replaceAll("#\\w+", "").replaceAll("[^\\p{L}\\p{N}\\p{P}\\p{Z}]", "").trim();

        // Criar um prompt direto para a imagem - não instruções para criar um prompt
        StringBuilder enhancedPrompt = new StringBuilder();
        enhancedPrompt.append("Una imagen inspiradora que muestra rayos de luz divina descendiendo del cielo ");
        enhancedPrompt.append("mientras una persona está orando, representando protección divina y éxito, ");
        enhancedPrompt.append("ambiente protector, colores brillantes, prayer, divine light, holy text, ");
        enhancedPrompt.append("professional photography, inspirational, spiritual, sacred, high quality, detailed");

        return enhancedPrompt.toString();
    }
}
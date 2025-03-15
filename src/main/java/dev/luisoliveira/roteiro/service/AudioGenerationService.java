package dev.luisoliveira.roteiro.service;

import dev.luisoliveira.roteiro.event.AudioGeneratedEvent;
import dev.luisoliveira.roteiro.event.DescriptionGeneratedEvent;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Service;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Service
@RequiredArgsConstructor
@Slf4j
public class AudioGenerationService {

    private final ProcessTrackingService processTrackingService;
    private final EventBusService eventBusService;
    private final FileStorageService fileStorageService;
    // Adicione aqui o serviço responsável pela geração real do áudio (ex: TextToSpeechService)

    /**
     * Escuta o evento de descrição gerada para gerar o áudio da oração.
     */
    @EventListener
    public void handleDescriptionGeneratedEvent(DescriptionGeneratedEvent event) {
        String processId = event.getProcessId();

        // Verificar se o áudio deve ser gerado para este processo
        if (!processTrackingService.deveGerarAudio(processId)) {
            log.info("Geração de áudio desativada para o processo: {}", processId);
            return;
        }

        try {
            // Atualizar status
            processTrackingService.updateStatus(
                    processId,
                    "Gerando áudio para a oração...",
                    92
            );

            // Obter conteúdos
            String title = event.getTitle();
            String oracaoContent = event.getOracaoContent();
            String shortContent = event.getShortContent();

            // Aqui você chamaria um serviço real de conversão texto para voz
            // Por enquanto, vamos simular com bytes vazios
            byte[] fullAudioContent = simulateAudioGeneration(oracaoContent);
            byte[] shortAudioContent = simulateAudioGeneration(shortContent);

            // Salvar os arquivos de áudio nas pastas corretas
            String[] audioPaths = fileStorageService.saveAudioFiles(
                    processId,
                    title,
                    fullAudioContent,
                    shortAudioContent
            );

            String fullAudioPath = audioPaths[0];
            String shortAudioPath = audioPaths[1];

            // Armazenar caminhos dos arquivos de áudio
            processTrackingService.storeAudioPaths(processId, fullAudioPath, shortAudioPath);

            // Atualizar status
            processTrackingService.updateStatus(
                    processId,
                    "Áudio gerado com sucesso",
                    94
            );

            log.info("Áudio gerado com sucesso para o processo: {}", processId);

            // Publicar evento com os caminhos dos arquivos de áudio
            eventBusService.publish(new AudioGeneratedEvent(
                    processId,
                    title,
                    fullAudioPath,
                    shortAudioPath
            ));

        } catch (Exception e) {
            // Lidar com erros
            log.error("Erro ao gerar áudio: {}", e.getMessage(), e);
            processTrackingService.updateStatus(
                    event.getProcessId(),
                    "Erro ao gerar áudio: " + e.getMessage(),
                    0
            );
        }
    }

    /**
     * Método de simulação de geração de áudio
     * Em um ambiente real, você usaria um serviço de TTS como Google Cloud TTS, Amazon Polly, etc.
     */
    private byte[] simulateAudioGeneration(String text) {
        // Simulação - em um ambiente real, aqui seria chamado o serviço de conversão texto-fala
        int simulatedSize = Math.min(text.length() * 100, 1024 * 1024); // Tamanho simulado
        return new byte[simulatedSize]; // Retorna um array vazio para simulação
    }
}
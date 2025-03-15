package dev.luisoliveira.roteiro.service;

import dev.luisoliveira.roteiro.dto.NotificationMessage;
import dev.luisoliveira.roteiro.event.AudioGeneratedEvent;
import dev.luisoliveira.roteiro.event.ContentCompletedEvent;
import dev.luisoliveira.roteiro.event.DescriptionGeneratedEvent;
import dev.luisoliveira.roteiro.event.OracaoGeneratedEvent;
import dev.luisoliveira.roteiro.event.ShortGeneratedEvent;
import dev.luisoliveira.roteiro.event.TitleSelectedEvent;
import org.springframework.context.event.EventListener;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

/**
 * Serviço responsável por enviar notificações para o frontend através de WebSockets
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class NotificationService {

    private final SimpMessagingTemplate messagingTemplate;

    /**
     * Envia uma notificação para um usuário específico ou processo
     * @param processId ID do processo
     * @param type Tipo da notificação
     * @param message Mensagem da notificação
     * @param data Dados adicionais (opcional)
     */
    public void sendNotification(String processId, String type, String message, Object data) {
        NotificationMessage notification = new NotificationMessage(processId, type, message, data);

        // Envia para o tópico específico do processo
        String destination = "/topic/notifications/" + processId;
        log.info("Enviando notificação para {}: {}", destination, notification);

        messagingTemplate.convertAndSend(destination, notification);

        // Também envia para o tópico geral de notificações
        messagingTemplate.convertAndSend("/topic/notifications", notification);
    }

    /**
     * Escuta eventos de conclusão de conteúdo e envia notificações
     */
    @EventListener
    public void handleContentCompletedEvent(ContentCompletedEvent event) {
        log.info("Enviando notificação de conclusão para o processo: {}", event.getProcessId());

        sendNotification(
                event.getProcessId(),
                "PROCESS_COMPLETED",
                "Processo concluído com sucesso!",
                event.getResultPath()
        );
    }

    /**
     * Escuta eventos de títulos selecionados
     */
    @EventListener
    public void handleTitleSelectedEvent(TitleSelectedEvent event) {
        log.info("Título selecionado para o processo: {}", event.getProcessId());

        sendNotification(
                event.getProcessId(),
                "TITLE_SELECTED",
                "Título selecionado: " + event.getSelectedTitle(),
                event.getSelectedTitle()
        );
    }

    /**
     * Escuta eventos de oração gerada
     */
    @EventListener
    public void handleOracaoGeneratedEvent(OracaoGeneratedEvent event) {
        log.info("Oração gerada para o processo: {}", event.getProcessId());

        sendNotification(
                event.getProcessId(),
                "ORACAO_GENERATED",
                "Oração gerada com sucesso",
                event.getTitle()
        );
    }

    /**
     * Escuta eventos de versão curta gerada
     */
    @EventListener
    public void handleShortGeneratedEvent(ShortGeneratedEvent event) {
        log.info("Versão curta gerada para o processo: {}", event.getProcessId());

        sendNotification(
                event.getProcessId(),
                "SHORT_GENERATED",
                "Versão curta gerada com sucesso",
                event.getTitle()
        );
    }

    /**
     * Escuta eventos de descrição gerada
     */
    @EventListener
    public void handleDescriptionGeneratedEvent(DescriptionGeneratedEvent event) {
        log.info("Descrição gerada para o processo: {}", event.getProcessId());

        sendNotification(
                event.getProcessId(),
                "DESCRIPTION_GENERATED",
                "Descrição gerada com sucesso",
                event.getTitle()
        );
    }

    /**
     * Escuta eventos de áudio gerado
     */
    @EventListener
    public void handleAudioGeneratedEvent(AudioGeneratedEvent event) {
        log.info("Áudio gerado para o processo: {}", event.getProcessId());

        sendNotification(
                event.getProcessId(),
                "AUDIO_GENERATED",
                "Áudio gerado com sucesso",
                event.getTitle()
        );
    }

    /**
     * Método para enviar notificação de erro
     */
    public void sendErrorNotification(String processId, String errorMessage) {
        log.error("Enviando notificação de erro para o processo: {}", processId);

        sendNotification(
                processId,
                "ERROR",
                "Ocorreu um erro no processamento: " + errorMessage,
                null
        );
    }

    /**
     * Método para enviar notificação de progresso
     */
    public void sendProgressNotification(String processId, int progress, String stage) {
        log.debug("Enviando notificação de progresso para o processo: {} - {}%: {}", processId, progress, stage);

        sendNotification(
                processId,
                "PROGRESS_UPDATE",
                stage,
                progress
        );
    }
}
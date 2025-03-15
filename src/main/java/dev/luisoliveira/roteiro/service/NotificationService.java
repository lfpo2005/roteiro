package dev.luisoliveira.roteiro.service;

import dev.luisoliveira.roteiro.dto.NotificationMessage;
import dev.luisoliveira.roteiro.event.ContentCompletedEvent;
import org.springframework.context.event.EventListener;
import org.springframework.messaging.MessagingException;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

/**
 * Serviço responsável por enviar notificações para o frontend através de
 * WebSockets
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class NotificationService {

    private final SimpMessagingTemplate messagingTemplate;

    /**
     * Envia uma notificação apenas para eventos de conclusão final.
     * As notificações de progresso não serão mais enviadas.
     */
    @EventListener
    public void handleContentCompletedEvent(ContentCompletedEvent event) {
        String processId = event.getProcessId();
        log.info("Enviando notificação de conclusão para o processo: {}", processId);

        NotificationMessage notification = new NotificationMessage(
                processId,
                "PROCESS_COMPLETED",
                "Processo concluído com sucesso!",
                event.getResultPath());

        try {
            // Enviar para o tópico específico do processo
            String destination = "/topic/notifications/" + processId;
            log.info("Enviando notificação para {}: {}", destination, notification);

            messagingTemplate.convertAndSend(destination, notification);
            log.debug("Notificação enviada com sucesso para {}", destination);

            // Também enviar para o tópico geral de notificações
            log.info("Enviando notificação para o tópico geral: /topic/notifications");
            messagingTemplate.convertAndSend("/topic/notifications", notification);
            log.debug("Notificação enviada com sucesso para o tópico geral");
        } catch (MessagingException e) {
            log.error("Erro ao enviar notificação para o processo {}: {}", processId, e.getMessage(), e);
        } catch (Exception e) {
            log.error("Erro inesperado ao enviar notificação para o processo {}: {}", processId, e.getMessage(), e);
        }
    }

    /**
     * Método para enviar notificação de erro.
     * Mantemos este método pois erros são importantes de serem notificados.
     */
    public void sendErrorNotification(String processId, String errorMessage) {
        log.error("Enviando notificação de erro para o processo: {}", processId);

        NotificationMessage notification = new NotificationMessage(
                processId,
                "ERROR",
                "Ocorreu um erro no processamento: " + errorMessage,
                null);

        try {
            String destination = "/topic/notifications/" + processId;
            messagingTemplate.convertAndSend(destination, notification);
            messagingTemplate.convertAndSend("/topic/notifications", notification);
            log.debug("Notificação de erro enviada com sucesso");
        } catch (MessagingException e) {
            log.error("Erro ao enviar notificação de erro para o processo {}: {}", processId, e.getMessage(), e);
        } catch (Exception e) {
            log.error("Erro inesperado ao enviar notificação de erro para o processo {}: {}", processId, e.getMessage(),
                    e);
        }
    }

    /**
     * Removemos o método sendProgressNotification pois não queremos
     * enviar notificações de progresso intermediário.
     */
}
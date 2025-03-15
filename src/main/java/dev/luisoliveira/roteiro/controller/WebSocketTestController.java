package dev.luisoliveira.roteiro.controller;

import dev.luisoliveira.roteiro.dto.NotificationMessage;
import dev.luisoliveira.roteiro.service.NotificationService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.SendTo;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RestController;

/**
 * Controlador para testar a funcionalidade do WebSocket
 */
@RestController
@RequiredArgsConstructor
@Slf4j

public class WebSocketTestController {

    private final SimpMessagingTemplate messagingTemplate;
    private final NotificationService notificationService;

    /**
     * Endpoint para testar o envio de mensagens WebSocket
     * 
     * @param processId ID do processo
     * @return Mensagem de confirmação
     */
    @GetMapping("/api/test/notification/{processId}")
    public String testNotification(@PathVariable String processId) {
        log.info("Testando envio de notificação para o processo: {}", processId);

        // Enviando notificação diretamente, sem usar o método sendNotification que foi
        // removido
        NotificationMessage notification = new NotificationMessage(
                processId,
                "TEST",
                "Teste de notificação WebSocket",
                "Dados de teste");

        // Enviando para o tópico específico do processo
        String destination = "/topic/notifications/" + processId;
        messagingTemplate.convertAndSend(destination, notification);

        // Enviando para o tópico geral
        messagingTemplate.convertAndSend("/topic/notifications", notification);

        return "Notificação de teste enviada para o processo: " + processId;
    }

    /**
     * Endpoint para testar o envio direto de mensagens WebSocket
     * 
     * @param processId ID do processo
     * @return Mensagem de confirmação
     */
    @GetMapping("/api/test/direct-notification/{processId}")
    public String testDirectNotification(@PathVariable String processId) {
        log.info("Testando envio direto de notificação para o processo: {}", processId);

        NotificationMessage notification = new NotificationMessage(
                processId,
                "DIRECT_TEST",
                "Teste direto de notificação WebSocket",
                "Dados de teste direto");

        String destination = "/topic/notifications/" + processId;
        messagingTemplate.convertAndSend(destination, notification);
        messagingTemplate.convertAndSend("/topic/notifications", notification);

        return "Notificação direta de teste enviada para o processo: " + processId;
    }

    /**
     * Endpoint STOMP para testar a comunicação bidirecional
     * 
     * @param message Mensagem recebida
     * @return Mensagem de resposta
     */
    @MessageMapping("/test")
    @SendTo("/topic/test")
    public NotificationMessage testMessage(NotificationMessage message) {
        log.info("Recebida mensagem de teste: {}", message);
        return new NotificationMessage(
                message.getProcessId(),
                "ECHO",
                "Resposta ao teste: " + message.getMessage(),
                message.getData());
    }
}
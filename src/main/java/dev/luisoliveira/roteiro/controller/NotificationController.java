package dev.luisoliveira.roteiro.controller;

import dev.luisoliveira.roteiro.dto.NotificationMessage;
import dev.luisoliveira.roteiro.service.NotificationService;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.SendTo;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.CrossOrigin;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

/**
 * Controlador para gerenciar as conexões e mensagens WebSocket
 */
@Controller
@RequiredArgsConstructor
@Slf4j
@CrossOrigin(origins = "*")
public class NotificationController {

    private final NotificationService notificationService;

    /**
     * Endpoint para o cliente se registrar para receber notificações de um processo específico
     */
    @MessageMapping("/register/{processId}")
    @SendTo("/topic/notifications/{processId}")
    public NotificationMessage registerForNotifications(String processId) {
        log.info("Cliente registrado para receber notificações do processo: {}", processId);

        // Retorna uma mensagem de confirmação
        return new NotificationMessage(
                processId,
                "REGISTRATION_CONFIRMED",
                "Registro para notificações confirmado",
                null
        );
    }

    /**
     * Endpoint para solicitar o status atual de um processo
     */
    @MessageMapping("/status/{processId}")
    public void requestStatus(String processId) {
        log.info("Solicitação de status recebida para o processo: {}", processId);

        // Este método pode buscar o status atual do processo e enviar como notificação
        // Implementação depende da lógica de negócio
    }
}
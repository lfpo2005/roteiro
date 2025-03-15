package dev.luisoliveira.roteiro.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * Objeto de transferência de dados para mensagens de notificação
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
public class NotificationMessage {
    private String processId;
    private String type; // PROCESS_COMPLETED, ERROR, PROGRESS_UPDATE, etc.
    private String message;
    private Object data; // Dados adicionais (pode ser o caminho do resultado, porcentagem de progresso, etc.)
    private LocalDateTime timestamp;

    public NotificationMessage(String processId, String type, String message, Object data) {
        this.processId = processId;
        this.type = type;
        this.message = message;
        this.data = data;
        this.timestamp = LocalDateTime.now();
    }
}
package dev.luisoliveira.roteiro.dto;

import java.time.LocalDateTime;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

/**
 * DTO para resposta de status de processo
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class ProcessStatusResponse {

    private String processId;
    private String status;
    private int progress;
    private String message;
    private LocalDateTime timestamp;
    private String contentId;
    private String audioId;

    public ProcessStatusResponse(String processId, String status, int progress, String message) {
        this.processId = processId;
        this.status = status;
        this.progress = progress;
        this.message = message;
        this.timestamp = LocalDateTime.now();
    }
}
package dev.luisoliveira.roteiro.dto;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class GenerationResponse {
    private String processId;
    private String message;
}

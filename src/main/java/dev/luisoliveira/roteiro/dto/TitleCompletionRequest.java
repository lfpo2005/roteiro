package dev.luisoliveira.roteiro.dto;

import java.util.List;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * DTO para requisição de conclusão de geração de título
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class TitleCompletionRequest {
    
    private String processId;
    private List<String> titles;
    private String selectedTitle;
} 
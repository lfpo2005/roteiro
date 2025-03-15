package dev.luisoliveira.roteiro.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Objeto de transferência de dados para configurações do sistema
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SystemConfigDto {
    private String outputPath;
    private boolean createDirectoryIfNotExists;
}
package dev.luisoliveira.roteiro.service;

import dev.luisoliveira.roteiro.dto.SystemConfigDto;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import jakarta.annotation.PostConstruct;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Serviço simplificado para configurações do sistema (versão transitória para
 * MongoDB)
 */
@Service
@Slf4j
@RequiredArgsConstructor
public class SystemConfigService {

    private final FileStorageService fileStorageService;

    @Value("${file.output.path:./gerados}")
    private String defaultOutputPath;

    // Configurações em memória
    private boolean createDirectoryIfNotExists = true;
    private String outputPath;

    @PostConstruct
    public void init() {
        outputPath = defaultOutputPath;
        log.info("SystemConfigService inicializado com configurações padrão");
    }

    /**
     * Obtém as configurações atuais do sistema
     *
     * @return SystemConfigDto com as configurações atuais
     */
    public SystemConfigDto getSystemConfig() {
        return SystemConfigDto.builder()
                .outputPath(outputPath)
                .createDirectoryIfNotExists(createDirectoryIfNotExists)
                .build();
    }

    /**
     * Atualiza o caminho de saída nas configurações do sistema
     * (implementação simplificada em memória)
     *
     * @param newOutputPath Novo caminho de saída
     * @return SystemConfigDto com as configurações atualizadas
     */
    public SystemConfigDto updateOutputPath(String newOutputPath) {
        if (newOutputPath == null || newOutputPath.trim().isEmpty()) {
            log.warn("Tentativa de definir caminho de saída vazio ou nulo. Usando valor padrão.");
            newOutputPath = defaultOutputPath;
        } else {
            newOutputPath = newOutputPath.trim();
        }

        this.outputPath = newOutputPath;
        log.info("Caminho de saída atualizado para: {}", newOutputPath);

        return getSystemConfig();
    }

    /**
     * Atualiza a configuração para criar diretório automaticamente
     * (implementação simplificada em memória)
     *
     * @param createDirectory Se deve criar diretório automaticamente
     * @return SystemConfigDto com as configurações atualizadas
     */
    public SystemConfigDto updateCreateDirectory(boolean createDirectory) {
        this.createDirectoryIfNotExists = createDirectory;
        log.info("Configuração para criar diretório automaticamente atualizada para: {}", createDirectory);

        return getSystemConfig();
    }

    /**
     * Redefine as configurações para os valores padrão
     * (implementação simplificada em memória)
     *
     * @return SystemConfigDto com as configurações redefinidas
     */
    public SystemConfigDto resetToDefaults() {
        this.outputPath = defaultOutputPath;
        this.createDirectoryIfNotExists = true;

        log.info("Configurações redefinidas para os valores padrão");

        return getSystemConfig();
    }

    /**
     * Método para compatibilidade - Simulação de criação de diretório
     * 
     * @param path Caminho do diretório (ignorado nesta versão)
     * @return sempre true
     */
    public boolean createDirectory(String path) {
        if (path == null || path.isEmpty()) {
            log.error("Tentativa de criar diretório com caminho vazio");
            throw new IllegalArgumentException("Caminho não pode ser vazio");
        }

        log.info("Criação de diretório simulada para: {}", path);
        return true;
    }

    /**
     * Método para compatibilidade - Simulação de listagem de diretórios
     * 
     * @param basePath Caminho base (ignorado nesta versão)
     * @return Mapa com informações simuladas de diretórios
     */
    public Map<String, Object> listDirectories(String basePath) {
        Map<String, Object> result = new HashMap<>();
        List<Map<String, Object>> directories = new ArrayList<>();

        // Simular um diretório padrão
        Map<String, Object> dir = new HashMap<>();
        dir.put("name", "MongoDB (Armazenamento em nuvem)");
        dir.put("path", "mongodb://cloud");
        dir.put("canWrite", true);
        dir.put("isDefault", true);
        directories.add(dir);

        result.put("directories", directories);
        result.put("currentPath", "mongodb://cloud");
        result.put("canWrite", true);
        result.put("message", "Sistema transitório para MongoDB - diretórios simulados");

        log.info("Listagem de diretórios simulada");
        return result;
    }
}
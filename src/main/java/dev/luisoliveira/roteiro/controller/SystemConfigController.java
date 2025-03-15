package dev.luisoliveira.roteiro.controller;

import dev.luisoliveira.roteiro.dto.SystemConfigDto;
import dev.luisoliveira.roteiro.service.SystemConfigService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

/**
 * Controller para gerenciar configurações do sistema
 */
@RestController
@RequestMapping("/config")
@RequiredArgsConstructor
@Slf4j
@CrossOrigin
public class SystemConfigController {

    private final SystemConfigService systemConfigService;

    /**
     * Obtém as configurações atuais do sistema
     *
     * @return Configurações do sistema
     */
    @GetMapping
    public ResponseEntity<SystemConfigDto> getConfig() {
        log.info("Obtendo configurações do sistema");
        SystemConfigDto config = systemConfigService.getSystemConfig();
        return ResponseEntity.ok(config);
    }

    /**
     * Atualiza o caminho de saída nas configurações do sistema
     *
     * @param outputPath Novo caminho de saída
     * @return Configurações atualizadas
     */
    @PostMapping("/output-path")
    public ResponseEntity<SystemConfigDto> updateOutputPath(@RequestBody String outputPath) {
        log.info("Atualizando caminho de saída para: {}", outputPath);
        SystemConfigDto config = systemConfigService.updateOutputPath(outputPath);
        return ResponseEntity.ok(config);
    }

    /**
     * Atualiza a configuração de criação automática de diretório
     *
     * @param createDirectory Se deve criar o diretório automaticamente
     * @return Configurações atualizadas
     */
    @PostMapping("/create-directory")
    public ResponseEntity<SystemConfigDto> updateCreateDirectory(@RequestBody boolean createDirectory) {
        log.info("Atualizando configuração de criação de diretório para: {}", createDirectory);
        SystemConfigDto config = systemConfigService.updateCreateDirectory(createDirectory);
        return ResponseEntity.ok(config);
    }

    /**
     * Redefine as configurações do sistema para os valores padrão
     *
     * @return Configurações redefinidas
     */
    @PostMapping("/reset")
    public ResponseEntity<SystemConfigDto> resetConfig() {
        log.info("Redefinindo configurações do sistema");
        SystemConfigDto config = systemConfigService.resetToDefaults();
        return ResponseEntity.ok(config);
    }

    /**
     * Lista os diretórios em um caminho específico
     *
     * @param path Caminho para listar os diretórios (opcional)
     * @return Lista de diretórios e informações relacionadas
     */
    @GetMapping("/directories")
    public ResponseEntity<Map<String, Object>> listDirectories(@RequestParam(required = false) String path) {
        log.info("Listando diretórios em: {}", path);
        try {
            Map<String, Object> result = systemConfigService.listDirectories(path);
            return ResponseEntity.ok(result);
        } catch (Exception e) {
            log.error("Erro ao listar diretórios: {}", e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Collections.singletonMap("error", e.getMessage()));
        }
    }

    /**
     * Cria um novo diretório no caminho especificado
     *
     * @param request Mapa contendo o caminho onde criar o diretório
     * @return Status da operação
     */
    @PostMapping("/create-directory-path")
    public ResponseEntity<Map<String, Object>> createDirectory(@RequestBody Map<String, String> request) {
        String path = request.get("path");
        log.info("Criando diretório em: {}", path);
        try {
            boolean created = systemConfigService.createDirectory(path);
            Map<String, Object> result = new HashMap<>();
            result.put("success", created);
            result.put("path", path);
            return ResponseEntity.ok(result);
        } catch (Exception e) {
            log.error("Erro ao criar diretório: {}", e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Collections.singletonMap("error", e.getMessage()));
        }
    }
}
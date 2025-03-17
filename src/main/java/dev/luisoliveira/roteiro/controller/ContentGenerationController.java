package dev.luisoliveira.roteiro.controller;

import dev.luisoliveira.roteiro.dto.GenerationRequest;
import dev.luisoliveira.roteiro.dto.GenerationResponse;
import dev.luisoliveira.roteiro.dto.ProcessStatus;
import dev.luisoliveira.roteiro.dto.TitleSelectionRequest;
import dev.luisoliveira.roteiro.event.ContentInitiatedEvent;
import dev.luisoliveira.roteiro.event.TitleSelectedEvent;
import dev.luisoliveira.roteiro.service.EventBusService;
import dev.luisoliveira.roteiro.service.ProcessTrackingService;
import org.springframework.core.io.FileSystemResource;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.mongodb.core.MongoTemplate;

import java.io.File;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.UUID;
import java.time.LocalDateTime;
import java.util.Map;
import java.util.HashMap;

@RestController
@RequestMapping("/content")
@RequiredArgsConstructor
@Slf4j
@CrossOrigin(origins = "*", maxAge = 3600)
public class ContentGenerationController {

    private final EventBusService eventBusService;
    private final ProcessTrackingService processTrackingService;
    private final MongoTemplate mongoTemplate;
    private final dev.luisoliveira.roteiro.repository.PrayerContentRepository prayerContentRepository;

    @PostMapping("/generate")
    public ResponseEntity<GenerationResponse> startGeneration(@RequestBody GenerationRequest request) {
        String processId = UUID.randomUUID().toString();
        log.info(
                "[PROCESSO] Iniciando processo de geração com ID: {} (idioma: {}, título: {}, gerarVersaoShort: {}, gerarAudio: {})",
                processId,
                request.getIdioma() != null ? request.getIdioma() : "es (padrão)",
                request.getTitulo() != null ? "fornecido" : "não fornecido",
                request.getGerarVersaoShort(),
                request.getGerarAudio());

        // Inicializar status
        processTrackingService.initializeProcess(processId);
        log.debug("[PROCESSO] Status inicializado para processo: {}", processId);

        // Verificar e configurar idioma padrão se necessário
        String idioma = request.getIdioma();
        if (idioma == null || idioma.isBlank()) {
            idioma = "es"; // Padrão: espanhol
            request.setIdioma(idioma);
            log.debug("[PROCESSO] Idioma padrão definido: {}", idioma);
        }

        // Verificar duração para decidir se deve mostrar a opção de gerar short
        String duracao = request.getDuracao();
        Boolean gerarVersaoShort = request.getGerarVersaoShort();
        Boolean gerarAudio = request.getGerarAudio();

        // Se for uma duração curta, definir gerarVersaoShort como false
        if (duracao != null && (duracao.toLowerCase().contains("muito curta") ||
                duracao.toLowerCase().contains("curta") ||
                duracao.toLowerCase().contains("mini"))) {
            // Para durações curtas, não oferecemos a opção de gerar short
            gerarVersaoShort = false;
            request.setGerarVersaoShort(false);
            log.debug("[PROCESSO] Versão short desativada devido à duração curta: {}", duracao);
        }

        // Proteção contra NullPointerException - verificar se gerarVersaoShort e
        // gerarAudio são null
        // antes de usar diretamente no método setProcessInfo
        if (gerarVersaoShort == null) {
            log.debug("[PROCESSO] gerarVersaoShort é null, definindo valor padrão");
            gerarVersaoShort = Boolean.TRUE; // ou FALSE, dependendo do comportamento padrão desejado
        }

        // Armazenar informações do processo
        processTrackingService.setProcessInfo(
                processId,
                request.getTema(),
                request.getEstiloOracao(),
                request.getDuracao(),
                request.getTipoOracao(),
                request.getIdioma(),
                request.getTitulo(),
                request.getObservacoes(),
                gerarVersaoShort,
                gerarAudio);
        log.debug("[PROCESSO] Informações do processo armazenadas: {}", processId);

        // Publicar evento inicial
        eventBusService.publish(new ContentInitiatedEvent(
                processId,
                request.getTema(),
                request.getEstiloOracao(),
                request.getDuracao(),
                request.getTipoOracao(),
                request.getIdioma(),
                request.getTitulo(),
                request.getObservacoes(),
                gerarVersaoShort,
                gerarAudio));
        log.info("[EVENTO] Evento ContentInitiatedEvent publicado para processo: {}", processId);

        String message;
        if (request.getTitulo() != null && !request.getTitulo().isEmpty()) {
            message = "Processo iniciado com sucesso usando o título fornecido: " + request.getTitulo();
        } else {
            message = "Processo iniciado com sucesso. Um título será gerado automaticamente.";
        }

        // Adicionar informação sobre a versão short
        if (duracao != null && (duracao.toLowerCase().contains("muito curta") ||
                duracao.toLowerCase().contains("curta") ||
                duracao.toLowerCase().contains("mini"))) {
            message += " Para orações de curta duração, não será gerada uma versão short.";
        } else if (gerarVersaoShort != null) {
            if (gerarVersaoShort) {
                message += " Será gerada uma versão short da oração.";
            } else {
                message += " Não será gerada uma versão short da oração.";
            }
        }

        // Adicionar informação sobre a geração de áudio
        if (gerarAudio != null) {
            if (gerarAudio) {
                message += " Áudio será gerado para esta oração.";
            } else {
                message += " Áudio não será gerado para esta oração.";
            }
        }

        log.info("[RESPOSTA] Retornando resposta para processo: {}", processId);
        return ResponseEntity.accepted()
                .body(new GenerationResponse(processId, message));
    }

    @GetMapping("/db-status")
    public ResponseEntity<Map<String, Object>> checkDatabaseStatus() {
        log.info("[DIAGNÓSTICO] Verificando status do banco de dados");

        Map<String, Object> status = new HashMap<>();
        status.put("timestamp", LocalDateTime.now());
        status.put("service", "MongoDB");

        try {
            // Verificar se o MongoDB está funcionando
            mongoTemplate.getDb().runCommand(new org.bson.Document("ping", 1));
            status.put("status", "UP");
            status.put("message", "Conexão com MongoDB estabelecida com sucesso");
            log.info("[DIAGNÓSTICO] Conexão com MongoDB estabelecida com sucesso");

            // Verificar se a coleção prayer_contents existe
            boolean collectionExists = mongoTemplate.collectionExists("prayer_contents");
            status.put("collectionExists", collectionExists);
            log.info("[DIAGNÓSTICO] Coleção prayer_contents existe: {}", collectionExists);

            // Contar documentos
            long count = mongoTemplate.count(new org.springframework.data.mongodb.core.query.Query(),
                    "prayer_contents");
            status.put("documentCount", count);
            log.info("[DIAGNÓSTICO] Número de documentos na coleção prayer_contents: {}", count);

            return ResponseEntity.ok(status);
        } catch (Exception e) {
            status.put("status", "DOWN");
            status.put("message", "Erro ao conectar com MongoDB: " + e.getMessage());
            status.put("error", e.getClass().getName());
            log.error("[DIAGNÓSTICO] Erro ao verificar status do MongoDB: {}", e.getMessage(), e);
            return ResponseEntity.status(500).body(status);
        }
    }

    @PostMapping("/test-document")
    public ResponseEntity<Map<String, Object>> createTestDocument() {
        log.info("[DIAGNÓSTICO] Criando documento de teste");

        Map<String, Object> response = new HashMap<>();
        response.put("timestamp", LocalDateTime.now());

        try {
            // Criar um documento de teste
            String testId = "test_" + System.currentTimeMillis();
            log.debug("[DIAGNÓSTICO] ID do documento de teste: {}", testId);

            // Criar um documento diretamente usando MongoTemplate
            org.bson.Document testDoc = new org.bson.Document();
            testDoc.append("_id", testId);
            testDoc.append("processId", testId);
            testDoc.append("title", "Documento de Teste");
            testDoc.append("createdAt", new java.util.Date());
            testDoc.append("expiresAt", new java.util.Date(System.currentTimeMillis() + 86400000)); // +1 dia
            testDoc.append("isDownloaded", false);
            testDoc.append("downloadCount", 0);

            mongoTemplate.getCollection("prayer_contents").insertOne(testDoc);
            log.info("[DIAGNÓSTICO] Documento de teste inserido com sucesso: {}", testId);

            // Verificar se o documento foi realmente salvo
            org.bson.Document foundDoc = mongoTemplate.getCollection("prayer_contents")
                    .find(new org.bson.Document("_id", testId))
                    .first();

            if (foundDoc != null) {
                log.info("[DIAGNÓSTICO] Documento encontrado após inserção: {}", foundDoc.toJson());
                response.put("documentFound", true);
                response.put("documentContent", foundDoc.toJson());
            } else {
                log.warn("[DIAGNÓSTICO] Documento NÃO encontrado após inserção!");
                response.put("documentFound", false);
            }

            response.put("status", "SUCCESS");
            response.put("message", "Documento de teste criado com sucesso");
            response.put("documentId", testId);

            return ResponseEntity.ok(response);
        } catch (Exception e) {
            response.put("status", "ERROR");
            response.put("message", "Erro ao criar documento de teste: " + e.getMessage());
            response.put("error", e.getClass().getName());
            log.error("[DIAGNÓSTICO] Erro ao criar documento de teste: {}", e.getMessage(), e);
            return ResponseEntity.status(500).body(response);
        }
    }

    @GetMapping("/test-document/{id}")
    public ResponseEntity<Map<String, Object>> getTestDocument(@PathVariable String id) {
        log.info("[DIAGNÓSTICO] Buscando documento de teste com ID: {}", id);

        Map<String, Object> response = new HashMap<>();
        response.put("timestamp", LocalDateTime.now());

        try {
            // Buscar o documento pelo ID
            org.bson.Document foundDoc = mongoTemplate.getCollection("prayer_contents")
                    .find(new org.bson.Document("_id", id))
                    .first();

            if (foundDoc != null) {
                log.info("[DIAGNÓSTICO] Documento encontrado: {}", foundDoc.toJson());
                response.put("documentFound", true);
                response.put("documentContent", foundDoc.toJson());
                response.put("status", "SUCCESS");
                response.put("message", "Documento encontrado com sucesso");
            } else {
                log.warn("[DIAGNÓSTICO] Documento não encontrado com ID: {}", id);
                response.put("documentFound", false);
                response.put("status", "NOT_FOUND");
                response.put("message", "Documento não encontrado");
            }

            return ResponseEntity.ok(response);
        } catch (Exception e) {
            response.put("status", "ERROR");
            response.put("message", "Erro ao buscar documento de teste: " + e.getMessage());
            response.put("error", e.getClass().getName());
            log.error("[DIAGNÓSTICO] Erro ao buscar documento de teste: {}", e.getMessage(), e);
            return ResponseEntity.status(500).body(response);
        }
    }

    @GetMapping("/env-check")
    public ResponseEntity<Map<String, Object>> checkEnvironmentVariables() {
        log.info("[DIAGNÓSTICO] Verificando variáveis de ambiente");

        Map<String, Object> response = new HashMap<>();
        response.put("timestamp", LocalDateTime.now());

        // Verificar variáveis de ambiente
        String openaiKey = System.getenv("OPENAI_API_KEY");
        String elevenlabsKey = System.getenv("ELEVENLABS_API_KEY");
        String replicateKey = System.getenv("REPLICATE_API_KEY");

        // Verificar propriedades do sistema
        String openaiProp = System.getProperty("openai.api.key");
        String elevenlabsProp = System.getProperty("elevenlabs.api.key");

        // Verificar configurações do Spring
        String openaiConfig = null;
        String elevenlabsConfig = null;
        try {
            // Não podemos acessar o Environment diretamente do MongoTemplate
            // Vamos apenas verificar as variáveis de ambiente e propriedades do sistema
            openaiConfig = "Não disponível neste contexto";
            elevenlabsConfig = "Não disponível neste contexto";
        } catch (Exception e) {
            log.error("[DIAGNÓSTICO] Erro ao acessar configurações do Spring: {}", e.getMessage());
        }

        // Adicionar informações ao response
        response.put("env_openai_key",
                openaiKey != null ? (openaiKey.substring(0, Math.min(10, openaiKey.length())) + "...")
                        : "não definida");
        response.put("env_elevenlabs_key",
                elevenlabsKey != null ? (elevenlabsKey.substring(0, Math.min(10, elevenlabsKey.length())) + "...")
                        : "não definida");
        response.put("env_replicate_key",
                replicateKey != null ? (replicateKey.substring(0, Math.min(10, replicateKey.length())) + "...")
                        : "não definida");

        response.put("prop_openai_key",
                openaiProp != null ? (openaiProp.substring(0, Math.min(10, openaiProp.length())) + "...")
                        : "não definida");
        response.put("prop_elevenlabs_key",
                elevenlabsProp != null ? (elevenlabsProp.substring(0, Math.min(10, elevenlabsProp.length())) + "...")
                        : "não definida");

        response.put("config_openai_key", openaiConfig);
        response.put("config_elevenlabs_key", elevenlabsConfig);

        return ResponseEntity.ok(response);
    }

    @PostMapping("/test-repository")
    public ResponseEntity<Map<String, Object>> testRepository() {
        log.info("[DIAGNÓSTICO] Testando repositório diretamente");

        Map<String, Object> response = new HashMap<>();
        response.put("timestamp", LocalDateTime.now());

        try {
            // Criar um documento de teste usando o repositório
            String testId = "test_repo_" + System.currentTimeMillis();
            log.debug("[DIAGNÓSTICO] ID do documento de teste: {}", testId);

            // Criar um documento usando o repositório
            dev.luisoliveira.roteiro.document.PrayerContent testContent = new dev.luisoliveira.roteiro.document.PrayerContent(
                    testId, "Teste via Repositório");

            log.info("[DIAGNÓSTICO] Salvando documento via repositório: {}", testContent);
            dev.luisoliveira.roteiro.document.PrayerContent savedContent = prayerContentRepository.save(testContent);
            log.info("[DIAGNÓSTICO] Documento salvo via repositório com ID: {}", savedContent.getId());

            // Verificar se o documento foi realmente salvo
            java.util.Optional<dev.luisoliveira.roteiro.document.PrayerContent> foundContent = prayerContentRepository
                    .findById(testId);

            if (foundContent.isPresent()) {
                log.info("[DIAGNÓSTICO] Documento encontrado via repositório: {}", foundContent.get().getId());
                response.put("documentFound", true);
                response.put("documentId", foundContent.get().getId());
                response.put("documentTitle", foundContent.get().getTitle());
                response.put("documentProcessId", foundContent.get().getProcessId());
            } else {
                log.warn("[DIAGNÓSTICO] Documento NÃO encontrado via repositório!");
                response.put("documentFound", false);
            }

            // Verificar também via MongoTemplate
            org.bson.Document foundDoc = mongoTemplate.getCollection("prayer_contents")
                    .find(new org.bson.Document("_id", testId))
                    .first();

            if (foundDoc != null) {
                log.info("[DIAGNÓSTICO] Documento encontrado via MongoTemplate: {}", foundDoc.toJson());
                response.put("documentFoundViaTemplate", true);
            } else {
                log.warn("[DIAGNÓSTICO] Documento NÃO encontrado via MongoTemplate!");
                response.put("documentFoundViaTemplate", false);
            }

            response.put("status", "SUCCESS");
            response.put("message", "Teste de repositório concluído");

            return ResponseEntity.ok(response);
        } catch (Exception e) {
            response.put("status", "ERROR");
            response.put("message", "Erro ao testar repositório: " + e.getMessage());
            response.put("error", e.getClass().getName());
            log.error("[DIAGNÓSTICO] Erro ao testar repositório: {}", e.getMessage(), e);
            return ResponseEntity.status(500).body(response);
        }
    }

    @PostMapping("/test-id")
    public ResponseEntity<Map<String, Object>> testIdHandling() {
        log.info("[DIAGNÓSTICO] Testando manipulação de IDs");

        Map<String, Object> response = new HashMap<>();
        response.put("timestamp", LocalDateTime.now());

        try {
            // Criar um documento de teste com ID explícito
            String testId = "test_id_" + System.currentTimeMillis();
            log.info("[DIAGNÓSTICO] ID do documento de teste: {}", testId);

            // Criar um documento usando o construtor modificado
            dev.luisoliveira.roteiro.document.PrayerContent testContent = new dev.luisoliveira.roteiro.document.PrayerContent(
                    testId, "Teste de ID");

            // Verificar se o ID foi definido corretamente
            log.info("[DIAGNÓSTICO] ID antes de salvar: {}", testContent.getId());
            log.info("[DIAGNÓSTICO] ProcessID antes de salvar: {}", testContent.getProcessId());

            // Salvar o documento
            dev.luisoliveira.roteiro.document.PrayerContent savedContent = prayerContentRepository.save(testContent);

            // Verificar o ID após salvar
            log.info("[DIAGNÓSTICO] ID após salvar: {}", savedContent.getId());
            log.info("[DIAGNÓSTICO] ProcessID após salvar: {}", savedContent.getProcessId());

            // Tentar recuperar o documento pelo ID
            java.util.Optional<dev.luisoliveira.roteiro.document.PrayerContent> foundById = prayerContentRepository
                    .findById(testId);

            // Tentar recuperar o documento pelo processId
            java.util.Optional<dev.luisoliveira.roteiro.document.PrayerContent> foundByProcessId = prayerContentRepository
                    .findByProcessId(testId);

            // Verificar também via MongoTemplate
            org.bson.Document foundDoc = mongoTemplate.getCollection("prayer_contents")
                    .find(new org.bson.Document("_id", testId))
                    .first();

            // Adicionar resultados à resposta
            response.put("originalId", testId);
            response.put("savedId", savedContent.getId());
            response.put("savedProcessId", savedContent.getProcessId());
            response.put("foundById", foundById.isPresent());
            response.put("foundByProcessId", foundByProcessId.isPresent());
            response.put("foundByMongoTemplate", foundDoc != null);

            if (foundById.isPresent()) {
                response.put("foundByIdValue", foundById.get().getId());
            }

            if (foundByProcessId.isPresent()) {
                response.put("foundByProcessIdValue", foundByProcessId.get().getId());
            }

            if (foundDoc != null) {
                response.put("foundByMongoTemplateValue", foundDoc.toJson());
            }

            response.put("status", "SUCCESS");
            response.put("message", "Teste de manipulação de IDs concluído");

            return ResponseEntity.ok(response);
        } catch (Exception e) {
            response.put("status", "ERROR");
            response.put("message", "Erro ao testar manipulação de IDs: " + e.getMessage());
            response.put("error", e.getClass().getName());
            log.error("[DIAGNÓSTICO] Erro ao testar manipulação de IDs: {}", e.getMessage(), e);
            return ResponseEntity.status(500).body(response);
        }
    }
}
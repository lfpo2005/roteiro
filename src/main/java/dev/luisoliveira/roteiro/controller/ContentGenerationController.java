package dev.luisoliveira.roteiro.controller;

import dev.luisoliveira.roteiro.config.security.UserPrincipal;
import dev.luisoliveira.roteiro.dto.GenerationRequest;
import dev.luisoliveira.roteiro.dto.GenerationResponse;
import dev.luisoliveira.roteiro.dto.ProcessStatus;
import dev.luisoliveira.roteiro.dto.TitleSelectionRequest;
import dev.luisoliveira.roteiro.event.ContentInitiatedEvent;
import dev.luisoliveira.roteiro.event.TitleSelectedEvent;
import dev.luisoliveira.roteiro.model.PrayerContent;
import dev.luisoliveira.roteiro.repository.PrayerContentRepository;
import dev.luisoliveira.roteiro.service.ContentCompilationService;
import dev.luisoliveira.roteiro.service.EventBusService;
import dev.luisoliveira.roteiro.service.ProcessTrackingService;
import dev.luisoliveira.roteiro.dto.ProcessStatusResponse;
import dev.luisoliveira.roteiro.dto.TitleCompletionRequest;
import dev.luisoliveira.roteiro.service.FileStorageService;

import org.springframework.core.io.ByteArrayResource;
import org.springframework.core.io.FileSystemResource;
import org.springframework.core.io.Resource;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import java.io.File;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.UUID;
import java.time.LocalDateTime;
import java.util.Map;
import java.util.HashMap;
import java.util.Optional;

@RestController
@RequestMapping("/content")
@RequiredArgsConstructor
@Slf4j
@CrossOrigin(origins = "*", maxAge = 3600)
public class ContentGenerationController {

    private final EventBusService eventBusService;
    private final ProcessTrackingService processTrackingService;
    private final ContentCompilationService contentCompilationService;
    private final MongoTemplate mongoTemplate;
    private final PrayerContentRepository prayerContentRepository;
    private final FileStorageService fileStorageService;

    @PostMapping("/generate")
    public ResponseEntity<GenerationResponse> startGeneration(@RequestBody GenerationRequest request) {
        String processId = UUID.randomUUID().toString();

        // Obter o usuário autenticado
        String userId = null;
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication != null && authentication.isAuthenticated() &&
                !authentication.getPrincipal().equals("anonymousUser")) {
            UserPrincipal userPrincipal = (UserPrincipal) authentication.getPrincipal();
            userId = String.valueOf(userPrincipal.getId());
            log.info("Processo iniciado pelo usuário: {}", userId);
        } else {
            log.info("Processo iniciado sem usuário autenticado");
        }

        log.info(
                "[PROCESSO] Iniciando processo de geração com ID: {} (idioma: {}, título: {}, gerarVersaoShort: {}, gerarAudio: {})",
                processId,
                request.getIdioma() != null ? request.getIdioma() : "es (padrão)",
                request.getTitulo() != null ? "fornecido" : "não fornecido",
                request.getGerarVersaoShort(),
                request.getGerarAudio());

        // Inicializar status
        processTrackingService.initializeProcess(processId);

        // Armazenar o userId no processo
        if (userId != null) {
            processTrackingService.setUserId(processId, userId);
        }
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

            // Verificar se a coleção oracoes existe
            boolean collectionExists = mongoTemplate.collectionExists("oracoes");
            status.put("collectionExists", collectionExists);
            log.info("[DIAGNÓSTICO] Coleção oracoes existe: {}", collectionExists);

            // Contar documentos
            long count = mongoTemplate.count(new org.springframework.data.mongodb.core.query.Query(),
                    "oracoes");
            status.put("documentCount", count);
            log.info("[DIAGNÓSTICO] Número de documentos na coleção oracoes: {}", count);

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
            testDoc.append("texto", "Conteúdo de teste");
            testDoc.append("createdAt", new java.util.Date());
            testDoc.append("updatedAt", new java.util.Date());

            mongoTemplate.getCollection("oracoes").insertOne(testDoc);
            log.info("[DIAGNÓSTICO] Documento de teste inserido com sucesso: {}", testId);

            // Verificar se o documento foi realmente salvo
            org.bson.Document foundDoc = mongoTemplate.getCollection("oracoes")
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
            org.bson.Document foundDoc = mongoTemplate.getCollection("oracoes")
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


    @GetMapping("/download/{processId}/{filename}")
    public ResponseEntity<Resource> downloadFile(@PathVariable String processId, @PathVariable String filename) {
        log.info("Iniciando download do arquivo {} do processo {}", filename, processId);

        try {
            // Buscar o arquivo no MongoDB
            byte[] fileData = fileStorageService.getBinaryFile(filename);

            if (fileData == null) {
                log.error("Arquivo não encontrado: {}", filename);
                return ResponseEntity.notFound().build();
            }

            // Criar o recurso para download
            ByteArrayResource resource = new ByteArrayResource(fileData);

            // Determinar o tipo de conteúdo baseado na extensão do arquivo
            String contentType = determineContentType(filename);

            // Configurar os headers para download
            HttpHeaders headers = new HttpHeaders();
            headers.add(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + filename + "\"");
            headers.setContentType(MediaType.parseMediaType(contentType));
            headers.setContentLength(fileData.length);

            return ResponseEntity.ok()
                    .headers(headers)
                    .body(resource);

        } catch (Exception e) {
            log.error("Erro ao baixar arquivo {}: {}", filename, e.getMessage(), e);
            return ResponseEntity.internalServerError().build();
        }
    }

    private String determineContentType(String filename) {
        String extension = filename.substring(filename.lastIndexOf(".") + 1).toLowerCase();
        switch (extension) {
            case "mp3":
                return "audio/mpeg";
            case "txt":
                return "text/plain";
            case "srt":
                return "text/plain";
            case "jpg":
            case "jpeg":
                return "image/jpeg";
            case "png":
                return "image/png";
            default:
                return "application/octet-stream";
        }
    }

    @GetMapping("/status/{processId}")
    public ResponseEntity<ProcessStatusResponse> getStatus(@PathVariable String processId) {
        // Log menos verboso para evitar poluir os logs
        log.debug("Verificando status do processo: {}", processId);

        try {
            // Primeiro, tentar obter o status do ProcessTrackingService
            ProcessStatus processStatus = processTrackingService.getStatus(processId);

            if (processStatus != null) {
                // Criar resposta com o status do processo
                ProcessStatusResponse response = new ProcessStatusResponse();
                response.setProcessId(processId);
                response.setStatus(processStatus.getCurrentStage());
                response.setProgress(processStatus.getProgressPercentage());
                response.setMessage("Processo em andamento: " + processStatus.getCurrentStage());
                response.setTimestamp(processStatus.getLastUpdated());

                // Se o processo estiver concluído, adicionar os IDs de conteúdo e áudio
                if (processStatus.isCompleted()) {
                    response.setContentId(processStatus.getResultPath());
                    response.setAudioId(processTrackingService.getFullAudioId(processId));
                    response.setMessage("Processo concluído");
                }

                // Log apenas para mudanças significativas de status
                if (processStatus.getProgressPercentage() % 10 == 0) {
                    log.info("Status do processo {}: {}, progresso: {}",
                            processId,
                            response.getStatus(),
                            response.getProgress());
                }

                return ResponseEntity.ok(response);
            }

            // Se não encontrou no ProcessTrackingService, tentar buscar no MongoDB
            List<PrayerContent> documents = prayerContentRepository.findByProcessId(processId);

            if (!documents.isEmpty()) {
                PrayerContent document = documents.get(0);

                // Criar resposta com o status do processo
                ProcessStatusResponse response = new ProcessStatusResponse();
                response.setProcessId(processId);
                response.setStatus("COMPLETED"); // Valor padrão para documentos no MongoDB
                response.setProgress(100); // Valor padrão para documentos no MongoDB
                response.setMessage("Processo concluído");
                response.setTimestamp(document.getCreatedAt());
                response.setContentId(String.valueOf(document.getId()));
                response.setAudioId(document.getAudioUrl());

                // Log apenas uma vez por sessão para cada processo
                log.debug("Status do processo {} (via MongoDB): COMPLETED, progresso: 100", processId);

                return ResponseEntity.ok(response);
            }

            // Se não encontrou em nenhum lugar, retornar 404
            log.warn("Processo não encontrado: {}", processId);
            return ResponseEntity.notFound().build();

        } catch (Exception e) {
            log.error("Erro ao verificar status do processo: {}", processId, e);
            return ResponseEntity.internalServerError().build();
        }
    }
}
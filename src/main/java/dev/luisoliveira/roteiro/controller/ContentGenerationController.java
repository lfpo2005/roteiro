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

import java.io.File;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/content")
@RequiredArgsConstructor
@Slf4j
public class ContentGenerationController {

    private final EventBusService eventBusService;
    private final ProcessTrackingService processTrackingService;

    @PostMapping("/generate")
    public ResponseEntity<GenerationResponse> startGeneration(@RequestBody GenerationRequest request) {
        String processId = UUID.randomUUID().toString();
        log.info("Iniciando processo de geração com ID: {} (idioma: {}, título: {})",
                processId,
                request.getIdioma() != null ? request.getIdioma() : "es (padrão)",
                request.getTitulo() != null ? "fornecido" : "não fornecido");

        // Inicializar status
        processTrackingService.initializeProcess(processId);

        // Verificar e configurar idioma padrão se necessário
        String idioma = request.getIdioma();
        if (idioma == null || idioma.isBlank()) {
            idioma = "es"; // Padrão: espanhol
            request.setIdioma(idioma);
        }

        // Obter flag de geração de imagem, definindo como false se não fornecido
        Boolean gerarImagem = request.getGerarImagem();
        if (gerarImagem == null) {
            gerarImagem = false;
            request.setGerarImagem(false);
        }

        // Obter flag de geração de áudio, definindo como false se não fornecido
        Boolean gerarAudio = request.getGerarAudio();
        if (gerarAudio == null) {
            gerarAudio = false;
            request.setGerarAudio(false);
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
                gerarImagem,
                gerarAudio
        );

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
                gerarImagem,
                gerarAudio
        ));

        String message;
        if (request.getTitulo() != null && !request.getTitulo().isEmpty()) {
            message = "Processo iniciado com sucesso usando o título fornecido: " + request.getTitulo();
        } else {
            message = "Processo iniciado com sucesso. Um título será gerado automaticamente.";
        }

        return ResponseEntity.accepted()
                .body(new GenerationResponse(processId, message));
    }

    @GetMapping("/titles/{processId}")
    public ResponseEntity<List<String>> getTitles(@PathVariable String processId) {
        List<String> titles = processTrackingService.getTitles(processId);

        if (titles == null || titles.isEmpty()) {
            return ResponseEntity.notFound().build();
        }

        return ResponseEntity.ok(titles);
    }

    @PostMapping("/select-title/{processId}")
    public ResponseEntity<GenerationResponse> selectTitle(
            @PathVariable String processId,
            @RequestBody TitleSelectionRequest request) {

        // Publicar evento de título selecionado
        eventBusService.publish(new TitleSelectedEvent(
                processId,
                request.getSelectedTitle()
        ));

        return ResponseEntity.accepted()
                .body(new GenerationResponse(processId, "Título selecionado com sucesso"));
    }

    @GetMapping("/status/{processId}")
    public ResponseEntity<ProcessStatus> checkStatus(@PathVariable String processId) {
        ProcessStatus status = processTrackingService.getStatus(processId);

        if (status == null) {
            return ResponseEntity.notFound().build();
        }

        return ResponseEntity.ok(status);
    }

    @GetMapping("/result/{processId}")
    public ResponseEntity<String> getResult(@PathVariable String processId) {
        String result = processTrackingService.getResult(processId);

        if (result == null) {
            return ResponseEntity.notFound().build();
        }

        return ResponseEntity.ok(result);
    }

    @GetMapping("/download/{processId}/{filename:.+}")
    public ResponseEntity<Resource> downloadFile(@PathVariable String processId, @PathVariable String filename) {
        String resultPath = processTrackingService.getResult(processId);

        if (resultPath == null) {
            return ResponseEntity.notFound().build();
        }

        Path filePath = Paths.get(resultPath, filename);
        File file = filePath.toFile();

        if (!file.exists()) {
            return ResponseEntity.notFound().build();
        }

        Resource resource = new FileSystemResource(file);

        HttpHeaders headers = new HttpHeaders();
        headers.add(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + filename + "\"");

        MediaType mediaType;
        if (filename.endsWith(".srt")) {
            mediaType = MediaType.parseMediaType("application/x-subrip");
        } else if (filename.endsWith(".txt")) {
            mediaType = MediaType.TEXT_PLAIN;
        } else {
            mediaType = MediaType.APPLICATION_OCTET_STREAM;
        }

        return ResponseEntity.ok()
                .headers(headers)
                .contentLength(file.length())
                .contentType(mediaType)
                .body(resource);
    }

    @GetMapping("/files/{processId}")
    public ResponseEntity<List<String>> getFiles(@PathVariable String processId) {
        String resultPath = processTrackingService.getResult(processId);

        if (resultPath == null) {
            return ResponseEntity.notFound().build();
        }

        File directory = new File(resultPath);
        if (!directory.exists() || !directory.isDirectory()) {
            return ResponseEntity.notFound().build();
        }

        List<String> fileList = List.of(directory.list());

        return ResponseEntity.ok(fileList);
    }
}
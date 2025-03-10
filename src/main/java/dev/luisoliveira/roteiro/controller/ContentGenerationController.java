package dev.luisoliveira.roteiro.controller;

import dev.luisoliveira.roteiro.dto.GenerationRequest;
import dev.luisoliveira.roteiro.dto.GenerationResponse;
import dev.luisoliveira.roteiro.dto.ProcessStatus;
import dev.luisoliveira.roteiro.dto.TitleSelectionRequest;
import dev.luisoliveira.roteiro.event.ContentInitiatedEvent;
import dev.luisoliveira.roteiro.event.TitlesGeneratedEvent;
import dev.luisoliveira.roteiro.service.EventBusService;
import dev.luisoliveira.roteiro.service.ProcessTrackingService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import lombok.RequiredArgsConstructor;


import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/content")
@RequiredArgsConstructor
public class ContentGenerationController {

    private final EventBusService eventBusService;
    private final ProcessTrackingService processTrackingService;

    @PostMapping("/generate")
    public ResponseEntity<GenerationResponse> startGeneration(@RequestBody GenerationRequest request) {
        String processId = UUID.randomUUID().toString();

        // Inicializar status
        processTrackingService.initializeProcess(processId);

        // Armazenar informações do processo
        processTrackingService.setProcessInfo(
                processId,
                request.getTema(),
                request.getEstiloOracao(),
                request.getDuracao(),
                request.getTipoOracao()
        );

        // Publicar evento inicial
        eventBusService.publish(new ContentInitiatedEvent(
                processId,
                request.getTema(),
                request.getEstiloOracao(),
                request.getDuracao(),
                request.getTipoOracao()
        ));

        return ResponseEntity.accepted()
                .body(new GenerationResponse(processId, "Processo iniciado com sucesso"));
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
        eventBusService.publish(new TitlesGeneratedEvent(
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
}
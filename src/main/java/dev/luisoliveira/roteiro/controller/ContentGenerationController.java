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
@RequestMapping("/api/content")
@RequiredArgsConstructor
@Slf4j
@CrossOrigin(origins = "*", maxAge = 3600)
public class ContentGenerationController {

    private final EventBusService eventBusService;
    private final ProcessTrackingService processTrackingService;

    @PostMapping("/generate")
    public ResponseEntity<GenerationResponse> startGeneration(@RequestBody GenerationRequest request) {
        String processId = UUID.randomUUID().toString();
        log.info(
                "Iniciando processo de geração com ID: {} (idioma: {}, título: {}, gerarVersaoShort: {}, gerarAudio: {})",
                processId,
                request.getIdioma() != null ? request.getIdioma() : "es (padrão)",
                request.getTitulo() != null ? "fornecido" : "não fornecido",
                request.getGerarVersaoShort(),
                request.getGerarAudio());

        // Inicializar status
        processTrackingService.initializeProcess(processId);

        // Verificar e configurar idioma padrão se necessário
        String idioma = request.getIdioma();
        if (idioma == null || idioma.isBlank()) {
            idioma = "es"; // Padrão: espanhol
            request.setIdioma(idioma);
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
        }

        // Proteção contra NullPointerException - verificar se gerarVersaoShort e
        // gerarAudio são null
        // antes de usar diretamente no método setProcessInfo
        if (gerarVersaoShort == null) {
            log.debug("gerarVersaoShort é null, definindo valor padrão");
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

        return ResponseEntity.accepted()
                .body(new GenerationResponse(processId, message));
    }

    // Resto do controlador permanece inalterado...
}
package dev.luisoliveira.roteiro.event;

import lombok.Getter;

/**
 * Evento disparado quando a compilação de conteúdo é concluída
 */
@Getter
public class ContentCompilationCompletedEvent extends ContentEvent {
    
    private final String contentId;
    
    public ContentCompilationCompletedEvent(Object source, String processId, String contentId) {
        super(processId);
        this.contentId = contentId;
    }
} 
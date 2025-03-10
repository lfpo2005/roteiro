package dev.luisoliveira.roteiro.event;

import lombok.Getter;

/**
 * Evento disparado quando um prompt de imagem é gerado e a imagem é criada
 */
@Getter
public class ImagePromptGeneratedEvent extends ContentEvent {
    private final String title;
    private final String imagePath;

    public ImagePromptGeneratedEvent(String processId, String title, String imagePath) {
        super(processId);
        this.title = title;
        this.imagePath = imagePath;
    }
}
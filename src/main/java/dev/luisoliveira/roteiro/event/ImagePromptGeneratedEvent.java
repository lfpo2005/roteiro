package dev.luisoliveira.roteiro.event;

import lombok.Getter;

@Getter
public class ImagePromptGeneratedEvent extends ContentEvent {
    private final String title;
    private final String imagePrompt;

    public ImagePromptGeneratedEvent(String processId, String title, String imagePrompt) {
        super(processId);
        this.title = title;
        this.imagePrompt = imagePrompt;
    }
}
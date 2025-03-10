package dev.luisoliveira.roteiro.event;

import lombok.Getter;

@Getter
public class ShortGeneratedEvent extends ContentEvent {
    private final String title;
    private final String oracaoContent;
    private final String shortContent;

    public ShortGeneratedEvent(String processId, String title, String oracaoContent, String shortContent) {
        super(processId);
        this.title = title;
        this.oracaoContent = oracaoContent;
        this.shortContent = shortContent;
    }
}
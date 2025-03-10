package dev.luisoliveira.roteiro.event;

import lombok.Getter;

@Getter
public class OracaoGeneratedEvent extends ContentEvent {
    private final String title;
    private final String oracaoContent;

    public OracaoGeneratedEvent(String processId, String title, String oracaoContent) {
        super(processId);
        this.title = title;
        this.oracaoContent = oracaoContent;
    }
}

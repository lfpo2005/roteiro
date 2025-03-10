package dev.luisoliveira.roteiro.event;

import lombok.Getter;

@Getter
public class ContentEvent {
    protected final String processId;

    public ContentEvent(String processId) {
        this.processId = processId;
    }
}
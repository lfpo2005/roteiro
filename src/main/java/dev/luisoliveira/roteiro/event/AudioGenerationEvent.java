package dev.luisoliveira.roteiro.event;

import lombok.Getter;

@Getter
public class AudioGenerationEvent extends ContentEvent {

    public AudioGenerationEvent(Object source, String processId) {
        super(processId);
    }
}
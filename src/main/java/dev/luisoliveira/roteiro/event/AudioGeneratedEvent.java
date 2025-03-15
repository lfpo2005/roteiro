package dev.luisoliveira.roteiro.event;

import lombok.Getter;

/**
 * Evento disparado quando os arquivos de áudio são gerados com sucesso.
 */
@Getter
public class AudioGeneratedEvent extends ContentEvent {
    private final String title;
    private final String fullAudioPath;
    private final String shortAudioPath;

    public AudioGeneratedEvent(String processId, String title, String fullAudioPath, String shortAudioPath) {
        super(processId);
        this.title = title;
        this.fullAudioPath = fullAudioPath;
        this.shortAudioPath = shortAudioPath;
    }

    public String getOracaoAudioPath() {
        return fullAudioPath;
    }
}
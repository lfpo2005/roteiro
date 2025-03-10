package dev.luisoliveira.roteiro.event;

import lombok.Getter;

@Getter
public class ContentCompletedEvent extends ContentEvent {
    private final String title;
    private final String resultPath;

    public ContentCompletedEvent(String processId, String title, String resultPath) {
        super(processId);
        this.title = title;
        this.resultPath = resultPath;
    }
}
package dev.luisoliveira.roteiro.event;

import lombok.Getter;

@Getter
public class TitleSelectedEvent extends ContentEvent {
    private final String selectedTitle;

    public TitleSelectedEvent(String processId, String selectedTitle) {
        super(processId);
        this.selectedTitle = selectedTitle;
    }
}

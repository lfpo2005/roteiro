package dev.luisoliveira.roteiro.event;

import lombok.Getter;

@Getter
public class DescriptionGeneratedEvent extends ContentEvent {
    private final String title;
    private final String oracaoContent;
    private final String shortContent;
    private final String descriptionContent;

    public DescriptionGeneratedEvent(String processId, String title, String oracaoContent,
                                     String shortContent, String descriptionContent) {
        super(processId);
        this.title = title;
        this.oracaoContent = oracaoContent;
        this.shortContent = shortContent;
        this.descriptionContent = descriptionContent;
    }


}
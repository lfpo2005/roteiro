package dev.luisoliveira.roteiro.event;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@AllArgsConstructor
@NoArgsConstructor
public class ImagePromptGeneratedEvent extends ContentEvent {
    private final String title;
    private final String imagePrompt;

}
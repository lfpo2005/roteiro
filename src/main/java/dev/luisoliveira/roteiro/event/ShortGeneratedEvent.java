package dev.luisoliveira.roteiro.event;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@AllArgsConstructor
@NoArgsConstructor
public class ShortGeneratedEvent extends ContentEvent {
    private final String title;
    private final String oracaoContent;
    private final String shortContent;

}
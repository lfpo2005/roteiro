package dev.luisoliveira.roteiro.event;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.List;

@Getter
@AllArgsConstructor
@NoArgsConstructor
public class TitlesGeneratedEvent extends ContentEvent {

    private String title;
    private String tema;
    private String estiloOracao;
    private String duracao;
    private String tipoOracao;
}
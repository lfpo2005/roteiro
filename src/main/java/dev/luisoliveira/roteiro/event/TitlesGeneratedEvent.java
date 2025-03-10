package dev.luisoliveira.roteiro.event;

import lombok.Getter;
import java.util.List;

@Getter
public class TitlesGeneratedEvent extends ContentEvent {
    private final List<String> titles;
    private final String tema;
    private final String estiloOracao;
    private final String duracao;
    private final String tipoOracao;

    public TitlesGeneratedEvent(String processId, List<String> titles, String tema,
                                String estiloOracao, String duracao, String tipoOracao) {
        super(processId);
        this.titles = titles;
        this.tema = tema;
        this.estiloOracao = estiloOracao;
        this.duracao = duracao;
        this.tipoOracao = tipoOracao;
    }
}

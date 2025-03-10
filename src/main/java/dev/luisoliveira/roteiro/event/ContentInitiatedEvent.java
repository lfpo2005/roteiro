package dev.luisoliveira.roteiro.event;

import lombok.Getter;

@Getter
public class ContentInitiatedEvent extends ContentEvent {
    private final String tema;
    private final String estiloOracao;
    private final String duracao;
    private final String tipoOracao;
    private final String idioma;

    public ContentInitiatedEvent(String processId, String tema, String estiloOracao,
                                 String duracao, String tipoOracao, String idioma) {
        super(processId);
        this.tema = tema;
        this.estiloOracao = estiloOracao;
        this.duracao = duracao;
        this.tipoOracao = tipoOracao;
        this.idioma = idioma;
    }
}
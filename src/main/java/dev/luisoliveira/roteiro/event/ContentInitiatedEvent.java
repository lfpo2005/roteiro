package dev.luisoliveira.roteiro.event;

import lombok.Getter;

@Getter
public class ContentInitiatedEvent extends ContentEvent {
    private final String tema;
    private final String estiloOracao;
    private final String duracao;
    private final String tipoOracao;
    private final String idioma;
    private final String titulo;
    private final String observacoes;
    private final boolean gerarImagem;
    private final boolean gerarAudio;

    public ContentInitiatedEvent(String processId, String tema, String estiloOracao,
                                 String duracao, String tipoOracao, String idioma,
                                 String titulo, String observacoes, boolean gerarImagem, boolean gerarAudio) {
        super(processId);
        this.tema = tema;
        this.estiloOracao = estiloOracao;
        this.duracao = duracao;
        this.tipoOracao = tipoOracao;
        this.idioma = idioma;
        this.titulo = titulo;
        this.observacoes = observacoes;
        this.gerarImagem = gerarImagem;
        this.gerarAudio = gerarAudio;
    }

    // Verifica se um título foi fornecido
    public boolean hasTitulo() {
        return titulo != null && !titulo.trim().isEmpty();
    }

    // Verifica se observações foram fornecidas
    public boolean hasObservacoes() {
        return observacoes != null && !observacoes.trim().isEmpty();
    }
}
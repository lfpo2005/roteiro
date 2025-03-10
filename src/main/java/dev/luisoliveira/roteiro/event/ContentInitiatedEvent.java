package dev.luisoliveira.roteiro.event;

import lombok.Getter;

@Getter
public class ContentInitiatedEvent extends ContentEvent {
    private final String tema;
    private final String estiloOracao;
    private final String duracao;
    private final String tipoOracao;
    private final String idioma;
    private final String titulo; // Título opcional
    private final String observacoes; // Observações adicionais

    public ContentInitiatedEvent(String processId, String tema, String estiloOracao,
                                 String duracao, String tipoOracao, String idioma,
                                 String titulo, String observacoes) {
        super(processId);
        this.tema = tema;
        this.estiloOracao = estiloOracao;
        this.duracao = duracao;
        this.tipoOracao = tipoOracao;
        this.idioma = idioma;
        this.titulo = titulo;
        this.observacoes = observacoes;
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
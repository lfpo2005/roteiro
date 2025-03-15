package dev.luisoliveira.roteiro.dto;

import lombok.Data;

@Data
public class GenerationRequest {
    private String tema;
    private String estiloOracao;
    private String duracao;
    private String tipoOracao;
    private String idioma = "es";
    private String titulo;
    private String observacoes;
    private Boolean gerarVersaoShort = null;
    private Boolean gerarAudio = null;
}
package dev.luisoliveira.roteiro.service;

import org.springframework.stereotype.Service;
import lombok.extern.slf4j.Slf4j;

/**
 * Serviço para conversão de texto para formato SRT (legendas)
 */
@Service
@Slf4j
public class SrtConverterService {

    private static final int DURACAO_BLOCO = 30; // duração em segundos
    private static final int INTERVALO_ENTRE_BLOCOS = 20; // intervalo em segundos
    private static final int CARACTERES_POR_BLOCO = 500;
    private static final int PALAVRAS_MAX_BLOCO = 100;

    /**
     * Converte o texto da oração para o formato SRT
     * 
     * @param texto Texto a ser convertido
     * @return String no formato SRT
     */
    public String converterParaSRT(String texto) {
        StringBuilder srt = new StringBuilder();
        int contador = 1;
        int tempoAcumulado = 0;
        String[] palavras = texto.split("\\s+");
        StringBuilder blocoAtual = new StringBuilder();
        int palavrasNoBloco = 0;

        for (String palavra : palavras) {
            if (blocoAtual.length() + palavra.length() <= CARACTERES_POR_BLOCO
                    && palavrasNoBloco < PALAVRAS_MAX_BLOCO) {
                blocoAtual.append(palavra).append(" ");
                palavrasNoBloco++;
            } else {
                int ultimoPontoFinal = blocoAtual.lastIndexOf(".");
                if (ultimoPontoFinal != -1 && ultimoPontoFinal != blocoAtual.length() - 1) {
                    String resto = blocoAtual.substring(ultimoPontoFinal + 1);
                    blocoAtual.setLength(ultimoPontoFinal + 1);
                    srt.append(formatarBlocoSRT(contador, tempoAcumulado, blocoAtual.toString()));
                    contador++;
                    tempoAcumulado += DURACAO_BLOCO + INTERVALO_ENTRE_BLOCOS;
                    blocoAtual = new StringBuilder(resto).append(palavra).append(" ");
                    palavrasNoBloco = resto.split("\\s+").length + 1;
                } else {
                    srt.append(formatarBlocoSRT(contador, tempoAcumulado, blocoAtual.toString()));
                    contador++;
                    tempoAcumulado += DURACAO_BLOCO + INTERVALO_ENTRE_BLOCOS;
                    blocoAtual = new StringBuilder(palavra).append(" ");
                    palavrasNoBloco = 1;
                }
            }
        }

        // Adicionar o último bloco
        if (blocoAtual.length() > 0) {
            srt.append(formatarBlocoSRT(contador, tempoAcumulado, blocoAtual.toString()));
        }

        return srt.toString().trim();
    }

    private String formatarBlocoSRT(int contador, int tempoInicio, String texto) {
        int tempoFim = tempoInicio + DURACAO_BLOCO;
        return String.format("%d\n%s --> %s\n%s\n\n",
                contador,
                formatarTempo(tempoInicio),
                formatarTempo(tempoFim),
                texto.trim());
    }

    private String formatarTempo(int segundos) {
        int horas = segundos / 3600;
        int minutos = (segundos % 3600) / 60;
        int segsRestantes = segundos % 60;
        return String.format("%02d:%02d:%02d,000", horas, minutos, segsRestantes);
    }

    /**
     * Converte texto para formato SRT
     * 
     * @param text Texto a ser convertido
     * @return Texto no formato SRT
     */
    public String convertToSrt(String text) {
        log.info("Convertendo texto para formato SRT");
        // Implementação simplificada
        return text;
    }
}
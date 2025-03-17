package dev.luisoliveira.roteiro.service;

import org.springframework.stereotype.Service;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import java.time.Duration;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Serviço para conversão de texto em formato SRT (legendas)
 */
@Service
@Slf4j
@RequiredArgsConstructor
public class SrtConverterService {

    // Tempo médio de leitura em caracteres por segundo
    private static final int CHARS_PER_SECOND = 15;
    // Tamanho máximo de cada legenda
    private static final int MAX_SUBTITLE_LENGTH = 40;

    /**
     * Converte texto em formato SRT (legendas)
     * 
     * @param text Texto a ser convertido
     * @return Texto em formato SRT
     */
    public String convertToSrt(String text) {
        log.info("Convertendo texto para formato SRT (tamanho: {} caracteres)", text.length());

        // Dividir o texto em sentenças
        List<String> sentences = splitIntoSentences(text);

        // Dividir sentenças em legendas de tamanho adequado
        List<String> subtitles = new ArrayList<>();
        for (String sentence : sentences) {
            if (sentence.length() <= MAX_SUBTITLE_LENGTH) {
                subtitles.add(sentence);
            } else {
                // Dividir sentenças longas em partes menores
                subtitles.addAll(splitLongSentence(sentence));
            }
        }

        // Gerar o arquivo SRT
        StringBuilder srtContent = new StringBuilder();
        int index = 1;
        long startTime = 0;

        for (String subtitle : subtitles) {
            // Calcular duração baseada no número de caracteres
            long duration = (long) Math.ceil((double) subtitle.length() / CHARS_PER_SECOND) * 1000;
            long endTime = startTime + duration;

            // Formatar tempos
            String startTimeFormatted = formatTime(startTime);
            String endTimeFormatted = formatTime(endTime);

            // Adicionar entrada SRT
            srtContent.append(index).append("\n");
            srtContent.append(startTimeFormatted).append(" --> ").append(endTimeFormatted).append("\n");
            srtContent.append(subtitle).append("\n\n");

            // Preparar para próxima legenda
            index++;
            startTime = endTime + 100; // 100ms de pausa entre legendas
        }

        log.info("Conversão para SRT concluída. Geradas {} legendas", subtitles.size());
        return srtContent.toString();
    }

    /**
     * Divide texto em sentenças
     * 
     * @param text Texto a ser dividido
     * @return Lista de sentenças
     */
    private List<String> splitIntoSentences(String text) {
        List<String> sentences = new ArrayList<>();
        Pattern pattern = Pattern.compile("[^.!?\\s][^.!?]*(?:[.!?](?!['\"]?\\s|$)[^.!?]*)*[.!?]?['\"]?(?=\\s|$)");
        Matcher matcher = pattern.matcher(text);

        while (matcher.find()) {
            String sentence = matcher.group().trim();
            if (!sentence.isEmpty()) {
                sentences.add(sentence);
            }
        }

        return sentences;
    }

    /**
     * Divide uma sentença longa em partes menores
     * 
     * @param sentence Sentença a ser dividida
     * @return Lista de partes da sentença
     */
    private List<String> splitLongSentence(String sentence) {
        List<String> parts = new ArrayList<>();
        int length = sentence.length();
        int start = 0;

        while (start < length) {
            int end = Math.min(start + MAX_SUBTITLE_LENGTH, length);

            // Ajustar para não cortar palavras
            if (end < length && !Character.isWhitespace(sentence.charAt(end))) {
                int lastSpace = sentence.lastIndexOf(' ', end);
                if (lastSpace > start) {
                    end = lastSpace;
                }
            }

            parts.add(sentence.substring(start, end).trim());
            start = end;
        }

        return parts;
    }

    /**
     * Formata tempo em milissegundos para formato SRT (HH:MM:SS,mmm)
     * 
     * @param timeMs Tempo em milissegundos
     * @return Tempo formatado
     */
    private String formatTime(long timeMs) {
        Duration duration = Duration.ofMillis(timeMs);
        long hours = duration.toHours();
        long minutes = duration.toMinutesPart();
        long seconds = duration.toSecondsPart();
        long millis = duration.toMillisPart();

        return String.format("%02d:%02d:%02d,%03d", hours, minutes, seconds, millis);
    }
}
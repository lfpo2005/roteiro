package dev.luisoliveira.roteiro.service;

import dev.luisoliveira.roteiro.dto.ProcessStatus;
import org.springframework.stereotype.Service;
import lombok.extern.slf4j.Slf4j;

import java.time.LocalDateTime;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.List;
import java.util.ArrayList;

@Service
@Slf4j
public class ProcessTrackingService {

    private final Map<String, ProcessStatus> processes = new ConcurrentHashMap<>();
    private final Map<String, List<String>> processTitles = new ConcurrentHashMap<>();
    private final Map<String, String> processResults = new ConcurrentHashMap<>();
    private final Map<String, ProcessInfo> processInfos = new ConcurrentHashMap<>();

    // Classe interna para armazenar informações do processo
    private static class ProcessInfo {
        private String tema;
        private String estiloOracao;
        private String duracao;
        private String tipoOracao;
        private String idioma;
        private String titulo;
        private String observacoes;
        private String oracaoContent;
        private String shortContent;
        private String descriptionContent;
        private boolean imageBeingProcessed = false;
        private boolean gerarImagem = false;

        public String getTema() { return tema; }
        public void setTema(String tema) { this.tema = tema; }

        public String getEstiloOracao() { return estiloOracao; }
        public void setEstiloOracao(String estiloOracao) { this.estiloOracao = estiloOracao; }

        public String getDuracao() { return duracao; }
        public void setDuracao(String duracao) { this.duracao = duracao; }

        public String getTipoOracao() { return tipoOracao; }
        public void setTipoOracao(String tipoOracao) { this.tipoOracao = tipoOracao; }

        public String getIdioma() { return idioma; }
        public void setIdioma(String idioma) { this.idioma = idioma; }

        public String getTitulo() { return titulo; }
        public void setTitulo(String titulo) { this.titulo = titulo; }

        public String getObservacoes() { return observacoes; }
        public void setObservacoes(String observacoes) { this.observacoes = observacoes; }

        public String getOracaoContent() { return oracaoContent; }
        public void setOracaoContent(String oracaoContent) { this.oracaoContent = oracaoContent; }

        public String getShortContent() { return shortContent; }
        public void setShortContent(String shortContent) { this.shortContent = shortContent; }

        public String getDescriptionContent() { return descriptionContent; }
        public void setDescriptionContent(String descriptionContent) { this.descriptionContent = descriptionContent; }

        public boolean isImageBeingProcessed() { return imageBeingProcessed; }
        public void setImageBeingProcessed(boolean imageBeingProcessed) { this.imageBeingProcessed = imageBeingProcessed; }

        public boolean isGerarImagem() { return gerarImagem; }
        public void setGerarImagem(boolean gerarImagem) { this.gerarImagem = gerarImagem; }
    }

    public void initializeProcess(String processId) {
        ProcessStatus status = new ProcessStatus();
        status.setProcessId(processId);
        status.setCurrentStage("Iniciado");
        status.setProgressPercentage(0);
        status.setStartTime(LocalDateTime.now());
        status.setLastUpdated(LocalDateTime.now());
        status.setCompleted(false);

        processes.put(processId, status);
        processInfos.put(processId, new ProcessInfo());
        log.debug("Processo inicializado: {}", processId);
    }

    public void setProcessInfo(String processId, String tema, String estiloOracao,
                               String duracao, String tipoOracao, String idioma,
                               String titulo, String observacoes) {
        ProcessInfo info = processInfos.get(processId);
        if (info != null) {
            info.setTema(tema);
            info.setEstiloOracao(estiloOracao);
            info.setDuracao(duracao);
            info.setTipoOracao(tipoOracao);
            info.setIdioma(idioma != null ? idioma : "es"); // Padrão para espanhol se não especificado
            info.setTitulo(titulo);
            info.setObservacoes(observacoes);
            log.debug("Informações do processo configuradas: processId={}, idioma={}, titulo={}",
                    processId, idioma, titulo != null ? "fornecido" : "não fornecido");
        }
    }

    public String getTema(String processId) {
        ProcessInfo info = processInfos.get(processId);
        return info != null ? info.getTema() : null;
    }

    public String getEstiloOracao(String processId) {
        ProcessInfo info = processInfos.get(processId);
        return info != null ? info.getEstiloOracao() : null;
    }

    public String getDuracao(String processId) {
        ProcessInfo info = processInfos.get(processId);
        return info != null ? info.getDuracao() : null;
    }

    public String getTipoOracao(String processId) {
        ProcessInfo info = processInfos.get(processId);
        return info != null ? info.getTipoOracao() : null;
    }

    public String getIdioma(String processId) {
        ProcessInfo info = processInfos.get(processId);
        return info != null ? info.getIdioma() : "es"; // Padrão para espanhol
    }

    public String getTitulo(String processId) {
        ProcessInfo info = processInfos.get(processId);
        return info != null ? info.getTitulo() : null;
    }

    public String getObservacoes(String processId) {
        ProcessInfo info = processInfos.get(processId);
        return info != null ? info.getObservacoes() : null;
    }

    public boolean hasTitulo(String processId) {
        String titulo = getTitulo(processId);
        return titulo != null && !titulo.trim().isEmpty();
    }

    public void updateStatus(String processId, String currentStage, int progressPercentage) {
        if (processes.containsKey(processId)) {
            ProcessStatus status = processes.get(processId);
            status.setCurrentStage(currentStage);
            status.setProgressPercentage(progressPercentage);
            status.setLastUpdated(LocalDateTime.now());
            log.debug("Status atualizado: processId={}, stage={}, progress={}%",
                    processId, currentStage, progressPercentage);
        }
    }

    public void storeTitles(String processId, List<String> titles) {
        processTitles.put(processId, titles);
        log.debug("Títulos armazenados: processId={}, quantidade={}", processId, titles.size());
    }

    public List<String> getTitles(String processId) {
        return processTitles.getOrDefault(processId, new ArrayList<>());
    }

    public void storeResult(String processId, String filePath) {
        processResults.put(processId, filePath);
        if (processes.containsKey(processId)) {
            ProcessStatus status = processes.get(processId);
            status.setCompleted(true);
            status.setProgressPercentage(100);
            status.setCurrentStage("Concluído");
            status.setResultPath(filePath);
            status.setLastUpdated(LocalDateTime.now());
            log.info("Processo concluído: processId={}, resultPath={}", processId, filePath);
        }
    }

    public ProcessStatus getStatus(String processId) {
        return processes.get(processId);
    }

    public String getResult(String processId) {
        return processResults.get(processId);
    }

    public void storeOracaoContent(String processId, String oracaoContent) {
        ProcessInfo info = processInfos.get(processId);
        if (info != null) {
            info.setOracaoContent(oracaoContent);
            log.debug("Conteúdo da oração armazenado para processo: {}", processId);
        }
    }

    /**
     * Armazena o conteúdo da versão short para uso posterior
     */
    public void storeShortContent(String processId, String shortContent) {
        ProcessInfo info = processInfos.get(processId);
        if (info != null) {
            info.setShortContent(shortContent);
            log.debug("Conteúdo short armazenado para processo: {}", processId);
        }
    }

    /**
     * Armazena o conteúdo da descrição para uso posterior
     */
    public void storeDescriptionContent(String processId, String descriptionContent) {
        ProcessInfo info = processInfos.get(processId);
        if (info != null) {
            info.setDescriptionContent(descriptionContent);
            log.debug("Conteúdo da descrição armazenado para processo: {}", processId);
        }
    }

    /**
     * Marca que a imagem está sendo processada
     */
    public void setImageBeingProcessed(String processId, boolean isProcessing) {
        ProcessInfo info = processInfos.get(processId);
        if (info != null) {
            info.setImageBeingProcessed(isProcessing);
            log.debug("Status de processamento de imagem atualizado para {}: {}",
                    processId, isProcessing ? "em processamento" : "concluído");
        }
    }

    /**
     * Verifica se a imagem está sendo processada
     */
    public boolean isImageBeingProcessed(String processId) {
        ProcessInfo info = processInfos.get(processId);
        return info != null && info.isImageBeingProcessed();
    }

    /**
     * Recupera o conteúdo da oração
     */
    public String getOracaoContent(String processId) {
        ProcessInfo info = processInfos.get(processId);
        return info != null ? info.getOracaoContent() : null;
    }

    /**
     * Recupera o conteúdo da versão short
     */
    public String getShortContent(String processId) {
        ProcessInfo info = processInfos.get(processId);
        return info != null ? info.getShortContent() : null;
    }

    /**
     * Recupera o conteúdo da descrição
     */
    public String getDescriptionContent(String processId) {
        ProcessInfo info = processInfos.get(processId);
        return info != null ? info.getDescriptionContent() : null;
    }


}
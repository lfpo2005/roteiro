package dev.luisoliveira.roteiro.service;

import dev.luisoliveira.roteiro.dto.ProcessStatus;
import org.springframework.stereotype.Service;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import java.time.LocalDateTime;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.List;
import java.util.ArrayList;

/**
 * Serviço para rastreamento de processos de geração de conteúdo (versão
 * transitória para MongoDB)
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class ProcessTrackingService {

    private final NotificationService notificationService;
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
        private Boolean gerarVersaoShort = null;
        private Boolean gerarAudio = null;
        private String fullAudioId; // ID do áudio da oração completa
        private String shortAudioId; // ID do áudio da versão curta

        // Getters e setters
        public String getTema() {
            return tema;
        }

        public void setTema(String tema) {
            this.tema = tema;
        }

        public String getEstiloOracao() {
            return estiloOracao;
        }

        public void setEstiloOracao(String estiloOracao) {
            this.estiloOracao = estiloOracao;
        }

        public String getDuracao() {
            return duracao;
        }

        public void setDuracao(String duracao) {
            this.duracao = duracao;
        }

        public String getTipoOracao() {
            return tipoOracao;
        }

        public void setTipoOracao(String tipoOracao) {
            this.tipoOracao = tipoOracao;
        }

        public String getIdioma() {
            return idioma;
        }

        public void setIdioma(String idioma) {
            this.idioma = idioma;
        }

        public String getTitulo() {
            return titulo;
        }

        public void setTitulo(String titulo) {
            this.titulo = titulo;
        }

        public String getObservacoes() {
            return observacoes;
        }

        public void setObservacoes(String observacoes) {
            this.observacoes = observacoes;
        }

        public String getOracaoContent() {
            return oracaoContent;
        }

        public void setOracaoContent(String oracaoContent) {
            this.oracaoContent = oracaoContent;
        }

        public String getShortContent() {
            return shortContent;
        }

        public void setShortContent(String shortContent) {
            this.shortContent = shortContent;
        }

        public String getDescriptionContent() {
            return descriptionContent;
        }

        public void setDescriptionContent(String descriptionContent) {
            this.descriptionContent = descriptionContent;
        }

        public Boolean getGerarVersaoShort() {
            return gerarVersaoShort;
        }

        public void setGerarVersaoShort(Boolean gerarVersaoShort) {
            this.gerarVersaoShort = gerarVersaoShort;
        }

        public Boolean getGerarAudio() {
            return gerarAudio;
        }

        public void setGerarAudio(Boolean gerarAudio) {
            this.gerarAudio = gerarAudio;
        }

        public String getFullAudioId() {
            return fullAudioId;
        }

        public void setFullAudioId(String fullAudioId) {
            this.fullAudioId = fullAudioId;
        }

        public String getShortAudioId() {
            return shortAudioId;
        }

        public void setShortAudioId(String shortAudioId) {
            this.shortAudioId = shortAudioId;
        }
    }

    /**
     * Inicializa um novo processo
     * 
     * @param processId ID do processo
     */
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

    /**
     * Atualiza o status de um processo
     * 
     * @param processId          ID do processo
     * @param currentStage       Estágio atual
     * @param progressPercentage Percentual de progresso
     */
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

    /**
     * Armazena o resultado de um processo
     * 
     * @param processId ID do processo
     * @param contentId ID do conteúdo gerado
     */
    public void storeResult(String processId, String contentId) {
        processResults.put(processId, contentId);
        if (processes.containsKey(processId)) {
            ProcessStatus status = processes.get(processId);
            status.setCompleted(true);
            status.setProgressPercentage(100);
            status.setCurrentStage("Concluído");
            status.setResultPath(contentId); // Agora armazena o ID em vez do caminho
            status.setLastUpdated(LocalDateTime.now());
            log.info("Processo concluído: processId={}, contentId={}", processId, contentId);
        }
    }

    /**
     * Armazena os IDs de áudio para um processo
     * 
     * @param processId    ID do processo
     * @param fullAudioId  ID do áudio da oração completa
     * @param shortAudioId ID do áudio da versão curta
     */
    public void storeAudioIds(String processId, String fullAudioId, String shortAudioId) {
        ProcessInfo info = processInfos.get(processId);
        if (info != null) {
            info.setFullAudioId(fullAudioId);
            info.setShortAudioId(shortAudioId);
            log.info("IDs de áudio armazenados para o processo {}: full={}, short={}",
                    processId, fullAudioId, shortAudioId);
        } else {
            log.warn("Tentativa de armazenar IDs de áudio para processo inexistente: {}", processId);
        }
    }

    /**
     * Recupera o ID do áudio da oração completa
     * 
     * @param processId ID do processo
     * @return ID do áudio ou null se não existir
     */
    public String getFullAudioId(String processId) {
        ProcessInfo info = processInfos.get(processId);
        return info != null ? info.getFullAudioId() : null;
    }

    /**
     * Recupera o ID do áudio da versão curta
     * 
     * @param processId ID do processo
     * @return ID do áudio ou null se não existir
     */
    public String getShortAudioId(String processId) {
        ProcessInfo info = processInfos.get(processId);
        return info != null ? info.getShortAudioId() : null;
    }

    /**
     * Define as informações de um processo
     */
    public void setProcessInfo(String processId, String tema, String estiloOracao,
            String duracao, String tipoOracao, String idioma,
            String titulo, String observacoes, Boolean gerarVersaoShort) {
        setProcessInfo(processId, tema, estiloOracao, duracao, tipoOracao, idioma, titulo, observacoes,
                gerarVersaoShort, null);
    }

    /**
     * Define as informações de um processo incluindo a flag de áudio
     */
    public void setProcessInfo(String processId, String tema, String estiloOracao,
            String duracao, String tipoOracao, String idioma,
            String titulo, String observacoes, Boolean gerarVersaoShort, Boolean gerarAudio) {
        ProcessInfo info = processInfos.get(processId);
        if (info != null) {
            info.setTema(tema);
            info.setEstiloOracao(estiloOracao);
            info.setDuracao(duracao);
            info.setTipoOracao(tipoOracao);
            info.setIdioma(idioma != null ? idioma : "es"); // Padrão para espanhol se não especificado
            info.setTitulo(titulo);
            info.setObservacoes(observacoes);
            info.setGerarVersaoShort(gerarVersaoShort);
            info.setGerarAudio(gerarAudio);
            log.debug(
                    "Informações do processo configuradas: processId={}, idioma={}, titulo={}, gerarVersaoShort={}, gerarAudio={}",
                    processId, idioma, titulo != null ? "fornecido" : "não fornecido", gerarVersaoShort, gerarAudio);
        }
    }

    // Sobrecarga do método para manter compatibilidade com código existente
    public void setProcessInfo(String processId, String tema, String estiloOracao,
            String duracao, String tipoOracao, String idioma,
            String titulo, String observacoes) {
        setProcessInfo(processId, tema, estiloOracao, duracao, tipoOracao, idioma, titulo, observacoes, null, null);
    }

    /**
     * Verifica se o áudio deve ser gerado para este processo
     * 
     * @param processId ID do processo
     * @return true se o áudio deve ser gerado, false caso contrário
     */
    public boolean deveGerarAudio(String processId) {
        ProcessInfo info = processInfos.get(processId);
        if (info == null) {
            log.warn("Processo não encontrado: {}, assumindo que não deve gerar áudio", processId);
            return false;
        }

        // Se a flag estiver explicitamente definida, use esse valor
        if (info.getGerarAudio() != null) {
            return info.getGerarAudio();
        }

        // Por padrão, não gerar áudio (comportamento conservador)
        return false;
    }

    /**
     * Define se o áudio deve ser gerado para este processo
     * 
     * @param processId  ID do processo
     * @param gerarAudio true se o áudio deve ser gerado, false caso contrário
     */
    public void setGerarAudio(String processId, Boolean gerarAudio) {
        ProcessInfo info = processInfos.get(processId);
        if (info != null) {
            info.setGerarAudio(gerarAudio);
            log.debug("Flag gerarAudio atualizada para {}: {}", processId, gerarAudio);
        }
    }

    // Métodos getter para informações do processo
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
        return info != null ? info.getIdioma() : null;
    }

    public String getTitulo(String processId) {
        ProcessInfo info = processInfos.get(processId);
        return info != null ? info.getTitulo() : null;
    }

    public String getObservacoes(String processId) {
        ProcessInfo info = processInfos.get(processId);
        return info != null ? info.getObservacoes() : null;
    }

    public Boolean getGerarVersaoShort(String processId) {
        ProcessInfo info = processInfos.get(processId);
        return info != null ? info.getGerarVersaoShort() : null;
    }

    public void setTitulo(String processId, String titulo) {
        ProcessInfo info = processInfos.get(processId);
        if (info != null) {
            info.setTitulo(titulo);
        }
    }

    public void setOracaoContent(String processId, String oracaoContent) {
        ProcessInfo info = processInfos.get(processId);
        if (info != null) {
            info.setOracaoContent(oracaoContent);
        }
    }

    public void setShortContent(String processId, String shortContent) {
        ProcessInfo info = processInfos.get(processId);
        if (info != null) {
            info.setShortContent(shortContent);
        }
    }

    public void setDescriptionContent(String processId, String descriptionContent) {
        ProcessInfo info = processInfos.get(processId);
        if (info != null) {
            info.setDescriptionContent(descriptionContent);
        }
    }

    public String getOracaoContent(String processId) {
        ProcessInfo info = processInfos.get(processId);
        return info != null ? info.getOracaoContent() : null;
    }

    public String getShortContent(String processId) {
        ProcessInfo info = processInfos.get(processId);
        return info != null ? info.getShortContent() : null;
    }

    public String getDescriptionContent(String processId) {
        ProcessInfo info = processInfos.get(processId);
        return info != null ? info.getDescriptionContent() : null;
    }

    public ProcessStatus getStatus(String processId) {
        return processes.get(processId);
    }

    public void saveTitles(String processId, List<String> titles) {
        processTitles.put(processId, titles);
    }

    public List<String> getTitles(String processId) {
        return processTitles.getOrDefault(processId, new ArrayList<>());
    }

    public String getResultId(String processId) {
        return processResults.get(processId);
    }
}
package dev.luisoliveira.roteiro.service;

import dev.luisoliveira.roteiro.dto.ProcessStatus;
import org.springframework.stereotype.Service;
import java.time.LocalDateTime;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.List;
import java.util.ArrayList;

@Service
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

        public String getTema() { return tema; }
        public void setTema(String tema) { this.tema = tema; }

        public String getEstiloOracao() { return estiloOracao; }
        public void setEstiloOracao(String estiloOracao) { this.estiloOracao = estiloOracao; }

        public String getDuracao() { return duracao; }
        public void setDuracao(String duracao) { this.duracao = duracao; }

        public String getTipoOracao() { return tipoOracao; }
        public void setTipoOracao(String tipoOracao) { this.tipoOracao = tipoOracao; }
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
    }

    public void setProcessInfo(String processId, String tema, String estiloOracao, String duracao, String tipoOracao) {
        ProcessInfo info = processInfos.get(processId);
        if (info != null) {
            info.setTema(tema);
            info.setEstiloOracao(estiloOracao);
            info.setDuracao(duracao);
            info.setTipoOracao(tipoOracao);
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

    public void updateStatus(String processId, String currentStage, int progressPercentage) {
        if (processes.containsKey(processId)) {
            ProcessStatus status = processes.get(processId);
            status.setCurrentStage(currentStage);
            status.setProgressPercentage(progressPercentage);
            status.setLastUpdated(LocalDateTime.now());
        }
    }

    public void storeTitles(String processId, List<String> titles) {
        processTitles.put(processId, titles);
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
        }
    }

    public ProcessStatus getStatus(String processId) {
        return processes.get(processId);
    }

    public String getResult(String processId) {
        return processResults.get(processId);
    }
}
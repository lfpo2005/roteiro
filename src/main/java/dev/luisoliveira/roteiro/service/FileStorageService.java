package dev.luisoliveira.roteiro.service;

import org.springframework.stereotype.Service;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import java.util.List;

import jakarta.annotation.PostConstruct;

/**
 * Serviço para armazenamento de conteúdo (versão com MongoDB)
 */
@Service
@Slf4j
@RequiredArgsConstructor
public class FileStorageService {

    private final MongoStorageService mongoStorageService;
    private final SrtConverterService srtConverterService;

    /**
     * Inicializa o serviço
     * Método mantido para compatibilidade
     */
    @PostConstruct
    public void initialize() {
        log.info("Inicializando FileStorageService com MongoDB");
    }

    /**
     * Salva o conteúdo da oração formatado
     * 
     * @param processId        ID do processo
     * @param formattedContent Conteúdo formatado
     * @return ID do conteúdo salvo
     */
    public String saveOracaoFile(String processId, String formattedContent) {
        log.info("Salvando conteúdo da oração para processo: {}", processId);
        String filename = "oracao_" + processId + ".txt";
        return mongoStorageService.saveTextFile(filename, formattedContent);
    }

    /**
     * Salva o conteúdo da oração com título, conteúdo completo, versão curta e
     * descrição
     * 
     * @param processId     ID do processo
     * @param titulo        Título da oração
     * @param oracaoContent Conteúdo completo da oração
     * @param shortContent  Conteúdo da versão curta
     * @param description   Descrição
     * @param allTitles     Lista de todos os títulos sugeridos
     * @return ID do conteúdo salvo
     */
    public String saveOracaoFile(String processId, String titulo, String oracaoContent,
            String shortContent, String description, List<String> allTitles) {
        log.info("Salvando conteúdo completo da oração para processo: {}", processId);

        StringBuilder content = new StringBuilder();
        content.append("# ").append(titulo).append("\n\n");
        content.append("## Oração Completa\n\n").append(oracaoContent).append("\n\n");

        if (shortContent != null && !shortContent.isEmpty()) {
            content.append("## Versão Curta\n\n").append(shortContent).append("\n\n");
        }

        if (description != null && !description.isEmpty()) {
            content.append("## Descrição\n\n").append(description).append("\n\n");
        }

        String filename = "oracao_completa_" + processId + ".txt";
        return mongoStorageService.saveTextFile(filename, content.toString());
    }

    /**
     * Salva o conteúdo da oração com título, conteúdo completo, versão curta,
     * descrição e IDs de áudio
     * 
     * @param processId     ID do processo
     * @param titulo        Título da oração
     * @param oracaoContent Conteúdo completo da oração
     * @param shortContent  Conteúdo da versão curta
     * @param description   Descrição
     * @param allTitles     Lista de todos os títulos sugeridos
     * @param oracaoAudioId ID do áudio da oração completa
     * @param shortAudioId  ID do áudio da versão curta
     * @return ID do conteúdo salvo
     */
    public String saveOracaoFile(String processId, String titulo, String oracaoContent,
            String shortContent, String description, List<String> allTitles,
            String oracaoAudioId, String shortAudioId) {
        log.info("Salvando conteúdo completo da oração com IDs de áudio para processo: {}", processId);

        StringBuilder content = new StringBuilder();
        content.append("# ").append(titulo).append("\n\n");
        content.append("## Oração Completa\n\n").append(oracaoContent).append("\n\n");

        if (oracaoAudioId != null && !oracaoAudioId.isEmpty()) {
            content.append("### Áudio da Oração Completa\n\n");
            content.append("ID: ").append(oracaoAudioId).append("\n\n");
        }

        if (shortContent != null && !shortContent.isEmpty()) {
            content.append("## Versão Curta\n\n").append(shortContent).append("\n\n");

            if (shortAudioId != null && !shortAudioId.isEmpty()) {
                content.append("### Áudio da Versão Curta\n\n");
                content.append("ID: ").append(shortAudioId).append("\n\n");
            }
        }

        if (description != null && !description.isEmpty()) {
            content.append("## Descrição\n\n").append(description).append("\n\n");
        }

        String filename = "oracao_completa_" + processId + ".txt";
        return mongoStorageService.saveTextFile(filename, content.toString());
    }

    /**
     * Salva áudio
     * 
     * @param audioData Dados do áudio
     * @return ID do áudio salvo
     */
    public String saveAudio(byte[] audioData) {
        log.info("Salvando áudio (tamanho: {} bytes)", audioData.length);
        String filename = "audio_" + System.currentTimeMillis() + ".mp3";
        return mongoStorageService.saveBinaryFile(filename, audioData, "audio/mpeg");
    }

    /**
     * Salva áudio com nome específico
     * 
     * @param processId ID do processo
     * @param audioName Nome do áudio
     * @param audioData Dados do áudio
     * @return ID do áudio salvo
     */
    public String saveAudio(String processId, String audioName, byte[] audioData) {
        log.info("Salvando áudio {} para processo {} (tamanho: {} bytes)", audioName, processId, audioData.length);
        String filename = audioName + "_" + processId + ".mp3";
        return mongoStorageService.saveBinaryFile(filename, audioData, "audio/mpeg");
    }

    /**
     * Obtém o conteúdo de um arquivo
     * 
     * @param contentId ID do conteúdo
     * @return Conteúdo do arquivo
     */
    public String getContent(String contentId) {
        log.info("Obtendo conteúdo com ID: {}", contentId);
        try {
            return mongoStorageService.getTextFile(contentId);
        } catch (Exception e) {
            log.error("Erro ao obter conteúdo: {}", e.getMessage(), e);
            return null;
        }
    }

    /**
     * Obtém os dados de um áudio
     * 
     * @param audioId ID do áudio
     * @return Dados do áudio
     */
    public byte[] getAudio(String audioId) {
        log.info("Obtendo áudio com ID: {}", audioId);
        try {
            return mongoStorageService.getBinaryFile(audioId);
        } catch (Exception e) {
            log.error("Erro ao obter áudio: {}", e.getMessage(), e);
            return null;
        }
    }

    /**
     * Obtém o ID do conteúdo para um processo
     * 
     * @param processId ID do processo
     * @return ID do conteúdo
     */
    public String getContentIdForProcess(String processId) {
        // Implementação simplificada - em uma versão real, isso seria buscado no banco
        // de dados
        return null;
    }

    /**
     * Atualiza o caminho de saída
     * Método mantido para compatibilidade
     * 
     * @param newOutputPath Novo caminho de saída
     */
    public void updateOutputPath(String newOutputPath) {
        log.info("Método updateOutputPath chamado, mas não tem efeito na versão MongoDB");
    }

    /**
     * Verifica se um conteúdo existe
     * 
     * @param contentId ID do conteúdo
     * @return true se o conteúdo existe
     */
    public boolean contentExists(String contentId) {
        log.info("Verificando se conteúdo existe: {}", contentId);
        try {
            return mongoStorageService.getTextFile(contentId) != null;
        } catch (Exception e) {
            log.error("Erro ao verificar existência de conteúdo: {}", e.getMessage(), e);
            return false;
        }
    }

    /**
     * Verifica se um áudio existe
     * 
     * @param audioId ID do áudio
     * @return true se o áudio existe
     */
    public boolean audioExists(String audioId) {
        log.info("Verificando se áudio existe: {}", audioId);
        try {
            return mongoStorageService.getBinaryFile(audioId) != null;
        } catch (Exception e) {
            log.error("Erro ao verificar existência de áudio: {}", e.getMessage(), e);
            return false;
        }
    }

    /**
     * Remove um conteúdo
     * 
     * @param contentId ID do conteúdo
     */
    public void removeContent(String contentId) {
        log.info("Removendo conteúdo: {}", contentId);
        try {
            mongoStorageService.deleteFile(contentId);
        } catch (Exception e) {
            log.error("Erro ao remover conteúdo: {}", e.getMessage(), e);
        }
    }

    /**
     * Remove um áudio
     * 
     * @param audioId ID do áudio
     */
    public void removeAudio(String audioId) {
        log.info("Removendo áudio: {}", audioId);
        try {
            mongoStorageService.deleteFile(audioId);
        } catch (Exception e) {
            log.error("Erro ao remover áudio: {}", e.getMessage(), e);
        }
    }

    /**
     * Obtém os dados binários de um arquivo
     * 
     * @param fileId ID do arquivo
     * @return Dados binários do arquivo
     */
    public byte[] getBinaryFile(String fileId) {
        log.info("Obtendo arquivo binário com ID: {}", fileId);
        try {
            return mongoStorageService.getBinaryFile(fileId);
        } catch (Exception e) {
            log.error("Erro ao obter arquivo binário: {}", e.getMessage(), e);
            return null;
        }
    }
}
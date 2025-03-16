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
     * @return ID único para o conteúdo salvo
     */
    public String saveOracaoFile(String processId, String formattedContent) {
        log.info("Salvando conteúdo formatado para processo: {}", processId);

        // Criar ou atualizar documento no MongoDB
        dev.luisoliveira.roteiro.document.PrayerContent prayerContent = mongoStorageService.getPrayerContent(processId);

        if (prayerContent == null) {
            // Se não existe, criar um novo com título temporário
            prayerContent = mongoStorageService.createPrayerContent(processId, "Oração " + processId);
        }

        // Atualizar conteúdo
        mongoStorageService.updateContent(processId, formattedContent, null, null);

        return processId; // Retorna o ID do processo como identificador de conteúdo
    }

    /**
     * Salva conteúdo de oração
     * 
     * @param processId     ID do processo
     * @param titulo        Título da oração
     * @param oracaoContent Conteúdo completo
     * @param shortContent  Versão curta
     * @param description   Descrição
     * @param allTitles     Títulos alternativos
     * @return ID do conteúdo
     */
    public String saveOracaoFile(String processId, String titulo, String oracaoContent,
            String shortContent, String description, List<String> allTitles) {
        return saveOracaoFile(processId, titulo, oracaoContent, shortContent, description, allTitles, null, null);
    }

    /**
     * Salva conteúdo de oração com referências a áudios
     * 
     * @param processId     ID do processo
     * @param titulo        Título da oração
     * @param oracaoContent Conteúdo completo
     * @param shortContent  Versão curta
     * @param description   Descrição
     * @param allTitles     Títulos alternativos
     * @param oracaoAudioId ID do áudio completo
     * @param shortAudioId  ID do áudio da versão curta
     * @return ID do conteúdo
     */
    public String saveOracaoFile(String processId, String titulo, String oracaoContent,
            String shortContent, String description, List<String> allTitles,
            String oracaoAudioId, String shortAudioId) {

        log.info("Salvando conteúdo para processo: {}", processId);

        // Criar ou obter documento existente
        dev.luisoliveira.roteiro.document.PrayerContent prayerContent = mongoStorageService.getPrayerContent(processId);

        if (prayerContent == null) {
            prayerContent = mongoStorageService.createPrayerContent(processId, titulo);
        } else if (titulo != null && !titulo.isEmpty()) {
            prayerContent.setTitle(titulo);
        }

        // Atualizar conteúdo
        mongoStorageService.updateContent(processId, oracaoContent, shortContent, description);

        // Atualizar metadados
        mongoStorageService.updateMetadata(processId, null, null, null, null, allTitles);

        return processId; // Retorna o ID do processo como identificador de conteúdo
    }

    /**
     * Salva áudio no GridFS
     * 
     * @param audioData Dados do áudio
     * @return ID do áudio
     */
    public String saveAudio(byte[] audioData) {
        if (audioData == null || audioData.length == 0) {
            return null;
        }

        // Gerar um ID temporário para o processo
        String tempProcessId = "temp_" + java.util.UUID.randomUUID().toString();
        return mongoStorageService.saveAudio(tempProcessId, audioData, false);
    }

    /**
     * Salva áudio associado a um processo
     * 
     * @param processId ID do processo
     * @param audioName Nome do áudio
     * @param audioData Dados do áudio
     * @return ID do áudio
     */
    public String saveAudio(String processId, String audioName, byte[] audioData) {
        if (audioData == null || audioData.length == 0) {
            log.warn("Tentativa de salvar áudio vazio para processo: {}", processId);
            return null;
        }

        boolean isShortVersion = audioName != null && audioName.contains("short");
        return mongoStorageService.saveAudio(processId, audioData, isShortVersion);
    }

    /**
     * Recupera conteúdo pelo ID
     * 
     * @param contentId ID do conteúdo (processo)
     * @return Conteúdo formatado
     */
    public String getContent(String contentId) {
        dev.luisoliveira.roteiro.document.PrayerContent prayerContent = mongoStorageService.getPrayerContent(contentId);

        if (prayerContent != null && prayerContent.getContent() != null) {
            return prayerContent.getContent().getFullText();
        }

        return null;
    }

    /**
     * Recupera áudio pelo ID
     * 
     * @param audioId ID do áudio
     * @return Dados do áudio
     */
    public byte[] getAudio(String audioId) {
        return mongoStorageService.getAudio(audioId);
    }

    /**
     * Obtém ID de conteúdo para um processo
     * 
     * @param processId ID do processo
     * @return ID do conteúdo (mesmo que processId)
     */
    public String getContentIdForProcess(String processId) {
        // No MongoDB, o ID do processo é usado como identificador de conteúdo
        return processId;
    }

    /**
     * Método mantido para compatibilidade
     */
    public void updateOutputPath(String newOutputPath) {
        log.info("Método updateOutputPath chamado mas não tem efeito na versão MongoDB");
    }

    /**
     * Verifica se conteúdo existe
     * 
     * @param contentId ID do conteúdo (processo)
     * @return true se existir
     */
    public boolean contentExists(String contentId) {
        return mongoStorageService.getPrayerContent(contentId) != null;
    }

    /**
     * Verifica se áudio existe
     * 
     * @param audioId ID do áudio
     * @return true se existir
     */
    public boolean audioExists(String audioId) {
        try {
            return mongoStorageService.getAudio(audioId) != null;
        } catch (Exception e) {
            return false;
        }
    }

    /**
     * Remove conteúdo
     * 
     * @param contentId ID do conteúdo (processo)
     */
    public void removeContent(String contentId) {
        // Implementação pendente
        log.info("Requisição para remover conteúdo ignorada: compatibilidade mantida");
    }

    /**
     * Remove áudio
     * 
     * @param audioId ID do áudio
     */
    public void removeAudio(String audioId) {
        // Implementação pendente
        log.info("Requisição para remover áudio ignorada: compatibilidade mantida");
    }
}
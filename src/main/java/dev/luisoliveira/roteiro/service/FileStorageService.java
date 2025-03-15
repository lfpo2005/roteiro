package dev.luisoliveira.roteiro.service;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;

import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Service
@Slf4j
@RequiredArgsConstructor
public class FileStorageService {

    private final SrtConverterService srtConverterService;

    // Mapa para acompanhar os diretórios de cada processo
    private final Map<String, String> processDirectories = new ConcurrentHashMap<>();

    @Value("${file.output.path:./gerados}")
    private String outputPath;

    public void initialize() {
        try {
            Path path = Paths.get(outputPath);
            if (!Files.exists(path)) {
                Files.createDirectories(path);
                log.info("Diretório de saída criado: {}", outputPath);
            }
        } catch (IOException e) {
            log.error("Não foi possível criar o diretório para armazenar os arquivos gerados", e);
            throw new RuntimeException("Não foi possível criar o diretório para armazenar os arquivos gerados", e);
        }
    }

//    // Método simplificado sem referência a imagem
//    public String saveOracaoFile(String processId, String titulo, String oracaoContent,
//                                 String shortContent, String description, List<String> allTitles) {
//        return saveOracaoFile(processId, titulo, oracaoContent, shortContent, description, allTitles, null, null);
//    }

    // Método simplificado sem referência a arquivos de áudio
    public String saveOracaoFile(String processId, String titulo, String oracaoContent,
                                 String shortContent, String description, List<String> allTitles) {
        return saveOracaoFile(processId, titulo, oracaoContent, shortContent, description, allTitles, null, null);
    }

    public String saveOracaoFile(String processId, String titulo, String oracaoContent,
                                 String shortContent, String description, List<String> allTitles,
                                 String oracaoAudioPath, String shortAudioPath) {
        try {
            // Criar nome de arquivo seguro baseado no título
            String safeTitle = titulo.replaceAll("[^a-zA-Z0-9]", "_")
                    .replaceAll("_+", "_");

            // Criar a pasta principal com o título
            Path mainDir = Paths.get(outputPath, safeTitle);
            if (!Files.exists(mainDir)) {
                Files.createDirectories(mainDir);
            }

            // Criar subpastas para texto e áudio
            Path textoDir = mainDir.resolve("texto");
            Path audioDir = mainDir.resolve("audio");

            if (!Files.exists(textoDir)) {
                Files.createDirectories(textoDir);
            }

            if (!Files.exists(audioDir)) {
                Files.createDirectories(audioDir);
            }

            log.info("Estrutura de diretórios criada: {}", mainDir);

            // Armazenar o diretório principal deste processo para uso posterior
            processDirectories.put(processId, mainDir.toString());

            // Salvar arquivo de metadados em txt na pasta de texto
            String txtFileName = safeTitle + "_meta.txt";
            Path txtFilePath = textoDir.resolve(txtFileName);

            // Criar conteúdo do arquivo de metadados
            StringBuilder txtContent = new StringBuilder();
            txtContent.append("**Título: ").append(titulo).append("**\n\n");

            // Adicionar outros títulos gerados para referência
            if (allTitles != null && !allTitles.isEmpty()) {
                txtContent.append("**Outros títulos possíveis:**\n");
                for (String altTitle : allTitles) {
                    if (!altTitle.equals(titulo)) { // Não repetir o título principal
                        txtContent.append("- ").append(altTitle).append("\n");
                    }
                }
                txtContent.append("\n");
            }

            txtContent.append("**Oração Completa:**\n\n");
            txtContent.append(oracaoContent).append("\n\n");

            txtContent.append("**Short (30-60 segundos)**\n\n");
            txtContent.append(shortContent).append("\n\n");

            txtContent.append("**Descrição para YouTube e TikTok**\n\n");
            txtContent.append(description);

            // Adicionar informação sobre os áudios gerados, se disponíveis
            if (oracaoAudioPath != null) {
                txtContent.append("\n\n**Áudio da Oração Completa**\n\n");
                txtContent.append("Um áudio da oração completa foi gerado em: ./audio/").append(Paths.get(oracaoAudioPath).getFileName());

                // Mover/copiar o áudio para a pasta de áudio, se existir
                try {
                    Path sourceAudioPath = Paths.get(oracaoAudioPath);
                    if (Files.exists(sourceAudioPath)) {
                        Path targetAudioPath = audioDir.resolve(sourceAudioPath.getFileName());
                        Files.copy(sourceAudioPath, targetAudioPath);
                        log.info("Áudio da oração completa copiado para: {}", targetAudioPath);
                    }
                } catch (IOException e) {
                    log.warn("Não foi possível copiar o áudio da oração completa: {}", e.getMessage());
                }
            }

            if (shortAudioPath != null) {
                txtContent.append("\n\n**Áudio da Versão Curta**\n\n");
                txtContent.append("Um áudio da versão curta foi gerado em: ./audio/").append(Paths.get(shortAudioPath).getFileName());

                // Mover/copiar o áudio da versão curta para a pasta de áudio, se existir
                try {
                    Path sourceShortAudioPath = Paths.get(shortAudioPath);
                    if (Files.exists(sourceShortAudioPath)) {
                        Path targetShortAudioPath = audioDir.resolve(sourceShortAudioPath.getFileName());
                        Files.copy(sourceShortAudioPath, targetShortAudioPath);
                        log.info("Áudio da versão curta copiado para: {}", targetShortAudioPath);
                    }
                } catch (IOException e) {
                    log.warn("Não foi possível copiar o áudio da versão curta: {}", e.getMessage());
                }
            }

            // Escrever no arquivo de metadados
            try (FileWriter writer = new FileWriter(txtFilePath.toFile())) {
                writer.write(txtContent.toString());
            }
            log.info("Arquivo de metadados salvo com sucesso: {}", txtFilePath);

            // Converter oração para SRT e salvar na pasta de texto
            String srtFileName = safeTitle + ".srt";
            Path srtFilePath = textoDir.resolve(srtFileName);
            String srtContent = srtConverterService.converterParaSRT(oracaoContent);

            try (FileWriter writer = new FileWriter(srtFilePath.toFile())) {
                writer.write(srtContent);
            }
            log.info("Arquivo SRT salvo com sucesso: {}", srtFilePath);

            // Converter versão curta para SRT e salvar na pasta de texto
            String shortSrtFileName = safeTitle + "_short.srt";
            Path shortSrtFilePath = textoDir.resolve(shortSrtFileName);
            String shortSrtContent = srtConverterService.converterParaSRT(shortContent);

            try (FileWriter writer = new FileWriter(shortSrtFilePath.toFile())) {
                writer.write(shortSrtContent);
            }
            log.info("Arquivo SRT da versão curta salvo com sucesso: {}", shortSrtFilePath);

            // Retornar o caminho do diretório principal que contém todas as subpastas
            return mainDir.toString();
        } catch (IOException e) {
            log.error("Falha ao salvar arquivos", e);
            throw new RuntimeException("Falha ao salvar arquivos", e);
        }
    }

    /**
     * Obtém o diretório principal para um processo específico
     * @param processId ID do processo
     * @return Caminho do diretório principal ou null se não encontrado
     */
    public String getProcessDirectory(String processId) {
        return processDirectories.get(processId);
    }

    /**
     * Salva arquivos de áudio para um processo em um local específico
     *
     * @param processId ID do processo
     * @param titulo Título da oração
     * @param fullAudioContent Conteúdo do áudio completo
     * @param shortAudioContent Conteúdo do áudio curto (opcional)
     * @return Array com os caminhos dos arquivos [fullAudioPath, shortAudioPath]
     */
    /**
     * Salva arquivos de áudio com tratamento de caminho aprimorado
     */
    public String[] saveAudioFiles(String processId, String titulo, byte[] fullAudioData, byte[] shortAudioData) {
        log.info("Salvando arquivos de áudio para processId={}, título='{}'", processId, titulo);

        try {
            // Criar nome de arquivo ainda mais seguro, removendo acentos e caracteres especiais
            String safeTitle = createSafeFileName(titulo);
            log.debug("Nome de arquivo seguro gerado para áudio: '{}'", safeTitle);

            // Normalizar o caminho base de saída
            Path normalizedOutputPath = Paths.get(outputPath).normalize().toAbsolutePath();
            log.debug("Caminho base normalizado: {}", normalizedOutputPath);

            // Usar apenas a estrutura por processId para garantir consistência
            Path processDir = normalizedOutputPath.resolve(processId);
            if (!Files.exists(processDir)) {
                Files.createDirectories(processDir);
                log.debug("Diretório do processo criado: {}", processDir);
            }

            Path audioDir = processDir.resolve("audio");
            if (!Files.exists(audioDir)) {
                Files.createDirectories(audioDir);
                log.info("Diretório de áudio criado: {}", audioDir);
            }

            // Salvar áudio completo com nome simplificado
            String fullAudioName = safeTitle + "_full.mp3";
            Path fullAudioPath = audioDir.resolve(fullAudioName);

            // Verificar se o diretório pai existe
            if (!Files.exists(fullAudioPath.getParent())) {
                Files.createDirectories(fullAudioPath.getParent());
            }

            // Garantir que o arquivo existe (ou criar um vazio)
            if (!Files.exists(fullAudioPath)) {
                Files.createFile(fullAudioPath);
            }

            // Escrever dados
            Files.write(fullAudioPath, fullAudioData);
            log.info("Áudio completo salvo em: {}", fullAudioPath);

            // Salvar áudio da versão curta, se existir
            String shortAudioPathStr = null;
            if (shortAudioData != null) {
                String shortAudioName = safeTitle + "_short.mp3";
                Path shortAudioPath = audioDir.resolve(shortAudioName);

                // Verificar se o diretório pai existe
                if (!Files.exists(shortAudioPath.getParent())) {
                    Files.createDirectories(shortAudioPath.getParent());
                }

                // Garantir que o arquivo existe (ou criar um vazio)
                if (!Files.exists(shortAudioPath)) {
                    Files.createFile(shortAudioPath);
                }

                Files.write(shortAudioPath, shortAudioData);
                shortAudioPathStr = shortAudioPath.toString();
                log.info("Áudio da versão curta salvo em: {}", shortAudioPath);
            }

            // Retornar caminhos relativos ao diretório de trabalho
            Path workingDir = Paths.get("").toAbsolutePath();
            String relativeFullPath = workingDir.relativize(fullAudioPath).toString();
            String relativeShortPath = shortAudioPathStr != null ?
                    workingDir.relativize(Paths.get(shortAudioPathStr)).toString() :
                    null;

            log.debug("Caminho relativo do áudio completo: {}", relativeFullPath);
            if (relativeShortPath != null) {
                log.debug("Caminho relativo do áudio curto: {}", relativeShortPath);
            }

            // Garantir que o caminho usa a separação de diretórios adequada ao sistema
            relativeFullPath = relativeFullPath.replace('\\', '/');
            if (relativeShortPath != null) {
                relativeShortPath = relativeShortPath.replace('\\', '/');
            }

            return new String[] { relativeFullPath, relativeShortPath };
        } catch (IOException e) {
            log.error("Erro ao salvar arquivos de áudio para processId={}: {}", processId, e.getMessage(), e);
            throw new RuntimeException("Erro ao salvar arquivos de áudio: " + e.getMessage(), e);
        }
    }

    /**
     * Versão melhorada do método para criar nomes de arquivos seguros,
     * removendo também acentos e caracteres especiais
     */
    private String createSafeFileName(String title) {
        if (title == null) {
            return "unnamed";
        }

        // Remover acentos e normalizar
        String normalized = java.text.Normalizer.normalize(title, java.text.Normalizer.Form.NFD)
                .replaceAll("\\p{InCombiningDiacriticalMarks}+", "");

        // Remover emojis e caracteres especiais - mais restritivo, apenas letras, números e alguns símbolos básicos
        String safeTitle = normalized.replaceAll("[^a-zA-Z0-9 _\\-.]", "");

        // Remover palavras-chave como hashtags e menções
        safeTitle = safeTitle.replaceAll("#\\w+", "").replaceAll("@\\w+", "");

        // Converter espaços em underscore e remover underscores duplicados
        safeTitle = safeTitle.trim().replaceAll("\\s+", "_").replaceAll("_+", "_");

        // Truncar se for muito longo
        if (safeTitle.length() > 50) { // Reduzido para garantir compatibilidade
            safeTitle = safeTitle.substring(0, 50);
        }

        // Remover underscores no início e fim
        safeTitle = safeTitle.replaceAll("^_+|_+$", "");

        // Verificar se não ficou vazio
        if (StringUtils.isBlank(safeTitle)) {
            safeTitle = "unnamed";
        }

        return safeTitle;
    }
}
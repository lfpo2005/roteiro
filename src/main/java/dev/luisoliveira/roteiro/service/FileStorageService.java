package dev.luisoliveira.roteiro.service;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;

@Service
@Slf4j
@RequiredArgsConstructor
public class FileStorageService {

    private final SrtConverterService srtConverterService;

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

    // Método simplificado sem referência a imagem
    public String saveOracaoFile(String processId, String titulo, String oracaoContent,
                                 String shortContent, String description, List<String> allTitles) {
        return saveOracaoFile(processId, titulo, oracaoContent, shortContent, description, allTitles, null, null);
    }

    // Método legado mantido para compatibilidade, mas ignorando o parâmetro imagePath
    public String saveOracaoFile(String processId, String titulo, String oracaoContent,
                                 String shortContent, String description, List<String> allTitles,
                                 String imagePath) {
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
}
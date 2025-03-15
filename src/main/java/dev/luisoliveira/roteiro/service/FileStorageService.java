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

    public String saveOracaoFile(String processId, String titulo, String oracaoContent,
                                 String shortContent, String description, List<String> allTitles,
                                 String imagePath) {
        return saveOracaoFile(processId, titulo, oracaoContent, shortContent, description, allTitles, imagePath, null, null);
    }

    public String saveOracaoFile(String processId, String titulo, String oracaoContent,
                                 String shortContent, String description, List<String> allTitles,
                                 String imagePath, String oracaoAudioPath, String shortAudioPath) {
        try {
            // Criar nome de arquivo seguro baseado no título
            String safeTitle = titulo.replaceAll("[^a-zA-Z0-9]", "_")
                    .replaceAll("_+", "_");

            // Criar o diretório do processo se não existir
            Path processDir = Paths.get(outputPath, titulo); // alterado para salvar em diretório com o título
            if (!Files.exists(processDir)) {
                Files.createDirectories(processDir);
            }

            // Salvar arquivo de metadados em txt
            String txtFileName = safeTitle + "_meta.txt";
            Path txtFilePath = processDir.resolve(txtFileName);

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

            // Adicionar informação sobre a imagem gerada, se disponível
            if (imagePath != null) {
                txtContent.append("\n\n**Imagem de Capa**\n\n");
                txtContent.append("Uma imagem de capa foi gerada em: ").append(imagePath);
            }

            // Adicionar informação sobre os áudios gerados, se disponíveis
            if (oracaoAudioPath != null) {
                txtContent.append("\n\n**Áudio da Oração Completa**\n\n");
                txtContent.append("Um áudio da oração completa foi gerado em: ").append(oracaoAudioPath);
            }

            if (shortAudioPath != null) {
                txtContent.append("\n\n**Áudio da Versão Curta**\n\n");
                txtContent.append("Um áudio da versão curta foi gerado em: ").append(shortAudioPath);
            }

            // Escrever no arquivo de metadados
            try (FileWriter writer = new FileWriter(txtFilePath.toFile())) {
                writer.write(txtContent.toString());
            }
            log.info("Arquivo de metadados salvo com sucesso: {}", txtFilePath);

            // Converter oração para SRT e salvar
            String srtFileName = safeTitle + ".srt";
            Path srtFilePath = processDir.resolve(srtFileName);
            String srtContent = srtConverterService.converterParaSRT(oracaoContent);

            try (FileWriter writer = new FileWriter(srtFilePath.toFile())) {
                writer.write(srtContent);
            }
            log.info("Arquivo SRT salvo com sucesso: {}", srtFilePath);

            // Converter versão curta para SRT e salvar
            String shortSrtFileName = safeTitle + "_short.srt";
            Path shortSrtFilePath = processDir.resolve(shortSrtFileName);
            String shortSrtContent = srtConverterService.converterParaSRT(shortContent);

            try (FileWriter writer = new FileWriter(shortSrtFilePath.toFile())) {
                writer.write(shortSrtContent);
            }
            log.info("Arquivo SRT da versão curta salvo com sucesso: {}", shortSrtFilePath);

            // Retornar o caminho do diretório que contém todos os arquivos
            return processDir.toString();
        } catch (IOException e) {
            log.error("Falha ao salvar arquivos", e);
            throw new RuntimeException("Falha ao salvar arquivos", e);
        }
    }
}
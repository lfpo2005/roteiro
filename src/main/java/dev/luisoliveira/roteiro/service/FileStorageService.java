package dev.luisoliveira.roteiro.service;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

@Service
public class FileStorageService {

    @Value("${file.output.path:./gerados}")
    private String outputPath;

    public void initialize() {
        try {
            Path path = Paths.get(outputPath);
            if (!Files.exists(path)) {
                Files.createDirectories(path);
            }
        } catch (IOException e) {
            throw new RuntimeException("Não foi possível criar o diretório para armazenar os arquivos gerados", e);
        }
    }

    public String saveOracaoFile(String processId, String titulo, String oracaoContent,
                                 String shortContent, String description) {
        try {
            // Criar nome de arquivo seguro baseado no título
            String safeTitle = titulo.replaceAll("[^a-zA-Z0-9]", "_")
                    .replaceAll("_+", "_");

            // Criar o diretório do processo se não existir
            Path processDir = Paths.get(outputPath, processId);
            if (!Files.exists(processDir)) {
                Files.createDirectories(processDir);
            }

            // Caminho do arquivo
            String fileName = safeTitle + ".docx";
            Path filePath = processDir.resolve(fileName);

            // Criar conteúdo do arquivo
            StringBuilder fileContent = new StringBuilder();
            fileContent.append("**Título: ").append(titulo).append("**\n\n");
            fileContent.append(oracaoContent).append("\n\n");
            fileContent.append("**Short (30-60 segundos)**\n\n");
            fileContent.append(shortContent).append("\n\n");
            fileContent.append("**Descrição para YouTube e TikTok**\n\n");
            fileContent.append(description);

            // Escrever no arquivo
            try (FileWriter writer = new FileWriter(filePath.toFile())) {
                writer.write(fileContent.toString());
            }

            return filePath.toString();
        } catch (IOException e) {
            throw new RuntimeException("Falha ao salvar o arquivo de oração", e);
        }
    }
}

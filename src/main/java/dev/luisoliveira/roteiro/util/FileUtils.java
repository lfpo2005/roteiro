package dev.luisoliveira.roteiro.util;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

public class FileUtils {

    /**
     * Cria um diretório se não existir
     * @param directoryPath caminho do diretório
     * @return Path do diretório criado
     * @throws IOException se houver erro ao criar o diretório
     */
    public static Path createDirectoryIfNotExists(String directoryPath) throws IOException {
        Path path = Paths.get(directoryPath);
        if (!Files.exists(path)) {
            Files.createDirectories(path);
        }
        return path;
    }

    /**
     * Cria um nome de arquivo seguro a partir de um título
     * @param title título original
     * @return nome de arquivo seguro
     */
    public static String createSafeFileName(String title) {
        return title.replaceAll("[^a-zA-Z0-9]", "_")
                .replaceAll("_+", "_");
    }

    /**
     * Verifica se um arquivo existe
     * @param filePath caminho do arquivo
     * @return true se o arquivo existir
     */
    public static boolean fileExists(String filePath) {
        return Files.exists(Paths.get(filePath));
    }

    /**
     * Lê o conteúdo de um arquivo como string
     * @param filePath caminho do arquivo
     * @return conteúdo do arquivo
     * @throws IOException se houver erro ao ler o arquivo
     */
    public static String readFileContent(String filePath) throws IOException {
        return new String(Files.readAllBytes(Paths.get(filePath)));
    }

    /**
     * Salva conteúdo em um arquivo
     * @param filePath caminho do arquivo
     * @param content conteúdo a ser salvo
     * @throws IOException se houver erro ao salvar o arquivo
     */
    public static void saveContent(String filePath, String content) throws IOException {
        Files.write(Paths.get(filePath), content.getBytes());
    }
}
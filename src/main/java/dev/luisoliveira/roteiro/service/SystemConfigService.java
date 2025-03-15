package dev.luisoliveira.roteiro.service;

import dev.luisoliveira.roteiro.dto.SystemConfigDto;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import jakarta.annotation.PostConstruct;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;

/**
 * Serviço para gerenciar configurações do sistema com persistência em arquivo
 */
@Service
@Slf4j
@RequiredArgsConstructor
public class SystemConfigService {

    private final FileStorageService fileStorageService;

    private static final String CONFIG_FILE_PATH = "./config/system.properties";
    private static final String OUTPUT_PATH_KEY = "file.output.path";
    private static final String CREATE_DIR_KEY = "file.create.directory";

    @Value("${file.output.path:./gerados}")
    private String defaultOutputPath;

    @PostConstruct
    public void init() {
        // Garantir que o diretório de configuração existe
        try {
            Path configDir = Paths.get(CONFIG_FILE_PATH).getParent();
            if (configDir != null && !Files.exists(configDir)) {
                Files.createDirectories(configDir);
                log.info("Diretório de configuração criado: {}", configDir);
            }

            // Carregar configurações iniciais ou criar arquivo de configuração se não
            // existir
            Properties props = loadProperties();
            if (!props.containsKey(OUTPUT_PATH_KEY)) {
                props.setProperty(OUTPUT_PATH_KEY, defaultOutputPath);
                props.setProperty(CREATE_DIR_KEY, "true");
                saveProperties(props);
                log.info("Arquivo de configuração inicializado com valores padrão");
            }
        } catch (IOException e) {
            log.error("Erro ao inicializar diretório de configuração", e);
        }
    }

    /**
     * Obtém as configurações atuais do sistema
     *
     * @return SystemConfigDto com as configurações atuais
     */
    public SystemConfigDto getSystemConfig() {
        Properties props = loadProperties();
        String outputPath = props.getProperty(OUTPUT_PATH_KEY, defaultOutputPath);
        boolean createDirectoryIfNotExists = Boolean.parseBoolean(
                props.getProperty(CREATE_DIR_KEY, "true"));

        return SystemConfigDto.builder()
                .outputPath(outputPath)
                .createDirectoryIfNotExists(createDirectoryIfNotExists)
                .build();
    }

    /**
     * Atualiza o caminho de saída nas configurações do sistema
     *
     * @param newOutputPath Novo caminho de saída
     * @return SystemConfigDto com as configurações atualizadas
     */
    public SystemConfigDto updateOutputPath(String newOutputPath) {
        if (newOutputPath == null || newOutputPath.trim().isEmpty()) {
            log.warn("Tentativa de definir caminho de saída vazio ou nulo. Usando valor padrão.");
            newOutputPath = defaultOutputPath;
        } else {
            newOutputPath = newOutputPath.trim();
        }

        Properties props = loadProperties();
        props.setProperty(OUTPUT_PATH_KEY, newOutputPath);
        saveProperties(props);

        log.info("Caminho de saída atualizado para: {}", newOutputPath);

        // Criar diretório se necessário
        boolean createDir = Boolean.parseBoolean(props.getProperty(CREATE_DIR_KEY, "true"));
        if (createDir) {
            createDirectory(newOutputPath);
        }

        // Atualizar o FileStorageService com o novo caminho
        fileStorageService.updateOutputPath(newOutputPath);

        return getSystemConfig();
    }

    /**
     * Atualiza a configuração para criar diretório automaticamente
     *
     * @param createDirectory Se deve criar diretório automaticamente
     * @return SystemConfigDto com as configurações atualizadas
     */
    public SystemConfigDto updateCreateDirectory(boolean createDirectory) {
        Properties props = loadProperties();
        props.setProperty(CREATE_DIR_KEY, String.valueOf(createDirectory));
        saveProperties(props);

        log.info("Configuração para criar diretório automaticamente atualizada para: {}", createDirectory);

        // Se habilitado, cria o diretório imediatamente
        if (createDirectory) {
            String outputPath = props.getProperty(OUTPUT_PATH_KEY, defaultOutputPath);
            createDirectory(outputPath);
        }

        return getSystemConfig();
    }

    /**
     * Redefine as configurações para os valores padrão
     *
     * @return SystemConfigDto com as configurações redefinidas
     */
    public SystemConfigDto resetToDefaults() {
        Properties props = new Properties();
        props.setProperty(OUTPUT_PATH_KEY, defaultOutputPath);
        props.setProperty(CREATE_DIR_KEY, "true");
        saveProperties(props);

        log.info("Configurações redefinidas para os valores padrão");

        // Criar diretório padrão
        createDirectory(defaultOutputPath);

        // Atualizar o FileStorageService com o caminho padrão
        fileStorageService.updateOutputPath(defaultOutputPath);

        return getSystemConfig();
    }

    /**
     * Carrega as propriedades do arquivo de configuração
     *
     * @return Properties carregadas do arquivo
     */
    private Properties loadProperties() {
        Properties props = new Properties();
        File configFile = new File(CONFIG_FILE_PATH);

        if (configFile.exists()) {
            try (FileInputStream fis = new FileInputStream(configFile)) {
                props.load(fis);
                log.debug("Configurações carregadas de: {}", CONFIG_FILE_PATH);
            } catch (IOException e) {
                log.error("Erro ao carregar configurações de: {}", CONFIG_FILE_PATH, e);
            }
        } else {
            log.debug("Arquivo de configuração não encontrado, usando valores padrão");
        }

        return props;
    }

    /**
     * Salva as propriedades no arquivo de configuração
     *
     * @param props Properties a serem salvas
     */
    private void saveProperties(Properties props) {
        try (FileOutputStream fos = new FileOutputStream(CONFIG_FILE_PATH)) {
            props.store(fos, "System Configuration");
            log.debug("Configurações salvas em: {}", CONFIG_FILE_PATH);
        } catch (IOException e) {
            log.error("Erro ao salvar configurações em: {}", CONFIG_FILE_PATH, e);
        }
    }

    /**
     * Cria um diretório se não existir
     *
     * @param directoryPath Caminho do diretório a ser criado
     */
    public boolean createDirectory(String directoryPath) {
        if (directoryPath == null || directoryPath.isEmpty()) {
            throw new IllegalArgumentException("Caminho não pode ser vazio");
        }

        try {
            Path path = Paths.get(directoryPath);
            if (Files.exists(path)) {
                return Files.isDirectory(path); // Se já existir, verificar se é diretório
            }

            // Tentar criar o diretório e todos os diretórios pai necessários
            Files.createDirectories(path);
            log.info("Diretório criado: {}", path.toAbsolutePath());
            return true;
        } catch (IOException e) {
            log.error("Erro ao criar diretório: {}", directoryPath, e);
            return false;
        }
    }

    /**
     * Lista os diretórios em um caminho específico
     * 
     * @param basePath Caminho base para listar (ou null para raízes do sistema)
     * @return Mapa com informações dos diretórios
     */
    public Map<String, Object> listDirectories(String basePath) throws IOException {
        Map<String, Object> result = new HashMap<>();
        List<Map<String, Object>> directories = new ArrayList<>();

        if (basePath == null || basePath.isEmpty()) {
            // Listar unidades/raízes do sistema
            File[] roots = File.listRoots();
            for (File root : roots) {
                Map<String, Object> dir = new HashMap<>();
                dir.put("name", root.getAbsolutePath());
                dir.put("path", root.getAbsolutePath());
                dir.put("isRoot", true);
                directories.add(dir);
            }

            // Adicionar diretório home do usuário
            String userHome = System.getProperty("user.home");
            if (userHome != null && !userHome.isEmpty()) {
                File homeDir = new File(userHome);
                if (homeDir.exists() && homeDir.isDirectory()) {
                    Map<String, Object> dir = new HashMap<>();
                    dir.put("name", "Home (" + userHome + ")");
                    dir.put("path", userHome);
                    dir.put("isRoot", false);
                    directories.add(dir);
                }
            }
        } else {
            // Listar diretórios no caminho fornecido
            File baseDir = new File(basePath);
            if (!baseDir.exists() || !baseDir.isDirectory()) {
                throw new IOException("Caminho inválido ou não é um diretório: " + basePath);
            }

            // Verificar permissão de leitura
            if (!baseDir.canRead()) {
                throw new IOException("Sem permissão para ler o diretório: " + basePath);
            }

            // Adicionar referência para o diretório pai (../)
            if (baseDir.getParentFile() != null) {
                Map<String, Object> parentDir = new HashMap<>();
                parentDir.put("name", "../");
                parentDir.put("path", baseDir.getParentFile().getAbsolutePath());
                parentDir.put("isParent", true);
                directories.add(parentDir);
            }

            // Listar apenas diretórios (não arquivos)
            File[] files = baseDir.listFiles(File::isDirectory);
            if (files != null) {
                Arrays.sort(files); // Ordenar alfabeticamente

                for (File file : files) {
                    // Ignorar arquivos e diretórios ocultos
                    if (file.isHidden()) {
                        continue;
                    }

                    Map<String, Object> dir = new HashMap<>();
                    dir.put("name", file.getName());
                    dir.put("path", file.getAbsolutePath());
                    dir.put("canWrite", file.canWrite());
                    directories.add(dir);
                }
            }
        }

        result.put("directories", directories);
        result.put("currentPath", basePath != null ? basePath : "");

        // Se for um caminho específico, verificar se pode escrever
        if (basePath != null && !basePath.isEmpty()) {
            File baseDir = new File(basePath);
            result.put("canWrite", baseDir.exists() && baseDir.canWrite());
        }

        return result;
    }
}
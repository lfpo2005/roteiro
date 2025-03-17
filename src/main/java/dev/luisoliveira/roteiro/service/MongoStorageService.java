package dev.luisoliveira.roteiro.service;

import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.stereotype.Service;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import com.mongodb.client.gridfs.GridFSBucket;
import com.mongodb.client.gridfs.GridFSBuckets;
import com.mongodb.client.gridfs.model.GridFSFile;
import org.bson.types.ObjectId;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.data.mongodb.gridfs.GridFsResource;
import org.springframework.data.mongodb.gridfs.GridFsTemplate;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;

/**
 * Serviço para armazenamento de arquivos no MongoDB usando GridFS
 */
@Service
@Slf4j
@RequiredArgsConstructor
public class MongoStorageService {

    private final MongoTemplate mongoTemplate;
    private final GridFsTemplate gridFsTemplate;

    /**
     * Salva um arquivo de texto no MongoDB GridFS
     * 
     * @param filename Nome do arquivo
     * @param content  Conteúdo do arquivo
     * @return ID do arquivo salvo
     */
    public String saveTextFile(String filename, String content) {
        try {
            log.info("Salvando arquivo de texto no MongoDB GridFS: {}", filename);
            byte[] contentBytes = content.getBytes(StandardCharsets.UTF_8);
            InputStream inputStream = new ByteArrayInputStream(contentBytes);

            ObjectId fileId = gridFsTemplate.store(
                    inputStream,
                    filename,
                    "text/plain");

            log.info("Arquivo salvo com sucesso. ID: {}", fileId.toString());
            return fileId.toString();
        } catch (Exception e) {
            log.error("Erro ao salvar arquivo no MongoDB GridFS: {}", e.getMessage(), e);
            throw new RuntimeException("Erro ao salvar arquivo no MongoDB GridFS", e);
        }
    }

    /**
     * Salva um arquivo binário no MongoDB GridFS
     * 
     * @param filename    Nome do arquivo
     * @param data        Dados binários
     * @param contentType Tipo de conteúdo (MIME type)
     * @return ID do arquivo salvo
     */
    public String saveBinaryFile(String filename, byte[] data, String contentType) {
        try {
            log.info("Salvando arquivo binário no MongoDB GridFS: {}", filename);
            InputStream inputStream = new ByteArrayInputStream(data);

            ObjectId fileId = gridFsTemplate.store(
                    inputStream,
                    filename,
                    contentType);

            log.info("Arquivo binário salvo com sucesso. ID: {}", fileId.toString());
            return fileId.toString();
        } catch (Exception e) {
            log.error("Erro ao salvar arquivo binário no MongoDB GridFS: {}", e.getMessage(), e);
            throw new RuntimeException("Erro ao salvar arquivo binário no MongoDB GridFS", e);
        }
    }

    /**
     * Recupera um arquivo do MongoDB GridFS
     * 
     * @param fileId ID do arquivo
     * @return Conteúdo do arquivo como string
     */
    public String getTextFile(String fileId) {
        try {
            log.info("Recuperando arquivo de texto do MongoDB GridFS. ID: {}", fileId);
            GridFSFile gridFSFile = gridFsTemplate.findOne(new Query(Criteria.where("_id").is(new ObjectId(fileId))));

            if (gridFSFile == null) {
                log.warn("Arquivo não encontrado no MongoDB GridFS. ID: {}", fileId);
                return null;
            }

            GridFSBucket gridFSBucket = GridFSBuckets.create(mongoTemplate.getDb());
            GridFsResource resource = new GridFsResource(gridFSFile,
                    gridFSBucket.openDownloadStream(gridFSFile.getObjectId()));

            String content = new String(resource.getInputStream().readAllBytes(), StandardCharsets.UTF_8);
            log.info("Arquivo recuperado com sucesso. Tamanho: {} bytes", content.length());

            return content;
        } catch (IOException e) {
            log.error("Erro ao recuperar arquivo do MongoDB GridFS: {}", e.getMessage(), e);
            throw new RuntimeException("Erro ao recuperar arquivo do MongoDB GridFS", e);
        }
    }

    /**
     * Recupera um arquivo binário do MongoDB GridFS
     * 
     * @param fileId ID do arquivo
     * @return Dados binários do arquivo
     */
    public byte[] getBinaryFile(String fileId) {
        try {
            log.info("Recuperando arquivo binário do MongoDB GridFS. ID: {}", fileId);
            GridFSFile gridFSFile = gridFsTemplate.findOne(new Query(Criteria.where("_id").is(new ObjectId(fileId))));

            if (gridFSFile == null) {
                log.warn("Arquivo binário não encontrado no MongoDB GridFS. ID: {}", fileId);
                return null;
            }

            GridFSBucket gridFSBucket = GridFSBuckets.create(mongoTemplate.getDb());
            GridFsResource resource = new GridFsResource(gridFSFile,
                    gridFSBucket.openDownloadStream(gridFSFile.getObjectId()));

            byte[] content = resource.getInputStream().readAllBytes();
            log.info("Arquivo binário recuperado com sucesso. Tamanho: {} bytes", content.length);

            return content;
        } catch (IOException e) {
            log.error("Erro ao recuperar arquivo binário do MongoDB GridFS: {}", e.getMessage(), e);
            throw new RuntimeException("Erro ao recuperar arquivo binário do MongoDB GridFS", e);
        }
    }

    /**
     * Exclui um arquivo do MongoDB GridFS
     * 
     * @param fileId ID do arquivo
     */
    public void deleteFile(String fileId) {
        try {
            log.info("Excluindo arquivo do MongoDB GridFS. ID: {}", fileId);
            gridFsTemplate.delete(new Query(Criteria.where("_id").is(new ObjectId(fileId))));
            log.info("Arquivo excluído com sucesso");
        } catch (Exception e) {
            log.error("Erro ao excluir arquivo do MongoDB GridFS: {}", e.getMessage(), e);
            throw new RuntimeException("Erro ao excluir arquivo do MongoDB GridFS", e);
        }
    }
}
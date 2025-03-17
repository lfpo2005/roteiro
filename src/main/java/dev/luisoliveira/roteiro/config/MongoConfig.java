package dev.luisoliveira.roteiro.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.gridfs.GridFsTemplate;

/**
 * Configuração do MongoDB com suporte a GridFS
 */
@Configuration
public class MongoConfig {

    /**
     * Configura o GridFsTemplate para armazenamento de arquivos no MongoDB
     * 
     * @param mongoTemplate Template do MongoDB
     * @return GridFsTemplate configurado
     */
    @Bean
    public GridFsTemplate gridFsTemplate(MongoTemplate mongoTemplate) {
        return new GridFsTemplate(mongoTemplate.getMongoDatabaseFactory(), mongoTemplate.getConverter());
    }
}
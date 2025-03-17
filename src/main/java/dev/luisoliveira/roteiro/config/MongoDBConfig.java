package dev.luisoliveira.roteiro.config;

import org.bson.Document;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.event.ContextRefreshedEvent;
import org.springframework.context.event.EventListener;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.stereotype.Component;

@Component
public class MongoDBConfig {

    private static final Logger logger = LoggerFactory.getLogger(MongoDBConfig.class);

    private final MongoTemplate mongoTemplate;

    @Autowired
    public MongoDBConfig(MongoTemplate mongoTemplate) {
        this.mongoTemplate = mongoTemplate;
    }

    @EventListener(ContextRefreshedEvent.class)
    public void verificarConexaoMongoDB() {
        try {
            // Tenta obter informações do servidor para verificar a conexão
            String serverStatus = mongoTemplate.getDb().runCommand(new Document("serverStatus", 1)).toString();
            logger.info("Conexão com MongoDB estabelecida com sucesso!");
            logger.debug("Status do servidor MongoDB: {}", serverStatus);
        } catch (Exception e) {
            logger.error("Falha ao conectar com MongoDB: {}", e.getMessage());
            logger.error("Certifique-se de que o MongoDB está em execução na porta 27017");
            logger.error("Você pode iniciar o MongoDB usando o script 'iniciar-mongodb.bat'");
        }
    }
}
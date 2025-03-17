package dev.luisoliveira.roteiro.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.client.RestTemplate;

/**
 * Configuração do RestTemplate para chamadas HTTP
 */
@Configuration
public class RestTemplateConfig {

    /**
     * Cria e configura um bean RestTemplate para uso na aplicação
     * 
     * @return RestTemplate configurado
     */
    @Bean
    public RestTemplate restTemplate() {
        return new RestTemplate();
    }
}
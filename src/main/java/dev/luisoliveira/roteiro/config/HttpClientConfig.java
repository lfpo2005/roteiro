package dev.luisoliveira.roteiro.config;

import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.http.client.HttpClientAutoConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Esta classe desativa a auto-configuração do HttpClient
 */
@Configuration
@EnableAutoConfiguration(exclude = {HttpClientAutoConfiguration.class})
public class HttpClientConfig {
    // Configuração vazia para substituir a auto-configuração padrão
}
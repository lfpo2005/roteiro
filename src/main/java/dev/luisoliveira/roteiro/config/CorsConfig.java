package dev.luisoliveira.roteiro.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;
import org.springframework.web.filter.CorsFilter;

@Configuration
public class CorsConfig {

    @Bean
    public CorsFilter corsFilter() {
        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        CorsConfiguration config = new CorsConfiguration();

        // Permitir origens específicas (não usar * quando se trabalha com credenciais)
        config.addAllowedOrigin("http://localhost:3000");
        config.addAllowedOrigin("http://localhost:5173");
        config.addAllowedOrigin("http://localhost:8080");

        // Permitir credenciais (cookies, autenticação, etc)
        config.setAllowCredentials(true);

        // Métodos HTTP permitidos
        config.addAllowedMethod("GET");
        config.addAllowedMethod("POST");
        config.addAllowedMethod("PUT");
        config.addAllowedMethod("DELETE");
        config.addAllowedMethod("OPTIONS");

        // Cabeçalhos permitidos
        config.addAllowedHeader("*");

        // Expor cabeçalhos para o cliente
        config.addExposedHeader("Content-Disposition");

        // Tempo de cache para preflight (OPTIONS)
        config.setMaxAge(3600L);

        // Aplicar esta configuração a todos os endpoints
        source.registerCorsConfiguration("/**", config);

        return new CorsFilter(source);
    }
}
package dev.luisoliveira.roteiro.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.messaging.simp.config.MessageBrokerRegistry;
import org.springframework.web.socket.config.annotation.EnableWebSocketMessageBroker;
import org.springframework.web.socket.config.annotation.StompEndpointRegistry;
import org.springframework.web.socket.config.annotation.WebSocketMessageBrokerConfigurer;

/**
 * Configuração do WebSocket para notificações em tempo real
 */
@Configuration
@EnableWebSocketMessageBroker
public class WebSocketConfig implements WebSocketMessageBrokerConfigurer {

    @Override
    public void configureMessageBroker(MessageBrokerRegistry registry) {
        // Define os prefixos para os tópicos de destino
        registry.enableSimpleBroker("/topic");

        // Define o prefixo para endpoints que lidam com mensagens do cliente
        registry.setApplicationDestinationPrefixes("/app");
    }

    @Override
    public void registerStompEndpoints(StompEndpointRegistry registry) {
        // Registra o endpoint WebSocket e habilita SockJS para clientes que não suportam WebSocket
        registry.addEndpoint("/ws")
                .setAllowedOrigins("*") // Em produção, restringir para as origens permitidas
                .withSockJS();
    }
}
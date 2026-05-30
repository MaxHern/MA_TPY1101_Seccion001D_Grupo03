package com.vecizervi.backend;

import org.springframework.context.annotation.Configuration;
import org.springframework.messaging.simp.config.MessageBrokerRegistry;
import org.springframework.web.socket.config.annotation.EnableWebSocketMessageBroker;
import org.springframework.web.socket.config.annotation.StompEndpointRegistry;
import org.springframework.web.socket.config.annotation.WebSocketMessageBrokerConfigurer;

@Configuration
@EnableWebSocketMessageBroker
public class WebSocketConfig implements WebSocketMessageBrokerConfigurer {

    @Override
    public void registerStompEndpoints(StompEndpointRegistry registry) {
        // Endpoint al que se conecta el cliente Android
        registry.addEndpoint("/ws-chat")
                .setAllowedOriginPatterns("")
                .withSockJS();
    }

    @Override
    public void configureMessageBroker(MessageBrokerRegistry registry) {
        // Prefijo para mensajes del cliente hacia el servidor
        registry.setApplicationDestinationPrefixes("/app");
        // Prefijo de los tópicos a los que se suscriben los clientes
        registry.enableSimpleBroker("/topic");
    }
}
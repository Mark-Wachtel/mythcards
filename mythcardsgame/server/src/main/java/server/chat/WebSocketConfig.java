package server.chat;

import org.springframework.context.annotation.Configuration;
import org.springframework.messaging.simp.config.MessageBrokerRegistry;
import org.springframework.web.socket.config.annotation.*;

import server.CustomHandshakeHandler;
import server.UserHandshakeInterceptor;

@Configuration
@EnableWebSocketMessageBroker
public class WebSocketConfig implements WebSocketMessageBrokerConfigurer {

    private final UserHandshakeInterceptor interceptor;

    public WebSocketConfig(UserHandshakeInterceptor interceptor) {
        this.interceptor = interceptor;
    }

    @Override
    public void registerStompEndpoints(StompEndpointRegistry registry) {
        registry
            .addEndpoint("/ws")
            .setAllowedOriginPatterns("*")
            .addInterceptors(interceptor)
            .setHandshakeHandler(new CustomHandshakeHandler());
    }

    @Override
    public void configureMessageBroker(MessageBrokerRegistry registry) {
        registry
            .setApplicationDestinationPrefixes("/app")
            .enableSimpleBroker("/topic", "/queue");
    }
}

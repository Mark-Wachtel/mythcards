package server.chat;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Configuration;
import org.springframework.messaging.simp.config.MessageBrokerRegistry;
import org.springframework.web.socket.config.annotation.*;

import server.CustomHandshakeHandler;
import server.UserHandshakeInterceptor;

@Configuration
@EnableWebSocketMessageBroker
public class WebSocketConfig implements WebSocketMessageBrokerConfigurer {

    private final UserHandshakeInterceptor interceptor;
    private final CustomHandshakeHandler   handshakeHandler;

    @Autowired
    public WebSocketConfig(UserHandshakeInterceptor interceptor,
                           CustomHandshakeHandler handshakeHandler) {
        this.interceptor      = interceptor;
        this.handshakeHandler = handshakeHandler;
    }

    /* -------- WS-Endpoint ------------------------------------------------ */

    @Override
    public void registerStompEndpoints(StompEndpointRegistry registry) {
        registry.addEndpoint("/ws")                 // ← Client verbindet sich hier
                .setAllowedOrigins("*")             // TODO: Prod → konkrete Origins
                .addInterceptors(interceptor)
                .setHandshakeHandler(handshakeHandler);
    }

    /* -------- Broker-Konfiguration --------------------------------------- */

    @Override
    public void configureMessageBroker(MessageBrokerRegistry cfg) {

        /* RabbitMQ-STOMP Relay (haltbare Queues, Offline-Delivery) */
        cfg.enableStompBrokerRelay("/topic", "/queue")
                .setRelayHost("localhost")
                .setRelayPort(61613)
                .setClientLogin("guest").setClientPasscode("guest")
                .setSystemLogin("guest").setSystemPasscode("guest")
                .setSystemHeartbeatSendInterval(10_000)     // 10 s
                .setSystemHeartbeatReceiveInterval(10_000);

        cfg.setApplicationDestinationPrefixes("/app");      // z. B. /app/chat.send
        cfg.setUserDestinationPrefix("/user");              // /user/queue/…
    }
}
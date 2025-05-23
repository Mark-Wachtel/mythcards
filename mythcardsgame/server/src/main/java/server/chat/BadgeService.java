package server.chat;

import java.util.UUID;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.messaging.simp.SimpMessageHeaderAccessor;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Component;

import common.BadgeUpdateDTO;

@Component
public class BadgeService {

    private final SimpMessagingTemplate messaging;

    /** expliziter Constructor – kein Lombok */
    @Autowired
    public BadgeService(SimpMessagingTemplate messaging) {
        this.messaging = messaging;
    }

    public void publishBadge(UUID userId, BadgeUpdateDTO dto) {

        /* RabbitMQ soll die Nachricht haltbar (durable) ablegen */
        SimpMessageHeaderAccessor headers = SimpMessageHeaderAccessor.create();
        headers.setHeader("persistent", Boolean.TRUE);
        headers.setLeaveMutable(true);

        messaging.convertAndSendToUser(
                userId.toString(),          // → /user/{userId}/queue/badge
                "/queue/badge",
                dto,
                headers.getMessageHeaders());
    }
}
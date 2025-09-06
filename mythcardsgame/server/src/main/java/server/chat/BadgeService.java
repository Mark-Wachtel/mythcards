package server.chat;

import java.util.UUID;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.messaging.simp.SimpMessageHeaderAccessor;
import org.springframework.stereotype.Service;

import common.BadgeUpdateDTO;

/**
 * Versendet Badge-Updates
 *   • SOFORT per WebSocket (wenn der User online ist)  
 *   • zusätzlich haltbar in RabbitMQ (offline-Push)
 */
@Service
public class BadgeService {

    private final SimpMessagingTemplate ws;


    @Autowired
    public BadgeService(SimpMessagingTemplate ws
                       ) {

        this.ws       = ws;
    }

    // ---------------------------------------------------------------------
    public void publishBadge(UUID userId, BadgeUpdateDTO dto) {

        /* ---------------- 1) Sofort an alle aktiven WebSocket-Sessions */
        SimpMessageHeaderAccessor headers = SimpMessageHeaderAccessor.create();
        headers.setHeader("persistent", Boolean.TRUE);   // STOMP-Flag
        headers.setLeaveMutable(true);

        ws.convertAndSendToUser(
                userId.toString(),           //  /user/{id}/queue/badge
                "/queue/badge",
                dto,
                headers.getMessageHeaders());
    }
}
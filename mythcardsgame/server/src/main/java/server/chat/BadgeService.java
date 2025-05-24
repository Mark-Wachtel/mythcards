package server.chat;

import java.util.UUID;

import org.springframework.amqp.core.Binding;
import org.springframework.amqp.core.BindingBuilder;
import org.springframework.amqp.core.Queue;
import org.springframework.amqp.core.TopicExchange;
import org.springframework.amqp.rabbit.core.RabbitAdmin;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
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
    private final RabbitTemplate        rabbit;
    private final TopicExchange         exchange;
    private final RabbitAdmin           admin;

    @Autowired
    public BadgeService(SimpMessagingTemplate ws,
                        RabbitTemplate rabbit,
                        TopicExchange badgeExchange,
                        RabbitAdmin admin) {

        this.ws       = ws;
        this.rabbit   = rabbit;
        this.exchange = badgeExchange;
        this.admin    = admin;
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

        /* ---------------- 2) Haltbar in RabbitMQ ablegen ------------- */
        String queueName   = "badge." + userId;
        String routingKey  = "badge." + userId;

        // Queue ggf. dynamisch anlegen (durable, exklusiv=false, autoDelete=false)
        Queue   queue   = new Queue(queueName, true, false, false);
        Binding binding = BindingBuilder.bind(queue).to(exchange).with(routingKey);

        // idempotent – legt sie nur an, falls sie noch nicht existiert
        admin.declareQueue(queue);
        admin.declareBinding(binding);

        // Nachricht veröffentlichen (persistent by default bei durable Queue)
        rabbit.convertAndSend(RabbitBadgeConfig.EXCHANGE, routingKey, dto);
    }
}
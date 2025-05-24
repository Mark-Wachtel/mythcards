package server.chat;

import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Component;

import common.BadgeUpdateDTO;

@Component
public class BadgeQueueListener {

    private final SimpMessagingTemplate messaging;

    public BadgeQueueListener(SimpMessagingTemplate messaging) {
        this.messaging = messaging;
    }

    /** empfängt alle badge.*-Queues dank Platzhalter */
    @RabbitListener(bindings = @org.springframework.amqp.rabbit.annotation.QueueBinding(
            value       = @org.springframework.amqp.rabbit.annotation.Queue(value = "badge.*", durable = "true"),
            exchange    = @org.springframework.amqp.rabbit.annotation.Exchange(value = "badge.exchange", type = "topic"),
            key         = "badge.*"))
    public void forwardToStomp(BadgeUpdateDTO dto) {

        messaging.convertAndSendToUser(
                dto.conversationId().toString(),
                "/queue/badge",
                dto);
    }
}
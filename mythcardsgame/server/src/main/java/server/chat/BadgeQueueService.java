package server.chat;

import java.util.Map;
import java.util.UUID;

import org.springframework.amqp.core.*;
import org.springframework.amqp.rabbit.core.RabbitAdmin;
import org.springframework.stereotype.Service;

@Service
public class BadgeQueueService {

    private final TopicExchange badgeExchange;
    private final AmqpAdmin      rabbitAdmin;

    public BadgeQueueService(TopicExchange badgeExchange,
    		AmqpAdmin rabbitAdmin) {
        this.badgeExchange = badgeExchange;
        this.rabbitAdmin   = rabbitAdmin;
    }

    /** Beim **ersten** Login eines Users aufrufen. */
    public void ensureUserQueue(UUID userId) {

        String queueName   = "badge." + userId;
        String routingKey  = "badge." + userId;   // Publish & Bind identisch

        // bereits vorhanden? → nichts tun
        if (rabbitAdmin.getQueueProperties(queueName) != null) return;

        Map<String,Object> args = Map.of(
                "x-max-length", 1,          // nur _letzter_ Badge-Zähler bleibt
                "x-overflow",   "drop-head" // überschreibt alten Wert
        );

        Queue queue   = new Queue(queueName, true, false, false, args);

        Binding binding = BindingBuilder
                .bind(queue)
                .to(badgeExchange)
                .with(routingKey);

        rabbitAdmin.declareQueue(queue);
        rabbitAdmin.declareBinding(binding);
    }
}
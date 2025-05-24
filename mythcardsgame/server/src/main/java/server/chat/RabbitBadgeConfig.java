package server.chat;

import org.springframework.amqp.core.*;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class RabbitBadgeConfig {

    /** Exchange-Name – einmalig für alle Badge-Nachrichten. */
    public static final String EXCHANGE = "badge.exchange";

    @Bean
    public TopicExchange badgeExchange() {
        // durable = true  |  autoDelete = false
        return new TopicExchange(EXCHANGE, true, false);
    }
    /**
     * Für jede Anmeldung erzeugen wir später (programmgesteuert) eine
     * Queue „badge.<userId>“.  Deshalb müssen wir hier keine
     * statischen Queues definieren – nur den Exchange.
     */
}
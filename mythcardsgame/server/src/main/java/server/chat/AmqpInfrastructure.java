package server.chat;

import org.springframework.amqp.core.AmqpAdmin;
import org.springframework.amqp.rabbit.core.RabbitAdmin;
import org.springframework.amqp.rabbit.connection.ConnectionFactory;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.amqp.rabbit.annotation.EnableRabbit;

@Configuration
@EnableRabbit                  // aktiviert @RabbitListener-Verarbeitung
public class AmqpInfrastructure {

    /**
     * RabbitAdmin – wird für Queue/Exchange-Deklarationen benötigt
     * (BadgeQueueService, @RabbitListener, …).
     */
    @Bean
    public AmqpAdmin rabbitAdmin(ConnectionFactory connectionFactory) {
        // autoStartup = true (Default) → legt Queues/Exchanges beim Start an
        return new RabbitAdmin(connectionFactory);
    }
}
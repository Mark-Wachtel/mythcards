package server.chat;

import io.github.bucket4j.*;
import org.springframework.messaging.*;
import org.springframework.messaging.simp.stomp.StompCommand;
import org.springframework.messaging.simp.stomp.StompHeaderAccessor;
import org.springframework.messaging.support.ChannelInterceptor;
import org.springframework.stereotype.Component;

import java.time.Duration;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

@Component
public class RateLimitInterceptor implements ChannelInterceptor {

    private final ConcurrentHashMap<UUID, Bucket> buckets = new ConcurrentHashMap<>();

    @Override
    public Message<?> preSend(Message<?> message, MessageChannel channel) {
        StompHeaderAccessor accessor = StompHeaderAccessor.wrap(message);
        if (StompCommand.SEND.equals(accessor.getCommand())) {
            String user = accessor.getUser() != null ? accessor.getUser().getName() : null;
            if (user != null) {
                UUID uid = UUID.fromString(user);
                buckets.computeIfAbsent(uid, k -> Bucket.builder()
                        .addLimit(Bandwidth.simple(20, Duration.ofSeconds(10))).build());
                if (!buckets.get(uid).tryConsume(1)) {
                    throw new RateLimitException();
                }
            }
        }
        return message;
    }
}
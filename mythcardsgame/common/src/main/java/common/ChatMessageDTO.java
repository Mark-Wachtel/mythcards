package common;

import java.time.Instant;
import java.util.UUID;

public record ChatMessageDTO(
        UUID conversationId,
        UUID messageId,
        UUID senderId,
        Instant timestamp,
        String text,
        boolean important
) {}
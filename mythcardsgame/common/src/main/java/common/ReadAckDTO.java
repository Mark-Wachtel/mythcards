package common;

import java.time.Instant;
import java.util.UUID;

public record ReadAckDTO(
        UUID conversationId,
        Instant lastReadAt
) {}
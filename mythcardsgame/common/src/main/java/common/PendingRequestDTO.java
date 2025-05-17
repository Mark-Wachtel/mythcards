package common;

import java.time.Instant;
import java.util.UUID;

public record PendingRequestDTO(
        UUID requestId,
        UUID senderId,
        String senderUsername,
        Instant expiresAt
) {}
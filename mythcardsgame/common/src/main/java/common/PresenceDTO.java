package common;

import java.util.UUID;

/**
 * Einzelnes Präsenz-Update.
 *
 *  @param userId  ID des Nutzers, dessen Status sich geändert hat
 *  @param online  {@code true} → jetzt online, {@code false} → jetzt offline
 */
public record PresenceDTO(
        UUID userId,
        boolean online
) {}
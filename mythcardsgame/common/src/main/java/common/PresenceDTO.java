package common;

import java.util.UUID;

public record PresenceDTO(
        UUID userId,
        String status // "ONLINE" | "OFFLINE"
) {}
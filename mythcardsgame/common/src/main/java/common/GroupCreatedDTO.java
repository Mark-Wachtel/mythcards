package common;

import java.util.List;
import java.util.UUID;

public record GroupCreatedDTO(
        UUID conversationId,
        String name,
        List<UUID> members) {}
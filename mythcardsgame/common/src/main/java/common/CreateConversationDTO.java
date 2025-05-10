package common;

import java.util.List;
import java.util.UUID;

public record CreateConversationDTO(
        boolean group,
        String name,
        List<UUID> participantIds
) {}
package common;

import java.util.UUID;

public record BadgeUpdateDTO(
        UUID conversationId,
        int unreadCount
) {}
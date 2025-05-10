package common;

import java.util.UUID;
public record ImportantUpdateDTO(UUID conversationId, UUID messageId, boolean important) {}
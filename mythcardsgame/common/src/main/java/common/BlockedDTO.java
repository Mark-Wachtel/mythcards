package common;

import java.time.Instant;
import java.util.UUID;

public record BlockedDTO(UUID userId, String username, Instant since) {}

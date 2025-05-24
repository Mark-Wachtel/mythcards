package common;

import java.util.List;
import java.util.UUID;

/**
 * Initiale Online-Liste aller Freunde für eine frisch verbundene Session.
 *
 *  @param friendsOnline  Liste der Freund-IDs, die aktuell online sind
 */
public record PresenceInitDTO(
        List<UUID> friendsOnline
) {}
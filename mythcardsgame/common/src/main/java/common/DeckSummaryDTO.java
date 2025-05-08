package common;

import java.time.Instant;

public record DeckSummaryDTO(
        long deckId,
        String name,
        String format,
        int   count,
        String preview,
        Instant updated
) {}
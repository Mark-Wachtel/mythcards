package common;

import java.time.Instant;
import java.util.UUID;

import common.MatchDTO.Result;

public record MatchDTO(
        UUID   matchId,
        SimpleUser opponent,
        Result result,
        Instant timestamp,
        long   deckUsedId,
        int    turns
) {
    public enum Result { WIN, LOSS, DRAW }
}
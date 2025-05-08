package common;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

public record ProfileDTO(
        UUID userId,
        String username,
        String avatarUrl,
        int   rank,
        int   experience,
        int   level,
        List<FriendDTO>  friends,
        List<BlockedDTO> blocked,
        List<MatchDTO>   lastRandomMatches,
        List<DeckSummaryDTO> decks
) {}










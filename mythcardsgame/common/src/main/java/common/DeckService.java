package common;

import java.util.List;
import java.util.UUID;

public interface DeckService {
    DeckDto createDeck(UUID userId, String name, List<UUID> cardIds);
    DeckDto updateDeck(UUID userId, UUID deckId, String name, List<UUID> cardIds);
    void deleteDeck(UUID userId, UUID deckId);
    List<DeckDto> findAllByUser(UUID userId);
    DeckDetailDto findById(UUID userId, UUID deckId);
}
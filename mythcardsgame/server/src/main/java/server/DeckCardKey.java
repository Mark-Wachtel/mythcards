package server;

import jakarta.persistence.*;
import java.io.Serializable;
import java.util.Objects;
import java.util.UUID;

@Embeddable
public class DeckCardKey implements Serializable {
    @Column(name = "deck_id", columnDefinition = "BINARY(16)")
    private UUID deckId;

    @Column(name = "card_id", columnDefinition = "BINARY(16)")
    private UUID cardId;

    public DeckCardKey() {}
    public DeckCardKey(UUID deckId, UUID cardId) {
        this.deckId = deckId;
        this.cardId = cardId;
    }

    // equals und hashCode
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof DeckCardKey)) return false;
        DeckCardKey that = (DeckCardKey) o;
        return Objects.equals(deckId, that.deckId) && Objects.equals(cardId, that.cardId);
    }

    @Override
    public int hashCode() {
        return Objects.hash(deckId, cardId);
    }

    // Getter und Setter
    public UUID getDeckId() { return deckId; }
    public void setDeckId(UUID deckId) { this.deckId = deckId; }
    public UUID getCardId() { return cardId; }
    public void setCardId(UUID cardId) { this.cardId = cardId; }
}
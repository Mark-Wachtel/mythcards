package server;

import jakarta.persistence.*;

@Entity
@Table(name = "deck_cards")
public class DeckCardEntity {
    @EmbeddedId
    private DeckCardKey id;

    @ManyToOne(fetch = FetchType.LAZY)
    @MapsId("deckId")
    @JoinColumn(name = "deck_id", nullable = false)
    private DeckEntity deck;

    @ManyToOne(fetch = FetchType.LAZY)
    @MapsId("cardId")
    @JoinColumn(name = "card_id", nullable = false)
    private CardEntity card;

    @Column(nullable = false)
    private int quantity;

    public DeckCardEntity() {}
    public DeckCardEntity(DeckEntity deck, CardEntity card, int quantity) {
        this.deck = deck;
        this.card = card;
        this.quantity = quantity;
        this.id = new DeckCardKey(deck.getId(), card.getId());
    }

    // Getter und Setter
    public DeckCardKey getId() { return id; }
    public DeckEntity getDeck() { return deck; }
    public void setDeck(DeckEntity deck) { this.deck = deck; }
    public CardEntity getCard() { return card; }
    public void setCard(CardEntity card) { this.card = card; }
    public int getQuantity() { return quantity; }
    public void setQuantity(int quantity) { this.quantity = quantity; }
}
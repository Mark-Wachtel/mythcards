package common;

import java.util.List;
import java.util.UUID;


public class DeckDetailDto {
    private UUID id;
    private String name;
    private List<CardData> cards;

    public DeckDetailDto() {}
    public DeckDetailDto(UUID id, String name, List<CardData> cards) {
        this.setId(id);
        this.setName(name);
        this.setCards(cards);
    }
	public UUID getId() {
		return id;
	}
	public void setId(UUID id) {
		this.id = id;
	}
	public String getName() {
		return name;
	}
	public void setName(String name) {
		this.name = name;
	}
	public List<CardData> getCards() {
		return cards;
	}
	public void setCards(List<CardData> cards) {
		this.cards = cards;
	}

    // Getter & Setter
}
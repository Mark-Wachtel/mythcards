package common;

import java.util.List;
import java.util.UUID;

public class DeckDto {
    private UUID id;
    private UUID userId;
    private String name;
    private List<UUID> cardIds;

    public DeckDto() {}
    public DeckDto(UUID id, UUID userId, String name, List<UUID> cardIds) {
        this.setId(id);
        this.setUserId(userId);
        this.setName(name);
        this.setCardIds(cardIds);
    }
	public UUID getId() {
		return id;
	}
	public void setId(UUID id) {
		this.id = id;
	}
	public UUID getUserId() {
		return userId;
	}
	public void setUserId(UUID userId) {
		this.userId = userId;
	}
	public String getName() {
		return name;
	}
	public void setName(String name) {
		this.name = name;
	}
	public List<UUID> getCardIds() {
		return cardIds;
	}
	public void setCardIds(List<UUID> cardIds) {
		this.cardIds = cardIds;
	}
}
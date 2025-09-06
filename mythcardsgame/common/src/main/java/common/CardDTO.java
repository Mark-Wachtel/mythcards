package common;

import java.util.List;
import java.util.UUID;

public class CardDTO {
    
    private UUID id;
    private String title;
    private String description;
    private String imageUrl;
    private Integer manaCost;
    private Integer attack;
    private Integer defense;
    private String cardType;
    private List<String> abilities;
    
    // Konstruktoren
    public CardDTO() {}
    
    // Getters und Setters
    public UUID getId() {
        return id;
    }
    
    public void setId(UUID id) {
        this.id = id;
    }
    
    public String getTitle() {
        return title;
    }
    
    public void setTitle(String title) {
        this.title = title;
    }
    
    public String getDescription() {
        return description;
    }
    
    public void setDescription(String description) {
        this.description = description;
    }
    
    public String getImageUrl() {
        return imageUrl;
    }
    
    public void setImageUrl(String imageUrl) {
        this.imageUrl = imageUrl;
    }
    
    public Integer getManaCost() {
        return manaCost;
    }
    
    public void setManaCost(Integer manaCost) {
        this.manaCost = manaCost;
    }
    
    public Integer getAttack() {
        return attack;
    }
    
    public void setAttack(Integer attack) {
        this.attack = attack;
    }
    
    public Integer getDefense() {
        return defense;
    }
    
    public void setDefense(Integer defense) {
        this.defense = defense;
    }
    
    public String getCardType() {
        return cardType;
    }
    
    public void setCardType(String cardType) {
        this.cardType = cardType;
    }
    
    public List<String> getAbilities() {
        return abilities;
    }
    
    public void setAbilities(List<String> abilities) {
        this.abilities = abilities;
    }
}
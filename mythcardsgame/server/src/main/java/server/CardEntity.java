package server;

import jakarta.persistence.*;
import java.util.List;
import java.util.UUID;

@Entity
@Table(name = "cards")
public class CardEntity {
    
    @Id
    @GeneratedValue(generator = "UUID")
    private UUID id;
    
    @Column(nullable = false)
    private String title;
    
    @Column(length = 500)
    private String description;
    
    @Column(name = "image_url")
    private String imageUrl;
    
    @Column(name = "mana_cost")
    private Integer manaCost;
    
    private Integer attack;
    
    private Integer defense;
    
    @Column(name = "card_type")
    private String cardType;
    
    @ElementCollection
    @CollectionTable(name = "card_abilities", joinColumns = @JoinColumn(name = "card_id"))
    @Column(name = "ability")
    private List<String> abilities;
    
    // Konstruktoren
    public CardEntity() {}
    
    public CardEntity(String title, String description, Integer manaCost, 
                     Integer attack, Integer defense, 
                     String cardType, List<String> abilities) {
        this.title = title;
        this.description = description;
        this.manaCost = manaCost;
        this.attack = attack;
        this.defense = defense;
        this.cardType = cardType;
        this.abilities = abilities;
    }
    
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

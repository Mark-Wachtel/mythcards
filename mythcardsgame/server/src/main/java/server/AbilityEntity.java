package server;

import jakarta.persistence.*;

/* Entity */
@Entity
@Table(name = "card_abilities")
@IdClass(AbilityId.class)
public class AbilityEntity {

    @Id
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "card_id")
    private CardEntity card;

    @Id
    private short slot;          // 1-4

    private String nameKey;
    private String descKey;
    private String valueKey;

    public AbilityEntity() {}

    /* Getter / Setter */
    public CardEntity getCard()          { return card; }
    public void       setCard(CardEntity c){ this.card = c; }
    public short      getSlot()          { return slot; }
    public void       setSlot(short s)   { this.slot = s; }
    public String     getNameKey()       { return nameKey; }
    public void       setNameKey(String k){ this.nameKey = k; }
    public String     getDescKey()       { return descKey; }
    public void       setDescKey(String k){ this.descKey = k; }
    public String     getValueKey()      { return valueKey; }
    public void       setValueKey(String k){ this.valueKey = k; }
}
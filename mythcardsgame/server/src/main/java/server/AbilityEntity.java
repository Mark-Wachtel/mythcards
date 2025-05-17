package server;

import jakarta.persistence.*;
import java.util.Objects;

/**
 * Entity for one ability slot of a Card.
 * Composite primary key: (card_id, slot).
 */
@Entity
@Table(name = "card_abilities")
@IdClass(AbilityId.class)
public class AbilityEntity {

    @Id
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "card_id", nullable = false)
    private CardEntity card;

    @Id
    @Column(name = "slot", nullable = false)
    private short slot;

    @Column(name = "name_key", nullable = false)
    private String nameKey;

    @Column(name = "desc_key", nullable = false)
    private String descKey;

    @Column(name = "value_key", nullable = false)
    private String valueKey;

    // ---------------- Constructors ----------------

    protected AbilityEntity() {
        // for JPA
    }

    public AbilityEntity(CardEntity card,
                         short slot,
                         String nameKey,
                         String descKey,
                         String valueKey) {
        this.card     = card;
        this.slot     = slot;
        this.nameKey  = nameKey;
        this.descKey  = descKey;
        this.valueKey = valueKey;
    }

    // ---------------- Getters & Setters ----------------

    public CardEntity getCard() {
        return card;
    }

    public void setCard(CardEntity card) {
        this.card = card;
    }

    public short getSlot() {
        return slot;
    }

    public void setSlot(short slot) {
        this.slot = slot;
    }

    public String getNameKey() {
        return nameKey;
    }

    public void setNameKey(String nameKey) {
        this.nameKey = nameKey;
    }

    public String getDescKey() {
        return descKey;
    }

    public void setDescKey(String descKey) {
        this.descKey = descKey;
    }

    public String getValueKey() {
        return valueKey;
    }

    public void setValueKey(String valueKey) {
        this.valueKey = valueKey;
    }

    // ---------------- equals & hashCode (for composite key) ----------------

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof AbilityEntity)) return false;
        AbilityEntity that = (AbilityEntity) o;
        return slot == that.slot &&
               Objects.equals(card.getId(), that.card.getId());
    }

    @Override
    public int hashCode() {
        return Objects.hash(card.getId(), slot);
    }
}
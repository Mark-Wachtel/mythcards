package server;

import java.io.Serializable;
import java.util.UUID;
import java.util.Objects;

/**
 * Composite key class for AbilityEntity: (card.id, slot)
 */
public class AbilityId implements Serializable {

    private UUID card;
    private short slot;

    public AbilityId() {
        // for JPA
    }

    public AbilityId(UUID card, short slot) {
        this.card = card;
        this.slot = slot;
    }

    // equals & hashCode must match those in AbilityEntity

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof AbilityId)) return false;
        AbilityId that = (AbilityId) o;
        return slot == that.slot &&
               Objects.equals(card, that.card);
    }

    @Override
    public int hashCode() {
        return Objects.hash(card, slot);
    }
}
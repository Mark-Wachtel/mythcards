package server;

import java.io.Serializable;
import java.util.Objects;

/* PK-Klasse */
public class AbilityId implements Serializable {
    private Long  card;
    private Short slot;
    public AbilityId() {}
    public AbilityId(Long card, Short slot) { this.card = card; this.slot = slot; }
    @Override public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof AbilityId other)) return false;
        return Objects.equals(card, other.card) && Objects.equals(slot, other.slot);
    }
    @Override public int hashCode() { return Objects.hash(card, slot); }
}
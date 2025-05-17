package server;

import java.io.Serializable;
import java.util.Objects;
import java.util.UUID;

/**
 * Composite PK class for BlockEntity.
 */
public class BlockId implements Serializable {
    private UUID blocker;
    private UUID target;

    public BlockId() {
        // for JPA
    }

    public BlockId(UUID blocker, UUID target) {
        this.blocker = blocker;
        this.target  = target;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof BlockId)) return false;
        BlockId that = (BlockId) o;
        return Objects.equals(blocker, that.blocker) &&
               Objects.equals(target,  that.target);
    }

    @Override
    public int hashCode() {
        return Objects.hash(blocker, target);
    }

    // getters/setters if needed
    public UUID getBlocker() { return blocker; }
    public void setBlocker(UUID blocker) { this.blocker = blocker; }
    public UUID getTarget()  { return target; }
    public void setTarget(UUID target) { this.target = target; }
}
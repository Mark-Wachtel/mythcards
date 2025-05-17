package server;

import jakarta.persistence.*;
import java.io.Serializable;
import java.time.Instant;
import java.util.Objects;
import java.util.UUID;

/**
 * Entity representing a user block.
 * Composite PK: (blocker_id, target_id)
 */
@Entity
@Table(name = "blocks")
@IdClass(BlockId.class)
public class BlockEntity {

    @Id
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "blocker_id", nullable = false, updatable = false)
    private UserEntity blocker;

    @Id
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "target_id", nullable = false, updatable = false)
    private UserEntity target;

    @Column(name = "since", nullable = false, updatable = false)
    private Instant since = Instant.now();

    protected BlockEntity() {
        // for JPA
    }

    public BlockEntity(UserEntity blocker, UserEntity target) {
        this.blocker = blocker;
        this.target  = target;
        this.since   = Instant.now();
    }

    public UserEntity getBlocker() {
        return blocker;
    }

    public void setBlocker(UserEntity blocker) {
        this.blocker = blocker;
    }

    public UserEntity getTarget() {
        return target;
    }

    public void setTarget(UserEntity target) {
        this.target = target;
    }

    public Instant getSince() {
        return since;
    }

    public void setSince(Instant since) {
        this.since = since;
    }
}


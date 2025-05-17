package server;

import jakarta.persistence.*;
import java.time.Instant;

@Entity
@Table(name = "friends")
@IdClass(FriendshipId.class)
public class FriendshipEntity {

    @Id
    @ManyToOne(fetch = FetchType.EAGER, optional = false)
    @JoinColumn(name = "user1_id", nullable = false, updatable = false)
    private UserEntity user1;

    @Id
    @ManyToOne(fetch = FetchType.EAGER, optional = false)
    @JoinColumn(name = "user2_id", nullable = false, updatable = false)
    private UserEntity user2;

    @Column(name = "since", nullable = false, updatable = false)
    private Instant since = Instant.now();

    protected FriendshipEntity() {}

    public FriendshipEntity(UserEntity user1, UserEntity user2) {
        this.user1 = user1;
        this.user2 = user2;
    }

    public UserEntity getUser1() { return user1; }
    public void setUser1(UserEntity user1) { this.user1 = user1; }
    public UserEntity getUser2() { return user2; }
    public void setUser2(UserEntity user2) { this.user2 = user2; }
    public Instant getSince() { return since; }
    public void setSince(Instant since) { this.since = since; }
}

package server;

import java.io.Serializable;
import java.util.Objects;
import java.util.UUID;

public class FriendshipId implements Serializable {
    private UUID user1;
    private UUID user2;

    public FriendshipId() {}

    public FriendshipId(UUID user1, UUID user2) {
        this.user1 = user1;
        this.user2 = user2;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof FriendshipId)) return false;
        FriendshipId that = (FriendshipId) o;
        return Objects.equals(user1, that.user1) &&
               Objects.equals(user2, that.user2);
    }

    @Override
    public int hashCode() {
        return Objects.hash(user1, user2);
    }

    public UUID getUser1() { return user1; }
    public void setUser1(UUID user1) { this.user1 = user1; }
    public UUID getUser2() { return user2; }
    public void setUser2(UUID user2) { this.user2 = user2; }
}
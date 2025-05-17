package server;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.UUID;

public interface FriendshipRepository extends JpaRepository<FriendshipEntity, FriendshipId> {

	    List<FriendshipEntity> findByUser1_IdOrUser2_Id(UUID user1, UUID user2);
	
    boolean existsByUser1_IdAndUser2_Id(UUID user1, UUID user2);

    default boolean areFriends(UUID userA, UUID userB) {
        if (userA.equals(userB)) return false;
        return existsByUser1_IdAndUser2_Id(userA, userB)
            || existsByUser1_IdAndUser2_Id(userB, userA);
    }
}
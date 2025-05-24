package server;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Set;
import java.util.UUID;

/**
 * Repository für Freundschaften.
 *
 *  • {@code findByUser1_IdOrUser2_Id(...)}    → komplette Friendship-Entitäten,
 *    z. B. um Metadaten (Status, Datum) auszulesen.
 *
 *  • {@code findAcceptedFriendIds(...)}       → schlanke Methode: nur die UUIDs
 *    aller bestätigten Freunde. Optimal für Präsenz-Broadcast & Mengenoperationen.
 */
public interface FriendshipRepository extends JpaRepository<FriendshipEntity, FriendshipId> {

    /* -------- Vollständige Friendship-Entitäten laden -------- */
    List<FriendshipEntity> findByUser1_IdOrUser2_Id(UUID user1, UUID user2);

    /* -------- Einzelne Freundschaft vorhanden? -------- */
    boolean existsByUser1_IdAndUser2_Id(UUID user1, UUID user2);

    /**
     * Liefert die UUIDs aller bestätigten Freunde des Nutzers
     * – unabhängig davon, ob er in der Tabelle als user1 oder user2 steht.
     *
     * Annahme: Das Feld {@code status} hat den Enum/String-Wert {@code ACCEPTED},
     * sobald beide Seiten die Freundschaft bestätigt haben.
     */
    @Query("""
           SELECT f.user2.id
             FROM FriendshipEntity f
            WHERE f.user1.id = :uid
              AND f.status   = 'ACCEPTED'
           UNION
           SELECT f.user1.id
             FROM FriendshipEntity f
            WHERE f.user2.id = :uid
              AND f.status   = 'ACCEPTED'
           """)
    Set<UUID> findAcceptedFriendIds(@Param("uid") UUID userId);

    /* -------- Default-Helfer -------- */
    default boolean areFriends(UUID userA, UUID userB) {
        if (userA.equals(userB)) return false;
        return existsByUser1_IdAndUser2_Id(userA, userB)
            || existsByUser1_IdAndUser2_Id(userB, userA);
    }
}
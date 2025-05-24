package server;

import jakarta.persistence.*;
import java.time.Instant;
import java.util.Objects;

/**
 * Eine Freundschaft zwischen zwei Benutzern.
 *
 *  • <b>Composite-Key</b> (user1_id, user2_id) – Reihenfolge wird in der
 *    Factory-Methode automatisch sortiert, sodass jede Freundschaft nur
 *    genau <i>einmal</i> in der Tabelle existiert.<br>
 *  • <b>Status</b> (PENDING → ACCEPTED → BLOCKED) ermöglicht Anfragen /
 *    Ablehnung ohne separate Tabelle.<br>
 *  • <b>since</b> wird beim Persistieren automatisch gesetzt.
 */
@Entity
@Table(name = "friends")
@IdClass(FriendshipId.class)
@NamedQueries({
    /* Für Repository-Derivate optional – Beispiel */
    @NamedQuery(
        name  = "FriendshipEntity.findFriendsOf",
        query = """
                SELECT f
                  FROM FriendshipEntity f
                 WHERE f.user1.id = :uid
                    OR f.user2.id = :uid
                """
    )
})
public class FriendshipEntity {

    /* ---------- Primary Key (Teil 1) ---------- */
    @Id
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "user1_id", nullable = false, updatable = false)
    private UserEntity user1;

    /* ---------- Primary Key (Teil 2) ---------- */
    @Id
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "user2_id", nullable = false, updatable = false)
    private UserEntity user2;

    /* ---------- Zusatzfelder ---------- */

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false)
    private Status status = Status.PENDING;

    @Column(name = "since", nullable = false, updatable = false)
    private Instant since;

    /* ---------- Konstruktoren ---------- */

    /** Für JPA */
    protected FriendshipEntity() {}

    /**
     * Factory bewahrt Ordnung: {@code min(idA,idB)} landet immer in user1.
     */
    public static FriendshipEntity create(UserEntity a, UserEntity b) {
        Objects.requireNonNull(a);
        Objects.requireNonNull(b);
        if (a.getId().equals(b.getId())) {
            throw new IllegalArgumentException("Cannot befriend yourself");
        }
        // Ordnung nach UUID – garantiert eindeutige Zeile
        return a.getId().compareTo(b.getId()) < 0
               ? new FriendshipEntity(a, b)
               : new FriendshipEntity(b, a);
    }

    private FriendshipEntity(UserEntity user1, UserEntity user2) {
        this.user1  = user1;
        this.user2  = user2;
        this.status = Status.PENDING;
    }

    /* ---------- Lifecycle ---------- */
    @PrePersist
    private void onCreate() {
        since = Instant.now();
    }

    /* ---------- Getter / Setter ---------- */

    public UserEntity getUser1()      { return user1; }
    public UserEntity getUser2()      { return user2; }

    public Status     getStatus()     { return status; }
    public void       setStatus(Status status) { this.status = status; }

    public Instant    getSince()      { return since; }

    /* ---------- equals / hashCode basierend auf PK ---------- */

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof FriendshipEntity fe)) return false;
        return Objects.equals(user1.getId(), fe.user1.getId()) &&
               Objects.equals(user2.getId(), fe.user2.getId());
    }

    @Override
    public int hashCode() {
        return Objects.hash(user1.getId(), user2.getId());
    }

    /* ---------- Enum ---------- */

    public enum Status {
        PENDING,   // Anfrage gesendet, noch nicht bestätigt
        ACCEPTED,  // Freunde
        BLOCKED    // einer hat den anderen blockiert
    }
}
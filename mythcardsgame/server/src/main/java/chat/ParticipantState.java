package chat;

import jakarta.persistence.*;
import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "participant_state")
public class ParticipantState {

    @EmbeddedId
    private Id id;

    @MapsId("conversationId")
    @ManyToOne(fetch = FetchType.LAZY)
    private Conversation conversation;

    @Column(name = "last_read_at", nullable = false)
    private Instant lastReadAt = Instant.EPOCH;

    protected ParticipantState() {}

    public ParticipantState(Conversation conv, UUID userId) {
        this.conversation  = conv;
        this.id            = new Id(conv.getId(), userId);
    }

    /* ---------- Getter / Setter für Badge-Logik ---------- */
    public Instant getLastReadAt()            { return lastReadAt; }
    public void    setLastReadAt(Instant ts)  { this.lastReadAt = ts; }

    /* ------------ Embedded composite key ----------------- */
    @Embeddable
    public static class Id implements java.io.Serializable {
        private UUID conversationId;
        private UUID userId;
        protected Id() {}
        public Id(UUID convId, UUID userId) {
            this.conversationId = convId;
            this.userId        = userId;
        }
    }
}
package chat;

import jakarta.persistence.*;
import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "conversation_participant")
public class ConversationParticipant {

    public enum Role { OWNER, MOD, MEMBER }

    @EmbeddedId
    private Id id;

    @MapsId("conversationId")
    @ManyToOne(fetch = FetchType.LAZY)
    private Conversation conversation;

    @Column(name = "joined_at", nullable = false)
    private Instant joinedAt = Instant.now();

    @Enumerated(EnumType.STRING)
    private Role role = Role.MEMBER;

    protected ConversationParticipant() {}

    public ConversationParticipant(Conversation conversation, UUID userId, Role role) {
        this.conversation = conversation;
        this.id  = new Id(conversation.getId(), userId);
        this.role = role;
    }

    /* ---------- NEU ---------- */
    public UUID getUserId() {           // erleichtert Zugriffsprüfungen
        return id.userId;
    }

    /* -------- composite key --- */
    @Embeddable
    public static class Id implements java.io.Serializable {
        private UUID conversationId;
        private UUID userId;
        protected Id() {}
        public Id(UUID conversationId, UUID userId) {
            this.conversationId = conversationId;
            this.userId = userId;
        }
    }
}
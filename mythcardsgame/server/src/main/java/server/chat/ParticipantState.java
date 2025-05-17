package server.chat;

import jakarta.persistence.*;
import server.UserEntity;

import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "participant_state")
public class ParticipantState {

    @EmbeddedId
    private Id id;

    @MapsId("conversationId")
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "conversation_id", nullable = false, updatable = false)
    private Conversation conversation;

    @MapsId("userId")
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "user_id", nullable = false, updatable = false)
    private UserEntity user;

    @Column(name = "last_read_at", nullable = false)
    private Instant lastReadAt = Instant.EPOCH;

    protected ParticipantState() {}

    public ParticipantState(Conversation conversation, UserEntity user) {
        this.conversation = conversation;
        this.user         = user;
        this.id           = new Id(conversation.getId(), user.getId());
    }

    public Instant getLastReadAt() { return lastReadAt; }
    public void setLastReadAt(Instant ts) { this.lastReadAt = ts; }

    @Embeddable
    public static class Id implements java.io.Serializable {
        @Column(name = "conversation_id")
        private UUID conversationId;

        @Column(name = "user_id")
        private UUID userId;

        protected Id() {}

        public Id(UUID convId, UUID userId) {
            this.conversationId = convId;
            this.userId         = userId;
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (!(o instanceof Id)) return false;
            Id that = (Id)o;
            return conversationId.equals(that.conversationId)
                && userId.equals(that.userId);
        }

        @Override
        public int hashCode() {
            return conversationId.hashCode() ^ userId.hashCode();
        }
    }
}
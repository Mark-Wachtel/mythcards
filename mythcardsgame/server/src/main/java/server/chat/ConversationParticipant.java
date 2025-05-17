package server.chat;

import jakarta.persistence.*;
import org.hibernate.annotations.Type;

import java.time.Instant;
import java.util.UUID;

/**
 * Verknüpft einen User (per userId) mit einer Conversation und seiner Rolle.
 * PK: (conversation_id, user_id)
 */
@Entity
@Table(name = "conversation_participant")
@IdClass(ConversationParticipantId.class)
public class ConversationParticipant {

    @Id
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "conversation_id", nullable = false, updatable = false)
    private Conversation conversation;

    @Id
    @Column(name = "user_id", nullable = false, updatable = false, columnDefinition = "BINARY(16)")
    private UUID userId;

    @Column(name = "joined_at", nullable = false, updatable = false)
    private Instant joinedAt = Instant.now();

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private Role role = Role.MEMBER;

    protected ConversationParticipant() {}

    public ConversationParticipant(Conversation conversation, UUID userId, Role role) {
        this.conversation = conversation;
        this.userId = userId;
        this.role = role != null ? role : Role.MEMBER;
    }

    public Conversation getConversation() {
        return conversation;
    }

    public UUID getUserId() {
        return userId;
    }

    public Instant getJoinedAt() {
        return joinedAt;
    }

    public Role getRole() {
        return role;
    }

    public void setRole(Role role) {
        this.role = role;
    }

    public enum Role {
        OWNER,
        MOD,
        MEMBER
    }
}
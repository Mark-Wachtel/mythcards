package server.chat;

import java.util.Objects;
import java.util.UUID;
/**
 * Composite PK class für ConversationParticipant.
 */
public class ConversationParticipantId implements java.io.Serializable {
    private UUID conversation;
    private UUID userId;

    public ConversationParticipantId() { }

    public ConversationParticipantId(UUID conversation, UUID userId) {
        this.conversation = conversation;
        this.userId       = userId;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof ConversationParticipantId)) return false;
        ConversationParticipantId that = (ConversationParticipantId) o;
        return Objects.equals(conversation, that.conversation) &&
               Objects.equals(userId,       that.userId);
    }

    @Override
    public int hashCode() {
        return Objects.hash(conversation, userId);
    }

    // optional: Getter/Setter
    public UUID getConversation() { return conversation; }
    public void setConversation(UUID conversation) { this.conversation = conversation; }
    public UUID getUserId()       { return userId; }
    public void setUserId(UUID userId)       { this.userId = userId; }
}
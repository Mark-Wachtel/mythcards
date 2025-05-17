package server.chat;

import jakarta.persistence.*;
import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "chat_message")
public class ChatMessage {

    @Id
    @GeneratedValue
    @Column(name = "id", updatable = false, nullable = false)
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "conversation_id", nullable = false)
    private Conversation conversation;

    @Column(name = "sender_id", nullable = false)
    private UUID senderId;

    @Column(name = "text", length = 300, nullable = false)
    private String text;

    @Column(name = "important", nullable = false)
    private boolean important;

    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt = Instant.now();

    protected ChatMessage() { }

    public ChatMessage(Conversation conversation,
                       UUID senderId,
                       String text,
                       boolean important) {
        this.conversation = conversation;
        this.senderId     = senderId;
        this.text         = text;
        this.important    = important;
    }

    public UUID getId() {
        return id;
    }
    public Conversation getConversation() {
        return conversation;
    }
    public UUID getSenderId() {
        return senderId;
    }
    public String getText() {
        return text;
    }
    public boolean isImportant() {
        return important;
    }
    public Instant getCreatedAt() {
        return createdAt;
    }
}
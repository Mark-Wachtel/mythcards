package chat;

import jakarta.persistence.*;
import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "chat_message")
public class ChatMessage {
    @Id @GeneratedValue private UUID id;
    @ManyToOne(fetch = FetchType.LAZY) @JoinColumn(name = "conversation_id", nullable = false)
    private Conversation conversation;
    @Column(name = "sender_id", nullable = false) private UUID senderId;
    @Column(length = 300, nullable = false) private String text;
    @Column(nullable = false) private boolean important;
    @Column(name = "created_at", nullable = false) private Instant createdAt = Instant.now();

    protected ChatMessage() {}
    public ChatMessage(Conversation c, UUID senderId, String text, boolean important) {
        this.conversation = c; this.senderId = senderId; this.text = text; this.important = important;
    }

    // ----------- Getter ------------------------------------------------
    public UUID getId()              { return id; }
    public Conversation getConversation() { return conversation; }
    public UUID getSenderId()        { return senderId; }
    public String getText()          { return text; }
    public boolean isImportant()     { return important; }
    public Instant getCreatedAt()    { return createdAt; }
}

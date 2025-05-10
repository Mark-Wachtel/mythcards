package chat;

import jakarta.persistence.*;
import java.time.Instant;
import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

@Entity
@Table(name = "conversation")
public class Conversation {

    @Id
    @GeneratedValue
    private UUID id;

    @Column(name = "is_group", nullable = false)
    private boolean group;

    private String name; // null for 1‑to‑1 chats

    @Column(name = "created_by", nullable = false)
    private UUID createdBy;

    @Column(name = "created_at", nullable = false)
    private Instant createdAt = Instant.now();

    @OneToMany(mappedBy = "conversation", cascade = CascadeType.ALL, orphanRemoval = true)
    private Set<ConversationParticipant> participants = new HashSet<>();

    // Constructors, getters, setters --------------------------------------------------
    protected Conversation() {}

    public Conversation(boolean group, String name, UUID createdBy) {
        this.group = group;
        this.name = name;
        this.createdBy = createdBy;
    }

    public UUID getId()               { return id; }
    public boolean isGroup()          { return group; }
    public String getName()           { return name; }
    public UUID getCreatedBy()        { return createdBy; }
    public Instant getCreatedAt()     { return createdAt; }
    public Set<ConversationParticipant> getParticipants() { return participants; }

    public void addParticipant(UUID userId, ConversationParticipant.Role role) {
        participants.add(new ConversationParticipant(this, userId, role));
    }
}
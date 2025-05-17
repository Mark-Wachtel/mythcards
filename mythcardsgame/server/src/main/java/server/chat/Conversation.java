package server.chat;

import jakarta.persistence.*;
import org.hibernate.annotations.GenericGenerator;
import java.time.Instant;
import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

@Entity
@Table(name = "conversation")
public class Conversation {

    @Id
    @GeneratedValue(generator = "UUID")
    @GenericGenerator(
        name = "UUID",
        strategy = "org.hibernate.id.UUIDGenerator"
    )
    @Column(name = "id", updatable = false, nullable = false, columnDefinition = "BINARY(16)")
    private UUID id;

    @Column(name = "is_group", nullable = false)
    private boolean group;

    @Column(name = "name")
    private String name;  // null bei 1-to-1

    @Column(name = "created_by", nullable = false, updatable = false)
    private UUID createdBy;

    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt = Instant.now();

    @OneToMany(
        mappedBy = "conversation",
        cascade = CascadeType.ALL,
        orphanRemoval = true
    )
    private Set<ConversationParticipant> participants = new HashSet<>();

    // ---------------- Constructors ----------------

    protected Conversation() {
        // for JPA
    }

    public Conversation(boolean group, String name, UUID createdBy) {
        this.group     = group;
        this.name      = name;
        this.createdBy = createdBy;
    }

    // ---------------- Getters ----------------

    public UUID getId() {
        return id;
    }

    public boolean isGroup() {
        return group;
    }

    public String getName() {
        return name;
    }

    public UUID getCreatedBy() {
        return createdBy;
    }

    public Instant getCreatedAt() {
        return createdAt;
    }

    public Set<ConversationParticipant> getParticipants() {
        return participants;
    }

    // ---------------- Convenience ----------------

    /**
     * Wie vorher in deiner alten Version:
     * Fügt einen Teilnehmer hinzu (erstellt intern das Participant-Objekt).
     */
    public void addParticipant(UUID userId, ConversationParticipant.Role role) {
        participants.add(new ConversationParticipant(this, userId, role));
    }

    /**
     * Fügt ein bereits existierendes Participant-Objekt hinzu.
     */
    public void addParticipant(ConversationParticipant participant) {
        participants.add(participant);
    }

    public void removeParticipant(ConversationParticipant participant) {
        participants.remove(participant);
    }
}
package server.chat;

import java.util.Optional;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;

public interface ParticipantStateRepository extends JpaRepository<ParticipantState, ParticipantState.Id> {
    Optional<ParticipantState> findById_ConversationIdAndId_UserId(UUID conversationId, UUID userId);
}
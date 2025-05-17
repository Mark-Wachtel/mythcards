package server.chat;

import java.util.List;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;

public interface ConversationParticipantRepository extends JpaRepository<ConversationParticipant, ConversationParticipantId> {
    List<ConversationParticipant> findByConversation_Id(UUID conversationId);
}
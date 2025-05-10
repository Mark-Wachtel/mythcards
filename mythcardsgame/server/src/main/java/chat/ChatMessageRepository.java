package chat;

import org.springframework.data.jpa.repository.*;
import java.time.Instant;
import java.util.*;

public interface ChatMessageRepository extends JpaRepository<ChatMessage, UUID> {

    @Query(value = "SELECT * FROM chat_message WHERE conversation_id = :convId AND created_at < :before ORDER BY created_at DESC LIMIT :limit", nativeQuery = true)
    List<ChatMessage> loadHistory(UUID convId, Instant before, int limit);

    @Modifying
    @Query(value = "UPDATE chat_message SET important = :important WHERE id = :msgId", nativeQuery = true)
    int updateImportant(UUID msgId, boolean important);

    /* ----  NEU  ---------------------------------------------------- */
    @Query(value = "SELECT COUNT(*) FROM chat_message m " +
                   "WHERE m.conversation_id = :convId " +
                   "  AND m.created_at  > :lastRead " +
                   "  AND m.sender_id  <> :userId", nativeQuery = true)
    int countUnread(UUID convId, UUID userId, Instant lastRead);

    @Modifying
    @Query(value = "DELETE FROM chat_message WHERE important = FALSE AND created_at < :cutoff", nativeQuery = true)
    int purgeOld(Instant cutoff);
}

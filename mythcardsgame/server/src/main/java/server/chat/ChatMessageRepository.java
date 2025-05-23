package server.chat;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;

import common.BadgeUpdateDTO;

public interface ChatMessageRepository extends JpaRepository<ChatMessage, UUID> {

    /* ===========================
     * History-Pagination
     * =========================== */
    @Query(value = """
        SELECT * 
          FROM chat_message 
         WHERE conversation_id = :convId 
           AND created_at       < :before 
         ORDER BY created_at DESC 
         LIMIT :limit
    """, nativeQuery = true)
    List<ChatMessage> loadHistory(UUID convId, Instant before, int limit);

    /* ===========================
     * Flag „important“
     * =========================== */
    @Modifying
    @Query(value = """
        UPDATE chat_message 
           SET important = :important 
         WHERE id = :msgId
    """, nativeQuery = true)
    int updateImportant(UUID msgId, boolean important);

    /* ===========================
     * Ungelesene Nachrichten – einzeln
     * =========================== */
    @Query(value = """
        SELECT COUNT(*) 
          FROM chat_message m
         WHERE m.conversation_id = :convId
           AND m.created_at      > :lastRead
           AND m.sender_id       <> :userId
    """, nativeQuery = true)
    int countUnread(UUID convId, UUID userId, Instant lastRead);

    /* ===========================
     * Datenbereinigung
     * =========================== */
    @Modifying
    @Query(value = """
        DELETE 
          FROM chat_message 
         WHERE important = FALSE 
           AND created_at < :cutoff
    """, nativeQuery = true)
    int purgeOld(Instant cutoff);

    /* ================================================================
     * NEU: Ungelesene Nachrichten pro Conversation für einen User
     * ================================================================ */
    @Query(value = """
        SELECT m.conversation_id          AS conversationId,
               COUNT(*)                   AS unreadCount
          FROM chat_message      m
          JOIN participant_state p
            ON p.conversation_id = m.conversation_id
           AND p.user_id         = :userId
         WHERE m.created_at  > COALESCE(p.last_read_at, TIMESTAMP '1970-01-01')
           AND m.sender_id   <> :userId
         GROUP BY m.conversation_id
    """, nativeQuery = true)
    List<BadgeUpdateDTO> unreadCounts(UUID userId);
}
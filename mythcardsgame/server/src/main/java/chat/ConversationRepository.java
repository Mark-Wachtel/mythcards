package chat;

import org.springframework.data.jpa.repository.*;
import java.util.*;

public interface ConversationRepository extends JpaRepository<Conversation, UUID> {
    @Query("select distinct c from Conversation c join c.participants p where p.id.userId = :userId")
    List<Conversation> findAllForUser(UUID userId);
}

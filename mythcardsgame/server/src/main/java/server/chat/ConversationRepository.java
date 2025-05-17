package server.chat;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface ConversationRepository extends JpaRepository<Conversation, UUID> {

	@Query("""
		    SELECT c
		      FROM Conversation c
		      JOIN c.participants p1
		      JOIN c.participants p2
		     WHERE c.group = false
		       AND ((p1.userId = :userA AND p2.userId = :userB)
		         OR (p1.userId = :userB AND p2.userId = :userA))
		     GROUP BY c.id
		     ORDER BY c.createdAt ASC
		""")
		List<Conversation> findOneToOneBothDirections(@Param("userA") UUID userA, @Param("userB") UUID userB);
	
    @Query("""
        SELECT DISTINCT c 
          FROM Conversation c 
          JOIN c.participants p 
         WHERE p.userId = :userId
    """)
    List<Conversation> findAllForUser(UUID userId);
}
package server;

import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;
import java.util.UUID;

public interface DeckRepository extends JpaRepository<DeckEntity, UUID> {
    List<DeckEntity> findAllByUserId(UUID userId);
}
package server;

import org.springframework.data.jpa.repository.JpaRepository;

public interface DeckCardRepository extends JpaRepository<DeckCardEntity, DeckCardKey> {
    // zusätzliche Abfragen falls nötig
}
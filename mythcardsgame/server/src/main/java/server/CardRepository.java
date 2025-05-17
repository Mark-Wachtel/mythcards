package server;

import java.util.Optional;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

public interface CardRepository extends JpaRepository<CardEntity, UUID> {

    @Query("""
        SELECT c FROM CardEntity c
        LEFT JOIN FETCH c.abilities
        WHERE c.id = :id
    """)
    Optional<CardEntity> findWithAbilities(UUID id);
}
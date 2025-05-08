package server;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

public interface CardRepository extends JpaRepository<CardEntity, Long> {

    @Query("""
        SELECT c FROM CardEntity c
        LEFT JOIN FETCH c.abilities
        WHERE c.id = :id
    """)
    Optional<CardEntity> findWithAbilities(Long id);
}

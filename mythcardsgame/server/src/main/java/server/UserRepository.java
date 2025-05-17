package server;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;
import java.util.UUID;

public interface UserRepository extends JpaRepository<UserEntity, UUID> {
    // ggf. Custom-Queries, z.B. findByUsername
	boolean existsByEmail(String email);
    Optional<UserEntity> findByUsername(String username);
}

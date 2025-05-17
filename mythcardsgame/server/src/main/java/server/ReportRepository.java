package server;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.UUID;

public interface ReportRepository extends JpaRepository<ReportEntity, UUID> {
    List<ReportEntity> findByTarget_Id(UUID targetId);
}
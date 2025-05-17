package server;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import common.PendingRequestDTO;

import java.util.List;
import java.util.UUID;

public interface FriendRequestRepository
extends JpaRepository<FriendRequestEntity, UUID> {

@Query("""
select new common.PendingRequestDTO(
           fr.id,
           s.id,
           s.username,
           fr.expiresAt
)
  from FriendRequestEntity fr
  join fr.sender s
 where fr.receiver.id = :receiverId
   and fr.status      = :status
""")
List<PendingRequestDTO> findPendingRequests(@Param("receiverId") UUID receiverId,
                                        @Param("status")     FriendRequestEntity.FriendRequestStatus status);
}
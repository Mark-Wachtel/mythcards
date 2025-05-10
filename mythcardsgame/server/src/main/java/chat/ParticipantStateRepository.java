package chat;

import org.springframework.data.jpa.repository.*;
import java.util.UUID;

public interface ParticipantStateRepository extends JpaRepository<ParticipantState, ParticipantState.Id> {
}
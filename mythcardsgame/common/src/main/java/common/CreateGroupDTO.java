package common;

import java.util.List;
import java.util.UUID;

public record CreateGroupDTO(
        String name,
        List<UUID> memberIds            /* ohne Ersteller */
) {}
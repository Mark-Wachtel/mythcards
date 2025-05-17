package server.chat;

import java.security.Principal;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import common.CreateConversationDTO;
import common.GroupCreatedDTO;

@RestController
@RequestMapping("/api/conversations")
public class ConversationController {

    private final ConversationService conversationService;

    @Autowired
    public ConversationController(ConversationService conversationService) {
        this.conversationService = conversationService;
    }

    @PostMapping
    public GroupCreatedDTO createConversation(@Validated @RequestBody CreateConversationDTO dto,
                                              Principal principal) {
        // Der Ersteller kommt aus dem JWT-Principal
        UUID creatorId = UUID.fromString(principal.getName());

        // Conversation anlegen
        Conversation conv = conversationService.createConversation(
            creatorId,
            dto.group(),
            dto.name(),
            dto.participantIds()
        );

        // Alle Teilnehmer‐IDs zusammenstellen
        List<UUID> memberIds = conv.getParticipants().stream()
            .map(ConversationParticipant::getUserId)
            .collect(Collectors.toList());

        // GroupCreatedDTO mit (conversationId, name, members)
        return new GroupCreatedDTO(
            conv.getId(),
            conv.getName(),
            memberIds
        );
    }
}
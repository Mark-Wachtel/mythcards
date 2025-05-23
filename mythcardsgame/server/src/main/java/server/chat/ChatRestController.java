package server.chat;

import common.ChatMessageDTO;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import java.security.Principal;
import java.time.Instant;
import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/chat")
public class ChatRestController {

    /* --------------------------------------------------
     * Dependencies
     * -------------------------------------------------- */
    private final ChatService            chatService;
    private final ConversationRepository convRepo;

    /* --------------------------------------------------
     * Constructor-Injection  (ohne Lombok)
     * -------------------------------------------------- */
    public ChatRestController(ChatService chatService,
                              ConversationRepository convRepo) {
        this.chatService = chatService;
        this.convRepo    = convRepo;
    }

    /* --------------------------------------------------
     * 1) Badge-Reset („read-ack“)
     * -------------------------------------------------- */
    @GetMapping("/readAck")
    public void ackRead(@RequestParam UUID convId,
                        Principal principal) {

        UUID userId = UUID.fromString(principal.getName());
        chatService.readAck(userId, convId);     // komplett im Service
    }

    /* --------------------------------------------------
     * 2) Chat-History Pagination
     * -------------------------------------------------- */
    @Transactional(readOnly = true)
    @GetMapping("/history")
    public List<ChatMessageDTO> history(@RequestParam UUID convId,
                                        @RequestParam
                                        @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME)
                                        Instant before,
                                        @RequestParam(defaultValue = "25") int size,
                                        Principal principal) {

        UUID userId = UUID.fromString(principal.getName());

        /* --- Autorisierung: ist User Teilnehmer? --- */
        Conversation conv = convRepo.findById(convId)
                                    .orElseThrow(() ->
                        new ResponseStatusException(HttpStatus.NOT_FOUND));

        boolean member = conv.getParticipants()
                             .stream()
                             .anyMatch(p -> p.getUserId().equals(userId));

        if (!member) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN);
        }

        /* --- Daten abrufen --- */
        return chatService.loadHistory(convId, before, size);
    }
}
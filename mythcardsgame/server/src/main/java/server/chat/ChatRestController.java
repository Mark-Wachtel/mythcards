package server.chat;

import org.springframework.http.HttpStatus;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import common.ChatMessageDTO;

import org.springframework.format.annotation.DateTimeFormat;

import server.UserEntity;
import server.UserRepository;

import java.security.Principal;
import java.time.Instant;
import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/chat")
public class ChatRestController {

    private final ChatMessageRepository repo;
    private final ChatService chatService;
    private final ParticipantStateRepository stateRepo;
    private final ConversationRepository convRepo;
    private final UserRepository userRepo;    // << neu!

    public ChatRestController(ChatMessageRepository repo,
                              ParticipantStateRepository stateRepo,
                              ConversationRepository convRepo,
                              UserRepository userRepo,
                              ChatService chatService) {
        this.repo      = repo;
        this.stateRepo = stateRepo;
        this.convRepo  = convRepo;
        this.userRepo  = userRepo;
        this.chatService = chatService;
    }

    @GetMapping("/readAck")
    public void ackRead(@RequestParam UUID convId,
                        Principal principal) {
        UUID userId = UUID.fromString(principal.getName());
        Conversation conv = convRepo.findById(convId)
            .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND));

        UserEntity user = userRepo.findById(userId)
            .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND));

        ParticipantState.Id key = new ParticipantState.Id(convId, userId);
        ParticipantState st = stateRepo.findById(key)
            .orElseGet(() -> new ParticipantState(conv, user));

        st.setLastReadAt(Instant.now());
        stateRepo.save(st);
    }
    
    @Transactional(readOnly = true)
    @GetMapping("/history")
    public List<ChatMessageDTO> history(
            @RequestParam UUID convId,

            // ❶ optional machen  – Spring setzt dann bei fehlendem Parameter „null“
            @RequestParam(name = "before", required = false)
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

        /* --- Fallback, falls „before“ fehlt oder aus der Zukunft kommt --- */
        Instant cursor = (before == null || before.isAfter(Instant.now()))
                         ? Instant.now()
                         : before;

     
		return chatService.loadHistory(convId, cursor, size);
    }
}
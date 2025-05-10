package chat;

import org.springframework.web.bind.annotation.*;

import java.security.Principal;
import java.time.Instant;
import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/chat")
public class ChatRestController {

    private final ChatMessageRepository repo;
    private final ParticipantStateRepository stateRepo;
    private final ConversationRepository convRepo;

    public ChatRestController(ChatMessageRepository repo,
                              ParticipantStateRepository stateRepo,
                              ConversationRepository convRepo) {
        this.repo      = repo;
        this.stateRepo = stateRepo;
        this.convRepo  = convRepo;
    }

    /* -------- Verlauf (Pagination) -------- */
    @GetMapping("/history")
    public List<ChatMessage> history(@RequestParam UUID convId,
                                     @RequestParam(required = false) Instant before,
                                     @RequestParam(defaultValue = "25") int size) {
        return repo.loadHistory(convId,
                before == null ? Instant.now() : before,
                size);
    }

    /* -------- Lese-ACK für Badge-Reset -------- */
    @GetMapping("/readAck")
    public void ackRead(@RequestParam UUID convId,
                        Principal principal) {

        UUID uid = UUID.fromString(principal.getName());
        ParticipantState.Id key = new ParticipantState.Id(convId, uid);

        ParticipantState st = stateRepo.findById(key)
                .orElseGet(() -> new ParticipantState(
                        convRepo.getOne(convId), uid));

        st.setLastReadAt(Instant.now());
        stateRepo.save(st);
    }
}
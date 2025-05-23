package server.chat;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import common.BadgeUpdateDTO;
import common.ChatMessageDTO;
import common.ImportantUpdateDTO;
import io.github.bucket4j.*;
import server.UserEntity;
import server.UserRepository;

import java.time.*;
import java.util.List;
import java.util.UUID;

@Service
public class ChatService {

    private final ConversationRepository convRepo;
    private final ChatMessageRepository  msgRepo;
    private final ParticipantStateRepository stateRepo;
    private final UserRepository userRepo;  
    private final SimpMessagingTemplate broker;
    private final Bucket bucket;

    @Autowired
    public ChatService(ConversationRepository convRepo,
            ChatMessageRepository msgRepo,
            ParticipantStateRepository stateRepo,
            UserRepository userRepo,            // ② ctor-param
            SimpMessagingTemplate broker) {
    	this.convRepo  = convRepo;
    	this.msgRepo   = msgRepo;
    	this.stateRepo = stateRepo;
    	this.userRepo  = userRepo;                        // ③ Merk dir!
    	this.broker    = broker;
        this.bucket   = Bucket.builder()
                .addLimit(Bandwidth.simple(20, Duration.ofSeconds(10)))
                .build();
    }

    @Transactional
    public ChatMessageDTO send(UUID sender, UUID convId, String text, boolean important) {
        if (!bucket.tryConsume(1)) throw new RateLimitException();
        if (text.length() > 300)  throw new IllegalArgumentException("Text too long");

        Conversation conv = convRepo.findById(convId).orElseThrow();
        ChatMessage msg   = msgRepo.save(new ChatMessage(conv, sender, text, important));

        ChatMessageDTO dto = new ChatMessageDTO(convId, msg.getId(), sender,
                                                msg.getCreatedAt(), text, important);
        broker.convertAndSend("/topic/conversation/" + convId, dto);

        /* -------- Badge‑Update für alle anderen Teilnehmer -------- */
        for (ConversationParticipant cp : conv.getParticipants()) {
            UUID target = cp.getUserId();
            if (target.equals(sender)) continue; // Sender selbst überspringen
            // letzen Lesezeitpunkt holen
            ParticipantState st = stateRepo.findById(new ParticipantState.Id(convId, target)).orElse(null);
            Instant last = st != null ? st.getLastReadAt() : Instant.EPOCH;
            int unread = msgRepo.countUnread(convId, target, last);
            broker.convertAndSend("/user/" + target + "/queue/badge",
                                  new BadgeUpdateDTO(convId, unread));
        }
        return dto;
    }

    /**
     * Wichtig‑Flag umschalten.
     */
    @Transactional
    public void markImportant(UUID userId, UUID messageId, boolean important) {
        ChatMessage msg = msgRepo.findById(messageId).orElseThrow();
        Conversation conv = msg.getConversation();

        boolean allowed = conv.getParticipants().stream()
                .anyMatch(p -> p.getUserId().equals(userId));
        if (!allowed) throw new IllegalStateException("not participant");

        msgRepo.updateImportant(messageId, important);
        ImportantUpdateDTO upd = new ImportantUpdateDTO(conv.getId(), messageId, important);
        broker.convertAndSend("/topic/conversation/" + conv.getId(), upd);
    }
    
    @Transactional
    public void readAck(UUID userId, UUID convId) {

        Conversation conv = convRepo.findById(convId).orElseThrow();

        /* State holen oder lazily anlegen */
        ParticipantState state = stateRepo.findById(new ParticipantState.Id(convId, userId))
            .orElseGet(() -> {
                UserEntity user = userRepo.getReferenceById(userId);   // <<< ersetzt getUser()
                return new ParticipantState(conv, user);
            });

        state.setLastReadAt(Instant.now());
        stateRepo.save(state);

        /* Badge zurücksetzen – an **alle** Tabs des Users pushen */
        broker.convertAndSend("/user/" + userId + "/queue/badge",
                              new BadgeUpdateDTO(convId, 0));
    }
    
    @Transactional(readOnly = true)
    public List<ChatMessageDTO> loadHistory(UUID convId,
                                            Instant before,
                                            int size) {
        return msgRepo.loadHistory(convId, before, size)
                      .stream()
                      .map(ChatMapper::toDTO)
                      .toList();
    }
}
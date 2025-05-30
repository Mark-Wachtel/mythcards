package server.chat;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.lang.Nullable;
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

import java.time.Duration;
import java.time.Instant;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.messaging.MessageHeaders;
import org.springframework.messaging.simp.SimpMessageHeaderAccessor;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;

import io.github.bucket4j.Bandwidth;
import io.github.bucket4j.Bucket;

@Service
public class ChatService {

    /* ---------- Konstanten für Ziele ---------- */
    private static final String TOPIC_CONVERSATION = "/topic/conversation.";
    private static final String QUEUE_BADGE        = "/queue/badge";

    /* ---------- Repositories & Broker ---------- */
    private final ConversationRepository      convRepo;
    private final ChatMessageRepository       msgRepo;
    private final ParticipantStateRepository  stateRepo;
    private final UserRepository              userRepo;
    private final SimpMessagingTemplate       broker;

    /* ---------- Rate-Limit: pro User Bucket ---------- */
    private final Map<UUID, Bucket> buckets = new ConcurrentHashMap<>();

    @Autowired
    public ChatService(ConversationRepository convRepo,
                       ChatMessageRepository msgRepo,
                       ParticipantStateRepository stateRepo,
                       UserRepository userRepo,
                       SimpMessagingTemplate broker) {
        this.convRepo  = convRepo;
        this.msgRepo   = msgRepo;
        this.stateRepo = stateRepo;
        this.userRepo  = userRepo;
        this.broker    = broker;
    }

    /* ---------------------------------- */
    /*         Nachricht senden           */
    /* ---------------------------------- */
    @Transactional
    public ChatMessageDTO send(UUID sender, UUID convId, String text, boolean important) {

        /* ---- Rate-Limit check (pro Benutzer) ---- */
        Bucket bucket = buckets.computeIfAbsent(sender, __ ->
                Bucket.builder()
                        .addLimit(Bandwidth.simple(20, Duration.ofSeconds(10)))
                        .build());
        if (!bucket.tryConsume(1)) {
            throw new RateLimitException();
        }

        if (text.length() > 300) {
            throw new IllegalArgumentException("Text too long");
        }

        /* ---- Berechtigungsprüfung ---------------- */
        Conversation conv = convRepo.findById(convId).orElseThrow();
        boolean participant = conv.getParticipants().stream()
                                  .anyMatch(p -> p.getUserId().equals(sender));
        if (!participant) {
            throw new IllegalStateException("Sender is not participant of this conversation");
        }

        /* ---- Nachricht speichern ---------------- */
        ChatMessage msg = msgRepo.save(new ChatMessage(conv, sender, text, important));

        ChatMessageDTO dto = new ChatMessageDTO(convId, msg.getId(), sender,
                                                msg.getCreatedAt(), text, important);

        /* ---- Broadcast an Topic ----------------- */
        broker.convertAndSend(TOPIC_CONVERSATION + convId, dto);

        /* ---- Badge-Updates für andere Teilnehmer  */
     // 1) HeaderAccessor anlegen
        SimpMessageHeaderAccessor headerAccessor = SimpMessageHeaderAccessor.create();

        // 2) Header setzen
        headerAccessor.setHeader("persistent", Boolean.TRUE);

        // 3) Fertige MessageHeaders holen
        MessageHeaders persistentHeaders = headerAccessor.getMessageHeaders();

        for (ConversationParticipant cp : conv.getParticipants()) {
            UUID target = cp.getUserId();
            if (target.equals(sender)) {
                continue; // Sender selbst überspringen
            }

            ParticipantState st   = stateRepo.findById(new ParticipantState.Id(convId, target))
                                             .orElse(null);
            Instant lastRead      = st != null ? st.getLastReadAt() : Instant.EPOCH;
            int unread            = msgRepo.countUnread(convId, target, lastRead);

            broker.convertAndSendToUser(target.toString(),
                                        QUEUE_BADGE,
                                        new BadgeUpdateDTO(convId, unread),
                                        persistentHeaders);
        }

        return dto;
    }

    /* ---------------------------------- */
    /*         Wichtig-Flag ändern        */
    /* ---------------------------------- */
    @Transactional
    public void markImportant(UUID userId, UUID messageId, boolean important) {

        ChatMessage msg = msgRepo.findById(messageId).orElseThrow();
        Conversation conv = msg.getConversation();

        boolean allowed = conv.getParticipants().stream()
                              .anyMatch(p -> p.getUserId().equals(userId));
        if (!allowed) {
            throw new IllegalStateException("not participant");
        }

        msgRepo.updateImportant(messageId, important);

        ImportantUpdateDTO upd = new ImportantUpdateDTO(conv.getId(), messageId, important);
        broker.convertAndSend(TOPIC_CONVERSATION + conv.getId(), upd);
    }

    /* ---------------------------------- */
    /*         Lese-Bestätigung           */
    /* ---------------------------------- */
    @Transactional
    public void readAck(UUID userId, UUID convId) {

        Conversation conv = convRepo.findById(convId).orElseThrow();

        ParticipantState state = stateRepo.findById(new ParticipantState.Id(convId, userId))
                .orElseGet(() -> {
                    UserEntity user = userRepo.getReferenceById(userId);
                    return new ParticipantState(conv, user);
                });

        state.setLastReadAt(Instant.now());
        stateRepo.save(state);

        /* Badge zurücksetzen – an alle Tabs (Sessions) des Users pushen */
        broker.convertAndSendToUser(userId.toString(),
                                    QUEUE_BADGE,
                                    new BadgeUpdateDTO(convId, 0));
    }

    /* ---------------------------------- */
    /*         Verlauf laden              */
    /* ---------------------------------- */
    public List<ChatMessageDTO> loadHistory(UUID convId,
            Instant before,
            int size) {
return msgRepo.loadHistory(convId, before, size)
.stream()
.map(ChatMapper::toDTO)
.toList();
}
    
    public int countUnreadMessages(UUID conversationId, UUID userId) {
        // Hole lastReadAt für den User in dieser Konversation
    	Instant lastReadAt = stateRepo
            .findByConversationIdAndUserId(conversationId, userId)
            .map(ParticipantState::getLastReadAt)
            .orElse(Instant.EPOCH);

        return msgRepo
            .countByConversationIdAndCreatedAtAfter(conversationId, lastReadAt);
    }
    }

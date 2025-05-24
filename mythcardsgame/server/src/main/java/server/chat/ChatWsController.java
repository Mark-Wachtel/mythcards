package server.chat;

																											import java.security.Principal;
import java.time.Duration;
import java.util.UUID;

import org.springframework.messaging.MessageHeaders;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.messaging.simp.SimpMessageHeaderAccessor;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Controller;

import common.ChatMessageDTO;
import common.CreateGroupDTO;
import common.GroupCreatedDTO;
import common.ImportantUpdateDTO;

@Controller
public class ChatWsController {

    /* ---------- Konstanten ---------- */
    private static final String QUEUE_CONVERSATION = "/queue/conversation";

    /* ---------- Abhängigkeiten ---------- */
    private final ChatService          chatService;
    private final ConversationService  conversationService;
    private final SimpMessagingTemplate broker;

    public ChatWsController(ChatService chatService,
                            ConversationService conversationService,
                            SimpMessagingTemplate broker) {
        this.chatService         = chatService;
        this.conversationService = conversationService;
        this.broker              = broker;
    }

    /* ---------------- Nachricht senden ---------------- */
    @MessageMapping("/chat.send")
    public void handleSend(@Payload ChatMessageDTO dto,
                           Principal principal) {

        chatService.send(
                UUID.fromString(principal.getName()),
                dto.conversationId(),
                dto.text(),
                dto.important()
        );
    }

    /* -------------- Wichtig markieren ----------------- */
    @MessageMapping("/chat.markImportant")
    public void handleImportant(@Payload ImportantUpdateDTO dto,
                                Principal principal) {

        chatService.markImportant(
                UUID.fromString(principal.getName()),
                dto.messageId(),
                dto.important()
        );
    }

    /* ---------------- Gruppe anlegen ------------------ */
    @MessageMapping("/chat.createGroup")
    public void createGroup(@Payload CreateGroupDTO dto,
                            Principal principal) {

        UUID creator = UUID.fromString(principal.getName());

        Conversation conv = conversationService.createConversation(
                creator,
                true,               // group = true
                dto.name(),
                dto.memberIds()
        );

        /* Persistente Header (Offline-Zustellung ermöglichen) */
     // 1) HeaderAccessor anlegen
        SimpMessageHeaderAccessor headerAccessor = SimpMessageHeaderAccessor.create();

        // 2) Header setzen
        headerAccessor.setHeader("persistent", Boolean.TRUE);

        // 3) Fertige MessageHeaders holen
        MessageHeaders persistentHeaders = headerAccessor.getMessageHeaders();

        /* Push „Neue Gruppe“ an alle Mitglieder */
        GroupCreatedDTO out = new GroupCreatedDTO(
                conv.getId(),
                conv.getName(),
                dto.memberIds()
        );

        conv.getParticipants().forEach(cp ->
            broker.convertAndSendToUser(
                    cp.getUserId().toString(),
                    QUEUE_CONVERSATION,
                    out,
                    persistentHeaders
            )
        );
    }
}
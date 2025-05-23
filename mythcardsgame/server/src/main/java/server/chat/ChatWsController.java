package server.chat;

																												import org.springframework.messaging.handler.annotation.*;
																												import org.springframework.stereotype.Controller;
																												import org.springframework.messaging.simp.SimpMessagingTemplate; 
																												import common.ChatMessageDTO;
																												import common.CreateGroupDTO;
																												import common.GroupCreatedDTO;
																												import common.ImportantUpdateDTO;
																												
																												import java.security.Principal; 
																												import java.util.UUID;
																												
																												@Controller
																												public class ChatWsController {
																												
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
																												
																												    /* ------------- Nachricht senden ---------------- */
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
																												
																												    /* ------------- Wichtig markieren ---------------- */
																												    @MessageMapping("/chat.markImportant")
																												    public void handleImportant(@Payload ImportantUpdateDTO dto,
																												                                Principal principal) {
																												
																												        chatService.markImportant(
																												                UUID.fromString(principal.getName()),
																												                dto.messageId(),
																												                dto.important()
																												        );
																												    }
																												
																												    /* ------------- Gruppe anlegen ------------------- */
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
																												
																												        /* Push „Neue Gruppe“ an alle Mitglieder */
																												        GroupCreatedDTO out = new GroupCreatedDTO(
																												                conv.getId(),
																												                conv.getName(),
																												                dto.memberIds()
																												        );
																												
																												        conv.getParticipants().forEach(cp ->
																												            broker.convertAndSend(
																												                "/user/" + cp.getUserId() + "/queue/conversation",
																												                out
																												            )
																												        );
																												    }
																												}
package server.chat;

import java.util.UUID;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.messaging.Message;
import org.springframework.messaging.MessageChannel;
import org.springframework.messaging.simp.stomp.StompCommand;
import org.springframework.messaging.simp.stomp.StompHeaderAccessor;
import org.springframework.messaging.support.ChannelInterceptor;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Component;

@Component
public class SubscribeAuthInterceptor implements ChannelInterceptor {

    @Autowired ConversationRepository repo;

    @Override
    public Message<?> preSend(Message<?> msg, MessageChannel ch) {
        StompHeaderAccessor h = StompHeaderAccessor.wrap(msg);
        if (StompCommand.SUBSCRIBE.equals(h.getCommand())) {
            String dest = h.getDestination();     // /topic/conversation.{id}
            if (dest != null && dest.startsWith("/topic/conversation.")) {
                UUID convId = UUID.fromString(dest.substring(dest.lastIndexOf('.')+1));
                UUID uid    = UUID.fromString(h.getUser().getName());
                boolean ok  = repo.findById(convId)
                        .map(c -> c.getParticipants().stream()
                                   .anyMatch(p -> p.getUserId().equals(uid)))
                        .orElse(false);
                if (!ok)
                    throw new AccessDeniedException("Not a participant");
            }
        }
        return msg;
    }
}
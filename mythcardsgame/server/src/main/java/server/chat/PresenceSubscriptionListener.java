package server.chat;

import java.security.Principal;
import java.util.UUID;

import org.springframework.context.event.EventListener;
import org.springframework.messaging.simp.stomp.StompHeaderAccessor;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.messaging.SessionSubscribeEvent;

@Component
public class PresenceSubscriptionListener {

    private final PresenceService presenceService;

    public PresenceSubscriptionListener(PresenceService presenceService) {
        this.presenceService = presenceService;
    }

    @EventListener
    public void onSubscribe(SessionSubscribeEvent evt) {

        StompHeaderAccessor acc = StompHeaderAccessor.wrap(evt.getMessage());
        String dest = acc.getDestination();
        Principal user = acc.getUser();

        if (user != null && "/user/queue/friendsPresence".equals(dest)) {
            UUID uid = UUID.fromString(user.getName());
            presenceService.sendInitialPresence(uid);   // JETZT ist die Session registriert
        }
    }
}
package server.chat;

import org.springframework.context.event.EventListener;
import org.springframework.messaging.simp.stomp.StompHeaderAccessor;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.messaging.SessionSubscribeEvent;

import java.security.Principal;
import java.util.UUID;

@Component
public class PresenceSubscriptionListener {

	private final PresenceService presenceService;

    public PresenceSubscriptionListener(PresenceService presenceService) {
        this.presenceService = presenceService;
    }

    @EventListener
    public void onSubscribe(SessionSubscribeEvent evt) {

        StompHeaderAccessor acc  = StompHeaderAccessor.wrap(evt.getMessage());
        Principal           user = acc.getUser();

        /* Freunde-Presence wird genau auf dieses Ziel abonniert */
        if (user != null && "/user/queue/friendsPresence".equals(acc.getDestination())) {
            UUID uid = UUID.fromString(user.getName());
            presenceService.handleSubscribe(uid);      // <-- NEU
        }
    }
}
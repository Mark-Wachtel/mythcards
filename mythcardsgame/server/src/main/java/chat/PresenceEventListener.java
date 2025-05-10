package chat;

import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.messaging.*;
import org.springframework.messaging.simp.stomp.StompHeaderAccessor;

import java.security.Principal;
import java.util.UUID;

/**
 * Lauscht auf WebSocket‑Verbindungs‑Events und meldet Freunde online/offline.
 */
@Component
public class PresenceEventListener {

    private final PresenceService presence;
    public PresenceEventListener(PresenceService presence) { this.presence = presence; }

    @EventListener
    public void onSessionConnect(SessionConnectedEvent ev) {
        Principal p = StompHeaderAccessor.wrap(ev.getMessage()).getUser();
        if (p != null) presence.setOnline(UUID.fromString(p.getName()));
    }

    @EventListener
    public void onSessionDisconnect(SessionDisconnectEvent ev) {
        Principal p = ev.getUser();
        if (p != null) presence.setOffline(UUID.fromString(p.getName()));
    }
}

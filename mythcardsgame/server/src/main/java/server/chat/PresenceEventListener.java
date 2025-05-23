package server.chat;

import org.springframework.stereotype.Component;

import java.security.Principal;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.event.EventListener;
import org.springframework.messaging.simp.stomp.StompHeaderAccessor;
import org.springframework.web.socket.messaging.SessionConnectedEvent;
import org.springframework.web.socket.messaging.SessionDisconnectEvent;

/**
 * Lauscht auf WebSocket-Verbindungs-Events und meldet Freunde online/offline.
 * <p>
 * Ein User kann mehrere Browser-Tabs/Fenster (also mehrere WebSocket-Sessions)
 * offen haben.  Erst wenn die <b>erste</b> Session connectet, gilt er als
 * online; erst nach der <b>letzten</b> Session-Disconnect gilt er wieder als
 * offline.
 */
@Component
public class PresenceEventListener {

    /** Zählt aktuell offene WS-Sessions pro Benutzer */
    private final ConcurrentMap<UUID, Integer> sessionsPerUser = new ConcurrentHashMap<>();

    private final PresenceService presence;

    @Autowired
    public PresenceEventListener(PresenceService presence) {
        this.presence = presence;
    }

    /* ---------------------------------------------------- */

    @EventListener
    public void onSessionConnect(SessionConnectedEvent event) {

        Principal principal = StompHeaderAccessor.wrap(event.getMessage()).getUser();
        if (principal == null) {
            return;                             // sollte nie vorkommen
        }

        UUID userId = UUID.fromString(principal.getName());

        // Zähler hochsetzen (Thread-sicher)
        int count = sessionsPerUser.merge(userId, 1, Integer::sum);

        // War vorher 0 → User ist gerade frisch online
        if (count == 1) {
            presence.setOnline(userId);
        }
    }

    @EventListener
    public void onSessionDisconnect(SessionDisconnectEvent event) {

        Principal principal = event.getUser();
        if (principal == null) {
            return;
        }

        UUID userId = UUID.fromString(principal.getName());

        // Zähler runtersetzen
        sessionsPerUser.computeIfPresent(userId, (id, cnt) -> cnt > 1 ? cnt - 1 : null);

        // Wurde dabei der letzte Tab geschlossen?
        if (!sessionsPerUser.containsKey(userId)) {
            presence.setOffline(userId);
        }
    }
}
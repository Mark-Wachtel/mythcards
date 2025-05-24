package server.chat;

import java.security.Principal;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.event.EventListener;
import org.springframework.messaging.simp.stomp.StompHeaderAccessor;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.messaging.SessionConnectedEvent;
import org.springframework.web.socket.messaging.SessionDisconnectEvent;

/**
 * Lauscht auf WebSocket-Session-Events und meldet Freunde online / offline.
 *
 * Ein User kann mehrere Tabs (WebSocket-Sessions) gleichzeitig besitzen:
 *  • Erste Session verbindet → User gilt als ONLINE.<br>
 *  • Letzte Session trennt   → User gilt als OFFLINE.
 *
 * Zusätzlich wird bei <u>jeder</u> neuen Session der Initial-Presence-Snapshot
 * der Freundesliste an genau diesen Benutzer gepusht.
 */
@Component
public class PresenceEventListener {

    /** Offene WebSocket-Sessions pro Benutzer (thread-sicher) */
    private final ConcurrentMap<UUID, Integer> sessionsPerUser = new ConcurrentHashMap<>();

    private final PresenceService presence;

    @Autowired
    public PresenceEventListener(PresenceService presence) {
        this.presence = presence;
    }

    /* ----------------------- CONNECT ----------------------- */
    @EventListener
    public void onSessionConnect(SessionConnectedEvent event) {

        Principal principal = StompHeaderAccessor.wrap(event.getMessage()).getUser();
        if (principal == null) return;                     // sollte eigentlich nie passieren

        UUID userId = UUID.fromString(principal.getName());

        /* Tab-Zähler ++ */
        int count = sessionsPerUser.merge(userId, 1, Integer::sum);

        /* → Erster Tab? Dann als ONLINE melden */
        if (count == 1) {
            presence.setOnline(userId);
        }
    }

    /* ---------------------- DISCONNECT --------------------- */
    @EventListener
    public void onSessionDisconnect(SessionDisconnectEvent event) {

        Principal principal = event.getUser();
        if (principal == null) return;

        UUID userId = UUID.fromString(principal.getName());

        /* Tab-Zähler --   (wenn 0 → entfernen) */
        sessionsPerUser.computeIfPresent(userId, (id, cnt) -> cnt > 1 ? cnt - 1 : null);

        /* War das der letzte Tab? Dann als OFFLINE melden */
        if (!sessionsPerUser.containsKey(userId)) {
            presence.setOffline(userId);
        }
    }
}
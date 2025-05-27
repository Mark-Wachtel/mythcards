package server.chat;

import common.PresenceDTO;
import common.PresenceInitDTO;
import server.FriendService;

import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

@Service
public class PresenceService {

    private static final String QUEUE_FRIENDS_PRESENCE = "/queue/friendsPresence";

    private final SimpMessagingTemplate broker;
    private final FriendService         friendService;

    /** aktive WS-Sessions pro Benutzer */
    private final ConcurrentHashMap<UUID, Integer> sessionCounter = new ConcurrentHashMap<>();

    public PresenceService(SimpMessagingTemplate broker,
                           FriendService friendService) {
        this.broker        = broker;
        this.friendService = friendService;
    }

    /* ===========================================================
       Aufgerufen vom PresenceEventListener,
       sobald der Client  /user/queue/friendsPresence  ABONNIERT.
       =========================================================== */
    public void handleSubscribe(UUID userId) {

        /* -------- 1) Benutzer sofort als online markieren -------- */
        int newCount = sessionCounter.merge(userId, 1, (oldCnt, one) -> oldCnt + 1);

        /* -------- 2) Init-Liste erstellen und an Client schicken -------- */
        Set<UUID> friends = friendService.listFriends(userId);     // nur 1× aus DB/Cache holen
        List<UUID> onlineFriends = sessionCounter.keySet()
                                                 .stream()
                                                 .filter(friends::contains)
                                                 .toList();

        broker.convertAndSendToUser(
                userId.toString(),
                QUEUE_FRIENDS_PRESENCE,
                new PresenceInitDTO(onlineFriends)
        );

        /* -------- 3) Beim ERSTEN Connect an alle Freunde broadcasten -------- */
        if (newCount == 1) {
            broadcastToFriends(userId, true);
        }
    }

    /* ===========================================================
       Aufgerufen vom PresenceEventListener,
       wenn eine WS-Session des Benutzers geschlossen wurde.
       =========================================================== */
    public void handleDisconnect(UUID userId) {

        sessionCounter.computeIfPresent(userId,
                (id, cnt) -> cnt > 1 ? cnt - 1 : null);

        if (!sessionCounter.containsKey(userId)) {
            broadcastToFriends(userId, false);
        }
    }

    /* ====================== HILFSMETHODEN ===================== */

    /** true ⇒ Benutzer hat ≥1 offene Sessions */
    public boolean isOnline(UUID userId) {
        return sessionCounter.containsKey(userId);
    }

    /** an alle bestätigten Freunde *dieses* Users schicken */
    private void broadcastToFriends(UUID userId, boolean online) {

        PresenceDTO dto = new PresenceDTO(userId, online);

        for (UUID friendId : friendService.listFriends(userId)) {
            if (sessionCounter.containsKey(friendId)) {   // nur zustellen, wenn Freund online
                broker.convertAndSendToUser(
                        friendId.toString(),
                        QUEUE_FRIENDS_PRESENCE,
                        dto
                );
            }
        }
    }
}
package server.chat;

import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;

import common.PresenceDTO;
import common.PresenceInitDTO;
import server.FriendService;

import java.util.Set;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

@Service
public class PresenceService {

    private static final String QUEUE_FRIENDS_PRESENCE = "/queue/friendsPresence";

    private final SimpMessagingTemplate broker;
    private final FriendService friendService;

    /** Hält pro User-Id die Zahl aktiver WS-Sessions. */
    private final ConcurrentHashMap<UUID, Integer> onlineUsers = new ConcurrentHashMap<>();

    public PresenceService(SimpMessagingTemplate broker,
                           FriendService friendService) {
        this.broker        = broker;
        this.friendService = friendService;
    }

    /* ---------- Event von PresenceEventListener ---------- */
    public void setOnline(UUID userId) {
        onlineUsers.merge(userId, 1, Integer::sum);
        if (onlineUsers.get(userId) == 1) {        // erster Connect → Broadcast
            broadcastToFriends(userId, true);
        }
    }

    public void setOffline(UUID userId) {
        onlineUsers.computeIfPresent(userId, (id, cnt) ->
                cnt > 1 ? cnt - 1 : null);        // letzte Session weg?
        if (!onlineUsers.containsKey(userId)) {
            broadcastToFriends(userId, false);
        }
    }

    /* ---------- Nur Freunde informieren ---------- */
    private void broadcastToFriends(UUID userId, boolean online) {
        PresenceDTO dto = new PresenceDTO(userId, online);

        // Alle bestätigten Freunde ermitteln
        for (UUID friendId : friendService.listFriends(userId)) {
            if (onlineUsers.containsKey(friendId)) {          // Freund selbst online?
                broker.convertAndSendToUser(
                        friendId.toString(),
                        QUEUE_FRIENDS_PRESENCE,
                        dto
                );
            }
        }
    }
    
    public boolean isOnline(UUID userId) {
        return onlineUsers.containsKey(userId);
    }

    /* ---------- Initial-Sync für neu verbundene Session ---------- */
    public void sendInitialPresence(UUID userId) {

        PresenceInitDTO init = new PresenceInitDTO(
                onlineUsers.keySet()                  // derzeit online
                           .stream()
                           .filter(id -> friendService.listFriends(userId).contains(id))
                           .toList()
        );

        broker.convertAndSendToUser(
                userId.toString(),
                QUEUE_FRIENDS_PRESENCE,
                init
        );
    }
}
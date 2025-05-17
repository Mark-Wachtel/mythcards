package server.chat;

import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;

import common.PresenceDTO;

import java.util.Set;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

@Service
public class PresenceService {
    private final Set<UUID> onlineUsers = ConcurrentHashMap.newKeySet();
    private final SimpMessagingTemplate broker;

    public PresenceService(SimpMessagingTemplate broker) { this.broker = broker; }

    public void setOnline(UUID userId) {
        onlineUsers.add(userId);
        broadcast(userId, "ONLINE");
    }

    public void setOffline(UUID userId) {
        onlineUsers.remove(userId);
        broadcast(userId, "OFFLINE");
    }

    private void broadcast(UUID userId, String status) {
        PresenceDTO dto = new PresenceDTO(userId, status);
        broker.convertAndSend("/user/queue/presence", dto);
    }

    public boolean isOnline(UUID id) { return onlineUsers.contains(id); }
}
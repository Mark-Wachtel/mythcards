package chat;

import org.springframework.beans.factory.annotation.Autowired; 
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import server.FriendService;

import java.util.*;

@Service
public class ConversationService {
    private final ConversationRepository convRepo; private final FriendService friendService;
    @Autowired public ConversationService(ConversationRepository c, FriendService f) { this.convRepo = c; this.friendService = f; }

    @Transactional
    public Conversation createConversation(UUID creator, boolean group, String name, List<UUID> peers) {
        if (group && peers.size() > 9) throw new IllegalArgumentException("Max 10 participants");
        for (UUID id : peers) if (!friendService.areFriends(creator, id)) throw new IllegalStateException("Not all friends");
        Conversation conv = new Conversation(group, name, creator);
        conv.addParticipant(creator, ConversationParticipant.Role.OWNER);
        peers.forEach(id -> conv.addParticipant(id, ConversationParticipant.Role.MEMBER));
        return convRepo.save(conv);
    }
}
package server;

import common.FriendDTO;
import common.PendingRequestDTO;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import server.chat.Conversation;
import server.chat.ConversationRepository;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

/**
 * Zentrale Geschäftslogik rund um Freundschaften & Anfragen.
 * <p>
 *  – Constructor-Injection (kein Lombok, kein {@code @Autowired} an Feldern)  
 *  – Alle öffentlichen Methoden sind transaktional markiert  
 *  – DTO-Projection für {@code getPendingRequests()}, dadurch keine Lazy-Proxies
 */
@Service
public class FriendService {

    /* ----------------------------------------------------------
       Dependencies
     ---------------------------------------------------------- */

    private final FriendshipRepository friendshipRepository;
    private final FriendRequestRepository requestRepository;
    private final UserRepository userRepository;
    private final BlockRepository blockRepository;
    private final ReportRepository reportRepository;
    private final ConversationRepository conversationRepo;

    /* ----------------------------------------------------------
       Constructor – Spring injiziert automatisch
     ---------------------------------------------------------- */
    public FriendService(FriendshipRepository friendshipRepository,
                         FriendRequestRepository requestRepository,
                         UserRepository userRepository,
                         BlockRepository blockRepository,
                         ReportRepository reportRepository,
                         ConversationRepository conversationRepo) {

        this.friendshipRepository = friendshipRepository;
        this.requestRepository    = requestRepository;
        this.userRepository       = userRepository;
        this.blockRepository      = blockRepository;
        this.reportRepository     = reportRepository;
        this.conversationRepo     = conversationRepo;
    }

    /* ==========================================================
       READ-METHODS
       ========================================================== */

    /**
     * Liefert alle Freunde eines Users inklusive der zugehörigen 1-zu-1-Conversation-IDs.
     */
    @Transactional(readOnly = true)
    public List<FriendDTO> getFriendUsernames(UUID userId) {

        List<FriendshipEntity> friendships =
                friendshipRepository.findByUser1_IdOrUser2_Id(userId, userId);

        List<FriendDTO> dtos = new ArrayList<>();

        for (FriendshipEntity friendship : friendships) {

            // Den "anderen" User bestimmen
            UserEntity friend = friendship.getUser1().getId().equals(userId)
                                ? friendship.getUser2()
                                : friendship.getUser1();

            // 1-zu-1-Conversation in beiden Richtungen suchen
            List<Conversation> convs =
                    conversationRepo.findOneToOneBothDirections(userId, friend.getId());

            UUID convId = convs.isEmpty() ? null : convs.get(0).getId();

            System.out.printf("[FRIEND-LIST] pair %s ↔ %s  -->  convId=%s%n",
                    userId, friend.getId(), convId);

            dtos.add(new FriendDTO(friend.getId(),
                                   convId,
                                   friend.getUsername(),
                                   FriendDTO.Status.offline));
        }
        return dtos;
    }

    /**
     * Prüft, ob zwei Benutzer befreundet sind (in beliebiger Richtung).
     */
    @Transactional(readOnly = true)
    public boolean areFriends(UUID user1, UUID user2) {

        if (user1.equals(user2)) {
            System.out.printf("[DEBUG] Skipping friendship check (same ID): %s%n", user1);
            return false;
        }

        System.out.printf("[DEBUG] Checking friendship between %s and %s%n", user1, user2);

        boolean direct  = friendshipRepository.existsById(new FriendshipId(user1, user2));
        boolean reverse = friendshipRepository.existsById(new FriendshipId(user2, user1));

        System.out.printf("[DEBUG] Direct found: %b, Reverse found: %b%n", direct, reverse);
        return direct || reverse;
    }

    /**
     * Listet alle noch ausstehenden Freundschaftsanfragen an {@code userId}.
     * <br>Benutzt eine JPQL-DTO-Projection → nur eine SQL-Query, keine Lazy-Proxies.
     */
    @Transactional(readOnly = true)
    public List<PendingRequestDTO> getPendingRequests(UUID userId) {
        return requestRepository.findPendingRequests(
                userId,
                FriendRequestEntity.FriendRequestStatus.PENDING
        );
    }

    /* ==========================================================
       WRITE-METHODS
       ========================================================== */

    /**
     * Entfernt eine Freundschaft in beliebiger Richtung.
     */
    @Transactional
    public boolean removeFriend(UUID user1, UUID user2) {

        FriendshipId id1 = new FriendshipId(user1, user2);
        FriendshipId id2 = new FriendshipId(user2, user1);

        if (friendshipRepository.existsById(id1)) {
            friendshipRepository.deleteById(id1);
            return true;
        }
        if (friendshipRepository.existsById(id2)) {
            friendshipRepository.deleteById(id2);
            return true;
        }
        return false;
    }

    /**
     * Blockiert {@code targetId} durch {@code blockerId}. Dabei wird auch die Freundschaft entfernt.
     */
    @Transactional
    public boolean blockUser(UUID blockerId, UUID targetId) {
        UserEntity blocker = userRepository.findById(blockerId).orElse(null);
        UserEntity target  = userRepository.findById(targetId).orElse(null);
        if (blocker == null || target == null) return false;

        blockRepository.save(new BlockEntity(blocker, target));
        removeFriend(blockerId, targetId);
        return true;
    }

    /**
     * Meldet einen Benutzer und blockiert ihn anschließend.
     */
    @Transactional
    public boolean reportUser(UUID reporterId, UUID targetId, String reason) {
        UserEntity reporter = userRepository.findById(reporterId).orElse(null);
        UserEntity target   = userRepository.findById(targetId).orElse(null);
        if (reporter == null || target == null) return false;

        reportRepository.save(new ReportEntity(reporter, target, reason));
        blockUser(reporterId, targetId);
        return true;
    }

    /**
     * Sendet eine neue Freundschaftsanfrage.
     */
    @Transactional
    public boolean sendFriendRequest(UUID senderId, UUID receiverId) {

        if (senderId.equals(receiverId)) return false;

        UserEntity sender   = userRepository.findById(senderId).orElse(null);
        UserEntity receiver = userRepository.findById(receiverId).orElse(null);
        if (sender == null || receiver == null) return false;

        Instant expires = Instant.now().plus(30, ChronoUnit.DAYS);
        requestRepository.save(new FriendRequestEntity(sender, receiver, expires));
        return true;
    }

    /**
     * Akzeptiert eine ausstehende Anfrage und legt eine Freundschaft an.
     */
    @Transactional
    public boolean acceptFriendRequest(UUID requestId) {

        FriendRequestEntity request = requestRepository.findById(requestId).orElse(null);

        if (request == null ||
            request.getStatus() != FriendRequestEntity.FriendRequestStatus.PENDING) {
            return false;
        }

        request.setStatus(FriendRequestEntity.FriendRequestStatus.ACCEPTED);

        friendshipRepository.save(new FriendshipEntity(request.getSender(),
                                                       request.getReceiver()));
        return true;
    }

    /**
     * Lehnt eine ausstehende Anfrage ab.
     */
    @Transactional
    public boolean declineFriendRequest(UUID requestId) {

        FriendRequestEntity request = requestRepository.findById(requestId).orElse(null);

        if (request == null ||
            request.getStatus() != FriendRequestEntity.FriendRequestStatus.PENDING) {
            return false;
        }

        request.setStatus(FriendRequestEntity.FriendRequestStatus.DECLINED);
        return true;
    }
}
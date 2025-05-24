package server;

import org.springframework.boot.CommandLineRunner;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;
import server.chat.Conversation;
import server.chat.ConversationParticipant;
import server.chat.ConversationRepository;

import java.time.Instant;
import java.time.LocalDate;
import java.util.Set;
import java.util.UUID;

@Component
@Order(1)
public class DataInitializer implements CommandLineRunner {

    private static final boolean INSERT_TEST_DATA = false; // HIER EIN/AUSSCHALTEN

    private final UserRepository userRepo;
    private final CardRepository cardRepo;
    private final FriendRequestRepository requestRepo;
    private final FriendshipRepository friendshipRepo;
    private final ConversationRepository conversationRepo;

    public DataInitializer(UserRepository userRepo,
                           CardRepository cardRepo,
                           FriendRequestRepository requestRepo,
                           FriendshipRepository friendshipRepo,
                           ConversationRepository conversationRepo) {
        this.userRepo         = userRepo;
        this.cardRepo         = cardRepo;
        this.requestRepo      = requestRepo;
        this.friendshipRepo   = friendshipRepo;
        this.conversationRepo = conversationRepo;
    }

    @Override
    public void run(String... args) throws Exception {
        if (!INSERT_TEST_DATA) {
            System.out.println("Testdaten wurden übersprungen (INSERT_TEST_DATA = false).");
            return;
        }

        // -------- Users --------
        if (!userRepo.existsByEmail("admin@myth.local") && !userRepo.existsByEmail("player1@myth.local")) {
            UserEntity admin = new UserEntity(
                "admin",
                "{noop}adminpass",
                "default.png",
                1, 0, 1,
                LocalDate.now(),
                "admin@myth.local",
                Set.of("ADMIN", "USER")
            );
            UserEntity user = new UserEntity(
                "player1",
                "{noop}secret",
                "default.png",
                1, 0, 1,
                LocalDate.now(),
                "player1@myth.local",
                Set.of("USER")
            );
            userRepo.saveAll(Set.of(admin, user));

            // -------- Card --------
            CardEntity card = new CardEntity(
                "Dragoooon!",
                "01.01.2025",
                "assets/textures/dragooon.png",
                "assets/textures/bg.png",
                "assets/textures/logo.png",
                (short) 10, (short) 5, (short) 3, (short) 7, (short) 12
            );
            card.addAbility(new AbilityEntity(card, (short)1, "fireball", "Burns enemy", "+5"));
            card.addAbility(new AbilityEntity(card, (short)2, "shield", "Blocks attack", "+3"));
            cardRepo.save(card);

            // -------- Friend Request & Friendship --------
            FriendRequestEntity fr = new FriendRequestEntity(
                user, admin,
                Instant.now().plusSeconds(30*24*3600)
            );
            requestRepo.save(fr);
            fr.setStatus(FriendRequestEntity.FriendRequestStatus.ACCEPTED);
            requestRepo.save(fr);
            FriendshipEntity friendship = FriendshipEntity.create(user, admin);
            friendshipRepo.save(friendship);

            // -------- Conversation --------
            Conversation conv = new Conversation(false, null, admin.getId());
            conv.addParticipant(new ConversationParticipant(conv, admin.getId(), ConversationParticipant.Role.OWNER));
            conv.addParticipant(new ConversationParticipant(conv, user.getId(), ConversationParticipant.Role.MEMBER));
            conversationRepo.save(conv);
        } else {
            System.out.println("Testdaten bereits vorhanden – keine neuen Einträge erstellt.");
        }
    }
}
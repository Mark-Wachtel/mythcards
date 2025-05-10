package chat;

import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;  
import java.time.Instant;

@Component
public class MessagePurger {
    private final ChatMessageRepository repo; public MessagePurger(ChatMessageRepository r){this.repo=r;}
    @Scheduled(cron = "0 0 3 * * *") public void purge(){ repo.purgeOld(Instant.now().minusSeconds(30L*24*3600)); }
}
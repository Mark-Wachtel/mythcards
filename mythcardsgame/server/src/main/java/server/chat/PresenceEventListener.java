package server.chat;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.messaging.SessionDisconnectEvent;

import java.security.Principal;
import java.util.UUID;

@Component
public class PresenceEventListener {

	PresenceService presenceService;

    @Autowired
    public PresenceEventListener(PresenceService presence) {
        this.presenceService = presence;
    }

    /* nur noch DISCONNECT beobachten – das Zählen übernimmt PresenceService */
    @EventListener
    public void onSessionDisconnect(SessionDisconnectEvent event) {

        Principal principal = event.getUser();
        if (principal == null) return;

        UUID userId = UUID.fromString(principal.getName());
        presenceService.handleDisconnect(userId);             // <-- NEU
    }
}
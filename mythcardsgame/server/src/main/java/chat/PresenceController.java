package chat;

import org.springframework.web.bind.annotation.*;
import java.util.*;
import java.util.stream.Collectors;

@RestController @RequestMapping("/api/friends/presence")
public class PresenceController {
    private final PresenceService svc; public PresenceController(PresenceService s){this.svc=s;}
    @GetMapping public Map<UUID, Boolean> presence(@RequestParam List<UUID> ids){ return ids.stream().collect(Collectors.toMap(id->id, svc::isOnline)); }
}
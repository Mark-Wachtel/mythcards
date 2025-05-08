package server;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import common.CardDTO;

@RestController
@RequestMapping("/api")
public class CardController {

    private final CardService service;
    public CardController(CardService svc) { this.service = svc; }

    @GetMapping("/card/{id}")
    public ResponseEntity<CardDTO> card(@PathVariable long id) {
        return service.getCard(id)
                      .map(ResponseEntity::ok)
                      .orElse(ResponseEntity.notFound().build());
    }
}
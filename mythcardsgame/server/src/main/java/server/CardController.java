package server;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import common.CardDTO;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api")
public class CardController {

    private final CardService service;
    
    public CardController(CardService svc) { 
        this.service = svc; 
    }

    @GetMapping("/card/{id}")
    public ResponseEntity<CardDTO> getCard(@PathVariable UUID id) {
        return service.getCard(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }
    
    @GetMapping("/cards")
    public ResponseEntity<Page<CardDTO>> getAllCards(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        
        Pageable pageable = PageRequest.of(page, size);
        Page<CardDTO> cardPage = service.getAllCardsPaged(pageable);
        return ResponseEntity.ok(cardPage);
    }
    
    // Alternative ohne Pagination
    @GetMapping("/cards/all")
    public ResponseEntity<List<CardDTO>> getAllCardsList() {
        return ResponseEntity.ok(service.getAllCards());
    }
}
package server;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import common.DeckDetailDto;
import common.DeckDto;
import common.DeckService;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/decks")
public class DeckController {
    private final DeckService deckService;

    public DeckController(DeckService deckService) {
        this.deckService = deckService;
    }

    @GetMapping
    public List<DeckDto> getAll(@RequestParam UUID userId) {
        return deckService.findAllByUser(userId);
    }

    @GetMapping("/{id}")
    public DeckDetailDto getOne(@PathVariable UUID id, @RequestParam UUID userId) {
        return deckService.findById(userId, id);
    }

    @PostMapping
    public ResponseEntity<DeckDto> create(@RequestBody DeckDto dto) {
        DeckDto created = deckService.createDeck(dto.getUserId(), dto.getName(), dto.getCardIds());
        return ResponseEntity.status(201).body(created);
    }

    @PutMapping("/{id}")
    public DeckDto update(@PathVariable UUID id, @RequestBody DeckDto dto) {
        return deckService.updateDeck(dto.getUserId(), id, dto.getName(), dto.getCardIds());
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable UUID id, @RequestParam UUID userId) {
        deckService.deleteDeck(userId, id);
        return ResponseEntity.noContent().build();
    }
}
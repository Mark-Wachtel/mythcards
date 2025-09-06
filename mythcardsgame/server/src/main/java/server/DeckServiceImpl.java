package server;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import common.CardData;
import common.DeckDetailDto;
import common.DeckDto;
import common.DeckService;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@Transactional
public class DeckServiceImpl implements DeckService {
    private final DeckRepository deckRepo;
    private final DeckCardRepository deckCardRepo;
    private final CardRepository cardRepo;

    public DeckServiceImpl(DeckRepository deckRepo,
                           DeckCardRepository deckCardRepo,
                           CardRepository cardRepo) {
        this.deckRepo = deckRepo;
        this.deckCardRepo = deckCardRepo;
        this.cardRepo = cardRepo;
    }

    @Override
    public DeckDto createDeck(UUID userId, String name, List<UUID> cardIds) {
        DeckEntity deck = new DeckEntity();
        deck.setUserId(userId);
        deck.setName(name);
        deck = deckRepo.save(deck);

        // Karten hinzufügen: existierende CardEntity laden
        for (UUID cardId : cardIds) {
            CardEntity card = cardRepo.findById(cardId)
                    .orElseThrow(() -> new IllegalArgumentException("Karte nicht gefunden: " + cardId));
            DeckCardEntity dc = new DeckCardEntity(deck, card, 1);
            deckCardRepo.save(dc);
        }

        return new DeckDto(deck.getId(), userId, name, cardIds);
    }

    @Override
    public DeckDto updateDeck(UUID userId, UUID deckId, String name, List<UUID> cardIds) {
        DeckEntity deck = deckRepo.findById(deckId)
                .orElseThrow(() -> new IllegalArgumentException("Deck nicht gefunden: " + deckId));
        if (!deck.getUserId().equals(userId)) {
            throw new SecurityException("Kein Zugriff auf dieses Deck");
        }
        deck.setName(name);
        // Alte Karten löschen
        deckCardRepo.deleteAll(deck.getCards());
        // Neue Karten hinzufügen
        for (UUID cardId : cardIds) {
            CardEntity card = cardRepo.findById(cardId)
                    .orElseThrow(() -> new IllegalArgumentException("Karte nicht gefunden: " + cardId));
            DeckCardEntity dc = new DeckCardEntity(deck, card, 1);
            deckCardRepo.save(dc);
        }
        // Änderungen persistieren
        deck = deckRepo.save(deck);
        return new DeckDto(deck.getId(), userId, name, cardIds);
    }

    @Override
    public void deleteDeck(UUID userId, UUID deckId) {
        DeckEntity deck = deckRepo.findById(deckId)
                .orElseThrow(() -> new IllegalArgumentException("Deck nicht gefunden: " + deckId));
        if (!deck.getUserId().equals(userId)) {
            throw new SecurityException("Kein Zugriff auf dieses Deck");
        }
        deckRepo.delete(deck);
    }

    @Override
    public List<DeckDto> findAllByUser(UUID userId) {
        return deckRepo.findAllByUserId(userId).stream()
                .map(d -> new DeckDto(d.getId(), userId, d.getName(),
                        d.getCards().stream().map(c -> c.getCard().getId()).collect(Collectors.toList())))
                .collect(Collectors.toList());
    }

    @Override
    public DeckDetailDto findById(UUID userId, UUID deckId) {
        DeckEntity deck = deckRepo.findById(deckId)
            .orElseThrow(() -> new IllegalArgumentException("Deck nicht gefunden: " + deckId));
        // … Zugriffscheck …

        List<CardData> list = deck.getCards().stream()
            .map(dc -> mapToCardData(dc.getCard()))
            .collect(Collectors.toList());

        return new DeckDetailDto(deck.getId(), deck.getName(), list);
    }
    
    /** Mapping-Helper rein im Server-Modul, common bleibt clean */
    private CardData mapToCardData(CardEntity e) {
        CardData dto = new CardData();

        // IDs & Meta
        dto.id    = e.getId();                 // CardData.id ist UUID
        dto.title = e.getTitle();
        dto.type  = e.getCardType();           // wenn vorhanden
        dto.releaseKey = null;                 // aktuell nicht in CardEntity

        // Images (CardEntity hat imageUrl; Level/Logo derzeit nicht vorhanden)
        String img = e.getImageUrl();
        dto.imagePath      = (img == null) ? null : img.replaceFirst("^assets/textures/", "");
        dto.levelImagePath = null;
        dto.logoPng        = null;

        // Stats (Integer → int, null-safe)
        Integer atk = e.getAttack();
        Integer def = e.getDefense();
        Integer hp  = 0;           // in deiner Entity üblich; falls nicht, auf 0
        dto.attack = (atk != null) ? atk : 0;
        dto.def    = (def != null) ? def : 0;
        dto.hp     = (hp  != null) ? hp  : 0;
        dto.magic  = 0;                        // nicht im Entity → 0
        dto.speed  = 0;

        // Abilities: aktueller Stand ist List<String>; in CardData brauchen wir Objekte
        dto.abilities.clear();
        if (e.getAbilities() != null) {
            int slot = 1;
            for (String name : e.getAbilities()) {
                CardData.Ability ab = new CardData.Ability();
                ab.slot   = slot++;            // 1,2,3,...
                ab.name   = name;
                ab.effect = "";                // derzeit keine Felder im Entity
                ab.value  = "";
                dto.abilities.add(ab);
            }
        }

        return dto;
    }
}
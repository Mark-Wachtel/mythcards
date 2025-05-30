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
    
    /** Mapping­-Helper rein im Server-Modul, common bleibt clean */
    private CardData mapToCardData(CardEntity e) {
        CardData dto = new CardData();
        dto.id               = e.getId();          // UUID in common als String/UUID definiert
        dto.title            = e.getNameKey();
        dto.releaseKey       = e.getReleaseKey();
        dto.imagePath        = e.getMonsterPng().replaceFirst("^assets/textures/", "");
        dto.levelImagePath   = e.getBackgroundPng().replaceFirst("^assets/textures/", "");
        dto.logoPng          = e.getLogoPng().replaceFirst("^assets/textures/", "");
        dto.attack           = e.getAttack();
        dto.def              = e.getDefense();
        dto.hp               = e.getHealth();
        dto.magic            = e.getMagic();
        dto.speed            = e.getSpeed();
        // Abilities
        dto.abilities = e.getAbilities().stream().map(a -> {
            CardData.Ability ab = new CardData.Ability();
            ab.slot   = a.getSlot();
            ab.name   = a.getNameKey();
            ab.effect = a.getDescKey();
            ab.value  = a.getValueKey();
            return ab;
        }).collect(Collectors.toList());
        return dto;
    }
}
package server;

import java.util.Comparator;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import org.springframework.stereotype.Service;

import common.AbilityDTO;
import common.CardDTO;

@Service
public class CardService {

    private final CardRepository repo;

    public CardService(CardRepository r) { this.repo = r; }

    public Optional<CardDTO> getCard(long id) {
        return repo.findWithAbilities(id).map(this::map);
    }

    private CardDTO map(CardEntity c) {
        List<AbilityDTO> list = c.getAbilities().stream()
                .sorted(Comparator.comparing(AbilityEntity::getSlot))
                .map(a -> new AbilityDTO(a.getNameKey(), a.getDescKey(), a.getValueKey()))
                .collect(Collectors.toList());

        return new CardDTO(
                c.getNameKey(), c.getReleaseKey(),
                c.getMonsterPng(), c.getBackgroundPng(), c.getLogoPng(),
                c.getAttack(), c.getDefense(), c.getSpeed(), c.getMagic(), c.getHealth(),
                list);
    }
}
package server;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import common.CardDTO;

import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
public class CardService {
    
    private final CardRepository repo;
    
    public CardService(CardRepository repo) {
        this.repo = repo;
    }
    
    public Optional<CardDTO> getCard(UUID id) {
        return repo.findById(id).map(this::mapToDTO);
    }
    
    public List<CardDTO> getAllCards() {
        return repo.findAll().stream()
                .map(this::mapToDTO)
                .collect(Collectors.toList());
    }
    
    public Page<CardDTO> getAllCardsPaged(Pageable pageable) {
        Page<CardEntity> cardPage = repo.findAll(pageable);
        return cardPage.map(this::mapToDTO);
    }
    
    private CardDTO mapToDTO(CardEntity entity) {
        CardDTO dto = new CardDTO();
        dto.setId(entity.getId());
        dto.setTitle(entity.getTitle());
        dto.setDescription(entity.getDescription());
        dto.setImageUrl(entity.getImageUrl());
        dto.setManaCost(entity.getManaCost());
        dto.setAttack(entity.getAttack());
        dto.setDefense(entity.getDefense());
        dto.setCardType(entity.getCardType());
        dto.setAbilities(entity.getAbilities());
        return dto;
    }
}
package common;

import java.util.List;

public record CardDTO(
        String nameKey, String releaseKey,
        String monsterPng, String backgroundPng, String logoPng,
        int attack, int defense, int speed, int magic, int health,
        List<AbilityDTO> abilities
) {}	
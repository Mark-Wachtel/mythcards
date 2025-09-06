package common;

import java.util.UUID;

/**
 * Default ability implementation used by normal ability slots.
 */
public class CardAbility implements Ability {

    private final UUID id;
    private final TargetType target;
    private final AbilityType abilityType;
    private final boolean interruptible;
    private final int requiredLevel;
    private final int cooldown;
    private final DamageType damageType;

    public CardAbility(UUID id,
                       TargetType target,
                       AbilityType abilityType,
                       boolean interruptible,
                       int requiredLevel,
                       int cooldown,
                       DamageType damageType) {
        this.id = id;
        this.target = target;
        this.abilityType = abilityType;
        this.interruptible = interruptible;
        this.requiredLevel = requiredLevel;
        this.cooldown = cooldown;
        this.damageType = damageType;
    }

    @Override
    public UUID getId() {
        return id;
    }

    @Override
    public TargetType getTarget() {
        return target;
    }

    @Override
    public AbilityType getAbilityType() {
        return abilityType;
    }

    @Override
    public boolean canBeInterrupted() {
        return interruptible;
    }

    @Override
    public int getRequiredLevel() {
        return requiredLevel;
    }

    @Override
    public int getCooldown() {
        return cooldown;
    }

    public DamageType getDamageType() {
        return damageType;
    }
}
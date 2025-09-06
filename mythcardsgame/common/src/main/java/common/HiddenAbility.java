package common;

import java.util.UUID;

/**
 * Special ability that remains hidden until a trigger condition
 * is met. It extends {@link CardAbility} and can reuse all base
 * fields while adding its own specific behaviour.
 */
public class HiddenAbility extends CardAbility {

    /** Condition that reveals or activates the ability. */
    private final String triggerCondition;

    public HiddenAbility(UUID id,
                         TargetType target,
                         AbilityType abilityType,
                         boolean interruptible,
                         int requiredLevel,
                         int cooldown,
                         DamageType damageType,
                         String triggerCondition) {
        super(id, target, abilityType, interruptible, requiredLevel, cooldown, damageType);
        this.triggerCondition = triggerCondition;
    }

    public String getTriggerCondition() {
        return triggerCondition;
    }
}
package common;

import java.util.UUID;

/**
 * Generic ability interface. Concrete implementations like
 * {@link CardAbility} and {@link HiddenAbility} provide the
 * actual data fields.
 */
public interface Ability {

    /** Unique ability identifier. */
    UUID getId();

    /** Target of the ability (self, enemy …). */
    TargetType getTarget();

    /** Type of the ability. */
    AbilityType getAbilityType();

    /** Can the ability be interrupted? */
    boolean canBeInterrupted();

    /** Level required to use this ability. */
    int getRequiredLevel();

    /** Cooldown in turns. */
    int getCooldown();
}
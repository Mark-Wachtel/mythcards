package common;

import java.time.LocalDate;
import java.util.List;
import java.util.Locale;
import java.util.UUID;

/**
 * Base interface for all card types in Myth Cards.
 * <p>
 * The interface captures fields that are shared across monsters,
 * effect cards and equipment cards. The goal is to mirror the huge
 * specification table that defines all possible card attributes.
 * For now only a subset is implemented to provide a structured
 * foundation; additional fields from the table can be added over
 * time without breaking the inheritance design.
 */
public interface Card {

    /** Unique card identifier. */
    UUID getId();

    /** Localised card name. */
    String getName();

    /** Short code like "MON-001". */
    String getCardCode();

    /** Name of the card set (e.g. "01-Darkstars"). */
    String getCardSet();

    /** Owner account. */
    UUID getOwnerId();

    /** Card category. */
    Category getCategory();

    /** Version string, e.g. "0.0.1". */
    String getVersion();

    /** Release date as free form text. */
    String getReleaseDateText();

    /** When the card was last edited. */
    LocalDate getLastEdited();

    /** Language key like "de_DE". */
    Locale getLanguage();

    /** Optional illustrator credit. */
    String getIllustrator();

    /** Gameplay tags like "fire", "buff" … */
    List<String> getTags();

    /** Maximum copies allowed in a deck. */
    int getDeckLimit();

    /** Flavour text. */
    String getLore();

    /** Whether the card is unlocked for single player. */
    boolean isUnlocked();

    /** Maintenance flag. */
    boolean isEnabled();

    /** Banned in ranked mode. */
    boolean isBannedInRanked();

    /** Balance or developer notes. */
    String getBalanceNotes();

    /** Tooltip text. */
    String getTooltip();

    /** Path or identifier of the card background image. */
    String getCardBackground();

    /** Path or identifier of the card foreground image. */
    String getCardForeground();

    /** Path or identifier of the card name bar image. */
    String getCardNameBar();

    /** Abilities tied to this card (slots 1‑4 and hidden). */
    List<Ability> getAbilities();
}
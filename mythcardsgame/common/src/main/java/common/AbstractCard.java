package common;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.UUID;

/**
 * Convenience base class that stores the common card fields.
 * Specific card types extend this class.
 */
public abstract class AbstractCard implements Card {

    private final UUID id;
    private String name;
    private String cardCode;
    private String cardSet;
    private UUID ownerId;
    private Category category;
    private String version;
    private String releaseDateText;
    private LocalDate lastEdited;
    private Locale language;
    private String illustrator;
    private List<String> tags = new ArrayList<>();
    private int deckLimit;
    private String lore;
    private boolean unlocked;
    private boolean enabled = true;
    private boolean bannedInRanked;
    private String balanceNotes;
    private String tooltip;
    private String cardBackground;
    private String cardForeground;
    private String cardNameBar;
    private List<Ability> abilities = new ArrayList<>();

    protected AbstractCard(UUID id, Category category) {
        this.id = id;
        this.category = category;
    }

    @Override
    public UUID getId() {
        return id;
    }

    @Override
    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    @Override
    public String getCardCode() {
        return cardCode;
    }

    public void setCardCode(String cardCode) {
        this.cardCode = cardCode;
    }

    @Override
    public String getCardSet() {
        return cardSet;
    }

    public void setCardSet(String cardSet) {
        this.cardSet = cardSet;
    }

    @Override
    public UUID getOwnerId() {
        return ownerId;
    }

    public void setOwnerId(UUID ownerId) {
        this.ownerId = ownerId;
    }

    @Override
    public Category getCategory() {
        return category;
    }

    public void setCategory(Category category) {
        this.category = category;
    }

    @Override
    public String getVersion() {
        return version;
    }

    public void setVersion(String version) {
        this.version = version;
    }

    @Override
    public String getReleaseDateText() {
        return releaseDateText;
    }

    public void setReleaseDateText(String releaseDateText) {
        this.releaseDateText = releaseDateText;
    }

    @Override
    public LocalDate getLastEdited() {
        return lastEdited;
    }

    public void setLastEdited(LocalDate lastEdited) {
        this.lastEdited = lastEdited;
    }

    @Override
    public Locale getLanguage() {
        return language;
    }

    public void setLanguage(Locale language) {
        this.language = language;
    }

    @Override
    public String getIllustrator() {
        return illustrator;
    }

    public void setIllustrator(String illustrator) {
        this.illustrator = illustrator;
    }

    @Override
    public List<String> getTags() {
        return tags;
    }

    public void setTags(List<String> tags) {
        this.tags = tags;
    }

    @Override
    public int getDeckLimit() {
        return deckLimit;
    }

    public void setDeckLimit(int deckLimit) {
        this.deckLimit = deckLimit;
    }

    @Override
    public String getLore() {
        return lore;
    }

    public void setLore(String lore) {
        this.lore = lore;
    }

    @Override
    public boolean isUnlocked() {
        return unlocked;
    }

    public void setUnlocked(boolean unlocked) {
        this.unlocked = unlocked;
    }

    @Override
    public boolean isEnabled() {
        return enabled;
    }

    public void setEnabled(boolean enabled) {
        this.enabled = enabled;
    }

    @Override
    public boolean isBannedInRanked() {
        return bannedInRanked;
    }

    public void setBannedInRanked(boolean bannedInRanked) {
        this.bannedInRanked = bannedInRanked;
    }

    @Override
    public String getBalanceNotes() {
        return balanceNotes;
    }

    public void setBalanceNotes(String balanceNotes) {
        this.balanceNotes = balanceNotes;
    }

    @Override
    public String getTooltip() {
        return tooltip;
    }

    public void setTooltip(String tooltip) {
        this.tooltip = tooltip;
    }

    @Override
    public String getCardBackground() {
        return cardBackground;
    }

    public void setCardBackground(String cardBackground) {
        this.cardBackground = cardBackground;
    }

    @Override
    public String getCardForeground() {
        return cardForeground;
    }

    public void setCardForeground(String cardForeground) {
        this.cardForeground = cardForeground;
    }

    @Override
    public String getCardNameBar() {
        return cardNameBar;
    }

    public void setCardNameBar(String cardNameBar) {
        this.cardNameBar = cardNameBar;
    }

    @Override
    public List<Ability> getAbilities() {
        return abilities;
    }

    public void setAbilities(List<Ability> abilities) {
        this.abilities = abilities;
    }
}
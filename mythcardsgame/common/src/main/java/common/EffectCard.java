package common;

import java.util.UUID;

/**
 * Card that triggers an effect when played.
 */
public class EffectCard extends AbstractCard {

    private String effectImage;
    private boolean stackable;
    private boolean hiddenUntilTriggered;
    private int usesPerMatch;
    private String triggerCondition;

    public EffectCard(UUID id) {
        super(id, Category.EFFECT);
    }

    public String getEffectImage() {
        return effectImage;
    }

    public void setEffectImage(String effectImage) {
        this.effectImage = effectImage;
    }

    public boolean isStackable() {
        return stackable;
    }

    public void setStackable(boolean stackable) {
        this.stackable = stackable;
    }

    public boolean isHiddenUntilTriggered() {
        return hiddenUntilTriggered;
    }

    public void setHiddenUntilTriggered(boolean hiddenUntilTriggered) {
        this.hiddenUntilTriggered = hiddenUntilTriggered;
    }

    public int getUsesPerMatch() {
        return usesPerMatch;
    }

    public void setUsesPerMatch(int usesPerMatch) {
        this.usesPerMatch = usesPerMatch;
    }

    public String getTriggerCondition() {
        return triggerCondition;
    }

    public void setTriggerCondition(String triggerCondition) {
        this.triggerCondition = triggerCondition;
    }
}
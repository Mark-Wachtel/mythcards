package common;

import java.util.UUID;

/**
 * Card that equips a monster with additional stats.
 */
public class EquipmentCard extends AbstractCard {

    private String equipmentImage;
    private StatType buffStat;
    private int buffValue;
    private StatType debuffStat;
    private int debuffValue;
    private boolean interruptable;
    private boolean permanentActive;

    public EquipmentCard(UUID id) {
        super(id, Category.EQUIPMENT);
    }

    public String getEquipmentImage() {
        return equipmentImage;
    }

    public void setEquipmentImage(String equipmentImage) {
        this.equipmentImage = equipmentImage;
    }

    public StatType getBuffStat() {
        return buffStat;
    }

    public void setBuffStat(StatType buffStat) {
        this.buffStat = buffStat;
    }

    public int getBuffValue() {
        return buffValue;
    }

    public void setBuffValue(int buffValue) {
        this.buffValue = buffValue;
    }

    public StatType getDebuffStat() {
        return debuffStat;
    }

    public void setDebuffStat(StatType debuffStat) {
        this.debuffStat = debuffStat;
    }

    public int getDebuffValue() {
        return debuffValue;
    }

    public void setDebuffValue(int debuffValue) {
        this.debuffValue = debuffValue;
    }

    public boolean isInterruptable() {
        return interruptable;
    }

    public void setInterruptable(boolean interruptable) {
        this.interruptable = interruptable;
    }

    public boolean isPermanentActive() {
        return permanentActive;
    }

    public void setPermanentActive(boolean permanentActive) {
        this.permanentActive = permanentActive;
    }
}
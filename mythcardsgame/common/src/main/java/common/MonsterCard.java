package common;

import java.util.UUID;

/**
 * Representation of a monster card with combat stats.
 */
public class MonsterCard extends AbstractCard {

    private String cardFrameImage;
    private String cardLogoImage;
    private String monsterImage;

    private float maxHealth;
    private float currentHealth;
    private float maxDefense;
    private float currentDefense;
    private float attack;
    private float magic;
    private float speed;
    private float critChance;

    public MonsterCard(UUID id) {
        super(id, Category.MONSTER);
    }

    public String getCardFrameImage() {
        return cardFrameImage;
    }

    public void setCardFrameImage(String cardFrameImage) {
        this.cardFrameImage = cardFrameImage;
    }

    public String getCardLogoImage() {
        return cardLogoImage;
    }

    public void setCardLogoImage(String cardLogoImage) {
        this.cardLogoImage = cardLogoImage;
    }

    public String getMonsterImage() {
        return monsterImage;
    }

    public void setMonsterImage(String monsterImage) {
        this.monsterImage = monsterImage;
    }

    public float getMaxHealth() {
        return maxHealth;
    }

    public void setMaxHealth(float maxHealth) {
        this.maxHealth = maxHealth;
    }

    public float getCurrentHealth() {
        return currentHealth;
    }

    public void setCurrentHealth(float currentHealth) {
        this.currentHealth = currentHealth;
    }

    public float getMaxDefense() {
        return maxDefense;
    }

    public void setMaxDefense(float maxDefense) {
        this.maxDefense = maxDefense;
    }

    public float getCurrentDefense() {
        return currentDefense;
    }

    public void setCurrentDefense(float currentDefense) {
        this.currentDefense = currentDefense;
    }

    public float getAttack() {
        return attack;
    }

    public void setAttack(float attack) {
        this.attack = attack;
    }

    public float getMagic() {
        return magic;
    }

    public void setMagic(float magic) {
        this.magic = magic;
    }

    public float getSpeed() {
        return speed;
    }

    public void setSpeed(float speed) {
        this.speed = speed;
    }

    public float getCritChance() {
        return critChance;
    }

    public void setCritChance(float critChance) {
        this.critChance = critChance;
    }
}
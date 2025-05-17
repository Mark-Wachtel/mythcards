package server;

import jakarta.persistence.*;
import org.hibernate.annotations.GenericGenerator;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Entity
@Table(name = "cards")
public class CardEntity {

    @Id
    @GeneratedValue(generator = "UUID")
    @GenericGenerator(
        name = "UUID",
        strategy = "org.hibernate.id.UUIDGenerator"
    )
    @Column(name = "id", updatable = false, nullable = false,
            columnDefinition = "BINARY(16)")
    private UUID id;

    @Column(name = "name_key", nullable = false)
    private String nameKey;

    @Column(name = "release_key", nullable = false)
    private String releaseKey;

    @Column(name = "monster_png", nullable = false)
    private String monsterPng;

    @Column(name = "background_png", nullable = false)
    private String backgroundPng;

    @Column(name = "logo_png", nullable = false)
    private String logoPng;

    @Column(nullable = false)
    private short attack;

    @Column(nullable = false)
    private short defense;

    @Column(nullable = false)
    private short speed;

    @Column(nullable = false)
    private short magic;

    @Column(nullable = false)
    private short health;

    @OneToMany(
        mappedBy = "card",
        cascade = CascadeType.ALL,
        orphanRemoval = true,
        fetch = FetchType.LAZY
    )
    @OrderBy("slot ASC")
    private List<AbilityEntity> abilities = new ArrayList<>();

    protected CardEntity() {
        // for JPA
    }

    public CardEntity(String nameKey,
                      String releaseKey,
                      String monsterPng,
                      String backgroundPng,
                      String logoPng,
                      short attack,
                      short defense,
                      short speed,
                      short magic,
                      short health) {
        this.nameKey       = nameKey;
        this.releaseKey    = releaseKey;
        this.monsterPng    = monsterPng;
        this.backgroundPng = backgroundPng;
        this.logoPng       = logoPng;
        this.attack        = attack;
        this.defense       = defense;
        this.speed         = speed;
        this.magic         = magic;
        this.health        = health;
    }

    public UUID getId() {
        return id;
    }

    public String getNameKey() {
        return nameKey;
    }

    public void setNameKey(String nameKey) {
        this.nameKey = nameKey;
    }

    public String getReleaseKey() {
        return releaseKey;
    }

    public void setReleaseKey(String releaseKey) {
        this.releaseKey = releaseKey;
    }

    public String getMonsterPng() {
        return monsterPng;
    }

    public void setMonsterPng(String monsterPng) {
        this.monsterPng = monsterPng;
    }

    public String getBackgroundPng() {
        return backgroundPng;
    }

    public void setBackgroundPng(String backgroundPng) {
        this.backgroundPng = backgroundPng;
    }

    public String getLogoPng() {
        return logoPng;
    }

    public void setLogoPng(String logoPng) {
        this.logoPng = logoPng;
    }

    public short getAttack() {
        return attack;
    }

    public void setAttack(short attack) {
        this.attack = attack;
    }

    public short getDefense() {
        return defense;
    }

    public void setDefense(short defense) {
        this.defense = defense;
    }

    public short getSpeed() {
        return speed;
    }

    public void setSpeed(short speed) {
        this.speed = speed;
    }

    public short getMagic() {
        return magic;
    }

    public void setMagic(short magic) {
        this.magic = magic;
    }

    public short getHealth() {
        return health;
    }

    public void setHealth(short health) {
        this.health = health;
    }

    public List<AbilityEntity> getAbilities() {
        return abilities;
    }

    public void setAbilities(List<AbilityEntity> abilities) {
        this.abilities = abilities;
    }

    public void addAbility(AbilityEntity ability) {
        ability.setCard(this);
        this.abilities.add(ability);
    }

    public void removeAbility(AbilityEntity ability) {
        ability.setCard(null);
        this.abilities.remove(ability);
    }
}
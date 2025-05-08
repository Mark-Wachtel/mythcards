package server;

import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import jakarta.persistence.Id;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.OneToMany;
import jakarta.persistence.OrderBy;

import java.util.List;

import jakarta.persistence.CascadeType;
import jakarta.persistence.FetchType;

import java.util.ArrayList;

@Entity
@Table(name = "cards")
public class CardEntity {

    /* ---------- FIELDS ---------- */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String nameKey;
    private String releaseKey;

    private String monsterPng;
    private String backgroundPng;
    private String logoPng;

    private short attack;
    private short defense;
    private short speed;
    private short magic;
    private short health;

    @OneToMany(mappedBy = "card",
               cascade = CascadeType.ALL,
               orphanRemoval = true,
               fetch = FetchType.LAZY)
    @OrderBy("slot ASC")
    private List<AbilityEntity> abilities = new ArrayList<>();

    /* ---------- CONSTRUCTORS ---------- */
    public CardEntity() {}

    /* ---------- GETTER / SETTER ---------- */
    public Long               getId()            { return id; }
    public String             getNameKey()       { return nameKey; }
    public String             getReleaseKey()    { return releaseKey; }
    public String             getMonsterPng()    { return monsterPng; }
    public String             getBackgroundPng() { return backgroundPng; }
    public String             getLogoPng()       { return logoPng; }
    public short              getAttack()        { return attack; }
    public short              getDefense()       { return defense; }
    public short              getSpeed()         { return speed; }
    public short              getMagic()         { return magic; }
    public short              getHealth()        { return health; }
    public List<AbilityEntity> getAbilities()    { return abilities; }

    public void setId(Long id)                           { this.id = id; }
    public void setNameKey(String k)                     { this.nameKey = k; }
    public void setReleaseKey(String k)                  { this.releaseKey = k; }
    public void setMonsterPng(String p)                  { this.monsterPng = p; }
    public void setBackgroundPng(String p)               { this.backgroundPng = p; }
    public void setLogoPng(String p)                     { this.logoPng = p; }
    public void setAttack(short v)                       { this.attack = v; }
    public void setDefense(short v)                      { this.defense = v; }
    public void setSpeed(short v)                        { this.speed = v; }
    public void setMagic(short v)                        { this.magic = v; }
    public void setHealth(short v)                       { this.health = v; }
    public void setAbilities(List<AbilityEntity> list)   { this.abilities = list; }
}

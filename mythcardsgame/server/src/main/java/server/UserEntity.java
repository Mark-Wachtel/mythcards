package server;

import jakarta.persistence.*;
import org.hibernate.annotations.GenericGenerator;
import java.time.LocalDate;
import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

@Entity
@Table(name = "users")
public class UserEntity {

    @Id
    @GeneratedValue(generator = "UUID")
    @GenericGenerator(
        name = "UUID",
        strategy = "org.hibernate.id.UUIDGenerator"
    )
    @Column(name = "id", updatable = false, nullable = false)
    private UUID id;

    @Column(name = "username", unique = true, nullable = false)
    private String username;

    @Column(name = "pwd_hash", nullable = false)
    private String password;

    @Column(name = "avatar_url", nullable = false)
    private String avatarUrl;

    @Column(name = "`rank`", nullable = false)
    private int rank;

    @Column(nullable = false)
    private int experience;

    @Column(nullable = false)
    private int level;

    @Column(nullable = false)
    private LocalDate registered;

    @Column(name = "email", unique = true, nullable = false)
    private String email;

    @ElementCollection(fetch = FetchType.EAGER)
    @CollectionTable(
        name = "user_roles",
        joinColumns = @JoinColumn(name = "user_id")
    )
    @Column(name = "role", nullable = false)
    private Set<String> roles = new HashSet<>();

    // ---------------- Constructors ----------------

    protected UserEntity() {
        // for JPA
    }

    public UserEntity(String username,
                      String password,
                      String avatarUrl,
                      int rank,
                      int experience,
                      int level,
                      LocalDate registered,
                      String email,
                      Set<String> roles) {
        this.username   = username;
        this.password   = password;
        this.avatarUrl  = avatarUrl;
        this.rank       = rank;
        this.experience = experience;
        this.level      = level;
        this.registered = registered;
        this.email      = email;
        this.roles      = roles;
    }

    // ---------------- Getters & Setters ----------------

    public UUID getId() {
        return id;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public String getAvatarUrl() {
        return avatarUrl;
    }

    public void setAvatarUrl(String avatarUrl) {
        this.avatarUrl = avatarUrl;
    }

    public int getRank() {
        return rank;
    }

    public void setRank(int rank) {
        this.rank = rank;
    }

    public int getExperience() {
        return experience;
    }

    public void setExperience(int experience) {
        this.experience = experience;
    }

    public int getLevel() {
        return level;
    }

    public void setLevel(int level) {
        this.level = level;
    }

    public LocalDate getRegistered() {
        return registered;
    }

    public void setRegistered(LocalDate registered) {
        this.registered = registered;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public Set<String> getRoles() {
        return roles;
    }

    public void setRoles(Set<String> roles) {
        this.roles = roles;
    }

    public void addRole(String role) {
        this.roles.add(role);
    }

    public void removeRole(String role) {
        this.roles.remove(role);
    }
}
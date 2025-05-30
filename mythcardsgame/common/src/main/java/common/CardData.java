package common;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

import com.fasterxml.jackson.annotation.JsonAlias;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

/**
 * Data‑holder that is sent from the Spring back‑end to the FXGL client.
 * <p>
 * Besides the raw DTO fields we add two helper methods:
 * <ul>
 *   <li>{@link #imagePath(String)} – resolves the logical image‑ID used by
 *       {@link client.MonsterCardView} to a concrete texture path that FXGL can
 *       load.</li>
 *   <li>{@link #textKey(String)} – future stub for a DB‑based localisation.
 * </ul>
 */
@JsonIgnoreProperties(ignoreUnknown = true) // tolerate additional JSON fields
public class CardData {

    /* ---------- Primitive card data ---------- */
    public UUID    id;
    public String type;

    /** Name‑Key aus JSON (z. B. "Dragoooon!") */
    @JsonAlias("nameKey")
    public String title;

    /** Release‑Key (String – im Testformat "dd.MM.yyyy") */
    public String releaseKey;

    /** Elemente wie "fire", "water", "plant", … – beeinflusst Bar‑Grafiken */
    public String element;

    /* ---------- Images ---------- */
    /** Path to the monster illustration */
    @JsonAlias("monsterPng")
    public String imagePath;

    /** Path to the card background (level frame) */
    @JsonAlias("backgroundPng")
    public String levelImagePath;

    /** Path to the small game logo at the card bottom */
    @JsonAlias("logoPng")
    public String logoPng;

    /* ---------- Stats ---------- */
    @JsonAlias("health")  public int hp;
    @JsonAlias("defense") public int def;
    public int attack;
    public int magic;
    public int speed;

    // Additional, optional stats from older revisions
    public int magicRed, dmgRed, crit;

    /* ---------- Lists ---------- */
    public List<Ability>   abilities = new ArrayList<>();
    public List<Equipment> equipment = new ArrayList<>();

    /* ====================================================================== */
    /* === Helper logic ===================================================== */

    /** Prefix that is stored in the DB but must be stripped for FXGL. */
    private static final String TEX_PREFIX = "assets/textures/";

    /**
     * Strips the leading “assets/textures/” segment so that FXGL’s asset loader
     * can find the texture. If {@code null} is supplied the method safely
     * returns {@code null} – caller decide what to do with that.
     */
    private static String normalize(String fullPath) {
        if (fullPath == null) return null;
        return fullPath.startsWith(TEX_PREFIX) ? fullPath.substring(TEX_PREFIX.length())
                                              : fullPath;
    }

    /**
     * Maps the logical ID that {@link client.MonsterCardView} uses for every
     * {@link javafx.scene.image.ImageView} to an actual PNG under
     * {@code assets/textures/…}. Never returns {@code null}: on unknown IDs we
     * fall back to a generic placeholder so the client won’t crash.
     */
    public String imagePath(String id) {
        switch (id) {
            /* === Direct, one‑to‑one mappings ================================= */
            case "backgroundImage":   return normalize(levelImagePath);
            case "cardMonsterImage":  return normalize(imagePath);
            case "logoImage":         return normalize(logoPng);

            /* === Title bar =================================================== */
            case "cardNameBarImage": {
                String el = element != null ? element.toLowerCase() : "effect";
                return "bars/card_front_bars_" + el + "05.png";
            }

            /* === Stat bars (bar05Image … bar90Image) ========================= */
            default:
                if (id.startsWith("bar") && id.endsWith("Image") && id.length() >= 8) {
                    try {
                        int pct = Integer.parseInt(id.substring(3, 5)); // "05" … "90"
                        // We have five PNGs per element (…_01.png – …_05.png).
                        // Map the 18 possible bars roughly to those five tiers.
                        int tier = Math.min(5, Math.max(1, (pct + 14) / 20)); // 05‑19→1, 20‑39→2, …
                        String el = element != null ? element.toLowerCase() : "effect";
                        return "bars/card_front_bars_" + el + String.format("%02d", tier) + ".png";
                    } catch (NumberFormatException ignored) {
                        // fall through to placeholder
                    }
                }
                /* === Fallback ================================================= */
                return "bars/card_front_bars_effect01.png";
        }
    }

    /**
     * Stub for later DB‑driven localisation. For now we just return the key so
     * the UI at least renders something.
     */
    public String textKey(String key) {
        return key;
    }

    /* ====================================================================== */
    /* === Nested DTOs ====================================================== */

    public static class Ability {
        public int slot;

        @JsonAlias("nameKey")  public String name;
        @JsonAlias("descKey")  public String effect;
        @JsonAlias("valueKey") public String value;
    }

    public static class Equipment {
        public String imagePath;
        public String name;
        public String effect;
        public int bonusHp, bonusDef, bonusAttack,
                   bonusMagic, bonusCrit, bonusSpeed;
    }
}

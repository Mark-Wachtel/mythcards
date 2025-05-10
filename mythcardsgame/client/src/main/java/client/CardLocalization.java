package client;

import java.util.HashMap;
import java.util.Map;

import com.almasb.fxgl.dsl.FXGL;
import com.almasb.fxgl.localization.Language;
import com.almasb.fxgl.localization.LocalizationService;

import common.CardData;

public final class CardLocalization {

    private CardLocalization() { /* static helper */ }

    /** Registriert alle Texte einer Karte als ENGLISH-Bundle. */
    public static void register(CardData c) {
        Map<String, String> texts = new HashMap<>();

        /* ---------- Basis ---------- */
        texts.put("name",    c.title);
        texts.put("release", c.releaseKey);

        /* ---------- Stats ---------- */
        texts.put("statAttack",  String.valueOf(c.attack));
        texts.put("statDefense", String.valueOf(c.def));
        texts.put("statSpeed",   String.valueOf(c.speed));
        texts.put("statMagic",   String.valueOf(c.magic));
        texts.put("statHealth",  String.valueOf(c.hp));

        texts.put("statAttackRed",  String.valueOf(c.dmgRed));
        texts.put("statDefenseRed", String.valueOf(c.magicRed));
        texts.put("statSpeedRed",   "0");
        texts.put("statMagicRed",   "0");
        texts.put("statHealthRed",  "0");

        /* ---------- Abilities (max. 4) ---------- */
        int abilityCount = c.abilities != null ? c.abilities.size() : 0;
        for (int i = 0; i < Math.min(4, abilityCount); i++) {
            var a = c.abilities.get(i);
            int slot = i + 1;
            texts.put("abilityName"  + slot, a.name);
            texts.put("ability" + slot + "-desc", a.effect);
            texts.put("abilityValue" + slot, a.value);
        }

        /* ---------- Beim Localization-Service ablegen ---------- */
        LocalizationService loc = FXGL.getLocalizationService();
        loc.addLanguageData(Language.ENGLISH, texts);

        // Erstinitialisierung, falls noch kein Language gesetzt ist
        if (loc.getSelectedLanguage() == Language.NONE) {
            loc.setSelectedLanguage(Language.ENGLISH);
        }
    }
}
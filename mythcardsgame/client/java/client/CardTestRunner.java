package client;

import javafx.scene.image.ImageView;
import common.CardData;
import client.CardDataFetcher;
import com.almasb.fxgl.dsl.FXGL;

public class CardTestRunner {

    /**
     * Holt CardData über REST, lädt gerenderte PNG und gibt sie als FXGL ImageView zurück.
     */
    public static ImageView renderCardFromServer(int cardId) {
        CardData card = CardDataFetcher.fetchCardData(cardId);

        if (card == null) {
            System.err.println("[CardTestRunner] Karte mit ID " + cardId + " nicht gefunden!");
            return null;
        }

        // Sicherstellen, dass die Textur existiert
        try {
            String renderedPngPath = "generated/card_filled_" + cardId + ".png";
            return FXGL.texture(renderedPngPath);
        } catch (Exception e) {
            System.err.println("[CardTestRunner] Fehler beim Laden der Textur: " + e.getMessage());
            return null;
        }
    }
}
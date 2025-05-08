package client;

import common.CardData;
import common.CardData.Ability;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;

public class CardDataFetcher {

    private static final String BASE_URL = "http://localhost:8080/api/card/";
    

    public static CardData fetchCardData(int cardId) {
        try {
            URL url = new URL(BASE_URL + cardId);
            HttpURLConnection conn = (HttpURLConnection) url.openConnection();
            conn.setRequestMethod("GET");
            conn.setRequestProperty("Accept", "application/json");
            String token = AuthTokenStore.getInstance().getAccessToken();
            conn.setRequestProperty("Authorization", "Bearer " + token);

            if (conn.getResponseCode() != 200) {
                System.err.println("[CardDataFetcher] Fehler beim Abrufen der Karte: HTTP " + conn.getResponseCode());
                return null;
            }

            InputStream is = conn.getInputStream();
            ObjectMapper mapper = new ObjectMapper();
            CardData card = mapper.readValue(is, CardData.class);
            conn.disconnect();

            return card;

        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }
}

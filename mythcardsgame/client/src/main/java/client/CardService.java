package client;

import java.net.http.*;
import java.net.URI;
import com.fasterxml.jackson.databind.ObjectMapper;

import common.CardData;

import com.fasterxml.jackson.core.type.TypeReference;
import java.util.List;

public class CardService {

    private static final String BASE = "http://localhost:8080/api";

    /** Einzelne Karte */
    public static CardData fetchCard(long id) throws Exception {
        HttpRequest req = HttpRequest.newBuilder()
                .uri(URI.create(BASE + "/card/" + id))   //  <-- exakter Pfad!
                .GET()
                .build();

        HttpResponse<String> resp = HttpClient.newHttpClient()
                .send(req, HttpResponse.BodyHandlers.ofString());

        return new ObjectMapper().readValue(resp.body(), CardData.class);
    }

    /** Liste aller Karten  (funktioniert erst, wenn /api/cards im Server existiert) */
    public static List<CardData> fetchCards() throws Exception {
        HttpRequest req = HttpRequest.newBuilder()
                .uri(URI.create(BASE + "/cards"))
                .GET()
                .build();

        HttpResponse<String> resp = HttpClient.newHttpClient()
                .send(req, HttpResponse.BodyHandlers.ofString());

        return new ObjectMapper().readValue(
                resp.body(), new TypeReference<List<CardData>>() {});
    }
}
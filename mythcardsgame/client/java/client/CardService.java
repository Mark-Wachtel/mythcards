package client;

import java.net.http.*;
import java.net.URI;
import com.fasterxml.jackson.databind.ObjectMapper;

import common.CardData;

import com.fasterxml.jackson.core.type.TypeReference;
import java.util.List;

public class CardService {

    private static final String BASE = "http://localhost:8080/api";
    private static final HttpClient client = HttpClient.newHttpClient();
    private static final ObjectMapper objectMapper = new ObjectMapper();

    public static CardData fetchCard(long id) {
        try {
            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(BASE + "/card/" + id))
                    .GET()
                    .build();

            HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());

            if (response.statusCode() == 200 && !response.body().isBlank()) {
                return objectMapper.readValue(response.body(), CardData.class);
            } else {
                System.out.println("WARN: fetchCard - Server returned empty or error response: " + response.statusCode());
                return null;
            }
        } catch (IOException | InterruptedException e) {
            e.printStackTrace();
            return null;
        }
    }
}
package client;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import common.CardData;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;

public class CardServiceClient {
    
    private static final String BASE_URL = "http://localhost:8080/api";
    private final HttpClient httpClient;
    private final ObjectMapper objectMapper;
    private final String accessToken;
    
    public CardServiceClient(String accessToken) {
        this.httpClient = HttpClient.newHttpClient();
        this.objectMapper = new ObjectMapper();
        this.accessToken = accessToken;
    }
    
    /**
     * Holt alle verfügbaren Karten
     */
    public CompletableFuture<List<CardData>> getAllCards() {
        String url = BASE_URL + "/cards";
        
        HttpRequest request = HttpRequest.newBuilder()
            .uri(URI.create(url))
            .header("Authorization", "Bearer " + accessToken)
            .header("Accept", "application/json")
            .GET()
            .build();
            
        return httpClient.sendAsync(request, HttpResponse.BodyHandlers.ofString())
            .thenApply(response -> {
                if (response.statusCode() != 200) {
                    throw new RuntimeException("Fehler beim Abrufen der Karten: " + response.statusCode());
                }
                try {
                    return objectMapper.readValue(
                        response.body(), 
                        new TypeReference<List<CardData>>() {}
                    );
                } catch (Exception e) {
                    throw new RuntimeException("Fehler beim Parsen der Karten", e);
                }
            });
    }
    
    /**
     * Holt eine einzelne Karte
     */
    public CompletableFuture<CardData> getCard(UUID cardId) {
        String url = BASE_URL + "/card/" + cardId;
        
        HttpRequest request = HttpRequest.newBuilder()
            .uri(URI.create(url))
            .header("Authorization", "Bearer " + accessToken)
            .header("Accept", "application/json")
            .GET()
            .build();
            
        return httpClient.sendAsync(request, HttpResponse.BodyHandlers.ofString())
            .thenApply(response -> {
                if (response.statusCode() != 200) {
                    throw new RuntimeException("Fehler beim Abrufen der Karte: " + response.statusCode());
                }
                try {
                    return objectMapper.readValue(response.body(), CardData.class);
                } catch (Exception e) {
                    throw new RuntimeException("Fehler beim Parsen der Karte", e);
                }
            });
    }
}
package client;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import common.DeckDetailDto;
import common.DeckDto;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;

public class DeckServiceClient {
    
    private static final String BASE_URL = "http://localhost:8080/api/decks";
    private final HttpClient httpClient;
    private final ObjectMapper objectMapper;
    private final String accessToken;
    
    public DeckServiceClient(String accessToken) {
        this.httpClient = HttpClient.newHttpClient();
        this.objectMapper = new ObjectMapper();
        this.accessToken = accessToken;
    }
    
    public String getAccessToken() {
        return accessToken;
    }
    
    /**
     * Holt alle Decks eines Benutzers
     */
    public CompletableFuture<List<DeckDto>> getUserDecks(UUID userId) {
        String url = BASE_URL + "?userId=" + userId;
        
        HttpRequest request = HttpRequest.newBuilder()
            .uri(URI.create(url))
            .header("Authorization", "Bearer " + accessToken)
            .header("Accept", "application/json")
            .GET()
            .build();
            
        return httpClient.sendAsync(request, HttpResponse.BodyHandlers.ofString())
            .thenApply(response -> {
                if (response.statusCode() != 200) {
                    throw new RuntimeException("Fehler beim Abrufen der Decks: " + response.statusCode());
                }
                try {
                    return objectMapper.readValue(
                        response.body(), 
                        new TypeReference<List<DeckDto>>() {}
                    );
                } catch (Exception e) {
                    throw new RuntimeException("Fehler beim Parsen der Decks", e);
                }
            });
    }
    
    /**
     * Holt Details eines spezifischen Decks
     */
    public CompletableFuture<DeckDetailDto> getDeckDetails(UUID userId, UUID deckId) {
        String url = BASE_URL + "/" + deckId + "?userId=" + userId;
        
        HttpRequest request = HttpRequest.newBuilder()
            .uri(URI.create(url))
            .header("Authorization", "Bearer " + accessToken)
            .header("Accept", "application/json")
            .GET()
            .build();
            
        return httpClient.sendAsync(request, HttpResponse.BodyHandlers.ofString())
            .thenApply(response -> {
                if (response.statusCode() != 200) {
                    throw new RuntimeException("Fehler beim Abrufen des Decks: " + response.statusCode());
                }
                try {
                    return objectMapper.readValue(response.body(), DeckDetailDto.class);
                } catch (Exception e) {
                    throw new RuntimeException("Fehler beim Parsen des Decks", e);
                }
            });
    }
    
    /**
     * Erstellt ein neues Deck
     */
    public CompletableFuture<DeckDto> createDeck(UUID userId, String name, List<UUID> cardIds) {
        DeckDto dto = new DeckDto();
        dto.setUserId(userId);
        dto.setName(name);
        dto.setCardIds(cardIds);
        
        try {
            String jsonBody = objectMapper.writeValueAsString(dto);
            
            HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(BASE_URL))
                .header("Authorization", "Bearer " + accessToken)
                .header("Content-Type", "application/json")
                .header("Accept", "application/json")
                .POST(HttpRequest.BodyPublishers.ofString(jsonBody))
                .build();
                
            return httpClient.sendAsync(request, HttpResponse.BodyHandlers.ofString())
                .thenApply(response -> {
                    if (response.statusCode() != 201) {
                        throw new RuntimeException("Fehler beim Erstellen des Decks: " + response.statusCode());
                    }
                    try {
                        return objectMapper.readValue(response.body(), DeckDto.class);
                    } catch (Exception e) {
                        throw new RuntimeException("Fehler beim Parsen der Antwort", e);
                    }
                });
        } catch (Exception e) {
            CompletableFuture<DeckDto> future = new CompletableFuture<>();
            future.completeExceptionally(e);
            return future;
        }
    }
    
    /**
     * Aktualisiert ein bestehendes Deck
     */
    public CompletableFuture<DeckDto> updateDeck(UUID userId, UUID deckId, String name, List<UUID> cardIds) {
        DeckDto dto = new DeckDto();
        dto.setId(deckId);
        dto.setUserId(userId);
        dto.setName(name);
        dto.setCardIds(cardIds);
        
        try {
            String jsonBody = objectMapper.writeValueAsString(dto);
            
            HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(BASE_URL + "/" + deckId))
                .header("Authorization", "Bearer " + accessToken)
                .header("Content-Type", "application/json")
                .header("Accept", "application/json")
                .PUT(HttpRequest.BodyPublishers.ofString(jsonBody))
                .build();
                
            return httpClient.sendAsync(request, HttpResponse.BodyHandlers.ofString())
                .thenApply(response -> {
                    if (response.statusCode() != 200) {
                        throw new RuntimeException("Fehler beim Aktualisieren des Decks: " + response.statusCode());
                    }
                    try {
                        return objectMapper.readValue(response.body(), DeckDto.class);
                    } catch (Exception e) {
                        throw new RuntimeException("Fehler beim Parsen der Antwort", e);
                    }
                });
        } catch (Exception e) {
            CompletableFuture<DeckDto> future = new CompletableFuture<>();
            future.completeExceptionally(e);
            return future;
        }
    }
    
    /**
     * Löscht ein Deck
     */
    public CompletableFuture<Boolean> deleteDeck(UUID userId, UUID deckId) {
        String url = BASE_URL + "/" + deckId + "?userId=" + userId;
        
        HttpRequest request = HttpRequest.newBuilder()
            .uri(URI.create(url))
            .header("Authorization", "Bearer " + accessToken)
            .DELETE()
            .build();
            
        return httpClient.sendAsync(request, HttpResponse.BodyHandlers.ofString())
            .thenApply(response -> {
                if (response.statusCode() == 204) {
                    return true;
                } else {
                    throw new RuntimeException("Fehler beim Löschen des Decks: " + response.statusCode());
                }
            });
    }
}
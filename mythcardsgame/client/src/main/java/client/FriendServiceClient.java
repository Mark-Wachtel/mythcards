package client;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import common.FriendDTO;
import common.PendingRequestDTO;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionException;

public class FriendServiceClient {

    private static final String BASE_URL = "http://localhost:8080/api";
    private final HttpClient httpClient;
    private final ObjectMapper objectMapper;
    private String accessToken;

    public FriendServiceClient() {
        this.httpClient   = HttpClient.newHttpClient();
        this.objectMapper = new ObjectMapper();
    }

    public void setAccessToken(String token) {
        this.accessToken = token;
    }

    /**
     * Liste aller Freunde inkl. conversationId.
     */
    public CompletableFuture<List<FriendDTO>> fetchFriends(UUID userId) {
        String uri = String.format("%s/friends/list?userId=%s", BASE_URL, userId);
        HttpRequest.Builder b = HttpRequest.newBuilder()
            .uri(URI.create(uri))
            .GET()
            .header("Accept", "application/json");
        if (accessToken != null) {
            b.header("Authorization", "Bearer " + accessToken);
        }
        return httpClient.sendAsync(b.build(), HttpResponse.BodyHandlers.ofString())
          .thenApply(resp -> {
              if (resp.statusCode() != 200)
                  throw new CompletionException(new RuntimeException("fetchFriends failed: " + resp.statusCode()));
              System.out.println("RESPONSE BODY:");
              System.out.println(resp.body());
              return resp.body();
          })
          .thenApply(body -> {
              try {
                  return objectMapper.readValue(body, new TypeReference<List<FriendDTO>>() {});
              } catch (Exception e) {
                  throw new CompletionException(e);
              }
          });
    }

    /**
     * Alle anstehenden Friend-Requests.
     */
    public CompletableFuture<List<PendingRequestDTO>> fetchPendingRequests(UUID userId, String token) {
        String uri = String.format("%s/friends/requests?userId=%s", BASE_URL, userId);
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(uri))
                .GET()
                .header("Accept", "application/json")
                .header("Authorization", "Bearer " + token)
                .build();

        return httpClient.sendAsync(request, HttpResponse.BodyHandlers.ofString())
                .thenApply(HttpResponse::body)
                .thenApply(body -> {
                    try {
                        return objectMapper.readValue(body, new com.fasterxml.jackson.core.type.TypeReference<List<PendingRequestDTO>>() {});
                    } catch (Exception e) {
                        throw new CompletionException(e);
                    }
                });
    }

    /**
     * Hilfsmethode: löst username → UUID auf.
     */
    public CompletableFuture<UUID> fetchUserIdByUsername(String username) {
        String uri = String.format("%s/users/id?username=%s", BASE_URL, username);
        HttpRequest.Builder b = HttpRequest.newBuilder()
            .uri(URI.create(uri))
            .GET()
            .header("Accept", "application/json");
        if (accessToken != null) {
            b.header("Authorization", "Bearer " + accessToken);
        }
        return httpClient.sendAsync(b.build(), HttpResponse.BodyHandlers.ofString())
          .thenApply(resp -> {
              if (resp.statusCode() == 200 && !resp.body().isBlank()) {
                  return UUID.fromString(resp.body());
              }
              throw new CompletionException(new RuntimeException("User not found"));
          });
    }

    /**
     * Sendet einen Friend-Request.
     */
    public CompletableFuture<Boolean> sendFriendRequest(UUID senderId, UUID receiverId) {
        String uri = String.format("%s/friends/request?senderId=%s&receiverId=%s",
                                    BASE_URL, senderId, receiverId);
        HttpRequest.Builder b = HttpRequest.newBuilder()
            .uri(URI.create(uri))
            .POST(HttpRequest.BodyPublishers.noBody());
        if (accessToken != null) {
            b.header("Authorization", "Bearer " + accessToken);
        }
        return httpClient.sendAsync(b.build(), HttpResponse.BodyHandlers.discarding())
            .thenApply(resp -> resp.statusCode() == 200);
    }

    /**
     * Convenience-Overload: sendet Request an user via username.
     */
    public CompletableFuture<Boolean> sendFriendRequest(UUID senderId, String receiverUsername) {
        return fetchUserIdByUsername(receiverUsername)
            .thenCompose(receiverId -> sendFriendRequest(senderId, receiverId));
    }

    /**
     * Akzeptiert einen Pending-Request.
     */
    public CompletableFuture<Boolean> acceptFriendRequest(UUID requestId) {
        String uri = String.format("%s/friends/accept?requestId=%s", BASE_URL, requestId);
        HttpRequest.Builder b = HttpRequest.newBuilder()
            .uri(URI.create(uri))
            .POST(HttpRequest.BodyPublishers.noBody());
        if (accessToken != null) {
            b.header("Authorization", "Bearer " + accessToken);
        }
        return httpClient.sendAsync(b.build(), HttpResponse.BodyHandlers.discarding())
            .thenApply(resp -> resp.statusCode() == 200);
    }

    /**
     * Lehnt einen Pending-Request ab.
     */
    public CompletableFuture<Boolean> declineFriendRequest(UUID requestId) {
        String uri = String.format("%s/friends/decline?requestId=%s", BASE_URL, requestId);
        HttpRequest.Builder b = HttpRequest.newBuilder()
            .uri(URI.create(uri))
            .POST(HttpRequest.BodyPublishers.noBody());
        if (accessToken != null) {
            b.header("Authorization", "Bearer " + accessToken);
        }
        return httpClient.sendAsync(b.build(), HttpResponse.BodyHandlers.discarding())
            .thenApply(resp -> resp.statusCode() == 200);
    }
    /**
     * Entfernt eine Freundschaft.
     */
    public CompletableFuture<Boolean> removeFriend(UUID user1, UUID user2) {
        String uri = String.format("%s/friends/remove?user1=%s&user2=%s", BASE_URL, user1, user2);
        HttpRequest.Builder b = HttpRequest.newBuilder()
            .uri(URI.create(uri))
            .DELETE();
        if (accessToken != null) {
            b.header("Authorization", "Bearer " + accessToken);
        }
        return httpClient.sendAsync(b.build(), HttpResponse.BodyHandlers.discarding())
            .thenApply(resp -> resp.statusCode() == 200);
    }

    /**
     * Blockiert einen User.
     */
    public CompletableFuture<Boolean> blockUser(UUID blockerId, UUID targetId) {
        String uri = String.format("%s/friends/block?blockerId=%s&targetId=%s", BASE_URL, blockerId, targetId);
        HttpRequest.Builder b = HttpRequest.newBuilder()
            .uri(URI.create(uri))
            .POST(HttpRequest.BodyPublishers.noBody());
        if (accessToken != null) {
            b.header("Authorization", "Bearer " + accessToken);
        }
        return httpClient.sendAsync(b.build(), HttpResponse.BodyHandlers.discarding())
            .thenApply(resp -> resp.statusCode() == 200);
    }

    /**
     * Reportet einen User mit Grund.
     */
    public CompletableFuture<Boolean> reportUser(UUID reporterId, UUID targetId, String reason) {
        String uri = String.format("%s/friends/report?reporterId=%s&targetId=%s&reason=%s",
                                    BASE_URL, reporterId, targetId,
                                    URI.create(reason).toString());
        HttpRequest.Builder b = HttpRequest.newBuilder()
            .uri(URI.create(uri))
            .POST(HttpRequest.BodyPublishers.noBody());
        if (accessToken != null) {
            b.header("Authorization", "Bearer " + accessToken);
        }
        return httpClient.sendAsync(b.build(), HttpResponse.BodyHandlers.discarding())
            .thenApply(resp -> resp.statusCode() == 200);
    }
}
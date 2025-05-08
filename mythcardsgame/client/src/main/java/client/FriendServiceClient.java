package client;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionException;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;

import common.PendingRequestDTO;

public class FriendServiceClient {
    private static final String BASE_URL = "http://localhost:8080/api";
    private final HttpClient httpClient;
    private final ObjectMapper objectMapper;
    private String accessToken;

    public FriendServiceClient() {
        this.httpClient = HttpClient.newHttpClient();
        this.objectMapper = new ObjectMapper();
    }
     
    public void setAccessToken(String token) {
        this.accessToken = token;
        System.out.println("[DEBUG] Access token set: " + token);
    }

    /**
     * Fetch list of friend usernames for a given user.
     */
    public CompletableFuture<List<String>> fetchFriends(UUID userId) {
        String uri = String.format("%s/friends/list?userId=%s", BASE_URL, userId);
        System.out.println("[DEBUG] fetchFriends -> URI: " + uri);
        HttpRequest.Builder reqBuilder = HttpRequest.newBuilder()
                .uri(URI.create(uri))
                .GET();
        if (accessToken != null) {
            reqBuilder.header("Authorization", "Bearer " + accessToken);
            System.out.println("[DEBUG] fetchFriends -> Authorization header added");
        }
        HttpRequest request = reqBuilder.build();

        return httpClient.sendAsync(request, HttpResponse.BodyHandlers.ofString())
                .thenApply(response -> {
                    System.out.println("[DEBUG] fetchFriends -> Status: " + response.statusCode());
                    System.out.println("[DEBUG] fetchFriends -> Body: " + response.body());
                    return response.body();
                })
                .thenApply(json -> {
                    try {
                        List<String> list = objectMapper.readValue(json, new TypeReference<List<String>>() {});
                        System.out.println("[DEBUG] fetchFriends -> Parsed list size: " + list.size());
                        return list;
                    } catch (Exception e) {
                        System.err.println("[ERROR] fetchFriends -> JSON parsing failed: " + e.getMessage());
                        throw new CompletionException(e);
                    }
                })
                .exceptionally(ex -> {
                    System.err.println("[ERROR] fetchFriends -> Exception: " + ex.getMessage());
                    return List.of();
                });
    }

    /**
     * Resolve a user's UUID by their username.
     */
    public CompletableFuture<UUID> fetchUserIdByUsername(String username) {
        String uri = String.format("%s/users/id?username=%s", BASE_URL, username);
        System.out.println("[DEBUG] fetchUserIdByUsername -> URI: " + uri);
        HttpRequest.Builder reqBuilder = HttpRequest.newBuilder()
                .uri(URI.create(uri))
                .GET();
        if (accessToken != null) {
            reqBuilder.header("Authorization", "Bearer " + accessToken);
            System.out.println("[DEBUG] fetchUserIdByUsername -> Authorization header added");
        }
        HttpRequest request = reqBuilder.build();

        return httpClient.sendAsync(request, HttpResponse.BodyHandlers.ofString())
            .thenApply(response -> {
                System.out.println("[DEBUG] fetchUserIdByUsername -> Status: " + response.statusCode());
                System.out.println("[DEBUG] fetchUserIdByUsername -> Body: " + response.body());
                if (response.statusCode() == 200 && !response.body().isEmpty()) {
                    try {
                        UUID uid = UUID.fromString(response.body());
                        System.out.println("[DEBUG] fetchUserIdByUsername -> Parsed UUID: " + uid);
                        return uid;
                    } catch (Exception e) {
                        System.err.println("[ERROR] fetchUserIdByUsername -> Invalid UUID: " + e.getMessage());
                        return null;
                    }
                }
                return null;
            })
            .exceptionally(ex -> {
                System.err.println("[ERROR] fetchUserIdByUsername -> Exception: " + ex.getMessage());
                return null;
            });
    }

    /**
     * Send a friend request from sender to receiver.
     */
    public CompletableFuture<Boolean> sendFriendRequest(UUID senderId, UUID receiverId) {
        String uri = String.format("%s/friends/request?senderId=%s&receiverId=%s", BASE_URL, senderId, receiverId);
        System.out.println("[DEBUG] sendFriendRequest -> URI: " + uri);
        HttpRequest.Builder reqBuilder = HttpRequest.newBuilder()
                .uri(URI.create(uri))
                .POST(HttpRequest.BodyPublishers.noBody());
        if (accessToken != null) {
            reqBuilder.header("Authorization", "Bearer " + accessToken);
            System.out.println("[DEBUG] sendFriendRequest -> Authorization header added");
        }
        HttpRequest request = reqBuilder.build();

        return httpClient.sendAsync(request, HttpResponse.BodyHandlers.discarding())
                .thenApply(response -> {
                    System.out.println("[DEBUG] sendFriendRequest -> Status: " + response.statusCode());
                    return response.statusCode() == 200;
                })
                .exceptionally(ex -> {
                    System.err.println("[ERROR] sendFriendRequest -> Exception: " + ex.getMessage());
                    return false;
                });
    }

    /**
     * Accept a friend request.
     */
    public CompletableFuture<Boolean> acceptFriendRequest(UUID requestId) {
        String uri = String.format("%s/friends/accept?requestId=%s", BASE_URL, requestId);
        System.out.println("[DEBUG] acceptFriendRequest -> URI: " + uri);
        HttpRequest.Builder reqBuilder = HttpRequest.newBuilder()
                .uri(URI.create(uri))
                .POST(HttpRequest.BodyPublishers.noBody());
        if (accessToken != null) {
            reqBuilder.header("Authorization", "Bearer " + accessToken);
            System.out.println("[DEBUG] acceptFriendRequest -> Authorization header added");
        }
        HttpRequest request = reqBuilder.build();

        return httpClient.sendAsync(request, HttpResponse.BodyHandlers.discarding())
                .thenApply(response -> {
                    System.out.println("[DEBUG] acceptFriendRequest -> Status: " + response.statusCode());
                    return response.statusCode() == 200;
                })
                .exceptionally(ex -> {
                    System.err.println("[ERROR] acceptFriendRequest -> Exception: " + ex.getMessage());
                    return false;
                });
    }

    /**
     * Decline a friend request.
     */
    public CompletableFuture<Boolean> declineFriendRequest(UUID requestId) {
        String uri = String.format("%s/friends/decline?requestId=%s", BASE_URL, requestId);
        System.out.println("[DEBUG] declineFriendRequest -> URI: " + uri);
        HttpRequest.Builder reqBuilder = HttpRequest.newBuilder()
                .uri(URI.create(uri))
                .POST(HttpRequest.BodyPublishers.noBody());
        if (accessToken != null) {
            reqBuilder.header("Authorization", "Bearer " + accessToken);
            System.out.println("[DEBUG] declineFriendRequest -> Authorization header added");
        }
        HttpRequest request = reqBuilder.build();

        return httpClient.sendAsync(request, HttpResponse.BodyHandlers.discarding())
                .thenApply(response -> {
                    System.out.println("[DEBUG] declineFriendRequest -> Status: " + response.statusCode());
                    return response.statusCode() == 200;
                })
                .exceptionally(ex -> {
                    System.err.println("[ERROR] declineFriendRequest -> Exception: " + ex.getMessage());
                    return false;
                });
    }

    /**
     * Remove an existing friend.
     */
    public CompletableFuture<Boolean> removeFriend(UUID user1, UUID user2) {
        String uri = String.format("%s/friends/remove?user1=%s&user2=%s", BASE_URL, user1, user2);
        System.out.println("[DEBUG] removeFriend -> URI: " + uri);
        HttpRequest.Builder reqBuilder = HttpRequest.newBuilder()
                .uri(URI.create(uri))
                .DELETE();
        if (accessToken != null) {
            reqBuilder.header("Authorization", "Bearer " + accessToken);
            System.out.println("[DEBUG] removeFriend -> Authorization header added");
        }
        HttpRequest request = reqBuilder.build();

        return httpClient.sendAsync(request, HttpResponse.BodyHandlers.discarding())
                .thenApply(response -> {
                    System.out.println("[DEBUG] removeFriend -> Status: " + response.statusCode());
                    return response.statusCode() == 200;
                })
                .exceptionally(ex -> {
                    System.err.println("[ERROR] removeFriend -> Exception: " + ex.getMessage());
                    return false;
                });
    }

    /**
     * Block a user.
     */
    public CompletableFuture<Boolean> blockUser(UUID blockerId, UUID targetId) {
        String uri = String.format("%s/friends/block?blockerId=%s&targetId=%s", BASE_URL, blockerId, targetId);
        System.out.println("[DEBUG] blockUser -> URI: " + uri);
        HttpRequest.Builder reqBuilder = HttpRequest.newBuilder()
                .uri(URI.create(uri))
                .POST(HttpRequest.BodyPublishers.noBody());
        if (accessToken != null) {
            reqBuilder.header("Authorization", "Bearer " + accessToken);
            System.out.println("[DEBUG] blockUser -> Authorization header added");
        }
        HttpRequest request = reqBuilder.build();

        return httpClient.sendAsync(request, HttpResponse.BodyHandlers.discarding())
                .thenApply(response -> {
                    System.out.println("[DEBUG] blockUser -> Status: " + response.statusCode());
                    return response.statusCode() == 200;
                })
                .exceptionally(ex -> {
                    System.err.println("[ERROR] blockUser -> Exception: " + ex.getMessage());
                    return false;
                });
    }

    /**
     * Report a user.
     */
    public CompletableFuture<Boolean> reportUser(UUID reporterId, UUID targetId, String reason) {
        String uri = String.format("%s/friends/report?reporterId=%s&targetId=%s&reason=%s", BASE_URL, reporterId, targetId, reason);
        System.out.println("[DEBUG] reportUser -> URI: " + uri);
        HttpRequest.Builder reqBuilder = HttpRequest.newBuilder()
                .uri(URI.create(uri))
                .POST(HttpRequest.BodyPublishers.noBody());
        if (accessToken != null) {
            reqBuilder.header("Authorization", "Bearer " + accessToken);
            System.out.println("[DEBUG] reportUser -> Authorization header added");
        }
        HttpRequest request = reqBuilder.build();

        return httpClient.sendAsync(request, HttpResponse.BodyHandlers.discarding())
                .thenApply(response -> {
                    System.out.println("[DEBUG] reportUser -> Status: " + response.statusCode());
                    return response.statusCode() == 200;
                })
                .exceptionally(ex -> {
                    System.err.println("[ERROR] reportUser -> Exception: " + ex.getMessage());
                    return false;
                });
    }
    
    public CompletableFuture<List<PendingRequestDTO>> fetchPendingRequests(UUID userId) {
        String uri = String.format("%s/friends/requests?userId=%s", BASE_URL, userId);
        HttpRequest.Builder b = HttpRequest.newBuilder()
            .uri(URI.create(uri))
            .GET()
            .header("Authorization", "Bearer " + accessToken);

        return httpClient.sendAsync(b.build(), HttpResponse.BodyHandlers.ofString())
          .thenApply(HttpResponse::body)
          .thenApply(json -> {
              try {
                  return objectMapper.readValue(
                    json,
                    new TypeReference<List<PendingRequestDTO>>() {}
                  );
              } catch (Exception e) {
                  throw new CompletionException(e);
              }
          })
          .exceptionally(e -> List.of());
    }
}

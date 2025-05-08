package client;

public class AuthTokenStore {

    private static AuthTokenStore instance;
    private String accessToken;

    private AuthTokenStore() {
    }

    public static synchronized AuthTokenStore getInstance() {
        if (instance == null) {
            instance = new AuthTokenStore();
        }
        return instance;
    }

    public void setAccessToken(String token) {
        this.accessToken = token;
    }

    public String getAccessToken() {
        return accessToken;
    }

    public boolean hasToken() {
        return accessToken != null && !accessToken.isEmpty();
    }

    public void clear() {
        this.accessToken = null;
    }
}
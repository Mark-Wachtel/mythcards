package common;

public record LoginRequest(String username, String password) {

    public String getUsername() {
        return username;
    }

    public String getPassword() {
        return password;
    }
}
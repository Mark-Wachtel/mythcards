package common;

public record RegisterRequest(String username, String password) {

    public String getUsername() {
        return username;
    }

    public String getPassword() {
        return password;
    }
}
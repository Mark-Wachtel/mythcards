package common;

import java.util.List;
import java.util.UUID;


public record SimpleUser(UUID userId, String username, String password, List<String> roles) {

    public UUID getId() {
        return userId;
    }

    public String getUsername() {
        return username;
    }

    public String getPassword() {
        return password;
    }

    public List<String> getRoles() {
        return roles;
    }
}
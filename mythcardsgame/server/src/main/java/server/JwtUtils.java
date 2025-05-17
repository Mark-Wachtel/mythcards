package server;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.stereotype.Component;

import java.util.UUID;

@Component
public class JwtUtils {

    private final JwtDecoder decoder;

    @Autowired
    public JwtUtils(JwtDecoder decoder) {
        this.decoder = decoder;
    }

    public UUID extractUserId(String token) {
        Jwt jwt = decoder.decode(token);
        return UUID.fromString(jwt.getSubject());
    }
}
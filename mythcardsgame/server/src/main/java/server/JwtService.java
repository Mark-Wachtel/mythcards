package server;

import java.time.Duration;
import java.time.Instant;
import java.util.Date;
import java.util.List;
import java.util.UUID;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import javax.crypto.SecretKey; // <-- Diese Zeile beachten!

@Service
public class JwtService {

    @Value("${jwt.secret}")
    private String secret;

    @Value("${jwt.exp}")
    private Duration exp;

    private SecretKey key() {
        return Keys.hmacShaKeyFor(secret.getBytes());
    }

    // Neue Methode: Token mit Rollen generieren
    public String generate(UUID userId, List<String> roles) {
        Instant now = Instant.now();
        return Jwts.builder()
                .subject(userId.toString())
                .issuedAt(Date.from(now))
                .expiration(Date.from(now.plus(exp)))
                .claim("roles", roles) // <-- Claim für Rollen
                .signWith(key())
                .compact();
    }

    // Optional: einfache Variante ohne Rollen (setzt nur USER)
    public String generateBasic(UUID userId) {
        return generate(userId, List.of("USER"));
    }

    public UUID validate(String token) {
        return UUID.fromString(
            Jwts.parser().verifyWith(key()).build()
                .parseSignedClaims(token)
                .getPayload().getSubject()
        );
    }

    // Optional: Rolle extrahieren
    public List<String> extractRoles(String token) {
        var claims = Jwts.parser().verifyWith(key()).build()
                .parseSignedClaims(token)
                .getPayload();
        return claims.get("roles", List.class);
    }
}
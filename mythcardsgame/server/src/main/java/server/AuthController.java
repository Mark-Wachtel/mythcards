package server;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import common.AuthResponse;
import common.LoginRequest;
import common.RegisterRequest;
import common.SimpleUser;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/auth")
public class AuthController {

    @Autowired
    private JwtService jwtService;

    @Autowired
    private DBManager dbManager;

    @PostMapping("/login")
    public ResponseEntity<AuthResponse> login(@RequestBody LoginRequest request) {
        
        SimpleUser user = dbManager.findUserByUsername(request.getUsername());

        if (user == null || !request.getPassword().equals(user.getPassword())) {
            return new ResponseEntity<>(HttpStatus.UNAUTHORIZED);
        }

        String userId       = user.getId().toString();
        String accessToken = jwtService.generate(user.getId(),user.getRoles());
        String refreshToken = ""; // später bauen
        long expiresIn = 3600L; // Sekunden

        AuthResponse response = new AuthResponse(userId, accessToken, refreshToken, expiresIn);
        return new ResponseEntity<>(response, HttpStatus.OK);
    }

    @PostMapping("/register")
    public ResponseEntity<AuthResponse> register(@RequestBody RegisterRequest request) {

        SimpleUser existingUser = dbManager.findUserByUsername(request.getUsername());
        if (existingUser != null) {
            return new ResponseEntity<>(HttpStatus.CONFLICT); // Username schon vergeben
        }

        UUID userId = UUID.randomUUID();
        List<String> roles = new ArrayList<>();
        roles.add("USER");
        dbManager.createUser(userId, request.getUsername(), request.getPassword(),roles);

        String userId1       = userId.toString();
        String accessToken = jwtService.generate(userId,roles);
        String refreshToken = ""; 
        long expiresIn = 3600L;

        AuthResponse response = new AuthResponse(userId1, accessToken, refreshToken, expiresIn);
        return new ResponseEntity<>(response, HttpStatus.OK);
    }
}
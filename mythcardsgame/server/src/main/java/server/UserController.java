package server;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.sql.*;
import java.util.UUID;

@RestController
@RequestMapping("/api/users")
public class UserController {

    /**
     * Resolve a user's UUID by their username.
     */
    @GetMapping("/id")
    public ResponseEntity<String> getUserIdByUsername(@RequestParam String username) {
        String sql = "SELECT id FROM users WHERE username = ?";
        try (Connection conn = DBManager.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, username);
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    String id = rs.getString("id");
                    return ResponseEntity.ok(id);
                } else {
                    return ResponseEntity.notFound().build();
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
            return ResponseEntity.status(500).body("Error querying user");
        }
    }
}

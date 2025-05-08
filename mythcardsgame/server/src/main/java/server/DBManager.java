package server;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;
import java.util.UUID;
import java.time.LocalDate;

import org.springframework.stereotype.Service;

import common.SimpleUser;

@Service
public class DBManager {

    private static final String URL = "jdbc:mysql://localhost:3306/myth_cards";
    private static final String USER = "root";
    private static final String PASSWORD = "Le@gueOfLegendz2018!";

    public static Connection getConnection() throws SQLException {
        return DriverManager.getConnection(URL, USER, PASSWORD);
    }

    public SimpleUser findUserByUsername(String username) {
        String sql = "SELECT id, username, pwd_hash, role FROM users WHERE username = ?";
        try (Connection conn = getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, username);
            ResultSet rs = stmt.executeQuery();

            if (rs.next()) {
                UUID userId = UUID.fromString(rs.getString("id"));
                String user = rs.getString("username");
                String pass = rs.getString("pwd_hash");
                String roleStr = rs.getString("role"); // z.B. "USER,ADMIN"

                List<String> roles = List.of(roleStr.split(","));
                return new SimpleUser(userId, user, pass, roles);
            } else {
                return null;
            }
        } catch (SQLException e) {
            e.printStackTrace();
            return null;
        }
    }

    public void createUser(UUID userId, String username, String password, List<String> roles) {
        String sql = "INSERT INTO users (id, username, pwd_hash, avatar_url, `rank`, experience, level, registered, email, role) " +
                     "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";

        try (Connection conn = getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, userId.toString());
            stmt.setString(2, username);
            stmt.setString(3, password);
            stmt.setString(4, "default.png"); // avatar_url
            stmt.setInt(5, 1);                // rank
            stmt.setInt(6, 0);                // experience
            stmt.setInt(7, 1);                // level
            stmt.setString(8, LocalDate.now().toString()); // registered
            stmt.setString(9, username + "@myth.local");   // email (Platzhalter)
            stmt.setString(10, String.join(",", roles));  // role als CSV

            stmt.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

}


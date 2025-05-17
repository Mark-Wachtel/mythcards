package server;

import java.nio.ByteBuffer;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.time.LocalDate;

import org.springframework.stereotype.Service;

import common.SimpleUser;

@Service
public class DBManager {

    private static final String URL      = "jdbc:mysql://localhost:3306/myth_cards";
    private static final String USER     = "root";
    private static final String PASSWORD = "Le@gueOfLegendz2018!";

    public static Connection getConnection() throws SQLException {
        return DriverManager.getConnection(URL, USER, PASSWORD);
    }

    // Hilfs-Methoden zum (De-)Serialisieren der UUID als 16-Byte
    private static byte[] uuidToBytes(UUID uuid) {
        ByteBuffer bb = ByteBuffer.allocate(16);
        bb.putLong(uuid.getMostSignificantBits());
        bb.putLong(uuid.getLeastSignificantBits());
        return bb.array();
    }

    private static UUID bytesToUuid(byte[] bytes) {
        ByteBuffer bb = ByteBuffer.wrap(bytes);
        long hi = bb.getLong();
        long lo = bb.getLong();
        return new UUID(hi, lo);
    }

    /**
     * Liest Benutzer + Passwort + Rollen ein.
     */
    public SimpleUser findUserByUsername(String username) {
        String sqlUser = """
            SELECT id, username, pwd_hash
              FROM users
             WHERE username = ?
        """;
        try (Connection conn = getConnection();
             PreparedStatement psUser = conn.prepareStatement(sqlUser)) {

            psUser.setString(1, username);
            try (ResultSet rs = psUser.executeQuery()) {
                if (!rs.next()) {
                    return null;
                }

                UUID userId = bytesToUuid(rs.getBytes("id"));
                String user = rs.getString("username");
                String pass = rs.getString("pwd_hash");

                // Rollen aus user_roles holen
                String sqlRoles = "SELECT role FROM user_roles WHERE user_id = ?";
                try (PreparedStatement psRoles = conn.prepareStatement(sqlRoles)) {
                    psRoles.setBytes(1, uuidToBytes(userId));
                    try (ResultSet rsr = psRoles.executeQuery()) {
                        List<String> roles = new ArrayList<>();
                        while (rsr.next()) {
                            roles.add(rsr.getString("role"));
                        }
                        return new SimpleUser(userId, user, pass, roles);
                    }
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
            return null;
        }
    }

    /**
     * Legt neuen Benutzer an und schreibt anschließend die Rollen in user_roles.
     */
    public void createUser(UUID userId,
                           String username,
                           String password,
                           List<String> roles) {
        String sqlUser = """
            INSERT INTO users
              (id, username, pwd_hash, avatar_url, `rank`, experience, level, registered, email)
            VALUES
              (?, ?, ?, ?, ?, ?, ?, ?, ?)
        """;
        String sqlRole = "INSERT INTO user_roles (user_id, role) VALUES (?, ?)";
        try (Connection conn = getConnection()) {
            conn.setAutoCommit(false);

            // 1) users-Tabelle
            try (PreparedStatement psUser = conn.prepareStatement(sqlUser)) {
                psUser.setBytes(1, uuidToBytes(userId));
                psUser.setString(2, username);
                psUser.setString(3, password);
                psUser.setString(4, "default.png");
                psUser.setInt(5, 1);
                psUser.setInt(6, 0);
                psUser.setInt(7, 1);
                psUser.setString(8, LocalDate.now().toString());
                psUser.setString(9, username + "@myth.local");
                psUser.executeUpdate();
            }

            // 2) user_roles-Tabelle
            try (PreparedStatement psRole = conn.prepareStatement(sqlRole)) {
                psRole.setBytes(1, uuidToBytes(userId));
                for (String role : roles) {
                    psRole.setString(2, role);
                    psRole.addBatch();
                }
                psRole.executeBatch();
            }

            conn.commit();
        } catch (SQLException e) {
            e.printStackTrace();
            // Optional: rollback hier einfügen
        }
    }
}
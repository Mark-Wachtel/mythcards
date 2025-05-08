package server;

import org.springframework.stereotype.Service;

import common.PendingRequestDTO;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Service
public class FriendService {

    /**
     * Fetches the usernames of all friends for the given userId.
     */
    public List<String> getFriendUsernames(UUID userId) {
        String sql = "SELECT u.username " +
                     "FROM users u " +
                     "JOIN friends f ON (f.user1_id = ? AND f.user2_id = u.id) " +
                     "OR (f.user2_id = ? AND f.user1_id = u.id)";
        List<String> friends = new ArrayList<>();
        try (Connection conn = DBManager.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            String id = userId.toString();
            stmt.setString(1, id);
            stmt.setString(2, id);

            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    friends.add(rs.getString("username"));
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return friends;
    }

    // existing methods ...

    public boolean sendFriendRequest(UUID senderId, UUID receiverId) {
        String sql = "INSERT INTO friend_requests (id, sender_id, receiver_id, created_at, expires_at, status) VALUES (?, ?, ?, NOW(), DATE_ADD(NOW(), INTERVAL 30 DAY), 'PENDING')";
        try (Connection conn = DBManager.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, UUID.randomUUID().toString());
            stmt.setString(2, senderId.toString());
            stmt.setString(3, receiverId.toString());

            stmt.executeUpdate();
            return true;
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    public boolean acceptFriendRequest(UUID requestId) {
        String getRequestSql = "SELECT sender_id, receiver_id FROM friend_requests WHERE id = ? AND status = 'PENDING'";
        String updateRequestSql = "UPDATE friend_requests SET status = 'ACCEPTED' WHERE id = ?";
        String insertFriendSql = "INSERT INTO friends (user1_id, user2_id, since) VALUES (?, ?, NOW())";

        try (Connection conn = DBManager.getConnection()) {
            conn.setAutoCommit(false);

            try (PreparedStatement getRequest = conn.prepareStatement(getRequestSql);
                 PreparedStatement updateRequest = conn.prepareStatement(updateRequestSql);
                 PreparedStatement insertFriend = conn.prepareStatement(insertFriendSql)) {

                getRequest.setString(1, requestId.toString());
                ResultSet rs = getRequest.executeQuery();

                if (rs.next()) {
                    String user1 = rs.getString("sender_id");
                    String user2 = rs.getString("receiver_id");

                    updateRequest.setString(1, requestId.toString());
                    updateRequest.executeUpdate();

                    insertFriend.setString(1, user1);
                    insertFriend.setString(2, user2);
                    insertFriend.executeUpdate();

                    conn.commit();
                    return true;
                } else {
                    conn.rollback();
                    return false;
                }
            } catch (SQLException ex) {
                conn.rollback();
                ex.printStackTrace();
                return false;
            } finally {
                conn.setAutoCommit(true);
            }
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    public boolean declineFriendRequest(UUID requestId) {
        String sql = "UPDATE friend_requests SET status = 'DECLINED' WHERE id = ?";
        try (Connection conn = DBManager.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, requestId.toString());
            return stmt.executeUpdate() == 1;
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    public boolean removeFriend(UUID user1, UUID user2) {
        String sql = "DELETE FROM friends WHERE (user1_id = ? AND user2_id = ?) OR (user1_id = ? AND user2_id = ?)";
        try (Connection conn = DBManager.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, user1.toString());
            stmt.setString(2, user2.toString());
            stmt.setString(3, user2.toString());
            stmt.setString(4, user1.toString());

            return stmt.executeUpdate() > 0;
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    public boolean blockUser(UUID blockerId, UUID targetId) {
        String sql = "INSERT INTO blocks (blocker_id, target_id, since) VALUES (?, ?, NOW())";
        try (Connection conn = DBManager.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, blockerId.toString());
            stmt.setString(2, targetId.toString());
            stmt.executeUpdate();
            return true;
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    public boolean reportUser(UUID reporterId, UUID targetId, String reason) {
        String sql = "INSERT INTO reports (id, reporter_id, target_id, reason, reported_at) VALUES (?, ?, ?, ?, NOW())";
        try (Connection conn = DBManager.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, UUID.randomUUID().toString());
            stmt.setString(2, reporterId.toString());
            stmt.setString(3, targetId.toString());
            stmt.setString(4, reason);
            stmt.executeUpdate();
            return true;
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }
    
    /**
     * Holt alle PENDING-Requests, die an userId gerichtet sind.
     */

        /**
         * Holt alle pending Requests für einen User, inklusive Ablaufdatum.
         */
        public List<PendingRequestDTO> getPendingRequests(UUID userId) {
            String sql =
              "SELECT fr.id, fr.sender_id, u.username AS sender_username, fr.expires_at AS expiresAt " +
              "FROM friend_requests fr " +
              "  JOIN users u ON u.id = fr.sender_id " +
              "WHERE fr.receiver_id = ? AND fr.status = 'PENDING'";
            List<PendingRequestDTO> list = new ArrayList<>();
            try (Connection conn = DBManager.getConnection();
                 PreparedStatement stmt = conn.prepareStatement(sql)) {

                stmt.setString(1, userId.toString());
                try (ResultSet rs = stmt.executeQuery()) {
                    while (rs.next()) {
                        // Jetzt stimmt der Alias „expiresAt“
                        String expiresAt = rs.getString("expiresAt");
                        list.add(new PendingRequestDTO(
                            rs.getString("id"),
                            rs.getString("sender_id"),
                            rs.getString("sender_username"),
                            expiresAt
                        ));
                    }
                }
            } catch (SQLException e) {
                e.printStackTrace();
            }
            return list;
        }
}

package server;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import common.PendingRequestDTO;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/friends")
public class FriendController {

    @Autowired
    private FriendService friendService;

    /**
     * Get the list of friend usernames for a given user.
     */
    @GetMapping("/list")
    public ResponseEntity<List<String>> listFriends(@RequestParam UUID userId) {
        List<String> friends = friendService.getFriendUsernames(userId);
        return ResponseEntity.ok(friends);
    }

    /**
     * Get all pending friend requests received by a user.
     */
    @GetMapping("/requests")
    public ResponseEntity<List<PendingRequestDTO>> getPendingRequests(@RequestParam UUID userId) {
        List<PendingRequestDTO> requests = friendService.getPendingRequests(userId);
        return ResponseEntity.ok(requests);
    }

    @PostMapping("/request")
    public ResponseEntity<String> sendFriendRequest(@RequestParam UUID senderId,
                                                    @RequestParam UUID receiverId) {
        if (friendService.sendFriendRequest(senderId, receiverId)) {
            return ResponseEntity.ok("Friend request sent.");
        }
        return ResponseEntity.badRequest().body("Failed to send friend request.");
    }

    @PostMapping("/accept")
    public ResponseEntity<String> acceptFriendRequest(@RequestParam UUID requestId) {
        if (friendService.acceptFriendRequest(requestId)) {
            return ResponseEntity.ok("Friend request accepted.");
        }
        return ResponseEntity.badRequest().body("Failed to accept friend request.");
    }

    @PostMapping("/decline")
    public ResponseEntity<String> declineFriendRequest(@RequestParam UUID requestId) {
        if (friendService.declineFriendRequest(requestId)) {
            return ResponseEntity.ok("Friend request declined.");
        }
        return ResponseEntity.badRequest().body("Failed to decline friend request.");
    }

    @DeleteMapping("/remove")
    public ResponseEntity<String> removeFriend(@RequestParam UUID user1,
                                               @RequestParam UUID user2) {
        if (friendService.removeFriend(user1, user2)) {
            return ResponseEntity.ok("Friend removed.");
        }
        return ResponseEntity.badRequest().body("Failed to remove friend.");
    }

    @PostMapping("/block")
    public ResponseEntity<String> blockUser(@RequestParam UUID blockerId,
                                            @RequestParam UUID targetId) {
        if (friendService.blockUser(blockerId, targetId)) {
            return ResponseEntity.ok("User blocked.");
        }
        return ResponseEntity.badRequest().body("Failed to block user.");
    }

    @PostMapping("/report")
    public ResponseEntity<String> reportUser(@RequestParam UUID reporterId,
                                             @RequestParam UUID targetId,
                                             @RequestParam String reason) {
        if (friendService.reportUser(reporterId, targetId, reason)) {
            return ResponseEntity.ok("User reported.");
        }
        return ResponseEntity.badRequest().body("Failed to report user.");
    }
}

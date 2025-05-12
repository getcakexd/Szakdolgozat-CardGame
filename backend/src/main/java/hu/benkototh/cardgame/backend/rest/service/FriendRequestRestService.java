package hu.benkototh.cardgame.backend.rest.service;

import hu.benkototh.cardgame.backend.rest.controller.FriendRequestController;
import hu.benkototh.cardgame.backend.rest.Data.FriendRequest;
import hu.benkototh.cardgame.backend.rest.controller.FriendshipController;
import hu.benkototh.cardgame.backend.rest.controller.UserController;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/friends/request")
@Tag(name = "Friend Requests", description = "Operations for managing friend requests")
public class FriendRequestRestService {

    @Autowired
    private FriendRequestController friendRequestController;
    @Autowired
    private UserController userController;
    @Autowired
    private FriendshipController friendshipController;

    @Operation(summary = "Send friend request", description = "Sends a friend request to another user")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Friend request sent successfully"),
            @ApiResponse(responseCode = "400", description = "Invalid request - cannot send to self, already friends, or request already sent"),
            @ApiResponse(responseCode = "404", description = "User not found")
    })
    @PostMapping("/send")
    public ResponseEntity<Map<String, String>> sendFriendRequest(
            @Parameter(description = "Sender user ID", required = true) @RequestParam long senderId,
            @Parameter(description = "Receiver username", required = true) @RequestParam String receiverUsername) {
        Map<String, String> response = new HashMap<>();

        FriendRequest request = friendRequestController.sendFriendRequest(senderId, receiverUsername);

        if (request == null) {
            if (userController.findByUsername(receiverUsername) == null) {
                response.put("message", "Receiver not found.");
                return ResponseEntity.status(404).body(response);
            }

            if (senderId == userController.findByUsername(receiverUsername).getId()) {
                response.put("message", "You cannot send friend request to yourself.");
                return ResponseEntity.status(400).body(response);
            }

            if (friendRequestController.requestExists(
                    userController.findByUsername(receiverUsername),
                    userController.findByUsername(receiverUsername))) {
                response.put("message", "Friend request already sent.");
                return ResponseEntity.status(400).body(response);
            }

            if (friendshipController.friendshipExists(
                    userController.findByUsername(receiverUsername),
                    userController.findByUsername(receiverUsername))) {
                response.put("message", "You are already friends.");
                return ResponseEntity.status(400).body(response);
            }

            response.put("message", "Sender not found.");
            return ResponseEntity.status(404).body(response);
        }

        response.put("message", "Friend request sent.");
        return ResponseEntity.ok(response);
    }

    @Operation(summary = "Get pending requests", description = "Retrieves all pending friend requests for a user")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Successfully retrieved pending requests",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = FriendRequest.class)))
    })
    @GetMapping("/requests")
    public ResponseEntity<List<FriendRequest>> getPendingRequests(
            @Parameter(description = "User ID", required = true) @RequestParam long userId) {
        return ResponseEntity.ok(friendRequestController.getPendingRequests(userId));
    }

    @Operation(summary = "Get sent requests", description = "Retrieves all friend requests sent by a user")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Successfully retrieved sent requests",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = FriendRequest.class)))
    })
    @GetMapping("/sent")
    public ResponseEntity<List<FriendRequest>> getSentRequests(
            @Parameter(description = "User ID", required = true) @RequestParam long userId) {
        return ResponseEntity.ok(friendRequestController.getSentRequests(userId));
    }

    @Operation(summary = "Accept friend request", description = "Accepts a pending friend request")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Friend request accepted successfully"),
            @ApiResponse(responseCode = "404", description = "Friend request not found")
    })
    @PostMapping("/accept")
    public ResponseEntity<Map<String, String>> acceptFriendRequest(
            @Parameter(description = "Request ID", required = true) @RequestParam long requestId) {
        Map<String, String> response = new HashMap<>();

        FriendRequest request = friendRequestController.acceptFriendRequest(requestId);

        if (request == null) {
            response.put("message", "Friend request not found.");
            return ResponseEntity.status(404).body(response);
        }

        response.put("message", "Friend request accepted.");
        return ResponseEntity.ok(response);
    }

    @Operation(summary = "Decline friend request", description = "Declines a pending friend request")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Friend request declined successfully"),
            @ApiResponse(responseCode = "404", description = "Friend request not found")
    })
    @DeleteMapping("/decline")
    public ResponseEntity<Map<String, String>> declineFriendRequest(
            @Parameter(description = "Request ID", required = true) @RequestParam long requestId) {
        Map<String, String> response = new HashMap<>();

        FriendRequest request = friendRequestController.declineFriendRequest(requestId);

        if (request == null) {
            response.put("message", "Friend request not found.");
            return ResponseEntity.status(404).body(response);
        }

        response.put("message", "Friend request declined.");
        return ResponseEntity.ok(response);
    }

    @Operation(summary = "Cancel friend request", description = "Cancels a previously sent friend request")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Friend request cancelled successfully"),
            @ApiResponse(responseCode = "404", description = "Friend request not found")
    })
    @DeleteMapping("/cancel")
    public ResponseEntity<Map<String, String>> cancelFriendRequest(
            @Parameter(description = "Request ID", required = true) @RequestParam long requestId) {
        Map<String, String> response = new HashMap<>();

        boolean canceled = friendRequestController.cancelFriendRequest(requestId);

        if (!canceled) {
            response.put("message", "Friend request not found.");
            return ResponseEntity.status(404).body(response);
        }

        response.put("message", "Friend request cancelled.");
        return ResponseEntity.ok(response);
    }
}
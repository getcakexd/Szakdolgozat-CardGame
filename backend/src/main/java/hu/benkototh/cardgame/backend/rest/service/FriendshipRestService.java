package hu.benkototh.cardgame.backend.rest.service;

import hu.benkototh.cardgame.backend.rest.controller.FriendshipController;
import hu.benkototh.cardgame.backend.rest.Data.User;
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
@RequestMapping("/api/friends")
@Tag(name = "Friendships", description = "Operations for managing user friendships")
public class FriendshipRestService {

    @Autowired
    private FriendshipController friendshipController;

    @Operation(summary = "Get user's friends", description = "Retrieves a list of all friends for a specific user")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Successfully retrieved friends list",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = User.class))),
            @ApiResponse(responseCode = "404", description = "User not found")
    })
    @GetMapping("/list")
    public ResponseEntity<List<User>> getFriends(
            @Parameter(description = "User ID", required = true) @RequestParam long userId) {
        List<User> friends = friendshipController.getFriends(userId);

        if (friends == null) {
            return ResponseEntity.status(404).body(null);
        }

        return ResponseEntity.ok(friends);
    }

    @Operation(summary = "Remove friend", description = "Removes a friendship between two users")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Friendship removed successfully"),
            @ApiResponse(responseCode = "404", description = "User or friend not found")
    })
    @DeleteMapping("/remove")
    public ResponseEntity<Map<String, String>> removeFriend(
            @Parameter(description = "User ID", required = true) @RequestParam long userId,
            @Parameter(description = "Friend ID", required = true) @RequestParam long friendId) {
        Map<String, String> response = new HashMap<>();

        boolean removed = friendshipController.removeFriend(userId, friendId);

        if (!removed) {
            response.put("message", "User or friend not found.");
            return ResponseEntity.status(404).body(response);
        }

        response.put("message", "Friendship removed.");
        return ResponseEntity.ok(response);
    }
}
package hu.benkototh.cardgame.backend.rest.service;

import hu.benkototh.cardgame.backend.rest.controller.AgentController;
import hu.benkototh.cardgame.backend.rest.model.User;
import hu.benkototh.cardgame.backend.rest.controller.UserController;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/agent/users")
@Tag(name = "Agent", description = "Agent operations for user management")
public class AgentRestService {

    @Autowired
    private AgentController agentController;
    @Autowired
    private UserController userController;

    @Operation(summary = "Unlock user", description = "Unlocks a locked user account")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "User unlocked successfully",
                    content = @Content(mediaType = "application/json")),
            @ApiResponse(responseCode = "404", description = "User not found")
    })
    @PutMapping("/unlock")
    public ResponseEntity<Map<String, String>> unlockUser(
            @Parameter(description = "ID of the user to unlock", required = true) @RequestParam long userId) {
        Map<String, String> response = new HashMap<>();
        User user = agentController.unlockUser(userId);

        if (user == null) {
            response.put("message", "User not found.");
            return ResponseEntity.status(404).body(response);
        }

        response.put("message", "User unlocked successfully.");
        return ResponseEntity.ok(response);
    }

    @Operation(summary = "Modify user data", description = "Modifies user account information")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "User data modified successfully"),
            @ApiResponse(responseCode = "400", description = "Invalid input - username or email already in use"),
            @ApiResponse(responseCode = "404", description = "User not found")
    })
    @PutMapping("/modify")
    public ResponseEntity<Map<String, String>> modifyUserData(
            @Parameter(description = "User data to modify", required = true) @RequestBody Map<String, Object> userData) {
        Map<String, String> response = new HashMap<>();

        User user = agentController.modifyUserData(userData);

        if (user == null) {
            if (!userData.containsKey("userId")) {
                response.put("message", "User ID is required.");
                return ResponseEntity.status(400).body(response);
            }

            if (userData.containsKey("username") && userController.userExistsByUsername(userData.get("username").toString())) {
                response.put("message", "Username already in use.");
                return ResponseEntity.status(400).body(response);
            }

            if (userData.containsKey("email") && userController.userExistsByEmail(userData.get("email").toString())) {
                response.put("message", "Email already in use.");
                return ResponseEntity.status(400).body(response);
            }

            response.put("message", "User not found.");
            return ResponseEntity.status(404).body(response);
        }

        response.put("message", "User data modified successfully.");
        return ResponseEntity.ok(response);
    }
}
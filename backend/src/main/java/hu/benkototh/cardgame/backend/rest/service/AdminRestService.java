package hu.benkototh.cardgame.backend.rest.service;

import hu.benkototh.cardgame.backend.rest.controller.AdminController;
import hu.benkototh.cardgame.backend.rest.model.Game;
import hu.benkototh.cardgame.backend.rest.model.GameCreationDTO;
import hu.benkototh.cardgame.backend.rest.model.User;
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
@RequestMapping("/api/admin")
@Tag(name = "Admin", description = "Admin operations for user and game management")
public class AdminRestService {

    @Autowired
    private AdminController adminController;

    @Operation(summary = "Get all users", description = "Retrieves a list of all users in the system")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Successfully retrieved users",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = User.class)))
    })
    @GetMapping("/users/all")
    public ResponseEntity<List<User>> getAllUsers() {
        return ResponseEntity.ok(adminController.getAllUsers());
    }

    @Operation(summary = "Create user", description = "Creates a new user with admin privileges")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "User created successfully"),
            @ApiResponse(responseCode = "400", description = "Username or email already in use")
    })
    @PostMapping("/users/create")
    public ResponseEntity<Map<String, String>> createUser(
            @Parameter(description = "User details", required = true) @RequestBody User user) {
        Map<String, String> response = new HashMap<>();

        User createdUser = adminController.createUser(user);

        if (createdUser == null) {
            if (adminController.userExistsByUsername(user.getUsername())) {
                response.put("message", "Username already in use.");
                return ResponseEntity.status(400).body(response);
            } else if (adminController.userExistsByEmail(user.getEmail())) {
                response.put("message", "Email already in use.");
                return ResponseEntity.status(400).body(response);
            }
            return ResponseEntity.status(400).body(response);
        }

        response.put("message", "User created successfully.");
        return ResponseEntity.ok(response);
    }

    @Operation(summary = "Delete user", description = "Deletes a user from the system")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "User deleted successfully"),
            @ApiResponse(responseCode = "404", description = "User not found")
    })
    @DeleteMapping("/users/delete")
    public ResponseEntity<Map<String, String>> deleteUser(
            @Parameter(description = "ID of the user to delete", required = true) @RequestParam long userId) {
        Map<String, String> response = new HashMap<>();

        boolean deleted = adminController.deleteUser(userId);

        if (deleted) {
            response.put("message", "User deleted successfully.");
            return ResponseEntity.ok(response);
        } else {
            response.put("message", "User not found.");
            return ResponseEntity.status(404).body(response);
        }
    }

    @Operation(summary = "Promote user to agent", description = "Promotes a regular user to agent role")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "User promoted to agent successfully"),
            @ApiResponse(responseCode = "404", description = "User not found or not eligible for promotion")
    })
    @PutMapping("/users/promote-to-agent")
    public ResponseEntity<Map<String, String>> promoteToAgent(
            @Parameter(description = "ID of the user to promote", required = true) @RequestParam long userId) {
        Map<String, String> response = new HashMap<>();

        User user = adminController.promoteToAgent(userId);

        if (user == null) {
            response.put("message", "User not found or only regular users can be promoted to agents.");
            return ResponseEntity.status(404).body(response);
        }

        response.put("message", "User promoted to agent successfully.");
        return ResponseEntity.ok(response);
    }

    @Operation(summary = "Demote user from agent", description = "Demotes an agent to regular user role")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "User demoted from agent successfully"),
            @ApiResponse(responseCode = "404", description = "User not found or not eligible for demotion")
    })
    @PutMapping("/users/demote-from-agent")
    public ResponseEntity<Map<String, String>> demoteFromAgent(
            @Parameter(description = "ID of the user to demote", required = true) @RequestParam long userId) {
        Map<String, String> response = new HashMap<>();

        User user = adminController.demoteFromAgent(userId);

        if (user == null) {
            response.put("message", "User not found or only agents can be demoted to users.");
            return ResponseEntity.status(404).body(response);
        }

        response.put("message", "User demoted from agent successfully.");
        return ResponseEntity.ok(response);
    }

    @Operation(summary = "Promote user to admin", description = "Promotes a user to admin role")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "User promoted to admin successfully"),
            @ApiResponse(responseCode = "404", description = "User not found or already has admin privileges")
    })
    @PutMapping("/users/promote-to-admin")
    public ResponseEntity<Map<String, String>> promoteToAdmin(
            @Parameter(description = "ID of the user to promote", required = true) @RequestParam long userId) {
        Map<String, String> response = new HashMap<>();

        User user = adminController.promoteToAdmin(userId);

        if (user == null) {
            response.put("message", "User not found or user already has admin or higher privileges.");
            return ResponseEntity.status(404).body(response);
        }

        response.put("message", "User promoted to admin successfully.");
        return ResponseEntity.ok(response);
    }

    @Operation(summary = "Demote user from admin", description = "Demotes an admin to regular user role")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "User demoted from admin successfully"),
            @ApiResponse(responseCode = "404", description = "User not found or not eligible for demotion")
    })
    @PutMapping("/users/demote-from-admin")
    public ResponseEntity<Map<String, String>> demoteFromAdmin(
            @Parameter(description = "ID of the user to demote", required = true) @RequestParam long userId) {
        Map<String, String> response = new HashMap<>();

        User user = adminController.demoteFromAdmin(userId);

        if (user == null) {
            response.put("message", "User not found or only admins can be demoted.");
            return ResponseEntity.status(404).body(response);
        }

        response.put("message", "User demoted from admin successfully.");
        return ResponseEntity.ok(response);
    }

    @Operation(summary = "Get all games", description = "Retrieves a list of all games in the system")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Successfully retrieved games",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = Game.class)))
    })
    @GetMapping("/games/all")
    public ResponseEntity<List<Game>> getAllGames() {
        return ResponseEntity.ok(adminController.getAllGames());
    }

    @Operation(summary = "Create game", description = "Creates a new game")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Game created successfully"),
            @ApiResponse(responseCode = "400", description = "Game with this name already exists")
    })
    @PostMapping("/games/create")
    public ResponseEntity<Map<String, String>> createGame(
            @Parameter(description = "Game details", required = true) @RequestBody GameCreationDTO gameDTO) {
        Map<String, String> response = new HashMap<>();

        Game createdGame = adminController.createGame(gameDTO);

        if (createdGame == null) {
            response.put("message", "Game with this name already exists.");
            return ResponseEntity.status(400).body(response);
        }

        response.put("message", "Game created successfully.");
        response.put("id", String.valueOf(createdGame.getId()));
        return ResponseEntity.ok(response);
    }

    @Operation(summary = "Update game", description = "Updates an existing game")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Game updated successfully"),
            @ApiResponse(responseCode = "404", description = "Game not found or name already exists")
    })
    @PutMapping("/games/update/{id}")
    public ResponseEntity<Map<String, String>> updateGame(
            @Parameter(description = "ID of the game to update", required = true) @PathVariable long id,
            @Parameter(description = "Updated game details", required = true) @RequestBody GameCreationDTO gameDTO) {
        Map<String, String> response = new HashMap<>();

        Game updatedGame = adminController.updateGame(gameDTO, id);

        if (updatedGame == null) {
            response.put("message", "Game not found or name already exists.");
            return ResponseEntity.status(404).body(response);
        }

        response.put("message", "Game updated successfully.");
        return ResponseEntity.ok(response);
    }

    @Operation(summary = "Delete game", description = "Deletes a game from the system")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Game deleted successfully"),
            @ApiResponse(responseCode = "404", description = "Game not found")
    })
    @DeleteMapping("/games/delete")
    public ResponseEntity<Map<String, String>> deleteGame(
            @Parameter(description = "ID of the game to delete", required = true) @RequestParam long gameId) {
        Map<String, String> response = new HashMap<>();

        boolean deleted = adminController.deleteGame(gameId);

        if (deleted) {
            response.put("message", "Game deleted successfully.");
            return ResponseEntity.ok(response);
        } else {
            response.put("message", "Game not found.");
            return ResponseEntity.status(404).body(response);
        }
    }
}
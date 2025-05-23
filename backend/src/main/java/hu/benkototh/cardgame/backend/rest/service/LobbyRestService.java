package hu.benkototh.cardgame.backend.rest.service;

import hu.benkototh.cardgame.backend.rest.model.Game;
import hu.benkototh.cardgame.backend.rest.model.Lobby;
import hu.benkototh.cardgame.backend.rest.controller.LobbyController;
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
@RequestMapping("/api/lobbies")
@Tag(name = "Lobbies", description = "Operations for managing game lobbies")
public class LobbyRestService {

    @Autowired
    private LobbyController lobbyController;

    @Operation(summary = "Create lobby", description = "Creates a new game lobby")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Lobby created successfully",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = Lobby.class))),
            @ApiResponse(responseCode = "400", description = "Failed to create lobby")
    })
    @PostMapping("/create")
    public ResponseEntity<?> createLobby(
            @Parameter(description = "Leader user ID", required = true) @RequestParam long leaderId,
            @Parameter(description = "Game ID", required = true) @RequestParam long gameId,
            @Parameter(description = "Whether to play with points", required = true) @RequestParam boolean playWithPoints,
            @Parameter(description = "Whether the lobby is public", required = true) @RequestParam boolean isPublic
    ) {
        Lobby lobby = lobbyController.createLobby(leaderId, gameId, playWithPoints, isPublic);

        if (lobby == null) {
            Map<String, String> response = new HashMap<>();
            response.put("message", "Failed to create lobby. User might already be in a lobby.");
            return ResponseEntity.status(400).body(response);
        }

        return ResponseEntity.ok(lobby);
    }

    @Operation(summary = "Create club lobby", description = "Creates a new game lobby for a club")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Club lobby created successfully",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = Lobby.class))),
            @ApiResponse(responseCode = "400", description = "Failed to create club lobby")
    })
    @PostMapping("/create-club")
    public ResponseEntity<?> createClubLobby(
            @Parameter(description = "Leader user ID", required = true) @RequestParam long leaderId,
            @Parameter(description = "Game ID", required = true) @RequestParam long gameId,
            @Parameter(description = "Whether to play with points", required = true) @RequestParam boolean playWithPoints,
            @Parameter(description = "Club ID", required = true) @RequestParam long clubId
    ) {
        Lobby lobby = lobbyController.createClubLobby(leaderId, gameId, playWithPoints, clubId);

        if (lobby == null) {
            Map<String, String> response = new HashMap<>();
            response.put("message", "Failed to create club lobby. User might already be in a lobby or not a member of the club.");
            return ResponseEntity.status(400).body(response);
        }

        return ResponseEntity.ok(lobby);
    }

    @Operation(summary = "Join lobby", description = "Joins an existing lobby using its code")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Successfully joined lobby",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = Lobby.class))),
            @ApiResponse(responseCode = "400", description = "Failed to join lobby")
    })
    @PostMapping("/join")
    public ResponseEntity<?> joinLobby(
            @Parameter(description = "Lobby code", required = true) @RequestParam String code,
            @Parameter(description = "User ID", required = true) @RequestParam long userId) {
        Lobby lobby = lobbyController.joinLobby(code, userId);

        if (lobby == null) {
            Map<String, String> response = new HashMap<>();

            Lobby existingLobby = lobbyController.getLobbyByCode(code);
            if (existingLobby != null && existingLobby.getClub() != null) {
                response.put("message", "Failed to join club lobby. You must be a member of the club to join this lobby.");
            } else {
                response.put("message", "Failed to join lobby. Lobby might be full, in game, or user is already in a lobby.");
            }

            return ResponseEntity.status(400).body(response);
        }

        return ResponseEntity.ok(lobby);
    }

    @Operation(summary = "Kick player", description = "Removes a player from a lobby (leader action)")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Player kicked successfully",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = Lobby.class))),
            @ApiResponse(responseCode = "400", description = "Failed to kick player")
    })
    @PostMapping("/kick")
    public ResponseEntity<?> kickPlayer(
            @Parameter(description = "Lobby ID", required = true) @RequestParam long lobbyId,
            @Parameter(description = "Leader user ID", required = true) @RequestParam long leaderId,
            @Parameter(description = "Player user ID to kick", required = true) @RequestParam long playerId) {
        Lobby lobby = lobbyController.kickPlayer(lobbyId, leaderId, playerId);

        if (lobby == null) {
            Map<String, String> response = new HashMap<>();
            response.put("message", "Failed to kick player. You might not be the leader or player is not in the lobby.");
            return ResponseEntity.status(400).body(response);
        }

        return ResponseEntity.ok(lobby);
    }

    @Operation(summary = "Leave lobby", description = "Leaves a lobby")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Successfully left lobby")
    })
    @PostMapping("/leave")
    public ResponseEntity<?> leaveLobby(
            @Parameter(description = "Lobby ID", required = true) @RequestParam long lobbyId,
            @Parameter(description = "User ID", required = true) @RequestParam long userId) {
        Lobby lobby = lobbyController.leaveLobby(lobbyId, userId);

        Map<String, Object> response = new HashMap<>();
        if (lobby == null) {
            response.put("message", "You have left the lobby. The lobby might have been deleted if you were the last player.");
            response.put("lobbyDeleted", true);
        } else {
            response.put("message", "You have left the lobby.");
            response.put("lobbyDeleted", false);
            response.put("lobby", lobby);
        }

        return ResponseEntity.ok(response);
    }

    @Operation(summary = "Update lobby settings", description = "Updates the settings of a lobby (leader action)")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Lobby settings updated successfully",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = Lobby.class))),
            @ApiResponse(responseCode = "400", description = "Failed to update lobby settings")
    })
    @PutMapping("/update-settings")
    public ResponseEntity<?> updateLobbySettings(
            @Parameter(description = "Lobby ID", required = true) @RequestParam long lobbyId,
            @Parameter(description = "Leader user ID", required = true) @RequestParam long leaderId,
            @Parameter(description = "Game ID", required = true) @RequestParam long gameId,
            @Parameter(description = "Whether to play with points", required = true) @RequestParam boolean playWithPoints,
            @Parameter(description = "Whether the lobby is public", required = true) @RequestParam boolean isPublic) {
        Lobby lobby = lobbyController.updateLobbySettings(lobbyId, leaderId, gameId, playWithPoints, isPublic);

        if (lobby == null) {
            Map<String, String> response = new HashMap<>();
            response.put("message", "Failed to update lobby settings. You might not be the leader.");
            return ResponseEntity.status(400).body(response);
        }

        return ResponseEntity.ok(lobby);
    }

    @Operation(summary = "Start game", description = "Starts a game from a lobby (leader action)")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Game started successfully",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = Lobby.class))),
            @ApiResponse(responseCode = "400", description = "Failed to start game")
    })
    @PostMapping("/start-game")
    public ResponseEntity<?> startGame(
            @Parameter(description = "Lobby ID", required = true) @RequestParam long lobbyId,
            @Parameter(description = "Leader user ID", required = true) @RequestParam long leaderId) {
        Lobby lobby = lobbyController.startGame(lobbyId, leaderId);

        if (lobby == null) {
            Map<String, String> response = new HashMap<>();
            response.put("message", "Failed to start game. You might not be the leader or not enough players.");
            return ResponseEntity.status(400).body(response);
        }

        return ResponseEntity.ok(lobby);
    }

    @Operation(summary = "Get lobby by ID", description = "Retrieves a specific lobby by its ID")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Successfully retrieved lobby",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = Lobby.class))),
            @ApiResponse(responseCode = "404", description = "Lobby not found")
    })
    @GetMapping("/get")
    public ResponseEntity<?> getLobby(
            @Parameter(description = "Lobby ID", required = true) @RequestParam long lobbyId) {
        Lobby lobby = lobbyController.getLobbyById(lobbyId);

        if (lobby == null) {
            Map<String, String> response = new HashMap<>();
            response.put("message", "Lobby not found.");
            return ResponseEntity.status(404).body(response);
        }

        return ResponseEntity.ok(lobby);
    }

    @Operation(summary = "Get lobby by code", description = "Retrieves a specific lobby by its join code")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Successfully retrieved lobby",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = Lobby.class))),
            @ApiResponse(responseCode = "404", description = "Lobby not found")
    })
    @GetMapping("/get-by-code")
    public ResponseEntity<?> getLobbyByCode(
            @Parameter(description = "Lobby code", required = true) @RequestParam String code) {
        Lobby lobby = lobbyController.getLobbyByCode(code);

        if (lobby == null) {
            Map<String, String> response = new HashMap<>();
            response.put("message", "Lobby not found.");
            return ResponseEntity.status(404).body(response);
        }

        return ResponseEntity.ok(lobby);
    }

    @Operation(summary = "Get lobbies by player", description = "Retrieves all lobbies a player is or was in")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Successfully retrieved lobbies",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = Lobby.class)))
    })
    @GetMapping("/get-by-player")
    public ResponseEntity<List<Lobby>> getLobbiesByPlayer(
            @Parameter(description = "User ID", required = true) @RequestParam long userId) {
        return ResponseEntity.ok(lobbyController.getLobbiesByPlayer(userId));
    }

    @Operation(summary = "Get player's current lobby", description = "Retrieves the current lobby a player is in")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Successfully retrieved lobby",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = Lobby.class)))
    })
    @GetMapping("/player-lobby")
    public ResponseEntity<Lobby> getLobbyByPlayer(
            @Parameter(description = "User ID", required = true) @RequestParam long userId){
        return ResponseEntity.ok(lobbyController.getLobbyByPlayer(userId));
    }

    @Operation(summary = "Get all games", description = "Retrieves all available games for lobbies")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Successfully retrieved games",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = Game.class)))
    })
    @GetMapping("/games")
    public ResponseEntity<List<Game>> getAllGames() {
        return ResponseEntity.ok(lobbyController.getAllGames());
    }

    @Operation(summary = "Get public lobbies", description = "Retrieves all public lobbies")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Successfully retrieved public lobbies",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = Lobby.class)))
    })
    @GetMapping("/public-lobbies")
    public ResponseEntity<List<Lobby>> getPublicLobbies() {
        return ResponseEntity.ok(lobbyController.getPublicLobbies());
    }

    @Operation(summary = "Get club lobbies", description = "Retrieves all lobbies for a specific club")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Successfully retrieved club lobbies",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = Lobby.class)))
    })
    @GetMapping("/club-lobbies")
    public ResponseEntity<List<Lobby>> getClubLobbies(
            @Parameter(description = "Club ID", required = true) @RequestParam long clubId) {
        return ResponseEntity.ok(lobbyController.getClubLobbies(clubId));
    }
}
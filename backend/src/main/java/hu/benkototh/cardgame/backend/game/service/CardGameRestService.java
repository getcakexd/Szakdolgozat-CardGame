package hu.benkototh.cardgame.backend.game.service;

import hu.benkototh.cardgame.backend.game.controller.CardGameController;
import hu.benkototh.cardgame.backend.game.controller.StatisticsController;
import hu.benkototh.cardgame.backend.game.model.CardGame;
import hu.benkototh.cardgame.backend.rest.model.Game;
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

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/card-games")
@Tag(name = "Card Games", description = "Operations for managing card games")
public class CardGameRestService {

    @Autowired
    private CardGameController cardGameController;

    @Autowired
    private StatisticsController statisticsController;

    @Operation(summary = "Get all game definitions", description = "Retrieves a list of all available game definitions")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Successfully retrieved game definitions",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = Game.class)))
    })
    @GetMapping("/definitions")
    public ResponseEntity<List<Game>> getGameDefinitions() {
        List<Game> gameDefinitions = cardGameController.getGameDefinitions();
        return ResponseEntity.ok(gameDefinitions);
    }

    @Operation(summary = "Get game definition by ID", description = "Retrieves a specific game definition by its ID")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Successfully retrieved game definition",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = Game.class))),
            @ApiResponse(responseCode = "404", description = "Game definition not found")
    })
    @GetMapping("/definitions/{id}")
    public ResponseEntity<Game> getGameDefinition(
            @Parameter(description = "Game definition ID", required = true) @PathVariable long id) {
        Game game = cardGameController.getGameDefinition(id);
        if (game == null) {
            return ResponseEntity.notFound().build();
        }
        return ResponseEntity.ok(game);
    }

    @Operation(summary = "Create a new card game", description = "Creates a new card game instance based on a game definition")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Game created successfully",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = CardGame.class)))
    })
    @PostMapping
    public ResponseEntity<CardGame> createGame(
            @Parameter(description = "Game creation details", required = true) @RequestBody Map<String, Object> request) {
        long gameDefinitionId = Long.parseLong(request.get("gameDefinitionId").toString());
        String creatorId = (String) request.get("creatorId");
        String gameName = (String) request.get("gameName");
        boolean trackStatistics = request.containsKey("trackStatistics") ?
                (boolean) request.get("trackStatistics") : true;

        CardGame game = cardGameController.createCardGame(gameDefinitionId, creatorId, gameName, trackStatistics);
        return ResponseEntity.ok(game);
    }

    @Operation(summary = "Join a card game", description = "Allows a user to join an existing card game")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Successfully joined game",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = CardGame.class)))
    })
    @PostMapping("/{gameId}/join")
    public ResponseEntity<CardGame> joinGame(
            @Parameter(description = "Game ID", required = true) @PathVariable String gameId,
            @Parameter(description = "Join request details", required = true) @RequestBody Map<String, String> request) {
        String userId = request.get("userId");
        CardGame game = cardGameController.joinGame(gameId, userId);
        return ResponseEntity.ok(game);
    }

    @Operation(summary = "Leave a card game", description = "Allows a user to leave a card game they have joined")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Successfully left game",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = CardGame.class)))
    })
    @PostMapping("/{gameId}/leave")
    public ResponseEntity<CardGame> leaveGame(
            @Parameter(description = "Game ID", required = true) @PathVariable String gameId,
            @Parameter(description = "Leave request details", required = true) @RequestBody Map<String, String> request) {
        String userId = request.get("userId");
        CardGame game = cardGameController.leaveGame(gameId, userId);
        return ResponseEntity.ok(game);
    }

    @Operation(summary = "Abandon a card game", description = "Allows a user to abandon an in-progress card game")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Successfully abandoned game",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = CardGame.class)))
    })
    @PostMapping("/{gameId}/abandon")
    public ResponseEntity<CardGame> abandonGame(
            @Parameter(description = "Game ID", required = true) @PathVariable String gameId,
            @Parameter(description = "Abandon request details", required = true) @RequestBody Map<String, String> request) {
        String userId = request.get("userId");
        CardGame game = cardGameController.abandonGame(gameId, userId);
        return ResponseEntity.ok(game);
    }

    @Operation(summary = "Start a card game", description = "Starts a card game that is in the waiting state")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Game started successfully",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = CardGame.class)))
    })
    @PostMapping("/{gameId}/start")
    public ResponseEntity<CardGame> startGame(
            @Parameter(description = "Game ID", required = true) @PathVariable String gameId,
            @Parameter(description = "Start request details", required = true) @RequestBody Map<String, String> request) {
        String userId = request.get("userId");
        CardGame game = cardGameController.startGame(gameId, userId);
        return ResponseEntity.ok(game);
    }

    @Operation(summary = "Get card games", description = "Retrieves a list of card games, optionally filtered by status")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Successfully retrieved games",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = CardGame.class)))
    })
    @GetMapping
    public ResponseEntity<List<CardGame>> getGames(
            @Parameter(description = "Game status filter (active, waiting)", required = false) @RequestParam(required = false) String status) {
        List<CardGame> games;
        if ("active".equalsIgnoreCase(status)) {
            games = cardGameController.getActiveGames();
        } else if ("waiting".equalsIgnoreCase(status)) {
            games = cardGameController.getWaitingGames();
        } else {
            games = cardGameController.getActiveGames();
        }
        return ResponseEntity.ok(games);
    }

    @Operation(summary = "Get games by definition", description = "Retrieves all card games of a specific game definition")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Successfully retrieved games",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = CardGame.class)))
    })
    @GetMapping("/by-definition/{gameDefinitionId}")
    public ResponseEntity<List<CardGame>> getGamesByDefinition(
            @Parameter(description = "Game definition ID", required = true) @PathVariable long gameDefinitionId) {
        List<CardGame> games = cardGameController.getGamesByDefinition(gameDefinitionId);
        return ResponseEntity.ok(games);
    }

    @Operation(summary = "Get game by ID", description = "Retrieves a specific card game by its ID")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Successfully retrieved game",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = CardGame.class))),
            @ApiResponse(responseCode = "404", description = "Game not found")
    })
    @GetMapping("/{gameId}")
    public ResponseEntity<CardGame> getGame(
            @Parameter(description = "Game ID", required = true) @PathVariable String gameId) {
        CardGame game = cardGameController.getGame(gameId);
        if (game == null) {
            return ResponseEntity.notFound().build();
        }
        return ResponseEntity.ok(game);
    }

    @Operation(summary = "Get user statistics", description = "Retrieves statistics for a specific user, optionally filtered by game type")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Successfully retrieved user statistics")
    })
    @GetMapping("/statistics/{userId}")
    public ResponseEntity<Map<String, Object>> getUserStatistics(
            @Parameter(description = "User ID", required = true) @PathVariable String userId,
            @Parameter(description = "Game type filter", required = false) @RequestParam(required = false) String gameType) {
        Map<String, Object> statistics = statisticsController.getUserStatistics(userId, gameType);
        return ResponseEntity.ok(statistics);
    }

    @Operation(summary = "Get user statistics by game definition", description = "Retrieves statistics for a specific user for a specific game definition")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Successfully retrieved user statistics")
    })
    @GetMapping("/statistics/{userId}/by-definition/{gameDefinitionId}")
    public ResponseEntity<Map<String, Object>> getUserStatisticsByGameDefinition(
            @Parameter(description = "User ID", required = true) @PathVariable String userId,
            @Parameter(description = "Game definition ID", required = true) @PathVariable long gameDefinitionId) {
        Map<String, Object> statistics = statisticsController.getUserStatisticsByGameDefinition(userId, gameDefinitionId);
        return ResponseEntity.ok(statistics);
    }
}
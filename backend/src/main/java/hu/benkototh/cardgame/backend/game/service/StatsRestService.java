package hu.benkototh.cardgame.backend.game.service;

import hu.benkototh.cardgame.backend.game.controller.StatsController;
import hu.benkototh.cardgame.backend.game.model.GameStatistics;
import hu.benkototh.cardgame.backend.game.model.UserGameStats;
import hu.benkototh.cardgame.backend.game.model.UserStats;
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
@RequestMapping("/api/stats")
@Tag(name = "User Statistics", description = "Operations for retrieving user game statistics")
public class StatsRestService {

    @Autowired
    private StatsController statsController;

    @Operation(summary = "Get user statistics", description = "Retrieves overall statistics for a specific user")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Successfully retrieved user statistics",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = UserStats.class))),
            @ApiResponse(responseCode = "404", description = "User not found")
    })
    @GetMapping("/user/{userId}")
    public ResponseEntity<?> getUserStats(
            @Parameter(description = "User ID", required = true) @PathVariable Long userId) {
        UserStats stats = statsController.getUserStats(userId);

        if (stats == null) {
            Map<String, String> response = new HashMap<>();
            response.put("message", "User not found");
            return ResponseEntity.status(404).body(response);
        }

        return ResponseEntity.ok(stats);
    }

    @Operation(summary = "Get user game statistics", description = "Retrieves statistics for all games played by a specific user")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Successfully retrieved user game statistics",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = UserGameStats.class)))
    })
    @GetMapping("/user/{userId}/games")
    public ResponseEntity<?> getUserGameStats(
            @Parameter(description = "User ID", required = true) @PathVariable Long userId) {
        List<UserGameStats> stats = statsController.getUserGameStats(userId);
        return ResponseEntity.ok(stats);
    }

    @Operation(summary = "Get user statistics for a specific game", description = "Retrieves statistics for a specific game played by a user")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Successfully retrieved user game statistics",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = UserGameStats.class))),
            @ApiResponse(responseCode = "404", description = "User or game not found")
    })
    @GetMapping("/user/{userId}/game/{gameDefinitionId}")
    public ResponseEntity<?> getUserGameStatsByGame(
            @Parameter(description = "User ID", required = true) @PathVariable Long userId,
            @Parameter(description = "Game definition ID", required = true) @PathVariable Long gameDefinitionId) {
        UserGameStats stats = statsController.getUserGameStatsByGame(userId, gameDefinitionId);

        if (stats == null) {
            Map<String, String> response = new HashMap<>();
            response.put("message", "User or game not found");
            return ResponseEntity.status(404).body(response);
        }

        return ResponseEntity.ok(stats);
    }

    @Operation(summary = "Get global leaderboard", description = "Retrieves a leaderboard of top players across all games")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Successfully retrieved leaderboard",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = UserGameStats.class)))
    })
    @GetMapping("/leaderboard")
    public ResponseEntity<?> getLeaderboard(
            @Parameter(description = "Number of players to return", example = "10") @RequestParam(defaultValue = "10") int limit) {
        List<UserGameStats> topPlayers = statsController.getTopPlayersByPoints(limit);
        return ResponseEntity.ok(topPlayers);
    }

    @Operation(summary = "Get game-specific leaderboard", description = "Retrieves a leaderboard of top players for a specific game")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Successfully retrieved game leaderboard",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = UserGameStats.class)))
    })
    @GetMapping("/leaderboard/game/{gameDefinitionId}")
    public ResponseEntity<?> getLeaderboardByGame(
            @Parameter(description = "Game definition ID", required = true) @PathVariable Long gameDefinitionId,
            @Parameter(description = "Number of players to return", example = "10") @RequestParam(defaultValue = "10") int limit) {
        List<UserGameStats> topPlayers = statsController.getTopPlayersByGameAndPoints(gameDefinitionId, limit);
        return ResponseEntity.ok(topPlayers);
    }

    @Operation(summary = "Get user's recent games", description = "Retrieves statistics for recent games played by a user")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Successfully retrieved recent games",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = GameStatistics.class)))
    })
    @GetMapping("/user/{userId}/recent")
    public ResponseEntity<?> getRecentGames(
            @Parameter(description = "User ID", required = true) @PathVariable Long userId,
            @Parameter(description = "Number of games to return", example = "10") @RequestParam(defaultValue = "10") int limit) {
        List<GameStatistics> recentGames = statsController.getRecentGames(userId, limit);
        return ResponseEntity.ok(recentGames);
    }

    @Operation(summary = "Get user's draw statistics", description = "Retrieves statistics about games that ended in a draw for a user")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Successfully retrieved draw statistics"),
            @ApiResponse(responseCode = "404", description = "User not found")
    })
    @GetMapping("/user/{userId}/draws")
    public ResponseEntity<?> getUserDrawStats(
            @Parameter(description = "User ID", required = true) @PathVariable Long userId) {
        UserStats stats = statsController.getUserStats(userId);

        if (stats == null) {
            Map<String, String> response = new HashMap<>();
            response.put("message", "User not found");
            return ResponseEntity.status(404).body(response);
        }

        Map<String, Object> drawStats = new HashMap<>();
        drawStats.put("gamesDrawn", stats.getGamesDrawn());
        drawStats.put("drawPercentage", stats.getGamesPlayed() > 0 ?
                (double) stats.getGamesDrawn() / stats.getGamesPlayed() * 100 : 0);

        return ResponseEntity.ok(drawStats);
    }
}
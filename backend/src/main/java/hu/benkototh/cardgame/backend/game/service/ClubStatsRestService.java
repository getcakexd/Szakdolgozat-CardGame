package hu.benkototh.cardgame.backend.game.service;

import hu.benkototh.cardgame.backend.game.controller.ClubStatsController;
import hu.benkototh.cardgame.backend.game.model.ClubGameStats;
import hu.benkototh.cardgame.backend.game.model.ClubMemberStats;
import hu.benkototh.cardgame.backend.game.model.ClubStats;
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
@RequestMapping("/api/stats/club")
@Tag(name = "Club Statistics", description = "Operations for retrieving club game statistics")
public class ClubStatsRestService {

    @Autowired
    private ClubStatsController clubStatsController;

    @Operation(summary = "Get club statistics", description = "Retrieves overall statistics for a specific club")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Successfully retrieved club statistics",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = ClubStats.class))),
            @ApiResponse(responseCode = "404", description = "Club not found")
    })
    @GetMapping("/{clubId}")
    public ResponseEntity<?> getClubStats(
            @Parameter(description = "Club ID", required = true) @PathVariable Long clubId) {
        ClubStats stats = clubStatsController.getClubStats(clubId);

        if (stats == null) {
            Map<String, String> response = new HashMap<>();
            response.put("message", "Club not found");
            return ResponseEntity.status(404).body(response);
        }

        return ResponseEntity.ok(stats);
    }

    @Operation(summary = "Get club game statistics", description = "Retrieves statistics for all games played by a specific club")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Successfully retrieved club game statistics",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = ClubGameStats.class)))
    })
    @GetMapping("/{clubId}/games")
    public ResponseEntity<?> getClubGameStats(
            @Parameter(description = "Club ID", required = true) @PathVariable Long clubId) {
        List<ClubGameStats> stats = clubStatsController.getClubGameStats(clubId);
        return ResponseEntity.ok(stats);
    }

    @Operation(summary = "Get club statistics for a specific game", description = "Retrieves statistics for a specific game played by a club")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Successfully retrieved club game statistics",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = ClubGameStats.class))),
            @ApiResponse(responseCode = "404", description = "Club or game not found")
    })
    @GetMapping("/{clubId}/game/{gameDefinitionId}")
    public ResponseEntity<?> getClubGameStatsByGame(
            @Parameter(description = "Club ID", required = true) @PathVariable Long clubId,
            @Parameter(description = "Game definition ID", required = true) @PathVariable Long gameDefinitionId) {
        ClubGameStats stats = clubStatsController.getClubGameStatsByGame(clubId, gameDefinitionId);

        if (stats == null) {
            Map<String, String> response = new HashMap<>();
            response.put("message", "Club or game not found");
            return ResponseEntity.status(404).body(response);
        }

        return ResponseEntity.ok(stats);
    }

    @Operation(summary = "Get club member statistics", description = "Retrieves statistics for all members of a specific club")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Successfully retrieved club member statistics",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = ClubMemberStats.class)))
    })
    @GetMapping("/{clubId}/members")
    public ResponseEntity<?> getClubMemberStats(
            @Parameter(description = "Club ID", required = true) @PathVariable Long clubId) {
        List<ClubMemberStats> stats = clubStatsController.getClubMemberStats(clubId);
        return ResponseEntity.ok(stats);
    }

    @Operation(summary = "Get club member statistics for a specific user", description = "Retrieves statistics for a specific member of a club")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Successfully retrieved club member statistics",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = ClubMemberStats.class))),
            @ApiResponse(responseCode = "404", description = "Club or user not found")
    })
    @GetMapping("/{clubId}/member/{userId}")
    public ResponseEntity<?> getClubMemberStatsByUser(
            @Parameter(description = "Club ID", required = true) @PathVariable Long clubId,
            @Parameter(description = "User ID", required = true) @PathVariable Long userId) {
        ClubMemberStats stats = clubStatsController.getClubMemberStatsByUser(clubId, userId);

        if (stats == null) {
            Map<String, String> response = new HashMap<>();
            response.put("message", "Club or user not found");
            return ResponseEntity.status(404).body(response);
        }

        return ResponseEntity.ok(stats);
    }

    @Operation(summary = "Get club leaderboard", description = "Retrieves a leaderboard of top clubs across all games")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Successfully retrieved club leaderboard",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = ClubGameStats.class)))
    })
    @GetMapping("/leaderboard")
    public ResponseEntity<?> getClubLeaderboard(
            @Parameter(description = "Number of clubs to return", example = "10") @RequestParam(defaultValue = "10") int limit) {
        List<ClubGameStats> topClubs = clubStatsController.getTopClubsByPoints(limit);
        return ResponseEntity.ok(topClubs);
    }

    @Operation(summary = "Get game-specific club leaderboard", description = "Retrieves a leaderboard of top clubs for a specific game")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Successfully retrieved game-specific club leaderboard",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = ClubGameStats.class)))
    })
    @GetMapping("/leaderboard/game/{gameDefinitionId}")
    public ResponseEntity<?> getClubLeaderboardByGame(
            @Parameter(description = "Game definition ID", required = true) @PathVariable Long gameDefinitionId,
            @Parameter(description = "Number of clubs to return", example = "10") @RequestParam(defaultValue = "10") int limit) {
        List<ClubGameStats> topClubs = clubStatsController.getTopClubsByGameAndPoints(gameDefinitionId, limit);
        return ResponseEntity.ok(topClubs);
    }

    @Operation(summary = "Get club member leaderboard", description = "Retrieves a leaderboard of top members within a specific club")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Successfully retrieved club member leaderboard",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = ClubMemberStats.class)))
    })
    @GetMapping("/{clubId}/leaderboard")
    public ResponseEntity<?> getClubMemberLeaderboard(
            @Parameter(description = "Club ID", required = true) @PathVariable Long clubId,
            @Parameter(description = "Number of members to return", example = "10") @RequestParam(defaultValue = "10") int limit) {
        List<ClubMemberStats> topMembers = clubStatsController.getTopMembersByClubAndPoints(clubId, limit);
        return ResponseEntity.ok(topMembers);
    }

    @Operation(summary = "Get game-specific club member leaderboard", description = "Retrieves a leaderboard of top members within a specific club for a specific game")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Successfully retrieved game-specific club member leaderboard",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = ClubMemberStats.class)))
    })
    @GetMapping("/{clubId}/leaderboard/game/{gameDefinitionId}")
    public ResponseEntity<?> getClubMemberLeaderboardByGame(
            @Parameter(description = "Club ID", required = true) @PathVariable Long clubId,
            @Parameter(description = "Game definition ID", required = true) @PathVariable Long gameDefinitionId,
            @Parameter(description = "Number of members to return", example = "10") @RequestParam(defaultValue = "10") int limit) {
        List<ClubMemberStats> topMembers = clubStatsController.getTopMembersByClubAndGameAndPoints(clubId, gameDefinitionId, limit);
        return ResponseEntity.ok(topMembers);
    }
}
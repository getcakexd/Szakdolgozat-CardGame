package hu.benkototh.cardgame.backend.game.service;

import hu.benkototh.cardgame.backend.game.controller.StatsController;
import hu.benkototh.cardgame.backend.game.model.GameStatistics;
import hu.benkototh.cardgame.backend.game.model.UserGameStats;
import hu.benkototh.cardgame.backend.game.model.UserStats;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/stats")
public class StatsRestService {

    @Autowired
    private StatsController statsController;

    @GetMapping("/user/{userId}")
    public ResponseEntity<?> getUserStats(@PathVariable Long userId) {
        UserStats stats = statsController.getUserStats(userId);

        if (stats == null) {
            Map<String, String> response = new HashMap<>();
            response.put("message", "User not found");
            return ResponseEntity.status(404).body(response);
        }

        return ResponseEntity.ok(stats);
    }

    @GetMapping("/user/{userId}/games")
    public ResponseEntity<?> getUserGameStats(@PathVariable Long userId) {
        List<UserGameStats> stats = statsController.getUserGameStats(userId);
        return ResponseEntity.ok(stats);
    }

    @GetMapping("/user/{userId}/game/{gameDefinitionId}")
    public ResponseEntity<?> getUserGameStatsByGame(
            @PathVariable Long userId,
            @PathVariable Long gameDefinitionId) {
        UserGameStats stats = statsController.getUserGameStatsByGame(userId, gameDefinitionId);

        if (stats == null) {
            Map<String, String> response = new HashMap<>();
            response.put("message", "User or game not found");
            return ResponseEntity.status(404).body(response);
        }

        return ResponseEntity.ok(stats);
    }

    @GetMapping("/leaderboard")
    public ResponseEntity<?> getLeaderboard(@RequestParam(defaultValue = "10") int limit) {
        List<UserGameStats> topPlayers = statsController.getTopPlayersByPoints(limit);
        return ResponseEntity.ok(topPlayers);
    }

    @GetMapping("/leaderboard/game/{gameDefinitionId}")
    public ResponseEntity<?> getLeaderboardByGame(
            @PathVariable Long gameDefinitionId,
            @RequestParam(defaultValue = "10") int limit) {
        List<UserGameStats> topPlayers = statsController.getTopPlayersByGameAndPoints(gameDefinitionId, limit);
        return ResponseEntity.ok(topPlayers);
    }

    @GetMapping("/user/{userId}/recent")
    public ResponseEntity<?> getRecentGames(
            @PathVariable Long userId,
            @RequestParam(defaultValue = "10") int limit) {
        List<GameStatistics> recentGames = statsController.getRecentGames(userId, limit);
        return ResponseEntity.ok(recentGames);
    }
}

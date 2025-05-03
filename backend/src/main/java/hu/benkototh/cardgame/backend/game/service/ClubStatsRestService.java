package hu.benkototh.cardgame.backend.game.service;

import hu.benkototh.cardgame.backend.game.controller.ClubStatsController;
import hu.benkototh.cardgame.backend.game.model.ClubGameStats;
import hu.benkototh.cardgame.backend.game.model.ClubMemberStats;
import hu.benkototh.cardgame.backend.game.model.ClubStats;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/stats/club")
public class ClubStatsRestService {

    @Autowired
    private ClubStatsController clubStatsController;

    @GetMapping("/{clubId}")
    public ResponseEntity<?> getClubStats(@PathVariable Long clubId) {
        ClubStats stats = clubStatsController.getClubStats(clubId);

        if (stats == null) {
            Map<String, String> response = new HashMap<>();
            response.put("message", "Club not found");
            return ResponseEntity.status(404).body(response);
        }

        return ResponseEntity.ok(stats);
    }

    @GetMapping("/{clubId}/games")
    public ResponseEntity<?> getClubGameStats(@PathVariable Long clubId) {
        List<ClubGameStats> stats = clubStatsController.getClubGameStats(clubId);
        return ResponseEntity.ok(stats);
    }

    @GetMapping("/{clubId}/game/{gameDefinitionId}")
    public ResponseEntity<?> getClubGameStatsByGame(
            @PathVariable Long clubId,
            @PathVariable Long gameDefinitionId) {
        ClubGameStats stats = clubStatsController.getClubGameStatsByGame(clubId, gameDefinitionId);

        if (stats == null) {
            Map<String, String> response = new HashMap<>();
            response.put("message", "Club or game not found");
            return ResponseEntity.status(404).body(response);
        }

        return ResponseEntity.ok(stats);
    }

    @GetMapping("/{clubId}/members")
    public ResponseEntity<?> getClubMemberStats(@PathVariable Long clubId) {
        List<ClubMemberStats> stats = clubStatsController.getClubMemberStats(clubId);
        return ResponseEntity.ok(stats);
    }

    @GetMapping("/{clubId}/member/{userId}")
    public ResponseEntity<?> getClubMemberStatsByUser(
            @PathVariable Long clubId,
            @PathVariable Long userId) {
        ClubMemberStats stats = clubStatsController.getClubMemberStatsByUser(clubId, userId);

        if (stats == null) {
            Map<String, String> response = new HashMap<>();
            response.put("message", "Club or user not found");
            return ResponseEntity.status(404).body(response);
        }

        return ResponseEntity.ok(stats);
    }

    @GetMapping("/leaderboard")
    public ResponseEntity<?> getClubLeaderboard(@RequestParam(defaultValue = "10") int limit) {
        List<ClubGameStats> topClubs = clubStatsController.getTopClubsByPoints(limit);
        return ResponseEntity.ok(topClubs);
    }

    @GetMapping("/leaderboard/game/{gameDefinitionId}")
    public ResponseEntity<?> getClubLeaderboardByGame(
            @PathVariable Long gameDefinitionId,
            @RequestParam(defaultValue = "10") int limit) {
        List<ClubGameStats> topClubs = clubStatsController.getTopClubsByGameAndPoints(gameDefinitionId, limit);
        return ResponseEntity.ok(topClubs);
    }

    @GetMapping("/{clubId}/leaderboard")
    public ResponseEntity<?> getClubMemberLeaderboard(
            @PathVariable Long clubId,
            @RequestParam(defaultValue = "10") int limit) {
        List<ClubMemberStats> topMembers = clubStatsController.getTopMembersByClubAndPoints(clubId, limit);
        return ResponseEntity.ok(topMembers);
    }
}

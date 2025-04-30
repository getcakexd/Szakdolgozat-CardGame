package hu.benkototh.cardgame.backend.game.service;

import hu.benkototh.cardgame.backend.game.controller.CardGameController;
import hu.benkototh.cardgame.backend.game.controller.StatisticsController;
import hu.benkototh.cardgame.backend.game.model.CardGame;
import hu.benkototh.cardgame.backend.rest.Data.Game;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/card-games")
public class CardGameRestService {

    @Autowired
    private CardGameController cardGameController;

    @Autowired
    private StatisticsController statisticsController;

    @GetMapping("/definitions")
    public ResponseEntity<List<Game>> getGameDefinitions() {
        List<Game> gameDefinitions = cardGameController.getGameDefinitions();
        return ResponseEntity.ok(gameDefinitions);
    }

    @GetMapping("/definitions/{id}")
    public ResponseEntity<Game> getGameDefinition(@PathVariable long id) {
        Game game = cardGameController.getGameDefinition(id);
        if (game == null) {
            return ResponseEntity.notFound().build();
        }
        return ResponseEntity.ok(game);
    }

    @PostMapping
    public ResponseEntity<CardGame> createGame(@RequestBody Map<String, Object> request) {
        long gameDefinitionId = Long.parseLong(request.get("gameDefinitionId").toString());
        String creatorId = (String) request.get("creatorId");
        String gameName = (String) request.get("gameName");
        boolean trackStatistics = request.containsKey("trackStatistics") ?
                (boolean) request.get("trackStatistics") : true;

        CardGame game = cardGameController.createCardGame(gameDefinitionId, creatorId, gameName, trackStatistics);
        return ResponseEntity.ok(game);
    }

    @PostMapping("/{gameId}/join")
    public ResponseEntity<CardGame> joinGame(@PathVariable String gameId, @RequestBody Map<String, String> request) {
        String userId = request.get("userId");
        CardGame game = cardGameController.joinGame(gameId, userId);
        return ResponseEntity.ok(game);
    }

    @PostMapping("/{gameId}/leave")
    public ResponseEntity<CardGame> leaveGame(@PathVariable String gameId, @RequestBody Map<String, String> request) {
        String userId = request.get("userId");
        CardGame game = cardGameController.leaveGame(gameId, userId);
        return ResponseEntity.ok(game);
    }

    @PostMapping("/{gameId}/abandon")
    public ResponseEntity<CardGame> abandonGame(@PathVariable String gameId, @RequestBody Map<String, String> request) {
        String userId = request.get("userId");
        CardGame game = cardGameController.abandonGame(gameId, userId);
        return ResponseEntity.ok(game);
    }

    @PostMapping("/{gameId}/start")
    public ResponseEntity<CardGame> startGame(@PathVariable String gameId, @RequestBody Map<String, String> request) {
        String userId = request.get("userId");
        CardGame game = cardGameController.startGame(gameId, userId);
        return ResponseEntity.ok(game);
    }

    @GetMapping
    public ResponseEntity<List<CardGame>> getGames(@RequestParam(required = false) String status) {
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

    @GetMapping("/by-definition/{gameDefinitionId}")
    public ResponseEntity<List<CardGame>> getGamesByDefinition(@PathVariable long gameDefinitionId) {
        List<CardGame> games = cardGameController.getGamesByDefinition(gameDefinitionId);
        return ResponseEntity.ok(games);
    }

    @GetMapping("/{gameId}")
    public ResponseEntity<CardGame> getGame(@PathVariable String gameId) {
        CardGame game = cardGameController.getGame(gameId);
        if (game == null) {
            return ResponseEntity.notFound().build();
        }
        return ResponseEntity.ok(game);
    }

    @GetMapping("/statistics/{userId}")
    public ResponseEntity<Map<String, Object>> getUserStatistics(
            @PathVariable String userId,
            @RequestParam(required = false) String gameType) {
        Map<String, Object> statistics = statisticsController.getUserStatistics(userId, gameType);
        return ResponseEntity.ok(statistics);
    }

    @GetMapping("/statistics/{userId}/by-definition/{gameDefinitionId}")
    public ResponseEntity<Map<String, Object>> getUserStatisticsByGameDefinition(
            @PathVariable String userId,
            @PathVariable long gameDefinitionId) {
        Map<String, Object> statistics = statisticsController.getUserStatisticsByGameDefinition(userId, gameDefinitionId);
        return ResponseEntity.ok(statistics);
    }
}

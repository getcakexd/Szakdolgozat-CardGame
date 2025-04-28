package hu.benkototh.cardgame.backend.game.controller;

import hu.benkototh.cardgame.backend.game.model.CardGame;
import hu.benkototh.cardgame.backend.game.model.GameAction;
import hu.benkototh.cardgame.backend.game.model.GameEvent;
import hu.benkototh.cardgame.backend.game.service.CardGameService;
import hu.benkototh.cardgame.backend.game.service.StatisticsService;
import hu.benkototh.cardgame.backend.rest.Data.Game;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.messaging.simp.SimpMessageHeaderAccessor;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/card-games")
public class CardGameController {

    @Autowired
    private CardGameService cardGameService;

    @Autowired
    private StatisticsService statisticsService;

    @GetMapping("/definitions")
    public ResponseEntity<List<Game>> getGameDefinitions() {
        return ResponseEntity.ok(cardGameService.getGameDefinitions());
    }

    @GetMapping("/definitions/{id}")
    public ResponseEntity<Game> getGameDefinition(@PathVariable long id) {
        Game game = cardGameService.getGameDefinition(id);
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

        CardGame game = cardGameService.createCardGame(gameDefinitionId, creatorId, gameName, trackStatistics);
        return ResponseEntity.ok(game);
    }

    @PostMapping("/{gameId}/join")
    public ResponseEntity<CardGame> joinGame(@PathVariable String gameId, @RequestBody Map<String, String> request) {
        String userId = request.get("userId");
        CardGame game = cardGameService.joinGame(gameId, userId);
        return ResponseEntity.ok(game);
    }

    @PostMapping("/{gameId}/leave")
    public ResponseEntity<CardGame> leaveGame(@PathVariable String gameId, @RequestBody Map<String, String> request) {
        String userId = request.get("userId");
        CardGame game = cardGameService.leaveGame(gameId, userId);
        return ResponseEntity.ok(game);
    }

    @PostMapping("/{gameId}/start")
    public ResponseEntity<CardGame> startGame(@PathVariable String gameId, @RequestBody Map<String, String> request) {
        String userId = request.get("userId");
        CardGame game = cardGameService.startGame(gameId, userId);
        return ResponseEntity.ok(game);
    }

    @GetMapping
    public ResponseEntity<List<CardGame>> getGames(@RequestParam(required = false) String status) {
        if ("active".equalsIgnoreCase(status)) {
            return ResponseEntity.ok(cardGameService.getActiveGames());
        } else if ("waiting".equalsIgnoreCase(status)) {
            return ResponseEntity.ok(cardGameService.getWaitingGames());
        } else {
            return ResponseEntity.ok(cardGameService.getActiveGames());
        }
    }

    @GetMapping("/by-definition/{gameDefinitionId}")
    public ResponseEntity<List<CardGame>> getGamesByDefinition(@PathVariable long gameDefinitionId) {
        return ResponseEntity.ok(cardGameService.getGamesByDefinition(gameDefinitionId));
    }

    @GetMapping("/{gameId}")
    public ResponseEntity<CardGame> getGame(@PathVariable String gameId) {
        CardGame game = cardGameService.getGame(gameId);
        if (game == null) {
            return ResponseEntity.notFound().build();
        }
        return ResponseEntity.ok(game);
    }

    @GetMapping("/statistics/{userId}")
    public ResponseEntity<Map<String, Object>> getUserStatistics(
            @PathVariable String userId,
            @RequestParam(required = false) String gameType) {
        Map<String, Object> statistics = statisticsService.getUserStatistics(userId, gameType);
        return ResponseEntity.ok(statistics);
    }

    @GetMapping("/statistics/{userId}/by-definition/{gameDefinitionId}")
    public ResponseEntity<Map<String, Object>> getUserStatisticsByGameDefinition(
            @PathVariable String userId,
            @PathVariable long gameDefinitionId) {
        Map<String, Object> statistics = statisticsService.getUserStatisticsByGameDefinition(userId, gameDefinitionId);
        return ResponseEntity.ok(statistics);
    }

    @MessageMapping("/game.action")
    public void processGameAction(@Payload GameAction action, SimpMessageHeaderAccessor headerAccessor) {
        String gameId = (String) headerAccessor.getSessionAttributes().get("gameId");
        String userId = (String) headerAccessor.getSessionAttributes().get("userId");

        cardGameService.executeGameAction(gameId, userId, action);
    }

    @MessageMapping("/game.join")
    public void joinGameWebSocket(
            @Payload Map<String, String> payload,
            SimpMessageHeaderAccessor headerAccessor) {
        String gameId = payload.get("gameId");
        String userId = payload.get("userId");

        headerAccessor.getSessionAttributes().put("gameId", gameId);
        headerAccessor.getSessionAttributes().put("userId", userId);

        cardGameService.joinGame(gameId, userId);
    }
}
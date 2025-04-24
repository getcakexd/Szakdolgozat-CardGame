package hu.benkototh.cardgame.backend.rest.service;

import hu.benkototh.cardgame.backend.rest.controller.GameController;
import hu.benkototh.cardgame.backend.rest.Data.Game;
import hu.benkototh.cardgame.backend.rest.Data.GameDescription;
import hu.benkototh.cardgame.backend.rest.Data.GameRules;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@RestController
@RequestMapping("/api/games")
public class GameRestService {

    @Autowired
    private GameController gameController;

    @GetMapping("/all")
    public ResponseEntity<List<Game>> getAllGames() {
        return ResponseEntity.ok(gameController.getAllGames());
    }

    @GetMapping("/active")
    public ResponseEntity<List<Game>> getActiveGames() {
        return ResponseEntity.ok(gameController.getActiveGames());
    }

    @GetMapping("/{id}")
    public ResponseEntity<?> getGameById(@PathVariable long id) {
        Optional<Game> game = gameController.getGameById(id);

        if (game.isPresent()) {
            return ResponseEntity.ok(game.get());
        } else {
            Map<String, String> response = new HashMap<>();
            response.put("message", "Game not found");
            return ResponseEntity.status(404).body(response);
        }
    }

    @GetMapping("/name/{name}")
    public ResponseEntity<?> getGameByName(@PathVariable String name) {
        Optional<Game> game = gameController.getGameByName(name);

        if (game.isPresent()) {
            return ResponseEntity.ok(game.get());
        } else {
            Map<String, String> response = new HashMap<>();
            response.put("message", "Game not found");
            return ResponseEntity.status(404).body(response);
        }
    }

    @PostMapping("/create")
    public ResponseEntity<Map<String, String>> createGame(@RequestBody Game game) {
        Map<String, String> response = new HashMap<>();

        Game createdGame = gameController.createGame(game);

        if (createdGame == null) {
            response.put("message", "Game with this name already exists.");
            return ResponseEntity.status(400).body(response);
        }

        response.put("message", "Game created successfully.");
        response.put("id", String.valueOf(createdGame.getId()));
        return ResponseEntity.ok(response);
    }

    @PutMapping("/update")
    public ResponseEntity<Map<String, String>> updateGame(@RequestBody Game game) {
        Map<String, String> response = new HashMap<>();

        Game updatedGame = gameController.updateGame(game);

        if (updatedGame == null) {
            response.put("message", "Game not found.");
            return ResponseEntity.status(404).body(response);
        }

        response.put("message", "Game updated successfully.");
        return ResponseEntity.ok(response);
    }

    @DeleteMapping("/delete/{id}")
    public ResponseEntity<Map<String, String>> deleteGame(@PathVariable long id) {
        Map<String, String> response = new HashMap<>();

        boolean deleted = gameController.deleteGame(id);

        if (deleted) {
            response.put("message", "Game deleted successfully.");
            return ResponseEntity.ok(response);
        } else {
            response.put("message", "Game not found.");
            return ResponseEntity.status(404).body(response);
        }
    }

    @PostMapping("/{id}/description")
    public ResponseEntity<Map<String, String>> addGameDescription(
            @PathVariable long id,
            @RequestBody GameDescription description) {
        Map<String, String> response = new HashMap<>();

        GameDescription addedDescription = gameController.addGameDescription(id, description);

        if (addedDescription == null) {
            response.put("message", "Game not found.");
            return ResponseEntity.status(404).body(response);
        }

        response.put("message", "Game description added/updated successfully.");
        return ResponseEntity.ok(response);
    }

    @PostMapping("/{id}/rules")
    public ResponseEntity<Map<String, String>> addGameRules(
            @PathVariable long id,
            @RequestBody GameRules rules) {
        Map<String, String> response = new HashMap<>();

        GameRules addedRules = gameController.addGameRules(id, rules);

        if (addedRules == null) {
            response.put("message", "Game not found.");
            return ResponseEntity.status(404).body(response);
        }

        response.put("message", "Game rules added/updated successfully.");
        return ResponseEntity.ok(response);
    }

    @DeleteMapping("/{id}/description/{language}")
    public ResponseEntity<Map<String, String>> removeGameDescription(
            @PathVariable long id,
            @PathVariable String language) {
        Map<String, String> response = new HashMap<>();

        boolean removed = gameController.removeGameDescription(id, language);

        if (removed) {
            response.put("message", "Game description removed successfully.");
            return ResponseEntity.ok(response);
        } else {
            response.put("message", "Game or description not found.");
            return ResponseEntity.status(404).body(response);
        }
    }

    @DeleteMapping("/{id}/rules/{language}")
    public ResponseEntity<Map<String, String>> removeGameRules(
            @PathVariable long id,
            @PathVariable String language) {
        Map<String, String> response = new HashMap<>();

        boolean removed = gameController.removeGameRules(id, language);

        if (removed) {
            response.put("message", "Game rules removed successfully.");
            return ResponseEntity.ok(response);
        } else {
            response.put("message", "Game or rules not found.");
            return ResponseEntity.status(404).body(response);
        }
    }
}
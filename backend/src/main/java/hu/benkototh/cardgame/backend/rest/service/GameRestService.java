package hu.benkototh.cardgame.backend.rest.service;

import hu.benkototh.cardgame.backend.rest.controller.GameController;
import hu.benkototh.cardgame.backend.rest.Data.Game;
import hu.benkototh.cardgame.backend.rest.Data.GameDescription;
import hu.benkototh.cardgame.backend.rest.Data.GameRules;
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
import java.util.Optional;

@RestController
@RequestMapping("/api/games")
@Tag(name = "Games", description = "Operations for managing card games")
public class GameRestService {

    @Autowired
    private GameController gameController;

    @Operation(summary = "Get all games", description = "Retrieves a list of all games")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Successfully retrieved games",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = Game.class)))
    })
    @GetMapping("/all")
    public ResponseEntity<List<Game>> getAllGames() {
        return ResponseEntity.ok(gameController.getAllGames());
    }

    @Operation(summary = "Get active games", description = "Retrieves a list of all active games")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Successfully retrieved active games",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = Game.class)))
    })
    @GetMapping("/active")
    public ResponseEntity<List<Game>> getActiveGames() {
        return ResponseEntity.ok(gameController.getActiveGames());
    }

    @Operation(summary = "Get game by ID", description = "Retrieves a specific game by its ID")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Successfully retrieved game",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = Game.class))),
            @ApiResponse(responseCode = "404", description = "Game not found")
    })
    @GetMapping("/{id}")
    public ResponseEntity<?> getGameById(
            @Parameter(description = "Game ID", required = true) @PathVariable long id) {
        Optional<Game> game = gameController.getGameById(id);

        if (game.isPresent()) {
            return ResponseEntity.ok(game.get());
        } else {
            Map<String, String> response = new HashMap<>();
            response.put("message", "Game not found");
            return ResponseEntity.status(404).body(response);
        }
    }

    @Operation(summary = "Get game by name", description = "Retrieves a specific game by its name")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Successfully retrieved game",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = Game.class))),
            @ApiResponse(responseCode = "404", description = "Game not found")
    })
    @GetMapping("/name/{name}")
    public ResponseEntity<?> getGameByName(
            @Parameter(description = "Game name", required = true) @PathVariable String name) {
        Optional<Game> game = gameController.getGameByName(name);

        if (game.isPresent()) {
            return ResponseEntity.ok(game.get());
        } else {
            Map<String, String> response = new HashMap<>();
            response.put("message", "Game not found");
            return ResponseEntity.status(404).body(response);
        }
    }

    @Operation(summary = "Create game", description = "Creates a new game")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Game created successfully"),
            @ApiResponse(responseCode = "400", description = "Game with this name already exists")
    })
    @PostMapping("/create")
    public ResponseEntity<Map<String, String>> createGame(
            @Parameter(description = "Game details", required = true) @RequestBody Game game) {
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

    @Operation(summary = "Update game", description = "Updates an existing game")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Game updated successfully"),
            @ApiResponse(responseCode = "404", description = "Game not found")
    })
    @PutMapping("/update")
    public ResponseEntity<Map<String, String>> updateGame(
            @Parameter(description = "Updated game details", required = true) @RequestBody Game game) {
        Map<String, String> response = new HashMap<>();

        Game updatedGame = gameController.updateGame(game);

        if (updatedGame == null) {
            response.put("message", "Game not found.");
            return ResponseEntity.status(404).body(response);
        }

        response.put("message", "Game updated successfully.");
        return ResponseEntity.ok(response);
    }

    @Operation(summary = "Delete game", description = "Deletes a game")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Game deleted successfully"),
            @ApiResponse(responseCode = "404", description = "Game not found")
    })
    @DeleteMapping("/delete/{id}")
    public ResponseEntity<Map<String, String>> deleteGame(
            @Parameter(description = "Game ID", required = true) @PathVariable long id) {
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

    @Operation(summary = "Add game description", description = "Adds or updates a description for a game")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Game description added/updated successfully"),
            @ApiResponse(responseCode = "404", description = "Game not found")
    })
    @PostMapping("/{id}/description")
    public ResponseEntity<Map<String, String>> addGameDescription(
            @Parameter(description = "Game ID", required = true) @PathVariable long id,
            @Parameter(description = "Game description details", required = true) @RequestBody GameDescription description) {
        Map<String, String> response = new HashMap<>();

        GameDescription addedDescription = gameController.addGameDescription(id, description);

        if (addedDescription == null) {
            response.put("message", "Game not found.");
            return ResponseEntity.status(404).body(response);
        }

        response.put("message", "Game description added/updated successfully.");
        return ResponseEntity.ok(response);
    }

    @Operation(summary = "Add game rules", description = "Adds or updates rules for a game")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Game rules added/updated successfully"),
            @ApiResponse(responseCode = "404", description = "Game not found")
    })
    @PostMapping("/{id}/rules")
    public ResponseEntity<Map<String, String>> addGameRules(
            @Parameter(description = "Game ID", required = true) @PathVariable long id,
            @Parameter(description = "Game rules details", required = true) @RequestBody GameRules rules) {
        Map<String, String> response = new HashMap<>();

        GameRules addedRules = gameController.addGameRules(id, rules);

        if (addedRules == null) {
            response.put("message", "Game not found.");
            return ResponseEntity.status(404).body(response);
        }

        response.put("message", "Game rules added/updated successfully.");
        return ResponseEntity.ok(response);
    }

    @Operation(summary = "Remove game description", description = "Removes a description for a game in a specific language")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Game description removed successfully"),
            @ApiResponse(responseCode = "404", description = "Game or description not found")
    })
    @DeleteMapping("/{id}/description/{language}")
    public ResponseEntity<Map<String, String>> removeGameDescription(
            @Parameter(description = "Game ID", required = true) @PathVariable long id,
            @Parameter(description = "Language code", required = true) @PathVariable String language) {
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

    @Operation(summary = "Remove game rules", description = "Removes rules for a game in a specific language")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Game rules removed successfully"),
            @ApiResponse(responseCode = "404", description = "Game or rules not found")
    })
    @DeleteMapping("/{id}/rules/{language}")
    public ResponseEntity<Map<String, String>> removeGameRules(
            @Parameter(description = "Game ID", required = true) @PathVariable long id,
            @Parameter(description = "Language code", required = true) @PathVariable String language) {
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
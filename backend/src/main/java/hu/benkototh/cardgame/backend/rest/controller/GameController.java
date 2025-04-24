package hu.benkototh.cardgame.backend.rest.controller;

import hu.benkototh.cardgame.backend.rest.Data.Game;
import hu.benkototh.cardgame.backend.rest.Data.GameDescription;
import hu.benkototh.cardgame.backend.rest.Data.GameRules;
import hu.benkototh.cardgame.backend.rest.repository.IGameRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;

import java.util.List;
import java.util.Optional;
import java.util.Set;

@Controller
public class GameController {

    @Autowired
    private IGameRepository gameRepository;

    @Autowired
    private AuditLogController auditLogController;

    public List<Game> getAllGames() {
        return gameRepository.findAll();
    }

    public List<Game> getActiveGames() {
        return findByActive();
    }

    public Optional<Game> getGameById(long gameId) {
        return gameRepository.findById(gameId);
    }

    public Optional<Game> getGameByName(String name) {
        return findByName(name);
    }

    public Game createGame(Game game) {
        if (existsByName(game.getName())) {
            auditLogController.logAction("GAME_CREATION_FAILED", 0L,
                    "Game creation failed: Game name already exists - " + game.getName());
            return null;
        }

        Game createdGame = gameRepository.save(game);

        auditLogController.logAction("CREATED_GAME", 0L,
                "Created game: " + game.getName() + " (ID: " + createdGame.getId() + ")");

        return createdGame;
    }

    public Game updateGame(Game game) {
        if (!gameRepository.existsById(game.getId())) {
            auditLogController.logAction("GAME_UPDATE_FAILED", 0L,
                    "Game update failed: Game not found - ID: " + game.getId());
            return null;
        }

        Game updatedGame = gameRepository.save(game);

        auditLogController.logAction("UPDATED_GAME", 0L,
                "Updated game: " + game.getName() + " (ID: " + updatedGame.getId() + ")");

        return updatedGame;
    }

    public boolean deleteGame(long gameId) {
        Optional<Game> gameOptional = gameRepository.findById(gameId);

        if (gameOptional.isEmpty()) {
            auditLogController.logAction("GAME_DELETION_FAILED", 0L,
                    "Game deletion failed: Game not found - ID: " + gameId);
            return false;
        }

        Game game = gameOptional.get();
        gameRepository.delete(game);

        auditLogController.logAction("DELETED_GAME", 0L,
                "Deleted game: " + game.getName() + " (ID: " + gameId + ")");

        return true;
    }

    public GameDescription addGameDescription(long gameId, GameDescription description) {
        Optional<Game> gameOptional = gameRepository.findById(gameId);

        if (gameOptional.isEmpty()) {
            auditLogController.logAction("GAME_DESCRIPTION_ADD_FAILED", 0L,
                    "Game description add failed: Game not found - ID: " + gameId);
            return null;
        }

        Game game = gameOptional.get();

        boolean languageExists = game.getDescriptions().stream()
                .anyMatch(desc -> desc.getLanguage().equals(description.getLanguage()));

        if (languageExists) {
            game.getDescriptions().stream()
                    .filter(desc -> desc.getLanguage().equals(description.getLanguage()))
                    .findFirst()
                    .ifPresent(desc -> desc.setContent(description.getContent()));
        } else {
            game.addDescription(description);
        }

        gameRepository.save(game);

        auditLogController.logAction("ADDED_GAME_DESCRIPTION", 0L,
                "Added/updated description for game: " + game.getName() + " in language: " + description.getLanguage());

        return description;
    }

    public GameRules addGameRules(long gameId, GameRules rules) {
        Optional<Game> gameOptional = gameRepository.findById(gameId);

        if (gameOptional.isEmpty()) {
            auditLogController.logAction("GAME_RULES_ADD_FAILED", 0L,
                    "Game rules add failed: Game not found - ID: " + gameId);
            return null;
        }

        Game game = gameOptional.get();

        boolean languageExists = game.getRules().stream()
                .anyMatch(r -> r.getLanguage().equals(rules.getLanguage()));

        if (languageExists) {
            game.getRules().stream()
                    .filter(r -> r.getLanguage().equals(rules.getLanguage()))
                    .findFirst()
                    .ifPresent(r -> r.setContent(rules.getContent()));
        } else {
            game.addRules(rules);
        }

        gameRepository.save(game);

        auditLogController.logAction("ADDED_GAME_RULES", 0L,
                "Added/updated rules for game: " + game.getName() + " in language: " + rules.getLanguage());

        return rules;
    }

    public boolean removeGameDescription(long gameId, String language) {
        Optional<Game> gameOptional = gameRepository.findById(gameId);

        if (gameOptional.isEmpty()) {
            auditLogController.logAction("GAME_DESCRIPTION_REMOVE_FAILED", 0L,
                    "Game description remove failed: Game not found - ID: " + gameId);
            return false;
        }

        Game game = gameOptional.get();

        boolean removed = false;
        for (GameDescription desc : Set.copyOf(game.getDescriptions())) {
            if (desc.getLanguage().equals(language)) {
                game.removeDescription(desc);
                removed = true;
                break;
            }
        }

        if (removed) {
            gameRepository.save(game);
            auditLogController.logAction("REMOVED_GAME_DESCRIPTION", 0L,
                    "Removed description for game: " + game.getName() + " in language: " + language);
            return true;
        }

        return false;
    }

    public boolean removeGameRules(long gameId, String language) {
        Optional<Game> gameOptional = gameRepository.findById(gameId);

        if (gameOptional.isEmpty()) {
            auditLogController.logAction("GAME_RULES_REMOVE_FAILED", 0L,
                    "Game rules remove failed: Game not found - ID: " + gameId);
            return false;
        }

        Game game = gameOptional.get();

        boolean removed = false;
        for (GameRules rules : Set.copyOf(game.getRules())) {
            if (rules.getLanguage().equals(language)) {
                game.removeRules(rules);
                removed = true;
                break;
            }
        }

        if (removed) {
            gameRepository.save(game);
            auditLogController.logAction("REMOVED_GAME_RULES", 0L,
                    "Removed rules for game: " + game.getName() + " in language: " + language);
            return true;
        }

        return false;
    }

    private List<Game> findByActive() {
        return gameRepository.findAll().stream()
                .filter(game -> game.isActive())
                .toList();
    }

    private Optional<Game> findByName(String name) {
        return gameRepository.findAll().stream()
                .filter(game -> game.getName().equalsIgnoreCase(name))
                .findFirst();
    }

    private boolean existsByName(String name) {
        return gameRepository.findAll().stream()
                .anyMatch(game -> game.getName().equalsIgnoreCase(name));
    }
}
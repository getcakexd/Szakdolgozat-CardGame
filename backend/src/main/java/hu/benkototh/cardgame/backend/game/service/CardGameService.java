package hu.benkototh.cardgame.backend.game.service;

import hu.benkototh.cardgame.backend.game.exception.GameException;
import hu.benkototh.cardgame.backend.game.model.*;
import hu.benkototh.cardgame.backend.game.repository.ICardGameRepository;
import hu.benkototh.cardgame.backend.rest.Data.Game;
import hu.benkototh.cardgame.backend.rest.Data.User;
import hu.benkototh.cardgame.backend.rest.controller.UserController;
import hu.benkototh.cardgame.backend.rest.repository.IGameRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

@Service
public class CardGameService {

    @Autowired
    private SimpMessagingTemplate messagingTemplate;

    @Autowired
    private UserController userController;

    @Autowired
    private ICardGameRepository cardGameRepository;

    @Autowired
    private IGameRepository gameRepository;

    @Autowired
    private StatisticsService statisticsService;

    private final Map<String, CardGame> activeGames = new ConcurrentHashMap<>();

    public CardGame createCardGame(long gameDefinitionId, String creatorId, String gameName, boolean trackStatistics) {
        Optional<Game> gameDefinition = gameRepository.findById(gameDefinitionId);
        if (gameDefinition.isEmpty()) {
            throw new GameException("Game definition not found");
        }

        Game gameEntity = gameDefinition.get();

        CardGame cardGame = GameFactory.createGame(gameEntity.getName());
        cardGame.setGameDefinitionId(gameDefinitionId);
        cardGame.setName(gameName);
        cardGame.setTrackStatistics(trackStatistics);

        User user = userController.getUser(Long.parseLong(creatorId));
        if (user == null) {
            throw new GameException("User not found");
        }

        Player player = new Player();
        player.setId(creatorId);
        player.setUsername(user.getUsername());
        player.setHand(new ArrayList<>());
        player.setWonCards(new ArrayList<>());

        cardGame.addPlayer(player);
        activeGames.put(cardGame.getId(), cardGame);

        cardGameRepository.save(cardGame);

        GameEvent event = new GameEvent("GAME_CREATED", cardGame.getId(), creatorId);
        event.addData("game", cardGame);
        broadcastGameEvent(event);

        return cardGame;
    }

    public CardGame joinGame(String gameId, String userId) {
        CardGame cardGame = activeGames.get(gameId);
        if (cardGame == null) {
            cardGame = cardGameRepository.findById(gameId).orElse(null);
            if (cardGame != null) {
                activeGames.put(gameId, cardGame);
            } else {
                throw new GameException("Game not found");
            }
        }

        if (cardGame.getStatus() != GameStatus.WAITING) {
            throw new GameException("Cannot join a game that has already started");
        }

        User user = userController.getUser(Long.parseLong(userId));
        if (user == null) {
            throw new GameException("User not found");
        }

        for (Player p : cardGame.getPlayers()) {
            if (p.getId().equals(userId)) {
                return cardGame;
            }
        }

        Player player = new Player();
        player.setId(userId);
        player.setUsername(user.getUsername());
        player.setHand(new ArrayList<>());
        player.setWonCards(new ArrayList<>());

        cardGame.addPlayer(player);

        cardGameRepository.save(cardGame);

        GameEvent event = new GameEvent("PLAYER_JOINED", cardGame.getId(), userId);
        event.addData("player", player);
        broadcastGameEvent(event);

        return cardGame;
    }

    public CardGame leaveGame(String gameId, String userId) {
        CardGame cardGame = activeGames.get(gameId);
        if (cardGame == null) {
            throw new GameException("Game not found");
        }

        cardGame.removePlayer(userId);

        if (cardGame.getPlayers().isEmpty()) {
            activeGames.remove(gameId);
            cardGameRepository.deleteById(gameId);
        } else {
            cardGameRepository.save(cardGame);
        }

        GameEvent event = new GameEvent("PLAYER_LEFT", cardGame.getId(), userId);
        broadcastGameEvent(event);

        return cardGame;
    }

    public CardGame startGame(String gameId, String userId) {
        CardGame cardGame = activeGames.get(gameId);
        if (cardGame == null) {
            throw new GameException("Game not found");
        }

        boolean isPlayerInGame = cardGame.getPlayers().stream()
                .anyMatch(p -> p.getId().equals(userId));

        if (!isPlayerInGame) {
            throw new GameException("User is not in the game");
        }

        cardGame.startGame();

        cardGameRepository.save(cardGame);

        GameEvent event = new GameEvent("GAME_STARTED", cardGame.getId());
        event.addData("game", cardGame);
        broadcastGameEvent(event);

        return cardGame;
    }

    public CardGame executeGameAction(String gameId, String userId, GameAction action) {
        CardGame cardGame = activeGames.get(gameId);
        if (cardGame == null) {
            throw new GameException("Game not found");
        }

        if (cardGame.getStatus() != GameStatus.ACTIVE) {
            throw new GameException("Game is not active");
        }

        if (!cardGame.getCurrentPlayer().getId().equals(userId)) {
            throw new GameException("It's not your turn");
        }

        if (!cardGame.isValidMove(userId, action)) {
            throw new GameException("Invalid move");
        }

        cardGame.executeMove(userId, action);

        if (cardGame.isGameOver()) {
            cardGame.endGame();

            if (cardGame.isTrackStatistics()) {
                Map<String, Integer> scores = cardGame.calculateScores();
                statisticsService.updateStatistics(cardGame, scores);
            }
        }

        cardGameRepository.save(cardGame);

        GameEvent event = new GameEvent("GAME_ACTION", cardGame.getId(), userId);
        event.addData("action", action);
        event.addData("gameState", cardGame.getGameState());
        broadcastGameEvent(event);

        return cardGame;
    }

    public List<CardGame> getActiveGames() {
        return cardGameRepository.findByStatus(GameStatus.ACTIVE);
    }

    public List<CardGame> getWaitingGames() {
        return cardGameRepository.findByStatus(GameStatus.WAITING);
    }

    public List<CardGame> getGamesByDefinition(long gameDefinitionId) {
        return cardGameRepository.findByGameDefinitionId(gameDefinitionId);
    }

    public CardGame getGame(String gameId) {
        CardGame cardGame = activeGames.get(gameId);
        if (cardGame == null) {
            cardGame = cardGameRepository.findById(gameId).orElse(null);
            if (cardGame != null) {
                activeGames.put(gameId, cardGame);
            }
        }
        return cardGame;
    }

    private void broadcastGameEvent(GameEvent event) {
        messagingTemplate.convertAndSend("/topic/game/" + event.getGameId(), event);
    }

    public List<Game> getGameDefinitions() {
        return gameRepository.findAll();
    }

    public Game getGameDefinition(long gameDefinitionId) {
        return gameRepository.findById(gameDefinitionId).orElse(null);
    }
}
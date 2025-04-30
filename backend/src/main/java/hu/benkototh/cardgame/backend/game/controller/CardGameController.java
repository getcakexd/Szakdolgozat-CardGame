package hu.benkototh.cardgame.backend.game.controller;

import hu.benkototh.cardgame.backend.game.exception.GameException;
import hu.benkototh.cardgame.backend.game.model.*;
import hu.benkototh.cardgame.backend.game.repository.ICardGameRepository;
import hu.benkototh.cardgame.backend.game.service.CardGameService;
import hu.benkototh.cardgame.backend.game.service.GameFactory;
import hu.benkototh.cardgame.backend.rest.Data.Game;
import hu.benkototh.cardgame.backend.rest.Data.User;
import hu.benkototh.cardgame.backend.rest.controller.UserController;
import hu.benkototh.cardgame.backend.rest.repository.IGameRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Controller;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Controller
public class CardGameController {
    private static final Logger logger = LoggerFactory.getLogger(CardGameController.class);

    @Autowired
    private SimpMessagingTemplate messagingTemplate;

    @Autowired
    private UserController userController;

    @Autowired
    private ICardGameRepository cardGameRepository;

    @Autowired
    private IGameRepository gameRepository;

    @Autowired
    private StatisticsController statisticsController;

    @Autowired
    private CardGameService cardGameService;

    @Transactional
    public CardGame createCardGame(long gameDefinitionId, String creatorId, String gameName, boolean trackStatistics) {
        logger.info("Creating card game with definition ID: {}, creator: {}, name: {}",
                gameDefinitionId, creatorId, gameName);

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
        player.setGame(cardGame);

        cardGame.addPlayer(player);

        cardGame = cardGameRepository.save(cardGame);
        logger.info("Card game created with ID: {}", cardGame.getId());

        GameEvent event = new GameEvent("GAME_CREATED", cardGame.getId(), creatorId);
        event.addData("game", cardGame);
        broadcastGameEvent(event);

        return cardGame;
    }

    @Transactional
    public CardGame joinGame(String gameId, String userId) {
        logger.info("User {} joining game {}", userId, gameId);

        Optional<CardGame> optionalCardGame = cardGameRepository.findById(gameId);
        if (optionalCardGame.isEmpty()) {
            throw new GameException("Game not found");
        }

        CardGame cardGame = optionalCardGame.get();

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
        player.setGame(cardGame);

        cardGame.addPlayer(player);

        cardGame = cardGameRepository.save(cardGame);
        logger.info("User {} joined game {}", userId, gameId);

        GameEvent event = new GameEvent("PLAYER_JOINED", cardGame.getId(), userId);
        event.addData("player", player);
        broadcastGameEvent(event);

        return cardGame;
    }

    @Transactional
    public CardGame leaveGame(String gameId, String userId) {
        logger.info("User {} leaving game {}", userId, gameId);

        Optional<CardGame> optionalCardGame = cardGameRepository.findById(gameId);
        if (optionalCardGame.isEmpty()) {
            throw new GameException("Game not found");
        }

        CardGame cardGame = optionalCardGame.get();

        if (cardGame.getCurrentPlayer() != null && cardGame.getCurrentPlayer().getId().equals(userId)) {
            cardGame.setCurrentPlayer(null);
        }

        cardGame.removePlayer(userId);

        if (cardGame.getPlayers().isEmpty()) {
            cardGameRepository.deleteById(gameId);
            logger.info("Game {} deleted as all players left", gameId);
        } else {
            cardGame = cardGameRepository.save(cardGame);
            logger.info("User {} left game {}", userId, gameId);
        }

        GameEvent event = new GameEvent("PLAYER_LEFT", cardGame.getId(), userId);
        broadcastGameEvent(event);

        return cardGame;
    }

    @Transactional
    public CardGame startGame(String gameId, String userId) {
        logger.info("Starting game {} by user {}", gameId, userId);

        Optional<CardGame> optionalCardGame = cardGameRepository.findById(gameId);
        if (optionalCardGame.isEmpty()) {
            throw new GameException("Game not found");
        }

        CardGame cardGame = optionalCardGame.get();

        boolean isPlayerInGame = cardGame.getPlayers().stream()
                .anyMatch(p -> p.getId().equals(userId));

        if (!isPlayerInGame) {
            throw new GameException("User is not in the game");
        }

        cardGame.startGame();
        cardGame = cardGameRepository.save(cardGame);
        logger.info("Game {} started successfully", gameId);

        GameEvent event = new GameEvent("GAME_STARTED", cardGame.getId());
        event.addData("game", cardGame);
        broadcastGameEvent(event);

        return cardGame;
    }

    @Transactional
    public CardGame executeGameAction(String gameId, String userId, GameAction action) {
        logger.info("Executing action {} in game {} by user {}", action.getActionType(), gameId, userId);

        Optional<CardGame> optionalCardGame = cardGameRepository.findById(gameId);
        if (optionalCardGame.isEmpty()) {
            throw new GameException("Game not found");
        }

        CardGame cardGame = optionalCardGame.get();

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
            logger.info("Game {} has ended", gameId);

            if (cardGame.isTrackStatistics()) {
                Map<String, Integer> scores = cardGame.calculateScores();
                statisticsController.updateStatistics(cardGame, scores);
                logger.info("Statistics updated for game {}", gameId);
            }
        }

        cardGame = cardGameRepository.save(cardGame);
        logger.info("Action executed successfully in game {}", gameId);

        cardGame = cardGameRepository.findById(gameId).orElse(cardGame);

        GameEvent event = new GameEvent("GAME_ACTION", cardGame.getId(), userId);
        event.addData("action", action);
        event.addData("gameState", cardGame.getGameState());
        broadcastGameEvent(event);

        return cardGame;
    }

    public void sendGameMessage(String gameId, String userId, String messageType, String content) {
        if (messageType != null && messageType.equals("PARTNER_MESSAGE")) {
            GameEvent event = new GameEvent("PARTNER_MESSAGE", gameId, userId);
            event.addData("content", content);
            broadcastGameEvent(event);
        }
    }

    @Transactional(readOnly = true)
    public List<CardGame> getActiveGames() {
        return cardGameRepository.findByStatus(GameStatus.ACTIVE);
    }

    @Transactional(readOnly = true)
    public List<CardGame> getWaitingGames() {
        return cardGameRepository.findByStatus(GameStatus.WAITING);
    }

    @Transactional(readOnly = true)
    public List<CardGame> getGamesByDefinition(long gameDefinitionId) {
        return cardGameRepository.findByGameDefinitionId(gameDefinitionId);
    }

    @Transactional(readOnly = true)
    public CardGame getGame(String gameId) {
        return cardGameRepository.findById(gameId).orElse(null);
    }

    private void broadcastGameEvent(GameEvent event) {
        messagingTemplate.convertAndSend("/topic/game/" + event.getGameId(), event);
    }

    @Transactional(readOnly = true)
    public List<Game> getGameDefinitions() {
        return gameRepository.findAll();
    }

    @Transactional(readOnly = true)
    public Game getGameDefinition(long gameDefinitionId) {
        return gameRepository.findById(gameDefinitionId).orElse(null);
    }

    @Transactional
    public void save(CardGame cardGame) {
        logger.info("Saving card game: {}", cardGame.getId());
        cardGameRepository.save(cardGame);
    }

    public void debugRepositoryState() {
        logger.info("Current repository state:");
        logger.info("Total games: {}", cardGameRepository.count());
        cardGameRepository.findAll().forEach(game -> {
            logger.info("Game ID: {}, Name: {}, Status: {}, Players: {}",
                    game.getId(), game.getName(), game.getStatus(), game.getPlayers().size());
        });
    }
}

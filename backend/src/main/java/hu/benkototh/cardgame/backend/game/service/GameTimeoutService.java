package hu.benkototh.cardgame.backend.game.service;

import hu.benkototh.cardgame.backend.game.controller.CardGameController;
import hu.benkototh.cardgame.backend.game.model.CardGame;
import hu.benkototh.cardgame.backend.game.model.GameStatus;
import hu.benkototh.cardgame.backend.game.model.Player;
import hu.benkototh.cardgame.backend.game.repository.ICardGameRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.event.ContextRefreshedEvent;
import org.springframework.context.event.EventListener;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.time.Duration;
import java.time.Instant;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Service
public class GameTimeoutService {
    private static final Logger logger = LoggerFactory.getLogger(GameTimeoutService.class);

    @Value("${game.timeout.minutes:5}")
    private long timeoutMinutes = 5;

    @Value("${game.timeout.check.interval:30000}")
    private long checkIntervalMs = 30000;

    @Autowired
    private ICardGameRepository cardGameRepository;

    @Autowired
    private CardGameController cardGameController;

    @Autowired
    private SimpMessagingTemplate messagingTemplate;

    private final Map<String, Map<String, Instant>> gamePlayerActivityMap = new ConcurrentHashMap<>();

    @EventListener(ContextRefreshedEvent.class)
    public void onApplicationEvent(ContextRefreshedEvent event) {
        logger.info("Application context refreshed, initializing GameTimeoutService");
        logger.info("Timeout set to {} minutes with check interval of {} ms", timeoutMinutes, checkIntervalMs);

        try {
            initializeActivityTracking();
        } catch (Exception e) {
            logger.error("Error initializing activity tracking: {}", e.getMessage(), e);
        }
    }

    @Transactional(readOnly = true)
    public void initializeActivityTracking() {
        logger.info("Initializing activity tracking for all active games");

        List<CardGame> activeGames = cardGameRepository.findByStatus(GameStatus.ACTIVE);
        Instant now = Instant.now();

        for (CardGame game : activeGames) {
            String gameId = game.getId();
            Map<String, Instant> playerMap = new ConcurrentHashMap<>();

            logger.info("Initializing activity tracking for game {}", gameId);

            for (Player player : game.getPlayers()) {
                String playerId = player.getId();
                playerMap.put(playerId, now);
                logger.info("Initialized activity for player {} in game {}", playerId, gameId);
            }

            gamePlayerActivityMap.put(gameId, playerMap);
        }

        logger.info("Activity tracking initialized for {} active games", activeGames.size());
    }

    public void recordActivity(String gameId, String userId) {
        if (gameId == null || userId == null) {
            logger.warn("Cannot record activity with null gameId or userId");
            return;
        }

        logger.debug("Recording activity for game {} by user {}", gameId, userId);

        gamePlayerActivityMap.computeIfAbsent(gameId, k -> new ConcurrentHashMap<>());

        gamePlayerActivityMap.get(gameId).put(userId, Instant.now());
    }

    @Scheduled(fixedRateString = "${game.timeout.check.interval:30000}")
    public void checkForTimedOutGames() {
        logger.info("Checking for timed out games...");
        Instant now = Instant.now();

        List<CardGame> activeGames;
        try {
            activeGames = getActiveGames();
        } catch (Exception e) {
            logger.error("Error getting active games: {}", e.getMessage(), e);
            return;
        }

        logger.info("Found {} active games to check for timeout", activeGames.size());

        for (CardGame game : activeGames) {
            try {
                checkGameForTimeout(game, now);
            } catch (Exception e) {
                logger.error("Error checking game {} for timeout: {}", game.getId(), e.getMessage(), e);
            }
        }

        cleanupFinishedGames();
    }

    @Transactional(readOnly = true)
    public List<CardGame> getActiveGames() {
        return cardGameRepository.findByStatus(GameStatus.ACTIVE);
    }

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void checkGameForTimeout(CardGame game, Instant now) {
        String gameId = game.getId();
        logger.debug("Checking game {} for timeout", gameId);

        if (!gamePlayerActivityMap.containsKey(gameId)) {
            logger.debug("No activity recorded for game {}, initializing", gameId);
            Map<String, Instant> playerMap = new ConcurrentHashMap<>();
            for (Player player : game.getPlayers()) {
                playerMap.put(player.getId(), now);
            }
            gamePlayerActivityMap.put(gameId, playerMap);
            return;
        }

        Player currentPlayer = game.getCurrentPlayer();
        if (currentPlayer == null) {
            logger.debug("No current player for game {}, skipping", gameId);
            return;
        }

        String currentPlayerId = currentPlayer.getId();
        Map<String, Instant> playerActivityMap = gamePlayerActivityMap.get(gameId);

        if (!playerActivityMap.containsKey(currentPlayerId)) {
            logger.debug("No activity recorded for current player {} in game {}, initializing",
                    currentPlayerId, gameId);
            playerActivityMap.put(currentPlayerId, now);
            return;
        }

        Instant lastActivity = playerActivityMap.get(currentPlayerId);
        Duration timeSinceLastActivity = Duration.between(lastActivity, now);

        logger.info("Game {}: Current player {} last activity was {} minutes ago",
                gameId, currentPlayerId, timeSinceLastActivity.toMinutes());

        if (timeSinceLastActivity.toMinutes() >= timeoutMinutes) {
            logger.info("Game {} has timed out after {} minutes of inactivity for player {}",
                    gameId, timeSinceLastActivity.toMinutes(), currentPlayerId);

            try {
                abandonGameForInactivePlayer(gameId, currentPlayerId);
            } catch (Exception e) {
                logger.error("Error abandoning timed out game {}: {}", gameId, e.getMessage(), e);
            }
        }
    }

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void abandonGameForInactivePlayer(String gameId, String playerId) {
        logger.info("Abandoning game {} for inactive player {}", gameId, playerId);

        try {
            CardGame game = cardGameController.getGame(gameId);

            if (game == null) {
                logger.warn("Game {} not found when trying to abandon for inactive player", gameId);
                return;
            }

            if (game.getStatus() != GameStatus.ACTIVE) {
                logger.info("Game {} is no longer active, skipping abandonment", gameId);
                return;
            }

            if (game.getCurrentPlayer() == null || !game.getCurrentPlayer().getId().equals(playerId)) {
                logger.info("Current player has changed in game {}, skipping abandonment", gameId);
                return;
            }

            CardGame abandonedGame = cardGameController.abandonGame(gameId, playerId);
            logger.info("Successfully abandoned game {} due to inactivity of player {}",
                    gameId, playerId);

            messagingTemplate.convertAndSend(
                    "/topic/game/" + gameId,
                    abandonedGame
            );

            gamePlayerActivityMap.remove(gameId);

        } catch (Exception e) {
            logger.error("Failed to abandon game {} for inactive player {}: {}",
                    gameId, playerId, e.getMessage(), e);
            throw e;
        }
    }

    private void cleanupFinishedGames() {
        gamePlayerActivityMap.keySet().removeIf(gameId -> {
            try {
                CardGame game = cardGameController.getGame(gameId);
                boolean shouldRemove = game == null || game.getStatus() == GameStatus.FINISHED;
                if (shouldRemove) {
                    logger.debug("Removing finished game {} from activity tracking", gameId);
                }
                return shouldRemove;
            } catch (Exception e) {
                logger.error("Error checking game status for cleanup: {}", e.getMessage());
                return false;
            }
        });
    }
}

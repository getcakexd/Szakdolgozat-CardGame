package hu.benkototh.cardgame.backend.game.service;

import hu.benkototh.cardgame.backend.game.controller.CardGameController;
import hu.benkototh.cardgame.backend.game.model.CardGame;
import hu.benkototh.cardgame.backend.game.model.GameStatus;
import hu.benkototh.cardgame.backend.game.repository.ICardGameRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.time.Instant;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Service
public class GameTimeoutService {
    private static final Logger logger = LoggerFactory.getLogger(GameTimeoutService.class);
    private static final long TIMEOUT_MINUTES = 5;

    @Autowired
    private ICardGameRepository cardGameRepository;

    @Autowired
    private CardGameController cardGameController;

    private final Map<String, Instant> lastActivityMap = new ConcurrentHashMap<>();

    public void recordActivity(String gameId, String userId) {
        lastActivityMap.put(gameId, Instant.now());
        logger.debug("Recorded activity for game {} by user {}", gameId, userId);
    }

    @Scheduled(fixedRate = 60000) // Run every minute
    public void checkForTimedOutGames() {
        logger.debug("Checking for timed out games");
        Instant now = Instant.now();

        cardGameRepository.findByStatus(GameStatus.ACTIVE).forEach(game -> {
            String gameId = game.getId();

            if (lastActivityMap.containsKey(gameId)) {
                Instant lastActivity = lastActivityMap.get(gameId);
                Duration timeSinceLastActivity = Duration.between(lastActivity, now);

                if (timeSinceLastActivity.toMinutes() >= TIMEOUT_MINUTES) {
                    logger.info("Game {} has timed out after {} minutes of inactivity",
                            gameId, timeSinceLastActivity.toMinutes());

                    String currentPlayerId = game.getCurrentPlayer() != null ?
                            game.getCurrentPlayer().getId() : game.getPlayers().get(0).getId();

                    try {
                        cardGameController.abandonGame(gameId, currentPlayerId);
                        logger.info("Automatically abandoned game {} due to inactivity", gameId);

                        lastActivityMap.remove(gameId);
                    } catch (Exception e) {
                        logger.error("Error abandoning timed out game {}: {}", gameId, e.getMessage());
                    }
                }
            } else {
                lastActivityMap.put(gameId, now);
            }
        });

        lastActivityMap.keySet().removeIf(gameId -> {
            CardGame game = cardGameRepository.findById(gameId).orElse(null);
            return game == null || game.getStatus() == GameStatus.FINISHED;
        });
    }
}

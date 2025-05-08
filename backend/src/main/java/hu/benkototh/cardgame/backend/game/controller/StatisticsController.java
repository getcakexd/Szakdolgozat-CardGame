package hu.benkototh.cardgame.backend.game.controller;

import hu.benkototh.cardgame.backend.game.model.CardGame;
import hu.benkototh.cardgame.backend.game.model.GameStatistics;
import hu.benkototh.cardgame.backend.game.repository.IGameStatisticsRepository;
import hu.benkototh.cardgame.backend.rest.Data.Lobby;
import hu.benkototh.cardgame.backend.rest.controller.LobbyController;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;
import java.util.stream.Collectors;

@Controller
public class StatisticsController {
    private static final Logger logger = LoggerFactory.getLogger(StatisticsController.class);

    @Autowired
    private IGameStatisticsRepository gameStatisticsRepository;

    @Autowired
    private StatsController statsController;

    @Transactional
    public void updateStatistics(CardGame game, Map<String, Integer> scores) {
        logger.info("Updating statistics for game {}", game.getId());

        int highestScore = scores.values().stream()
                .mapToInt(Integer::intValue)
                .max()
                .orElse(0);

        List<String> winnersIds = scores.entrySet().stream()
                .filter(entry -> entry.getValue() == highestScore)
                .map(Map.Entry::getKey)
                .collect(Collectors.toList());

        boolean isDraw = winnersIds.size() > 1;

        statsController.recordGameResult(game, scores, false, null, isDraw, winnersIds);

        for (Map.Entry<String, Integer> entry : scores.entrySet()) {
            String playerId = entry.getKey();
            int score = entry.getValue();
            boolean won = !isDraw && winnersIds.contains(playerId);
            boolean drawn = isDraw && winnersIds.contains(playerId);

            GameStatistics statistics = new GameStatistics();
            statistics.setUserId(playerId);
            statistics.setGameId(game.getId());
            statistics.setGameDefinitionId(game.getGameDefinitionId());
            statistics.setGameType(game.getClass().getSimpleName());
            statistics.setScore(score);
            statistics.setWon(won);
            statistics.setDrawn(drawn);
            statistics.setPlayedAt(new Date());

            gameStatisticsRepository.save(statistics);
        }

        logger.info("Statistics updated for game {}", game.getId());
    }

    @Transactional
    public void recordAbandonedGame(CardGame game, String abandonedBy) {
        logger.info("Recording abandoned game {}", game.getId());

        Map<String, Integer> scores = game.calculateScores();

        statsController.recordGameResult(game, scores, true, abandonedBy, false, Collections.emptyList());

        logger.info("Abandoned game {} recorded", game.getId());
    }

    public Map<String, Object> getUserStatistics(String userId, String gameType) {
        Map<String, Object> result = gameStatisticsRepository.getUserStatistics(userId, gameType);
        if (result == null) {
            result = new HashMap<>();
            result.put("gamesPlayed", 0);
            result.put("gamesWon", 0);
            result.put("gamesDrawn", 0);
            result.put("totalScore", 0);
            result.put("averageScore", 0);
        }
        return result;
    }

    public Map<String, Object> getUserStatisticsByGameDefinition(String userId, long gameDefinitionId) {
        Map<String, Object> result = gameStatisticsRepository.getUserStatisticsByGameDefinition(userId, gameDefinitionId);
        if (result == null) {
            result = new HashMap<>();
            result.put("gamesPlayed", 0);
            result.put("gamesWon", 0);
            result.put("gamesDrawn", 0);
            result.put("totalScore", 0);
            result.put("averageScore", 0);
        }
        return result;
    }
}

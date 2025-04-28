package hu.benkototh.cardgame.backend.game.controller;

import hu.benkototh.cardgame.backend.game.model.CardGame;
import hu.benkototh.cardgame.backend.game.model.Player;
import hu.benkototh.cardgame.backend.game.model.GameStatistics;
import hu.benkototh.cardgame.backend.game.repository.IGameStatisticsRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;

import java.util.Date;
import java.util.Map;

@Controller
public class StatisticsController {

    @Autowired
    private IGameStatisticsRepository statisticsRepository;

    public void updateStatistics(CardGame game, Map<String, Integer> scores) {
        for (Player player : game.getPlayers()) {
            GameStatistics statistics = new GameStatistics();
            statistics.setUserId(player.getId());
            statistics.setGameId(game.getId());
            statistics.setGameDefinitionId(game.getGameDefinitionId());
            statistics.setGameType(game.getClass().getSimpleName());
            statistics.setScore(scores.getOrDefault(player.getId(), 0));
            statistics.setWon(isWinner(player.getId(), scores));
            statistics.setPlayedAt(new Date());

            statisticsRepository.save(statistics);
        }
    }

    private boolean isWinner(String playerId, Map<String, Integer> scores) {
        int playerScore = scores.getOrDefault(playerId, 0);
        return scores.values().stream().noneMatch(score -> score > playerScore);
    }

    public Map<String, Object> getUserStatistics(String userId, String gameType) {
        return statisticsRepository.getUserStatistics(userId, gameType);
    }

    public Map<String, Object> getUserStatisticsByGameDefinition(String userId, long gameDefinitionId) {
        return statisticsRepository.getUserStatisticsByGameDefinition(userId, gameDefinitionId);
    }
}
package hu.benkototh.cardgame.backend.game.repository;

import hu.benkototh.cardgame.backend.game.model.GameStatistics;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.Map;

@Repository
public interface IGameStatisticsRepository extends JpaRepository<GameStatistics, Long> {

    @Query(value = "SELECT " +
            "COUNT(*) as gamesPlayed, " +
            "SUM(CASE WHEN won = true THEN 1 ELSE 0 END) as gamesWon, " +
            "SUM(score) as totalScore, " +
            "AVG(score) as averageScore " +
            "FROM game_statistics " +
            "WHERE user_id = ?1 " +
            "AND (?2 IS NULL OR game_type = ?2)",
            nativeQuery = true)
    Map<String, Object> getUserStatistics(String userId, String gameType);

    @Query(value = "SELECT " +
            "COUNT(*) as gamesPlayed, " +
            "SUM(CASE WHEN won = true THEN 1 ELSE 0 END) as gamesWon, " +
            "SUM(score) as totalScore, " +
            "AVG(score) as averageScore " +
            "FROM game_statistics " +
            "WHERE user_id = ?1 " +
            "AND game_definition_id = ?2",
            nativeQuery = true)
    Map<String, Object> getUserStatisticsByGameDefinition(String userId, long gameDefinitionId);
}
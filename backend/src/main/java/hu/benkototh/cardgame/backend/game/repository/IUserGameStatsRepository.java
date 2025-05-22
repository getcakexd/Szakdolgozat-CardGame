package hu.benkototh.cardgame.backend.game.repository;

import hu.benkototh.cardgame.backend.game.model.UserGameStats;
import hu.benkototh.cardgame.backend.rest.model.User;
import hu.benkototh.cardgame.backend.rest.model.Game;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface IUserGameStatsRepository extends JpaRepository<UserGameStats, Long> {
    List<UserGameStats> findByUser(User user);
    List<UserGameStats> findByUserId(Long userId);
    Optional<UserGameStats> findByUserAndGameDefinition(User user, Game gameDefinition);
    Optional<UserGameStats> findByUserIdAndGameDefinitionId(Long userId, Long gameDefinitionId);

    @Query("SELECT ugs FROM UserGameStats ugs WHERE ugs.user.id = :userId ORDER BY ugs.lastPlayed DESC")
    List<UserGameStats> findRecentGamesByUserId(Long userId);

    @Query("SELECT ugs FROM UserGameStats ugs ORDER BY ugs.totalPoints DESC")
    List<UserGameStats> findTopPlayersByPoints();

    @Query("SELECT ugs FROM UserGameStats ugs WHERE ugs.gameDefinition.id = :gameDefinitionId ORDER BY ugs.totalPoints DESC")
    List<UserGameStats> findTopPlayersByGameAndPoints(Long gameDefinitionId);
}

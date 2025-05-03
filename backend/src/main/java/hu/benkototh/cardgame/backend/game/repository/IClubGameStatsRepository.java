package hu.benkototh.cardgame.backend.game.repository;

import hu.benkototh.cardgame.backend.game.model.ClubGameStats;
import hu.benkototh.cardgame.backend.rest.Data.Club;
import hu.benkototh.cardgame.backend.rest.Data.Game;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface IClubGameStatsRepository extends JpaRepository<ClubGameStats, Long> {
    List<ClubGameStats> findByClub(Club club);
    List<ClubGameStats> findByClubId(Long clubId);
    Optional<ClubGameStats> findByClubAndGameDefinition(Club club, Game gameDefinition);
    Optional<ClubGameStats> findByClubIdAndGameDefinitionId(Long clubId, Long gameDefinitionId);

    @Query("SELECT cgs FROM ClubGameStats cgs ORDER BY cgs.totalPoints DESC")
    List<ClubGameStats> findTopClubsByPoints();

    @Query("SELECT cgs FROM ClubGameStats cgs WHERE cgs.gameDefinition.id = :gameDefinitionId ORDER BY cgs.totalPoints DESC")
    List<ClubGameStats> findTopClubsByGameAndPoints(Long gameDefinitionId);
}

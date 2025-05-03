package hu.benkototh.cardgame.backend.game.repository;

import hu.benkototh.cardgame.backend.game.model.ClubMemberStats;
import hu.benkototh.cardgame.backend.rest.Data.Club;
import hu.benkototh.cardgame.backend.rest.Data.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface IClubMemberStatsRepository extends JpaRepository<ClubMemberStats, Long> {
    Optional<ClubMemberStats> findByClubAndUser(Club club, User user);

    List<ClubMemberStats> findByClub(Club club);

    @Query("SELECT cms FROM ClubMemberStats cms WHERE cms.club.id = :clubId ORDER BY cms.totalPoints DESC")
    List<ClubMemberStats> findTopMembersByClubAndPoints(@Param("clubId") Long clubId);

    @Query("SELECT cms FROM ClubMemberStats cms " +
            "JOIN ClubGameStats cgs ON cms.club.id = cgs.club.id " +
            "WHERE cms.club.id = :clubId AND cgs.gameDefinition.id = :gameDefinitionId " +
            "ORDER BY cms.totalPoints DESC")
    List<ClubMemberStats> findTopMembersByClubAndGameAndPoints(
            @Param("clubId") Long clubId,
            @Param("gameDefinitionId") Long gameDefinitionId);
}

package hu.benkototh.cardgame.backend.game.repository;

import hu.benkototh.cardgame.backend.game.model.ClubMemberStats;
import hu.benkototh.cardgame.backend.rest.Data.Club;
import hu.benkototh.cardgame.backend.rest.Data.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface IClubMemberStatsRepository extends JpaRepository<ClubMemberStats, Long> {
    List<ClubMemberStats> findByClub(Club club);
    List<ClubMemberStats> findByClubId(Long clubId);
    List<ClubMemberStats> findByUser(User user);
    List<ClubMemberStats> findByUserId(Long userId);
    Optional<ClubMemberStats> findByClubAndUser(Club club, User user);
    Optional<ClubMemberStats> findByClubIdAndUserId(Long clubId, Long userId);

    @Query("SELECT cms FROM ClubMemberStats cms WHERE cms.club.id = :clubId ORDER BY cms.totalPoints DESC")
    List<ClubMemberStats> findTopMembersByClubAndPoints(Long clubId);
}

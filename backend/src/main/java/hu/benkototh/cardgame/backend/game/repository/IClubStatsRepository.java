package hu.benkototh.cardgame.backend.game.repository;

import hu.benkototh.cardgame.backend.game.model.ClubStats;
import hu.benkototh.cardgame.backend.rest.model.Club;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface IClubStatsRepository extends JpaRepository<ClubStats, Long> {
    Optional<ClubStats> findByClub(Club club);
    Optional<ClubStats> findByClubId(Long clubId);
}

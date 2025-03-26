package hu.benkototh.cardgame.backend.rest.repository;

import hu.benkototh.cardgame.backend.rest.Data.Club;
import org.springframework.data.jpa.repository.JpaRepository;

public interface IClubRepository extends JpaRepository<Club, Long> {
}

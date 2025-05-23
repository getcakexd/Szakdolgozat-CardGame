package hu.benkototh.cardgame.backend.rest.repository;

import hu.benkototh.cardgame.backend.rest.model.ClubMessage;
import org.springframework.data.jpa.repository.JpaRepository;

public interface IClubMessageRepository extends JpaRepository<ClubMessage, Long> {
}

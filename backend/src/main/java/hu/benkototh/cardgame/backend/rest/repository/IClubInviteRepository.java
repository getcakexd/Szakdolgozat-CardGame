package hu.benkototh.cardgame.backend.rest.repository;

import hu.benkototh.cardgame.backend.rest.model.ClubInvite;
import org.springframework.data.jpa.repository.JpaRepository;

public interface IClubInviteRepository extends JpaRepository<ClubInvite, Long> {
}

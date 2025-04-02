package hu.benkototh.cardgame.backend.rest.repository;

import hu.benkototh.cardgame.backend.rest.Data.Lobby;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ILobbyRepository extends JpaRepository<Lobby, Long> {
}

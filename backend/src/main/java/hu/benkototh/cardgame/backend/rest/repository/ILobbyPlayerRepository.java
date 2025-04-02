package hu.benkototh.cardgame.backend.rest.repository;

import hu.benkototh.cardgame.backend.rest.Data.LobbyPlayer;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ILobbyPlayerRepository extends JpaRepository<LobbyPlayer, Long> {
}

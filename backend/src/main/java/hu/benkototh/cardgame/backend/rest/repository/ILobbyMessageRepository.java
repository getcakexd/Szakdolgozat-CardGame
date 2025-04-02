package hu.benkototh.cardgame.backend.rest.repository;

import hu.benkototh.cardgame.backend.rest.Data.LobbyMessage;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ILobbyMessageRepository extends JpaRepository<LobbyMessage, Long> {
}

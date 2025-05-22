package hu.benkototh.cardgame.backend.rest.repository;

import hu.benkototh.cardgame.backend.rest.model.LobbyMessage;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ILobbyMessageRepository extends JpaRepository<LobbyMessage, Long> {
    List<LobbyMessage> findByIsLobbyMessageOrderByTimestampAsc(boolean isLobbyMessage);
    List<LobbyMessage> findByGameIdOrderByTimestampAsc(String gameId);
    List<LobbyMessage> findTop100ByIsLobbyMessageOrderByTimestampDesc(boolean isLobbyMessage);
    List<LobbyMessage> findTop100ByGameIdOrderByTimestampDesc(String gameId);
    List<LobbyMessage> findByLobbyIdOrderByTimestampAsc(long lobbyId);
    List<LobbyMessage> findTop100ByLobbyIdOrderByTimestampDesc(long lobbyId);
}

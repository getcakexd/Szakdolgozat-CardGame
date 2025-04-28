package hu.benkototh.cardgame.backend.game.repository;

import hu.benkototh.cardgame.backend.game.model.CardGame;
import hu.benkototh.cardgame.backend.game.model.GameStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ICardGameRepository extends JpaRepository<CardGame, String> {

    List<CardGame> findByStatus(GameStatus status);

    List<CardGame> findByGameDefinitionId(long gameDefinitionId);

    @Query("SELECT g FROM CardGame g JOIN g.players p WHERE p.id = :playerId")
    List<CardGame> findByPlayerId(String playerId);

    @Query("SELECT g FROM CardGame g WHERE g.status = :status AND g.gameDefinitionId = :gameDefinitionId")
    List<CardGame> findByStatusAndGameDefinitionId(GameStatus status, long gameDefinitionId);
}

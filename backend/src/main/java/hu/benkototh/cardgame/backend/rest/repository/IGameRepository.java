package hu.benkototh.cardgame.backend.rest.repository;

import hu.benkototh.cardgame.backend.rest.Data.Game;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface IGameRepository extends JpaRepository<Game, Long> {
}
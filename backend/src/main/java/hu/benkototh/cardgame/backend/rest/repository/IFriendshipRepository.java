package hu.benkototh.cardgame.backend.rest.repository;

import hu.benkototh.cardgame.backend.rest.model.Friendship;
import org.springframework.data.jpa.repository.JpaRepository;

public interface IFriendshipRepository extends JpaRepository<Friendship, Long> {
}

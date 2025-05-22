package hu.benkototh.cardgame.backend.rest.repository;

import hu.benkototh.cardgame.backend.rest.model.FriendRequest;
import org.springframework.data.jpa.repository.JpaRepository;

public interface IFriendRequestRepository extends JpaRepository<FriendRequest, Long> {
}

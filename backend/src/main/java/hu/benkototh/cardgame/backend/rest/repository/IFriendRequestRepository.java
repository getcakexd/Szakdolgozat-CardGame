package hu.benkototh.cardgame.backend.rest.repository;

import hu.benkototh.cardgame.backend.rest.Data.FriendRequest;
import hu.benkototh.cardgame.backend.rest.Data.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface IFriendRequestRepository extends JpaRepository<FriendRequest, Long> {
}

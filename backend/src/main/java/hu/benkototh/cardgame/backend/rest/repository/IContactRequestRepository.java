package hu.benkototh.cardgame.backend.rest.repository;

import hu.benkototh.cardgame.backend.rest.Data.ContactRequest;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface IContactRequestRepository extends JpaRepository<ContactRequest, Long> {
}
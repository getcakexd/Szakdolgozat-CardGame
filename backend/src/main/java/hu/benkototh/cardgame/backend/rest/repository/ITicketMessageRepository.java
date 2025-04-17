package hu.benkototh.cardgame.backend.rest.repository;

import hu.benkototh.cardgame.backend.rest.Data.TicketMessage;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface ITicketMessageRepository extends JpaRepository<TicketMessage, Long> {
}
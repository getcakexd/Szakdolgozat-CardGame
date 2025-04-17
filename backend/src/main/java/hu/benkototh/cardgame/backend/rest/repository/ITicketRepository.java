package hu.benkototh.cardgame.backend.rest.repository;

import hu.benkototh.cardgame.backend.rest.Data.Ticket;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface ITicketRepository extends JpaRepository<Ticket, Long> {
}
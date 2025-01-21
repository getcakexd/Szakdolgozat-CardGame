package hu.benkototh.cardgame.backend.rest.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import hu.benkototh.cardgame.backend.rest.Data.Data;

public interface DataRepository extends JpaRepository<Data, Long> {
}

package hu.benkototh.cardgame.backend.rest.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import hu.benkototh.cardgame.backend.rest.model.Data;

public interface IDataRepository extends JpaRepository<Data, Long> {
}

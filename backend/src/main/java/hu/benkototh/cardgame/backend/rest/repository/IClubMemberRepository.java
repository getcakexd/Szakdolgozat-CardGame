package hu.benkototh.cardgame.backend.rest.repository;

import hu.benkototh.cardgame.backend.rest.Data.ClubMember;
import org.springframework.data.jpa.repository.JpaRepository;

public interface IClubMemberRepository extends JpaRepository<ClubMember, Long> {
}

package hu.benkototh.cardgame.backend.rest.service;

import hu.benkototh.cardgame.backend.rest.Data.Club;
import hu.benkototh.cardgame.backend.rest.Data.ClubMember;
import hu.benkototh.cardgame.backend.rest.Data.User;
import hu.benkototh.cardgame.backend.rest.repository.IClubMemberRepository;
import hu.benkototh.cardgame.backend.rest.repository.IClubRepository;
import hu.benkototh.cardgame.backend.rest.repository.IUserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.Optional;

@RestController
@RequestMapping("/api/clubmembers")
public class ClubMemberRestService {

    @Autowired
    private IClubMemberRepository clubMemberRepository;

    @Autowired
    private IClubRepository clubRepository;

    @Autowired
    private IUserRepository userRepository;

    @GetMapping("/get")
    public List<ClubMember> getClubMembers(long clubId) {
        return findByClubId(clubId);
    }

    @PostMapping("/add")
    public ClubMember addClubMember(long clubId, long userId) {
        Optional<User> user = userRepository.findById(userId);
        Optional<Club> club = clubRepository.findById(clubId);

        if (user.isEmpty() || club.isEmpty()) {
            throw new IllegalArgumentException("User or club not found");
        }

        ClubMember clubMember = new ClubMember(club.get(), user.get());
        clubMemberRepository.save(clubMember);
        return clubMember;
    }

    private List<ClubMember> findByClubId(long clubId) {
        return clubMemberRepository.findAll().stream()
                .filter(clubMember -> clubMember.getClub().getId() == clubId)
                .toList();
    }
}

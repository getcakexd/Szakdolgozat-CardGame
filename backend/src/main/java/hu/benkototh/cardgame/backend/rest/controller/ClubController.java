package hu.benkototh.cardgame.backend.rest.controller;

import hu.benkototh.cardgame.backend.rest.Data.Club;
import hu.benkototh.cardgame.backend.rest.Data.ClubMember;
import hu.benkototh.cardgame.backend.rest.Data.User;
import hu.benkototh.cardgame.backend.rest.repository.IClubMemberRepository;
import hu.benkototh.cardgame.backend.rest.repository.IClubRepository;
import hu.benkototh.cardgame.backend.rest.repository.IUserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Controller
public class ClubController {

    @Autowired
    private IClubRepository clubRepository;

    @Autowired
    private IUserRepository userRepository;

    @Autowired
    private IClubMemberRepository clubMemberRepository;

    public List<Club> getAllClubs() {
        return clubRepository.findAll();
    }

    public List<Club> getPublicClubs() {
        return clubRepository.findAll().stream().filter(Club::isPublic).toList();
    }

    public List<Club> getJoinableClubs(long userId) {
        Optional<User> user = userRepository.findById(userId);
        
        if (user.isEmpty()) {
            return null;
        }
        
        return getJoinableClubs(user.get());
    }

    public List<Club> getJoinableClubs(User user) {
        return clubRepository.findAll().stream()
                .filter(club -> club.isPublic() &&
                        clubMemberRepository.findAll().stream()
                                .noneMatch(clubMember -> clubMember.getUser().equals(user) && clubMember.getClub().equals(club)))
                .collect(Collectors.toList());
    }

    public Club getClub(long clubId) {
        return clubRepository.findById(clubId).orElse(null);
    }

    public List<Club> getClubsByUser(long userId) {
        Optional<User> user = userRepository.findById(userId);
        
        if (user.isEmpty()) {
            return null;
        }
        
        return findClubsByUser(user.get());
    }

    public List<ClubMember> getClubMembers(long clubId) {
        Optional<Club> club = clubRepository.findById(clubId);
        
        if (club.isEmpty()) {
            return null;
        }
        
        return clubMemberRepository.findAll().stream()
                .filter(clubMember -> clubMember.getClub().equals(club.get()))
                .toList();
    }

    public Club createClub(String name, String description, boolean isPublic, long userId) {
        Optional<User> user = userRepository.findById(userId);
        
        if (user.isEmpty()) {
            return null;
        }
        
        Club club = new Club(name, description, isPublic);
        ClubMember clubMember = new ClubMember(club, user.get(), "admin");
        
        clubRepository.save(club);
        clubMemberRepository.save(clubMember);
        
        return club;
    }

    public Club updateClub(long clubId, String name, String description, boolean isPublic) {
        Optional<Club> clubOpt = clubRepository.findById(clubId);
        
        if (clubOpt.isEmpty()) {
            return null;
        }
        
        Club club = clubOpt.get();
        club.setName(name);
        club.setDescription(description);
        club.setPublic(isPublic);
        
        return clubRepository.save(club);
    }

    public boolean deleteClub(long clubId) {
        Optional<Club> club = clubRepository.findById(clubId);
        
        if (club.isEmpty()) {
            return false;
        }
        
        List<ClubMember> clubMembers = clubMemberRepository.findAll().stream()
                .filter(clubMember -> clubMember.getClub().equals(club.get()))
                .toList();
        
        clubMemberRepository.deleteAll(clubMembers);
        clubRepository.deleteById(clubId);
        
        return true;
    }

    public List<Club> findClubsByUser(User user) {
        return clubMemberRepository.findAll().stream()
                .filter(clubMember -> clubMember.getUser().equals(user))
                .map(ClubMember::getClub)
                .toList();
    }
}

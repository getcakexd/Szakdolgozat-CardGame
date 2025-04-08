package hu.benkototh.cardgame.backend.rest.controller;

import hu.benkototh.cardgame.backend.rest.Data.Club;
import hu.benkototh.cardgame.backend.rest.Data.ClubMember;
import hu.benkototh.cardgame.backend.rest.Data.User;
import hu.benkototh.cardgame.backend.rest.repository.IClubMemberRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Controller;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Controller
public class ClubMemberController {

    @Autowired
    private IClubMemberRepository clubMemberRepository;

    @Lazy
    @Autowired
    private UserController userController;

    @Lazy
    @Autowired
    private ClubController clubController;

    public List<ClubMember> getClubMembers(long clubId) {
        List<ClubMember> clubMembers = findByClubId(clubId);

        if (clubMembers.isEmpty()) {
            return null;
        }

        for (ClubMember member : clubMembers) {
            if (member.getUser() != null) {
                member.setUsername(member.getUser().getUsername());
            }
        }

        return clubMembers;
    }

    public ClubMember getClubMember(long clubId, long userId) {
        User user = userController.getUser(userId);
        Club club = clubController.getClub(clubId);
        
        if (user == null) {
            return null;
        }

        if (club == null) {
            return null;
        }

        return clubMemberRepository.findAll().stream()
                .filter(member -> member.getClub().getId() == club.getId() && member.getUser().getId() == user.getId())
                .findFirst()
                .orElse(null);
    }
    
    public ClubMember getClubMemberByUser(User user) {
        return clubMemberRepository.findAll().stream()
                .filter(clubMember -> clubMember.getUser().getId() == user.getId())
                .findFirst()
                .orElse(null);
    }

    public ClubMember addClubMember(long clubId, long userId) {
        User user = userController.getUser(userId);
        Club club = clubController.getClub(clubId);
        
        if (user == null || club == null) {
            return null;
        }

        ClubMember clubMember = new ClubMember(club, user);
        return clubMemberRepository.save(clubMember);
    }
    
    public ClubMember addClubMember(Club club, User user, String role) {
        ClubMember clubMember = new ClubMember(club, user, role);
        return clubMemberRepository.save(clubMember);
    }

    public String getClubMemberRole(long clubId, long userId) {
        return clubMemberRepository.findAll().stream()
                .filter(clubMember -> clubMember.getClub().getId() == clubId && clubMember.getUser().getId() == userId)
                .map(ClubMember::getRole)
                .findFirst()
                .orElse(null);
    }

    public ClubMember modifyClubMember(long clubId, long userId, String role) {
        Optional<ClubMember> clubMemberOpt = clubMemberRepository.findAll().stream()
                .filter(member -> member.getClub().getId() == clubId && member.getUser().getId() == userId)
                .findFirst();
                
        if (clubMemberOpt.isEmpty()) {
            return null;
        }
        
        ClubMember clubMember = clubMemberOpt.get();

        if (!role.equals("moderator") && !role.equals("member")) {
            return null;
        }

        clubMember.setRole(role);
        return clubMemberRepository.save(clubMember);
    }

    public boolean removeClubMember(long clubId, long userId) {
        Optional<ClubMember> clubMemberOpt = clubMemberRepository.findAll().stream()
                .filter(member -> member.getClub().getId() == clubId && member.getUser().getId() == userId)
                .findFirst();
                
        if (clubMemberOpt.isEmpty()) {
            return false;
        }
        
        clubMemberRepository.delete(clubMemberOpt.get());
        return true;
    }
    
    public void deleteClubMember(ClubMember clubMember) {
        clubMemberRepository.delete(clubMember);
    }

    public boolean leaveClub(long clubId, long userId) {
        Optional<ClubMember> clubMemberOpt = clubMemberRepository.findAll().stream()
                .filter(member -> member.getClub().getId() == clubId && member.getUser().getId() == userId)
                .findFirst();
                
        if (clubMemberOpt.isEmpty()) {
            return false;
        }
        
        ClubMember clubMember = clubMemberOpt.get();

        if (clubMember.getRole().equals("admin")) {
            return false;
        }
        
        clubMemberRepository.delete(clubMember);
        return true;
    }

    public List<ClubMember> findByClubId(long clubId) {
        return clubMemberRepository.findAll().stream()
                .filter(clubMember -> clubMember.getClub().getId() == clubId)
                .toList();
    }
    
    public boolean isMember(User user, Club club) {
        return clubMemberRepository.findAll().stream()
                .anyMatch(clubMember -> 
                    clubMember.getUser().equals(user) && 
                    clubMember.getClub().equals(club));
    }
    
    public List<Club> getClubsByUser(User user) {
        return clubMemberRepository.findAll().stream()
                .filter(clubMember -> clubMember.getUser().equals(user))
                .map(ClubMember::getClub)
                .toList();
    }
    
    public void deleteClubMembersByClub(Club club) {
        List<ClubMember> clubMembers = clubMemberRepository.findAll().stream()
                .filter(clubMember -> clubMember.getClub().equals(club))
                .toList();
        
        clubMemberRepository.deleteAll(clubMembers);
    }
}

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

@Controller
public class ClubMemberController {

    @Autowired
    private IClubMemberRepository clubMemberRepository;

    @Autowired
    private IClubRepository clubRepository;

    @Autowired
    private IUserRepository userRepository;

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
        Optional<User> user = userRepository.findById(userId);
        Optional<Club> club = clubRepository.findById(clubId);

        if (user.isEmpty() || club.isEmpty()) {
            return null;
        }

        return clubMemberRepository.findAll().stream()
                .filter(member -> member.getClub().getId() == clubId && member.getUser().getId() == userId)
                .findFirst()
                .orElse(null);
    }

    public ClubMember addClubMember(long clubId, long userId) {
        Optional<User> user = userRepository.findById(userId);
        Optional<Club> club = clubRepository.findById(clubId);

        if (user.isEmpty() || club.isEmpty()) {
            return null;
        }

        ClubMember clubMember = new ClubMember(club.get(), user.get());
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
}

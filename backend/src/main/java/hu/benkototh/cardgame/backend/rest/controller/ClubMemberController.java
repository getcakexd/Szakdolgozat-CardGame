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
    
    @Autowired
    private AuditLogController auditLogController;

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
        
        auditLogController.logAction("CLUB_MEMBERS_VIEWED", 0L,
                "Club members viewed for club: " + clubId);

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
        
        auditLogController.logAction("CLUB_MEMBER_CHECKED", userId,
                "Club membership checked for club: " + clubId);

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
        ClubMember savedMember = clubMemberRepository.save(clubMember);
        
        auditLogController.logAction("CLUB_MEMBER_ADDED", userId,
                "User joined club: " + clubId);
        
        return savedMember;
    }
    
    public ClubMember addClubMember(Club club, User user, String role) {
        ClubMember clubMember = new ClubMember(club, user, role);
        ClubMember savedMember = clubMemberRepository.save(clubMember);
        
        auditLogController.logAction("CLUB_MEMBER_ADDED_WITH_ROLE", user.getId(),
                "User added to club " + club.getId() + " with role: " + role);
        
        return savedMember;
    }

    public String getClubMemberRole(long clubId, long userId) {
        auditLogController.logAction("CLUB_MEMBER_ROLE_CHECKED", userId,
                "Club member role checked for club: " + clubId);
                
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
        ClubMember updatedMember = clubMemberRepository.save(clubMember);
        
        auditLogController.logAction("CLUB_MEMBER_ROLE_MODIFIED", 0L,
                "Club member role modified for user " + userId + " in club " + clubId + " to: " + role);
        
        return updatedMember;
    }

    public boolean removeClubMember(long clubId, long userId) {
        Optional<ClubMember> clubMemberOpt = clubMemberRepository.findAll().stream()
                .filter(member -> member.getClub().getId() == clubId && member.getUser().getId() == userId)
                .findFirst();
                
        if (clubMemberOpt.isEmpty()) {
            return false;
        }
        
        clubMemberRepository.delete(clubMemberOpt.get());
        
        auditLogController.logAction("CLUB_MEMBER_REMOVED", 0L,
                "User " + userId + " removed from club: " + clubId);
        
        return true;
    }
    
    public void deleteClubMember(ClubMember clubMember) {
        clubMemberRepository.delete(clubMember);
        
        auditLogController.logAction("CLUB_MEMBER_DELETED", clubMember.getUser().getId(),
                "Club membership deleted for club: " + clubMember.getClub().getId());
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
        
        auditLogController.logAction("CLUB_LEFT", userId,
                "User left club: " + clubId);
        
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
        
        auditLogController.logAction("CLUB_MEMBERS_DELETED", 0L,
                "All members deleted for club: " + club.getId());
    }
}

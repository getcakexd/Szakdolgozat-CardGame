package hu.benkototh.cardgame.backend.rest.controller;

import hu.benkototh.cardgame.backend.rest.Data.Club;
import hu.benkototh.cardgame.backend.rest.Data.User;
import hu.benkototh.cardgame.backend.rest.repository.IClubRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Controller;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Controller
public class ClubController {

    @Autowired
    private IClubRepository clubRepository;

    @Lazy
    @Autowired
    private UserController userController;
    
    @Lazy
    @Autowired
    private ClubMemberController clubMemberController;

    @Lazy
    @Autowired
    private ClubInviteController clubInviteController;
    
    @Autowired
    private AuditLogController auditLogController;

    public List<Club> getAllClubs() {
        auditLogController.logAction("ALL_CLUBS_VIEWED", 0L, "All clubs viewed");
        return clubRepository.findAll();
    }

    public List<Club> getPublicClubs() {
        auditLogController.logAction("PUBLIC_CLUBS_VIEWED", 0L, "Public clubs viewed");
        return clubRepository.findAll().stream().filter(Club::isPublic).toList();
    }

    public List<Club> getJoinableClubs(long userId) {
        User user = userController.getUser(userId);
        
        if (user == null) {
            return null;
        }
        
        auditLogController.logAction("JOINABLE_CLUBS_VIEWED", userId, "Joinable clubs viewed");
        return getJoinableClubs(user);
    }

    public List<Club> getJoinableClubs(User user) {
        auditLogController.logAction("JOINABLE_CLUBS_VIEWED", user, "Joinable clubs viewed");
        return clubRepository.findAll().stream()
                .filter(club -> club.isPublic() &&
                        !clubMemberController.isMember(user, club))
                .collect(Collectors.toList());
    }

    public Club getClub(long clubId) {
        Club club = clubRepository.findById(clubId).orElse(null);
        
        if (club != null) {
            auditLogController.logAction("CLUB_VIEWED", 0L, "Club viewed: " + clubId);
        }
        
        return club;
    }

    public List<Club> getClubsByUser(long userId) {
        User user = userController.getUser(userId);
        
        if (user == null) {
            return null;
        }
        
        auditLogController.logAction("USER_CLUBS_VIEWED", userId, "User's clubs viewed");
        return clubMemberController.getClubsByUser(user);
    }

    public Club createClub(String name, String description, boolean isPublic, long userId) {
        User user = userController.getUser(userId);
        
        if (user == null) {
            return null;
        }
        
        Club club = new Club(name, description, isPublic);
        club = clubRepository.save(club);

        clubMemberController.addClubMember(club, user, "admin");
        
        auditLogController.logAction("CLUB_CREATED", userId,
                "Club created: " + name + " (ID: " + club.getId() + ")");
        
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
        
        Club updatedClub = clubRepository.save(club);
        
        auditLogController.logAction("CLUB_UPDATED", 0L,
                "Club updated: " + clubId + " - New name: " + name);
        
        return updatedClub;
    }

    public boolean deleteClub(long clubId) {
        Optional<Club> club = clubRepository.findById(clubId);
        
        if (club.isEmpty()) {
            return false;
        }

        clubRepository.deleteById(clubId);
        
        auditLogController.logAction("CLUB_DELETED", 0L,
                "Club deleted: " + clubId + " - " + club.get().getName());
        
        return true;
    }

    public Optional<Club> getClubById(long clubId) {
        return clubRepository.findById(clubId);
    }
}

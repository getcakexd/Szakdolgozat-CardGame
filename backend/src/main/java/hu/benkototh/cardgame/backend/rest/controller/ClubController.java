package hu.benkototh.cardgame.backend.rest.controller;

import hu.benkototh.cardgame.backend.rest.model.Club;
import hu.benkototh.cardgame.backend.rest.model.User;
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
        return clubRepository.findAll();
    }

    public List<Club> getPublicClubs() {
        return clubRepository.findAll().stream().filter(Club::isPublic).toList();
    }

    public List<Club> getJoinableClubs(long userId) {
        User user = userController.getUser(userId);
        
        if (user == null) {
            return null;
        }

        return getJoinableClubs(user);
    }

    public List<Club> getJoinableClubs(User user) {
        return clubRepository.findAll().stream()
                .filter(club -> club.isPublic() &&
                        !clubMemberController.isMember(user, club))
                .collect(Collectors.toList());
    }

    public Club getClub(long clubId) {
        Club club = clubRepository.findById(clubId).orElse(null);
        
        if (club != null) {
        }
        
        return club;
    }

    public List<Club> getClubsByUser(long userId) {
        User user = userController.getUser(userId);
        
        if (user == null) {
            return null;
        }

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

    public boolean isUserMemberOfClub(long userId, long clubId) {
        User user = userController.getUser(userId);

        if (user == null) {
            return false;
        }

        Club club = getClub(clubId);

        if (club == null) {
            return false;
        }

        return clubMemberController.isMember(user, club);

    }
}

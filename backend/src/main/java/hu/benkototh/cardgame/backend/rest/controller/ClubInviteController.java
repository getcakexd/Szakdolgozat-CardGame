package hu.benkototh.cardgame.backend.rest.controller;

import hu.benkototh.cardgame.backend.rest.Data.Club;
import hu.benkototh.cardgame.backend.rest.Data.ClubInvite;
import hu.benkototh.cardgame.backend.rest.Data.User;
import hu.benkototh.cardgame.backend.rest.repository.IClubInviteRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Controller;

import java.util.List;
import java.util.Optional;

@Controller
public class ClubInviteController {

    @Autowired
    private IClubInviteRepository clubInviteRepository;

    @Lazy
    @Autowired
    private UserController userController;
    
    @Lazy
    @Autowired
    private ClubController clubController;
    
    @Lazy
    @Autowired
    private ClubMemberController clubMemberController;
    
    @Autowired
    private AuditLogController auditLogController;

    public List<ClubInvite> getClubInvites(long userId) {
        auditLogController.logAction("CLUB_INVITES_VIEWED", userId,
                "Club invites viewed by user");
                
        return clubInviteRepository.findAll().stream()
                .filter(invite ->
                        invite.getUser().getId() == userId &&
                        invite.getStatus().equals("pending")
                )
                .toList();
    }

    public List<ClubInvite> getPendingInvites(long clubId) {
        auditLogController.logAction("PENDING_CLUB_INVITES_VIEWED", 0L,
                "Pending club invites viewed for club: " + clubId);
                
         return clubInviteRepository.findAll().stream()
                .filter(invite ->
                        invite.getClub().getId() == clubId &&
                        invite.getStatus().equals("pending")
                ).toList();
    }

    public List<ClubInvite> getInviteHistory(long clubId) {
        auditLogController.logAction("CLUB_INVITE_HISTORY_VIEWED", 0L,
                "Club invite history viewed for club: " + clubId);
                
        return clubInviteRepository.findAll().stream()
                .filter(invite ->
                        invite.getClub().getId() == clubId &&
                        !invite.getStatus().equals("pending")
                ).toList();
    }

    public ClubInvite addClubInvite(long clubId, String username) {
        Club club = clubController.getClub(clubId);
        User user = userController.findByUsername(username);

        if (user == null || club == null) {
            auditLogController.logAction("CLUB_INVITE_FAILED", 0L,
                    "Club invite failed: User or club not found - Club: " + clubId + ", User: " + username);
            return null;
        }

        if (clubMemberController.isMember(user, club)) {
            auditLogController.logAction("CLUB_INVITE_FAILED", 0L,
                    "Club invite failed: User already a member - Club: " + clubId + ", User: " + username);
            return null;
        }

        Optional<ClubInvite> existingInvite = clubInviteRepository.findAll().stream()
                .filter(invite ->
                        invite.getClub().getId() == clubId &&
                        invite.getUser().getId() == user.getId() &&
                        invite.getStatus().equals("pending")
                )
                .findFirst();

        if (existingInvite.isPresent()) {
            auditLogController.logAction("CLUB_INVITE_FAILED", 0L,
                    "Club invite failed: Invite already exists - Club: " + clubId + ", User: " + username);
            return null;
        }

        ClubInvite clubInvite = new ClubInvite(club, user);
        ClubInvite savedInvite = clubInviteRepository.save(clubInvite);
        
        auditLogController.logAction("CLUB_INVITE_SENT", 0L,
                "Club invite sent to " + username + " for club: " + clubId);
        
        return savedInvite;
    }

    public ClubInvite acceptClubInvite(long id) {
        Optional<ClubInvite> clubInviteOpt = clubInviteRepository.findById(id);

        if (clubInviteOpt.isEmpty()) {
            auditLogController.logAction("CLUB_INVITE_ACCEPT_FAILED", 0L,
                    "Club invite accept failed: Invite not found - ID: " + id);
            return null;
        }

        ClubInvite clubInvite = clubInviteOpt.get();
        clubInvite.setStatus("accepted");

        clubMemberController.addClubMember(clubInvite.getClub(), clubInvite.getUser(), "member");
        
        ClubInvite updatedInvite = clubInviteRepository.save(clubInvite);
        
        auditLogController.logAction("CLUB_INVITE_ACCEPTED", clubInvite.getUser().getId(),
                "Club invite accepted for club: " + clubInvite.getClub().getId());
        
        return updatedInvite;
    }

    public ClubInvite declineClubInvite(long id) {
        Optional<ClubInvite> clubInviteOpt = clubInviteRepository.findById(id);

        if (clubInviteOpt.isEmpty()) {
            auditLogController.logAction("CLUB_INVITE_DECLINE_FAILED", 0L,
                    "Club invite decline failed: Invite not found - ID: " + id);
            return null;
        }

        ClubInvite clubInvite = clubInviteOpt.get();
        clubInvite.setStatus("declined");
        ClubInvite updatedInvite = clubInviteRepository.save(clubInvite);
        
        auditLogController.logAction("CLUB_INVITE_DECLINED", clubInvite.getUser().getId(),
                "Club invite declined for club: " + clubInvite.getClub().getId());
        
        return updatedInvite;
    }

    public boolean deleteClubInvite(long id) {
        Optional<ClubInvite> clubInvite = clubInviteRepository.findById(id);

        if (clubInvite.isEmpty()) {
            auditLogController.logAction("CLUB_INVITE_DELETE_FAILED", 0L,
                    "Club invite delete failed: Invite not found - ID: " + id);
            return false;
        }

        clubInviteRepository.delete(clubInvite.get());
        
        auditLogController.logAction("CLUB_INVITE_DELETED", 0L,
                "Club invite deleted - ID: " + id);
        
        return true;
    }
}

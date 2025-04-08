package hu.benkototh.cardgame.backend.rest.controller;

import hu.benkototh.cardgame.backend.rest.Data.Club;
import hu.benkototh.cardgame.backend.rest.Data.ClubInvite;
import hu.benkototh.cardgame.backend.rest.Data.ClubMember;
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

    public List<ClubInvite> getClubInvites(long userId) {
        return clubInviteRepository.findAll().stream()
                .filter(invite ->
                        invite.getUser().getId() == userId &&
                        invite.getStatus().equals("pending")
                )
                .toList();
    }

    public List<ClubInvite> getPendingInvites(long clubId) {
         return clubInviteRepository.findAll().stream()
                .filter(invite ->
                        invite.getClub().getId() == clubId &&
                        invite.getStatus().equals("pending")
                ).toList();
    }

    public List<ClubInvite> getInviteHistory(long clubId) {
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
            return null;
        }

        if (clubMemberController.isMember(user, club)) {
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
            return null;
        }

        ClubInvite clubInvite = new ClubInvite(club, user);
        return clubInviteRepository.save(clubInvite);
    }

    public ClubInvite acceptClubInvite(long id) {
        Optional<ClubInvite> clubInviteOpt = clubInviteRepository.findById(id);

        if (clubInviteOpt.isEmpty()) {
            return null;
        }

        ClubInvite clubInvite = clubInviteOpt.get();
        clubInvite.setStatus("accepted");

        clubMemberController.addClubMember(clubInvite.getClub(), clubInvite.getUser(), "member");
        
        return clubInviteRepository.save(clubInvite);
    }

    public ClubInvite declineClubInvite(long id) {
        Optional<ClubInvite> clubInviteOpt = clubInviteRepository.findById(id);

        if (clubInviteOpt.isEmpty()) {
            return null;
        }

        ClubInvite clubInvite = clubInviteOpt.get();
        clubInvite.setStatus("declined");
        return clubInviteRepository.save(clubInvite);
    }

    public boolean deleteClubInvite(long id) {
        Optional<ClubInvite> clubInvite = clubInviteRepository.findById(id);

        if (clubInvite.isEmpty()) {
            return false;
        }

        clubInviteRepository.delete(clubInvite.get());
        return true;
    }
    
    public void deleteInvitesByUser(User user) {
        List<ClubInvite> invites = clubInviteRepository.findAll().stream()
                .filter(clubInvite -> clubInvite.getUser().getId() == user.getId())
                .toList();
        
        clubInviteRepository.deleteAll(invites);
    }

    public void deleteInvitesByClub(Club club) {
        List<ClubInvite> invites = clubInviteRepository.findAll().stream()
                .filter(clubInvite -> clubInvite.getClub().getId() == club.getId())
                .toList();

        clubInviteRepository.deleteAll(invites);
    }
}

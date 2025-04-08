package hu.benkototh.cardgame.backend.rest.controller;

import hu.benkototh.cardgame.backend.rest.Data.Club;
import hu.benkototh.cardgame.backend.rest.Data.ClubInvite;
import hu.benkototh.cardgame.backend.rest.Data.ClubMember;
import hu.benkototh.cardgame.backend.rest.Data.User;
import hu.benkototh.cardgame.backend.rest.repository.IClubInviteRepository;
import hu.benkototh.cardgame.backend.rest.repository.IClubMemberRepository;
import hu.benkototh.cardgame.backend.rest.repository.IClubRepository;
import hu.benkototh.cardgame.backend.rest.repository.IUserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;

import java.util.List;
import java.util.Optional;

@Controller
public class ClubInviteController {

    @Autowired
    private IClubInviteRepository clubInviteRepository;

    @Autowired
    private IClubMemberRepository clubMemberRepository;

    @Autowired
    private IClubRepository clubRepository;

    @Autowired
    private IUserRepository userRepository;

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
        Optional<Club> club = clubRepository.findById(clubId);
        User user = findByUsername(username);

        if (user == null || club.isEmpty()) {
            return null;
        }

        Optional<ClubMember> existingMember = clubMemberRepository.findAll().stream()
                .filter(member -> member.getClub().getId() == clubId && member.getUser().getId() == user.getId())
                .findFirst();

        Optional<ClubInvite> existingInvite = clubInviteRepository.findAll().stream()
                .filter(invite ->
                        invite.getClub().getId() == clubId &&
                        invite.getUser().getId() == user.getId() &&
                        invite.getStatus().equals("pending")
                )
                .findFirst();

        if (existingMember.isPresent() || existingInvite.isPresent()) {
            return null;
        }

        ClubInvite clubInvite = new ClubInvite(club.get(), user);
        return clubInviteRepository.save(clubInvite);
    }

    public ClubInvite acceptClubInvite(long id) {
        Optional<ClubInvite> clubInviteOpt = clubInviteRepository.findById(id);

        if (clubInviteOpt.isEmpty()) {
            return null;
        }

        ClubInvite clubInvite = clubInviteOpt.get();
        clubInvite.setStatus("accepted");
        ClubMember clubMember = new ClubMember(clubInvite.getClub(), clubInvite.getUser());

        clubMemberRepository.save(clubMember);
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

    public User findByUsername(String username) {
        return userRepository.findAll().stream()
                .filter(user -> user.getUsername().equals(username))
                .findFirst()
                .orElse(null);
    }
}

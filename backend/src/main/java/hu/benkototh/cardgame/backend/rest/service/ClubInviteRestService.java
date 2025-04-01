package hu.benkototh.cardgame.backend.rest.service;

import hu.benkototh.cardgame.backend.rest.Data.Club;
import hu.benkototh.cardgame.backend.rest.Data.ClubInvite;
import hu.benkototh.cardgame.backend.rest.Data.ClubMember;
import hu.benkototh.cardgame.backend.rest.Data.User;
import hu.benkototh.cardgame.backend.rest.repository.IClubInviteRepository;
import hu.benkototh.cardgame.backend.rest.repository.IClubMemberRepository;
import hu.benkototh.cardgame.backend.rest.repository.IClubRepository;
import hu.benkototh.cardgame.backend.rest.repository.IUserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Optional;

@RestController
@RequestMapping("/api/clubinvites")
public class ClubInviteRestService {

    @Autowired
    private IClubInviteRepository clubInviteRepository;

    @Autowired
    private IClubMemberRepository clubMemberRepository;

    @Autowired
    private IClubRepository clubRepository;

    @Autowired
    private IUserRepository userRepository;

    @GetMapping("/list")
    public ResponseEntity<List<ClubInvite>> getClubInvites(@RequestParam long userId) {
        List<ClubInvite> clubInvites = clubInviteRepository.findAll().stream()
                .filter(invite ->
                        invite.getUser().getId() == userId &&
                        invite.getStatus().equals("pending")
                )
                .toList();

        return ResponseEntity.ok(clubInvites);
    }

    @GetMapping("/pending")
    public ResponseEntity<List<ClubInvite>> getClubInvite(@RequestParam long clubId) {
         List<ClubInvite> clubInvite = clubInviteRepository.findAll().stream()
                .filter(invite ->
                                invite.getClub().getId() == clubId &&
                                invite.getStatus().equals("pending")
                ).toList();

        return ResponseEntity.ok(clubInvite);
    }

    @GetMapping("/history")
    public ResponseEntity<List<ClubInvite>> getInviteHistory(@RequestParam long clubId) {
        List<ClubInvite> history = clubInviteRepository.findAll().stream()
                .filter(invite ->
                        invite.getClub().getId() == clubId &&
                                !invite.getStatus().equals("pending")
                ).toList();
        return ResponseEntity.ok(history);
    }

    @PostMapping("/add")
    public ResponseEntity<?> addClubInvite(@RequestParam long clubId, @RequestParam String username) {
        Optional<Club> club = clubRepository.findById(clubId);
        User user = findByUsername(username);

        Optional<ClubMember> existingMember = clubMemberRepository.findAll().stream()
                .filter(member -> member.getClub().getId() == clubId && member.getUser().getId() == user.getId())
                .findFirst();

        Optional<ClubInvite> existingInvite = clubInviteRepository.findAll().stream()
                .filter(invite -> invite.getClub().getId() == clubId && invite.getUser().getId() == user.getId())
                .findFirst();


        if (existingMember.isPresent()) {
            return ResponseEntity.status(400).body("User is already a member of this club.");
        }

        if (existingInvite.isPresent()) {
            return ResponseEntity.status(400).body("User already has a pending invite to this club.");
        }

        if (club.isEmpty()) {
            return ResponseEntity.status(400).body("Club not found.");
        }

        ClubInvite clubInvite = new ClubInvite(club.get(), user);
        clubInviteRepository.save(clubInvite);
        return ResponseEntity.ok(clubInvite);
    }

    @PutMapping("/accept")
    public ResponseEntity<ClubInvite> acceptClubInvite(@RequestParam long id) {
        Optional<ClubInvite> clubInviteOpt = clubInviteRepository.findById(id);

        if (clubInviteOpt.isEmpty()) {
            return ResponseEntity.status(404).body(null);
        }

        clubInviteOpt.get().setStatus("accepted");
        ClubMember clubMember = new ClubMember(clubInviteOpt.get().getClub(), clubInviteOpt.get().getUser());

        clubMemberRepository.save(clubMember);
        clubInviteRepository.save(clubInviteOpt.get());
        return ResponseEntity.ok(clubInviteOpt.get());
    }

    @PutMapping("/decline")
    public ResponseEntity<ClubInvite> declineClubInvite(@RequestParam long id) {
        Optional<ClubInvite> clubInviteOpt = clubInviteRepository.findById(id);

        if (clubInviteOpt.isEmpty()) {
            return ResponseEntity.status(404).body(null);
        }

        clubInviteOpt.get().setStatus("declined");
        clubInviteRepository.save(clubInviteOpt.get());
        return ResponseEntity.ok(clubInviteOpt.get());
    }

    @DeleteMapping("/delete")
    public ResponseEntity<?> deleteClubInvite(@RequestParam long id) {
        Optional<ClubInvite> clubInvite = clubInviteRepository.findById(id);

        if (clubInvite.isEmpty()) {
            return ResponseEntity.status(404).body(null);
        }

        clubInviteRepository.delete(clubInvite.get());
        return ResponseEntity.ok(null);
    }

    private User findByUsername(String username) {
        return userRepository.findAll().stream()
                .filter(user -> user.getUsername().equals(username))
                .findFirst().
                orElse(null);
    }
}

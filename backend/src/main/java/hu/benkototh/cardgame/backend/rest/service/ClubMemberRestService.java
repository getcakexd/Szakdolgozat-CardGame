package hu.benkototh.cardgame.backend.rest.service;

import hu.benkototh.cardgame.backend.rest.Data.Club;
import hu.benkototh.cardgame.backend.rest.Data.ClubMember;
import hu.benkototh.cardgame.backend.rest.Data.User;
import hu.benkototh.cardgame.backend.rest.repository.IClubMemberRepository;
import hu.benkototh.cardgame.backend.rest.repository.IClubRepository;
import hu.benkototh.cardgame.backend.rest.repository.IUserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@RestController
@RequestMapping("/api/clubmembers")
public class ClubMemberRestService {

    @Autowired
    private IClubMemberRepository clubMemberRepository;

    @Autowired
    private IClubRepository clubRepository;

    @Autowired
    private IUserRepository userRepository;

    @GetMapping("/list")
    public ResponseEntity<List<ClubMember>> getClubMembers(@RequestParam long clubId) {
        List<ClubMember> clubMembers = findByClubId(clubId);

        if (clubMembers.isEmpty()) {
            return ResponseEntity.status(404).body(null);
        }

        for (ClubMember member : clubMembers) {
            if (member.getUser() != null) {
                member.setUsername(member.getUser().getUsername());
            }
        }

        return ResponseEntity.ok(clubMembers);
    }

    @GetMapping("/get")
    public ResponseEntity<ClubMember> getClubMember(@RequestParam long clubId, @RequestParam long userId) {
        Optional<User> user = userRepository.findById(userId);
        Optional<Club> club = clubRepository.findById(clubId);

        if (user.isEmpty() || club.isEmpty()) {
            return ResponseEntity.status(404).body(null);
        }

        ClubMember clubMember = clubMemberRepository.findAll().stream()
                .filter(member -> member.getClub().getId() == clubId && member.getUser().getId() == userId)
                .findFirst()
                .orElse(null);

        if (clubMember == null) {
            return ResponseEntity.status(404).body(null);
        }

        return ResponseEntity.ok(clubMember);
    }

    @PostMapping("/add")
    public ResponseEntity<ClubMember> addClubMember(@RequestParam long clubId, @RequestParam long userId) {
        Optional<User> user = userRepository.findById(userId);
        Optional<Club> club = clubRepository.findById(clubId);

        if (user.isEmpty() || club.isEmpty()) {
            return ResponseEntity.status(404).body(null);
        }

        ClubMember clubMember = new ClubMember(club.get(), user.get());
        clubMemberRepository.save(clubMember);

        return ResponseEntity.status(201).body(clubMember);
    }

    @GetMapping("/role")
    public ResponseEntity<String> getClubMemberRole(@RequestParam long clubId, @RequestParam long userId) {

        String role = clubMemberRepository.findAll().stream()
                .filter(clubMember -> clubMember.getClub().getId() == clubId && clubMember.getUser().getId() == userId)
                .map(ClubMember::getRole)
                .findFirst()
                .orElse(null);

        if (role == null) {
            return ResponseEntity.status(404).body(null);
        }

        return ResponseEntity.ok(role);
    }

    @PutMapping("/modify")
    public ResponseEntity<ClubMember> promoteClubMember(
            @RequestParam long clubId, @RequestParam long userId, @RequestParam String role
    ) {

        ClubMember clubMember = clubMemberRepository.findAll().stream()
                .filter(member -> member.getClub().getId() == clubId && member.getUser().getId() == userId)
                .findFirst()
                .orElseThrow(() -> new IllegalArgumentException("User is not a member of the club"));

        if (!role.equals("moderator") && !role.equals("member")) {
            return ResponseEntity.status(400).body(null);
        }

        clubMember.setRole(role);
        clubMemberRepository.save(clubMember);

        return ResponseEntity.ok(clubMember);
    }

    @DeleteMapping("/kick")
    public ResponseEntity<String> removeClubMember(@RequestParam long clubId, @RequestParam long userId) {


        ClubMember clubMember = clubMemberRepository.findAll().stream()
                .filter(member -> member.getClub().getId() == clubId && member.getUser().getId() == userId)
                .findFirst()
                .orElseThrow(() -> new IllegalArgumentException("User is not a member of the club"));

        clubMemberRepository.delete(clubMember);
        return ResponseEntity.ok("User removed from the club.");
    }

    private List<ClubMember> findByClubId(long clubId) {
        return clubMemberRepository.findAll().stream()
                .filter(clubMember -> clubMember.getClub().getId() == clubId)
                .toList();
    }
}

package hu.benkototh.cardgame.backend.rest.service;

import hu.benkototh.cardgame.backend.rest.Data.Club;
import hu.benkototh.cardgame.backend.rest.Data.ClubMember;
import hu.benkototh.cardgame.backend.rest.Data.ClubMessage;
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
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/clubs")
public class ClubRestService {

    @Autowired
    private IClubRepository clubRepository;

    @Autowired
    private IUserRepository userRepository;

    @Autowired
    private IClubMemberRepository clubMemberRepository;

    @GetMapping("/list")
    public ResponseEntity<List<Club>> getClubs() {
        List<Club> clubs = clubRepository.findAll();
        return ResponseEntity.ok(clubs);
    }

    @GetMapping("/public")
    public ResponseEntity<List<Club>> getPublicClubs() {
        List<Club> clubs = clubRepository.findAll().stream().filter(Club::isPublic).toList();
        return ResponseEntity.ok(clubs);
    }

    @GetMapping("/joinable")
    public ResponseEntity<List<Club>> getPublicClubs(@RequestParam long userId) {
        Optional<User> user = userRepository.findById(userId);

        if (user.isEmpty()) {
            return ResponseEntity.status(404).body(null);
        }

        List<Club> clubs = getJoinableClubs(user.get());
        return ResponseEntity.ok(clubs);
    }

    private List<Club> getJoinableClubs(User user) {
        return clubRepository.findAll().stream()
                .filter(club -> club.isPublic() &&
                        clubMemberRepository.findAll().stream()
                                .noneMatch(clubMember -> clubMember.getUser().equals(user) && clubMember.getClub().equals(club)))
                .collect(Collectors.toList());
    }

    @GetMapping("/get")
    public ResponseEntity<Club> getClub(@RequestParam long clubId) {
        return clubRepository.findById(clubId)
                .map(ResponseEntity::ok)
                .orElseGet(() -> ResponseEntity.notFound().build());
    }

    @GetMapping("/user")
    public ResponseEntity<List<Club>> getClubsByUser(@RequestParam long userId) {
        Optional<User> user = userRepository.findById(userId);
        if (user.isEmpty()) {
            return ResponseEntity.status(404).body(null);
        }

        List<Club> clubs = findClubsByUser(user.get());
        return ResponseEntity.ok(clubs);
    }

    @GetMapping("/members")
    public ResponseEntity<?> getClubMembers(@RequestParam long clubId) {
        Optional<Club> club = clubRepository.findById(clubId);
        if (club.isEmpty()) {
            return ResponseEntity.status(404).body("Club not found");
        }

        List<ClubMember> clubMembers = clubMemberRepository.findAll().stream()
                .filter(clubMember -> clubMember.getClub().equals(club.get()))
                .toList();

        return ResponseEntity.ok(clubMembers);
    }

    @PostMapping("/create")
    public ResponseEntity<?> createClub(
            @RequestParam String name,
            @RequestParam String description,
            @RequestParam boolean isPublic,
            @RequestParam long userId) {

        Optional<User> user = userRepository.findById(userId);
        if (user.isEmpty()) {
            return ResponseEntity.status(404).body("User not found");
        }

        Club club = new Club(name, description, isPublic);
        ClubMember clubMember = new ClubMember(club, user.get(), "admin");

        clubRepository.save(club);
        clubMemberRepository.save(clubMember);

        return ResponseEntity.status(201).body(club);
    }

    @PutMapping("/update")
    public ResponseEntity<?> updateClub(
            @RequestParam long clubId,
            @RequestParam String name,
            @RequestParam String description,
            @RequestParam boolean isPublic) {

        Optional<Club> clubOpt = clubRepository.findById(clubId);
        if (clubOpt.isEmpty()) {
            return ResponseEntity.status(404).body("Club not found");
        }

        Club club = clubOpt.get();
        club.setName(name);
        club.setDescription(description);
        club.setPublic(isPublic);
        clubRepository.save(club);

        return ResponseEntity.ok(club);
    }

    @DeleteMapping("/delete")
    public ResponseEntity<Map<String, String>> deleteClub(@RequestParam long clubId) {
        Optional<Club> club = clubRepository.findById(clubId);
        Map<String, String> response = new HashMap<>();
        if (club.isEmpty()) {
            response.put("error", "Club not found");
            return ResponseEntity.status(404).body(response);
        }

        List<ClubMember> clubMembers = clubMemberRepository.findAll().stream()
                .filter(clubMember -> clubMember.getClub().equals(club.get()))
                .toList();


        clubMemberRepository.deleteAll(clubMembers);
        clubRepository.deleteById(clubId);

        response.put("ok", "Club deleted successfull");
        return ResponseEntity.ok(response);
    }

    private List<Club> findClubsByUser(User user) {
        return clubMemberRepository.findAll().stream()
                .filter(clubMember -> clubMember.getUser().equals(user))
                .map(ClubMember::getClub)
                .toList();
    }
}

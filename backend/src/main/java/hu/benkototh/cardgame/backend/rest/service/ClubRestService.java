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

import java.util.List;
import java.util.Optional;

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

    @GetMapping("/get")
    public ResponseEntity<Club> getClub(@RequestParam long id) {
        return ResponseEntity.of(clubRepository.findById(id));
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
            @RequestParam long id,
            @RequestParam String name,
            @RequestParam String description,
            @RequestParam boolean isPublic) {

        Optional<Club> clubOpt = clubRepository.findById(id);
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
    public ResponseEntity<?> deleteClub(@RequestParam long id) {
        Optional<Club> club = clubRepository.findById(id);
        if (club.isEmpty()) {
            return ResponseEntity.status(404).body("Club not found");
        }

        clubRepository.deleteById(id);
        return ResponseEntity.ok("Club deleted successfully");
    }
}

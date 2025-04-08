package hu.benkototh.cardgame.backend.rest.service;

import hu.benkototh.cardgame.backend.rest.controller.ClubController;
import hu.benkototh.cardgame.backend.rest.Data.Club;
import hu.benkototh.cardgame.backend.rest.Data.ClubMember;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/clubs")
public class ClubRestService {

    @Autowired
    private ClubController clubController;

    @GetMapping("/list")
    public ResponseEntity<List<Club>> getClubs() {
        return ResponseEntity.ok(clubController.getAllClubs());
    }

    @GetMapping("/public")
    public ResponseEntity<List<Club>> getPublicClubs() {
        return ResponseEntity.ok(clubController.getPublicClubs());
    }

    @GetMapping("/joinable")
    public ResponseEntity<List<Club>> getPublicClubs(@RequestParam long userId) {
        List<Club> clubs = clubController.getJoinableClubs(userId);
        
        if (clubs == null) {
            return ResponseEntity.status(404).body(null);
        }
        
        return ResponseEntity.ok(clubs);
    }

    @GetMapping("/get")
    public ResponseEntity<Club> getClub(@RequestParam long clubId) {
        Club club = clubController.getClub(clubId);
        
        if (club == null) {
            return ResponseEntity.notFound().build();
        }
        
        return ResponseEntity.ok(club);
    }

    @GetMapping("/user")
    public ResponseEntity<List<Club>> getClubsByUser(@RequestParam long userId) {
        List<Club> clubs = clubController.getClubsByUser(userId);
        
        if (clubs == null) {
            return ResponseEntity.status(404).body(null);
        }
        
        return ResponseEntity.ok(clubs);
    }

    @GetMapping("/members")
    public ResponseEntity<?> getClubMembers(@RequestParam long clubId) {
        List<ClubMember> clubMembers = clubController.getClubMembers(clubId);
        
        if (clubMembers == null) {
            return ResponseEntity.status(404).body("Club not found");
        }
        
        return ResponseEntity.ok(clubMembers);
    }

    @PostMapping("/create")
    public ResponseEntity<?> createClub(
            @RequestParam String name,
            @RequestParam String description,
            @RequestParam boolean isPublic,
            @RequestParam long userId) {

        Club club = clubController.createClub(name, description, isPublic, userId);
        
        if (club == null) {
            return ResponseEntity.status(404).body("User not found");
        }
        
        return ResponseEntity.status(201).body(club);
    }

    @PutMapping("/update")
    public ResponseEntity<?> updateClub(
            @RequestParam long clubId,
            @RequestParam String name,
            @RequestParam String description,
            @RequestParam boolean isPublic) {

        Club club = clubController.updateClub(clubId, name, description, isPublic);
        
        if (club == null) {
            return ResponseEntity.status(404).body("Club not found");
        }
        
        return ResponseEntity.ok(club);
    }

    @DeleteMapping("/delete")
    public ResponseEntity<Map<String, String>> deleteClub(@RequestParam long clubId) {
        Map<String, String> response = new HashMap<>();
        
        boolean deleted = clubController.deleteClub(clubId);
        
        if (!deleted) {
            response.put("message", "Club not found");
            return ResponseEntity.status(404).body(response);
        }
        
        response.put("message", "Club deleted");
        return ResponseEntity.ok(response);
    }
}

package hu.benkototh.cardgame.backend.rest.service;

import hu.benkototh.cardgame.backend.rest.controller.ClubMemberController;
import hu.benkototh.cardgame.backend.rest.Data.ClubMember;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/clubmembers")
public class ClubMemberRestService {

    @Autowired
    private ClubMemberController clubMemberController;

    @GetMapping("/list")
    public ResponseEntity<List<ClubMember>> getClubMembers(@RequestParam long clubId) {
        List<ClubMember> clubMembers = clubMemberController.getClubMembers(clubId);

        if (clubMembers == null) {
            return ResponseEntity.status(404).body(null);
        }

        return ResponseEntity.ok(clubMembers);
    }

    @GetMapping("/get")
    public ResponseEntity<ClubMember> getClubMember(@RequestParam long clubId, @RequestParam long userId) {
        ClubMember clubMember = clubMemberController.getClubMember(clubId, userId);

        if (clubMember == null) {
            return ResponseEntity.status(404).body(null);
        }

        return ResponseEntity.ok(clubMember);
    }

    @PostMapping("/add")
    public ResponseEntity<ClubMember> addClubMember(@RequestParam long clubId, @RequestParam long userId) {
        ClubMember clubMember = clubMemberController.addClubMember(clubId, userId);
        
        if (clubMember == null) {
            return ResponseEntity.status(404).body(null);
        }

        return ResponseEntity.status(201).body(clubMember);
    }

    @GetMapping("/role")
    public ResponseEntity<Map<String, String>> getClubMemberRole(@RequestParam long clubId, @RequestParam long userId) {
        Map<String, String> response = new HashMap<>();
        String role = clubMemberController.getClubMemberRole(clubId, userId);

        if (role == null) {
            response.put("message", "User is not a member of the club.");
        }
        response.put("role", role);
        return ResponseEntity.ok(response);
    }

    @PutMapping("/modify")
    public ResponseEntity<ClubMember> promoteClubMember(
            @RequestParam long clubId, @RequestParam long userId, @RequestParam String role
    ) {
        ClubMember clubMember = clubMemberController.modifyClubMember(clubId, userId, role);
        
        if (clubMember == null) {
            return ResponseEntity.status(400).body(null);
        }

        return ResponseEntity.ok(clubMember);
    }

    @DeleteMapping("/kick")
    public ResponseEntity<Map<String, String>> removeClubMember(@RequestParam long clubId, @RequestParam long userId) {
        Map<String, String> response = new HashMap<>();
        
        boolean removed = clubMemberController.removeClubMember(clubId, userId);
        
        if (!removed) {
            response.put("message", "User is not a member of the club.");
            return ResponseEntity.status(404).body(response);
        }

        response.put("message", "User removed from the club.");
        return ResponseEntity.ok(response);
    }

    @DeleteMapping("/leave")
    public ResponseEntity<Map<String, String>> leaveClub(@RequestParam long clubId, @RequestParam long userId) {
        Map<String, String> response = new HashMap<>();
        
        boolean left = clubMemberController.leaveClub(clubId, userId);
        
        if (!left) {
            response.put("message", "Admin cannot leave the club.");
            return ResponseEntity.status(400).body(response);
        }
        
        response.put("message", "User left the club.");
        return ResponseEntity.ok(response);
    }
}

package hu.benkototh.cardgame.backend.rest.service;

import hu.benkototh.cardgame.backend.rest.Data.ClubInvite;
import hu.benkototh.cardgame.backend.rest.controller.ClubInviteController;
import hu.benkototh.cardgame.backend.rest.controller.UserController;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/clubinvites")
public class ClubInviteRestService {

    @Autowired
    private ClubInviteController clubInviteController;
    @Autowired
    private UserController userController;

    @GetMapping("/list")
    public ResponseEntity<List<ClubInvite>> getClubInvites(@RequestParam long userId) {
        List<ClubInvite> clubInvites = clubInviteController.getClubInvites(userId);
        return ResponseEntity.ok(clubInvites);
    }

    @GetMapping("/pending")
    public ResponseEntity<List<ClubInvite>> getClubInvite(@RequestParam long clubId) {
        List<ClubInvite> clubInvites = clubInviteController.getPendingInvites(clubId);
        return ResponseEntity.ok(clubInvites);
    }

    @GetMapping("/history")
    public ResponseEntity<List<ClubInvite>> getInviteHistory(@RequestParam long clubId) {
        List<ClubInvite> history = clubInviteController.getInviteHistory(clubId);
        return ResponseEntity.ok(history);
    }

    @PostMapping("/add")
    public ResponseEntity<?> addClubInvite(@RequestParam long clubId, @RequestParam String username) {
        Map<String, String> response = new HashMap<>();
        
        ClubInvite clubInvite = clubInviteController.addClubInvite(clubId, username);
        
        if (clubInvite == null) {
            if (userController.findByUsername(username) == null) {
                response.put("message", "User not found.");
                return ResponseEntity.status(400).body(response);
            }
            
            response.put("message", "User is already a member of this club or invite already sent.");
            return ResponseEntity.status(400).body(response);
        }
        
        return ResponseEntity.ok(clubInvite);
    }

    @PutMapping("/accept")
    public ResponseEntity<ClubInvite> acceptClubInvite(@RequestParam long id) {
        ClubInvite clubInvite = clubInviteController.acceptClubInvite(id);
        
        if (clubInvite == null) {
            return ResponseEntity.status(404).body(null);
        }
        
        return ResponseEntity.ok(clubInvite);
    }

    @PutMapping("/decline")
    public ResponseEntity<ClubInvite> declineClubInvite(@RequestParam long id) {
        ClubInvite clubInvite = clubInviteController.declineClubInvite(id);
        
        if (clubInvite == null) {
            return ResponseEntity.status(404).body(null);
        }
        
        return ResponseEntity.ok(clubInvite);
    }

    @DeleteMapping("/delete")
    public ResponseEntity<?> deleteClubInvite(@RequestParam long id) {
        boolean deleted = clubInviteController.deleteClubInvite(id);
        
        if (!deleted) {
            return ResponseEntity.status(404).body(null);
        }
        
        return ResponseEntity.ok(null);
    }
}

package hu.benkototh.cardgame.backend.rest.service;

import hu.benkototh.cardgame.backend.rest.controller.ClubChatController;
import hu.benkototh.cardgame.backend.rest.Data.ClubMessage;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/clubchat")
public class ClubChatRestService {

    @Autowired
    private ClubChatController clubChatController;

    @GetMapping("/history")
    public ResponseEntity<List<ClubMessage>> getClubChatHistory(@RequestParam long clubId) {
        List<ClubMessage> history = clubChatController.getClubChatHistory(clubId);
        return ResponseEntity.ok(history);
    }

    @PostMapping("/send")
    public ResponseEntity<ClubMessage> sendClubMessage(@RequestParam long clubId, @RequestParam long senderId, @RequestParam String content) {
        ClubMessage message = clubChatController.sendClubMessage(clubId, senderId, content);
        
        if (message == null) {
            return ResponseEntity.status(404).build();
        }
        
        return ResponseEntity.ok(message);
    }

    @PutMapping("/unsend")
    public ResponseEntity<ClubMessage> unsendClubMessage(@RequestParam long messageId) {
        ClubMessage message = clubChatController.unsendClubMessage(messageId);
        
        if (message == null) {
            return ResponseEntity.status(404).build();
        }
        
        return ResponseEntity.ok(message);
    }

    @PutMapping("/remove")
    public ResponseEntity<ClubMessage> removeClubMessage(@RequestParam long messageId) {
        ClubMessage message = clubChatController.removeClubMessage(messageId);
        
        if (message == null) {
            return ResponseEntity.status(404).build();
        }
        
        return ResponseEntity.ok(message);
    }
}

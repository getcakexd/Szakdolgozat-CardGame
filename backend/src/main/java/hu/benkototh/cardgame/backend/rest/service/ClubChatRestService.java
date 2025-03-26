package hu.benkototh.cardgame.backend.rest.service;

import hu.benkototh.cardgame.backend.rest.Data.Club;
import hu.benkototh.cardgame.backend.rest.Data.ClubMessage;
import hu.benkototh.cardgame.backend.rest.Data.User;
import hu.benkototh.cardgame.backend.rest.repository.IClubMessageRepository;
import hu.benkototh.cardgame.backend.rest.repository.IClubRepository;
import hu.benkototh.cardgame.backend.rest.repository.IUserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Optional;

@RestController
@RequestMapping("/api/clubchat")
public class ClubChatRestService {

    @Autowired
    private IClubMessageRepository clubMessageRepository;

    @Autowired
    private IClubRepository clubRepository;

    @Autowired
    private IUserRepository userRepository;

    @GetMapping("/history")
    public ResponseEntity<List<ClubMessage>> getClubChatHistory(@RequestParam long clubId) {
        List<ClubMessage> history = clubMessageRepository.findAll().stream()
                .filter(message ->
                        message.getClub().getId() == clubId
                ).toList();

        return ResponseEntity.ok(history);
    }

    @PostMapping("/send")
    public ResponseEntity<ClubMessage> sendClubMessage(@RequestParam long clubId, @RequestParam long senderId, @RequestParam String content) {
        Optional<Club> club = clubRepository.findById(clubId);
        Optional<User> user = userRepository.findById(senderId);

        if (club.isEmpty() || user.isEmpty()) {
            return ResponseEntity.status(404).build();
        }

        ClubMessage message = new ClubMessage(club.get(), user.get(), content);
        clubMessageRepository.save(message);
        return ResponseEntity.ok(message);
    }

    @PutMapping("/unsend")
    public ResponseEntity<ClubMessage> unsendClubMessage(@RequestParam long messageId) {
        Optional<ClubMessage> message = clubMessageRepository.findById(messageId);

        if (message.isEmpty()) {
            return ResponseEntity.status(404).build();
        }

        message.get().setStatus("unsent");
        message.get().setContent("This message has been unsent.");
        clubMessageRepository.save(message.get());
        return ResponseEntity.ok(message.get());
    }

    @PutMapping("/remove")
    public ResponseEntity<ClubMessage> removeClubMessage(@RequestParam long messageId) {
        Optional<ClubMessage> message = clubMessageRepository.findById(messageId);

        if (message.isEmpty()) {
            return ResponseEntity.status(404).build();
        }

        message.get().setStatus("removed");
        message.get().setContent("This message has been removed by a moderator.");
        clubMessageRepository.save(message.get());
        return ResponseEntity.ok(message.get());
    }
}

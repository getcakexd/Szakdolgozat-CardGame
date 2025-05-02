package hu.benkototh.cardgame.backend.rest.service;

import hu.benkototh.cardgame.backend.rest.controller.LobbyChatController;
import hu.benkototh.cardgame.backend.rest.Data.LobbyMessage;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/lobbychat")
public class LobbyChatRestService {

    @Autowired
    private LobbyChatController lobbyChatController;

    @GetMapping("/lobby/{lobbyId}")
    public ResponseEntity<List<LobbyMessage>> getLobbyMessages(@PathVariable long lobbyId) {
        List<LobbyMessage> messages = lobbyChatController.getLobbyMessages(lobbyId);
        return ResponseEntity.ok(messages);
    }

    @GetMapping("/game/{gameId}")
    public ResponseEntity<List<LobbyMessage>> getGameMessages(@PathVariable String gameId) {
        List<LobbyMessage> messages = lobbyChatController.getGameMessages(gameId);
        return ResponseEntity.ok(messages);
    }

    @PostMapping("/send")
    public ResponseEntity<LobbyMessage> sendMessage(
            @RequestParam long lobbyId,
            @RequestParam long senderId,
            @RequestParam String content) {

        LobbyMessage message = lobbyChatController.sendLobbyMessage(senderId, lobbyId, content);

        if (message == null) {
            return ResponseEntity.status(404).build();
        }

        return ResponseEntity.ok(message);
    }

    @PostMapping("/game/{gameId}/send")
    public ResponseEntity<LobbyMessage> sendGameMessage(
            @PathVariable String gameId,
            @RequestParam long senderId,
            @RequestParam String content) {

        LobbyMessage message = lobbyChatController.sendGameMessage(senderId, gameId, content);

        if (message == null) {
            return ResponseEntity.status(404).build();
        }

        return ResponseEntity.ok(message);
    }

    @PutMapping("/unsend")
    public ResponseEntity<LobbyMessage> unsendMessage(@RequestParam long messageId) {
        LobbyMessage message = lobbyChatController.unsendLobbyMessage(messageId);

        if (message == null) {
            return ResponseEntity.status(404).build();
        }

        return ResponseEntity.ok(message);
    }

    @PutMapping("/remove")
    public ResponseEntity<LobbyMessage> removeMessage(@RequestParam long messageId) {
        LobbyMessage message = lobbyChatController.removeMessage(messageId);

        if (message == null) {
            return ResponseEntity.status(404).build();
        }

        return ResponseEntity.ok(message);
    }
}

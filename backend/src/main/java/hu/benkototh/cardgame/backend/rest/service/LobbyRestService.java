package hu.benkototh.cardgame.backend.rest.service;

import hu.benkototh.cardgame.backend.rest.Data.Game;
import hu.benkototh.cardgame.backend.rest.Data.Lobby;
import hu.benkototh.cardgame.backend.rest.controller.LobbyController;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/lobbies")
public class LobbyRestService {

    @Autowired
    private LobbyController lobbyController;

    @PostMapping("/create")
    public ResponseEntity<?> createLobby(@RequestParam long leaderId, @RequestParam long gameId, @RequestParam boolean playWithPoints) {
        Lobby lobby = lobbyController.createLobby(leaderId, gameId, playWithPoints);
        
        if (lobby == null) {
            Map<String, String> response = new HashMap<>();
            response.put("message", "Failed to create lobby. User might already be in a lobby.");
            return ResponseEntity.status(400).body(response);
        }
        
        return ResponseEntity.ok(lobby);
    }

    @PostMapping("/join")
    public ResponseEntity<?> joinLobby(@RequestParam String code, @RequestParam long userId) {
        Lobby lobby = lobbyController.joinLobby(code, userId);
        
        if (lobby == null) {
            Map<String, String> response = new HashMap<>();
            response.put("message", "Failed to join lobby. Lobby might be full, in game, or user is already in a lobby.");
            return ResponseEntity.status(400).body(response);
        }
        
        return ResponseEntity.ok(lobby);
    }

    @PostMapping("/kick")
    public ResponseEntity<?> kickPlayer(@RequestParam long lobbyId, @RequestParam long leaderId, @RequestParam long playerId) {
        Lobby lobby = lobbyController.kickPlayer(lobbyId, leaderId, playerId);
        
        if (lobby == null) {
            Map<String, String> response = new HashMap<>();
            response.put("message", "Failed to kick player. You might not be the leader or player is not in the lobby.");
            return ResponseEntity.status(400).body(response);
        }
        
        return ResponseEntity.ok(lobby);
    }

    @PostMapping("/leave")
    public ResponseEntity<?> leaveLobby(@RequestParam long lobbyId, @RequestParam long userId) {
        Lobby lobby = lobbyController.leaveLobby(lobbyId, userId);
        
        Map<String, Object> response = new HashMap<>();
        if (lobby == null) {
            response.put("message", "You have left the lobby. The lobby might have been deleted if you were the last player.");
            response.put("lobbyDeleted", true);
        } else {
            response.put("message", "You have left the lobby.");
            response.put("lobbyDeleted", false);
            response.put("lobby", lobby);
        }
        
        return ResponseEntity.ok(response);
    }

    @PutMapping("/update-settings")
    public ResponseEntity<?> updateLobbySettings(@RequestParam long lobbyId, @RequestParam long leaderId, 
                                               @RequestParam long gameId, @RequestParam boolean playWithPoints) {
        Lobby lobby = lobbyController.updateLobbySettings(lobbyId, leaderId, gameId, playWithPoints);
        
        if (lobby == null) {
            Map<String, String> response = new HashMap<>();
            response.put("message", "Failed to update lobby settings. You might not be the leader.");
            return ResponseEntity.status(400).body(response);
        }
        
        return ResponseEntity.ok(lobby);
    }

    @PostMapping("/start-game")
    public ResponseEntity<?> startGame(@RequestParam long lobbyId, @RequestParam long leaderId) {
        Lobby lobby = lobbyController.startGame(lobbyId, leaderId);
        
        if (lobby == null) {
            Map<String, String> response = new HashMap<>();
            response.put("message", "Failed to start game. You might not be the leader or not enough players.");
            return ResponseEntity.status(400).body(response);
        }
        
        return ResponseEntity.ok(lobby);
    }

    @GetMapping("/get")
    public ResponseEntity<?> getLobby(@RequestParam long lobbyId) {
        Lobby lobby = lobbyController.getLobbyById(lobbyId);
        
        if (lobby == null) {
            Map<String, String> response = new HashMap<>();
            response.put("message", "Lobby not found.");
            return ResponseEntity.status(404).body(response);
        }
        
        return ResponseEntity.ok(lobby);
    }

    @GetMapping("/get-by-code")
    public ResponseEntity<?> getLobbyByCode(@RequestParam String code) {
        Lobby lobby = lobbyController.getLobbyByCode(code);
        
        if (lobby == null) {
            Map<String, String> response = new HashMap<>();
            response.put("message", "Lobby not found.");
            return ResponseEntity.status(404).body(response);
        }
        
        return ResponseEntity.ok(lobby);
    }

    @GetMapping("/get-by-player")
    public ResponseEntity<List<Lobby>> getLobbiesByPlayer(@RequestParam long userId) {
        return ResponseEntity.ok(lobbyController.getLobbiesByPlayer(userId));
    }

    @GetMapping("/player-lobby")
    public ResponseEntity<Lobby> getLobbyByPlayer(@RequestParam long userId){
        return ResponseEntity.ok(lobbyController.getLobbyByPlayer(userId));
    }

    @GetMapping("/games")
    public ResponseEntity<List<Game>> getAllGames() {
        return ResponseEntity.ok(lobbyController.getAllGames());
    }
}
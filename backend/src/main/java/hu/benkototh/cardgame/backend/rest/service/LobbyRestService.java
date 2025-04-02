package hu.benkototh.cardgame.backend.rest.service;

import hu.benkototh.cardgame.backend.rest.Data.Lobby;
import hu.benkototh.cardgame.backend.rest.Data.LobbyMessage;
import hu.benkototh.cardgame.backend.rest.Data.LobbyPlayer;
import hu.benkototh.cardgame.backend.rest.Data.User;
import hu.benkototh.cardgame.backend.rest.repository.ILobbyMessageRepository;
import hu.benkototh.cardgame.backend.rest.repository.ILobbyPlayerRepository;
import hu.benkototh.cardgame.backend.rest.repository.ILobbyRepository;
import hu.benkototh.cardgame.backend.rest.repository.IUserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.web.bind.annotation.*;

import java.util.*;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/lobby")
public class LobbyRestService {

    @Autowired
    private ILobbyRepository lobbyRepository;

    @Autowired
    private ILobbyPlayerRepository playerRepository;

    @Autowired
    private ILobbyMessageRepository messageRepository;

    @Autowired
    private IUserRepository userRepository;

    @Autowired
    private SimpMessagingTemplate messagingTemplate;

    @PostMapping("/create")
    public ResponseEntity<Lobby> createLobby(@RequestBody Lobby lobbyData, @RequestParam long userId, @RequestHeader("X-Session-ID") String sessionId) {
        Map<String, String> response = new HashMap<>();

        Optional<User> userOpt = userRepository.findById(userId);
        if (userOpt.isEmpty()) {
            return ResponseEntity.status(404).body(null);
        }

        User user = userOpt.get();

        boolean alreadyInLobby = lobbyRepository.findAll().stream()
                .flatMap(l -> l.getPlayers().stream())
                .anyMatch(p -> p.getUser().getId() == userId);

        if (alreadyInLobby) {
            return ResponseEntity.status(400).body(null);
        }

        Lobby lobby = new Lobby();
        lobby.setName(lobbyData.getName());
        lobby.setPublic(lobbyData.isPublic());
        lobby.setWithPoints(lobbyData.isWithPoints());
        lobby.setGameMode(lobbyData.getGameMode());
        lobby.setMaxPlayers(lobbyData.getMaxPlayers());

        LobbyPlayer player = new LobbyPlayer(user, sessionId);
        player.setLeader(true);

        lobby.addPlayer(player);

        Lobby savedLobby = lobbyRepository.save(lobby);

        return ResponseEntity.ok(savedLobby);
    }

    @GetMapping("/current")
    public ResponseEntity<Lobby> getCurrentLobby(@RequestParam long userId) {
        Optional<User> currentUser = userRepository.findById(userId);

        if (currentUser.isEmpty()) {
            return ResponseEntity.notFound().build();
        }

        Lobby lobby = lobbyRepository.findAll().stream()
                .filter(l -> l.getPlayers().stream().anyMatch(p -> p.getUser().getId() == userId))
                .findFirst()
                .orElse(null);

        if (lobby == null) {
            return ResponseEntity.notFound().build();
        }

        return ResponseEntity.ok(lobby);
    }

    @GetMapping("/public")
    public ResponseEntity<List<Lobby>> getPublicLobbies() {
        List<Lobby> publicLobbies = lobbyRepository.findAll().stream()
                .filter(Lobby::isPublic)
                .collect(Collectors.toList());

        return ResponseEntity.ok(publicLobbies);
    }

    @GetMapping("/code/{code}")
    public ResponseEntity<Lobby> getLobbyByCode(@PathVariable String code) {
        Optional<Lobby> lobbyOpt = lobbyRepository.findAll().stream()
                .filter(l -> l.getCode().equals(code))
                .findFirst();

        if (lobbyOpt.isEmpty()) {
            return ResponseEntity.status(404).body(null);
        }

        return ResponseEntity.ok(lobbyOpt.get());
    }

    @PostMapping("/join/{code}")
    public ResponseEntity<Lobby> joinLobbyByCode(@PathVariable String code, @RequestParam long userId, @RequestHeader("X-Session-ID") String sessionId) {
        Map<String, String> response = new HashMap<>();

        Optional<User> userOpt = userRepository.findById(userId);
        if (userOpt.isEmpty()) {
            return ResponseEntity.status(404).body(null);
        }

        User user = userOpt.get();

        boolean alreadyInLobby = lobbyRepository.findAll().stream()
                .flatMap(l -> l.getPlayers().stream())
                .anyMatch(p -> p.getUser().getId() == userId);

        if (alreadyInLobby) {
            return ResponseEntity.status(400).body(null);
        }

        Optional<Lobby> lobbyOpt = lobbyRepository.findAll().stream()
                .filter(l -> l.getCode().equals(code))
                .findFirst();

        if (lobbyOpt.isEmpty()) {
            return ResponseEntity.status(404).body(null);
        }

        Lobby lobby = lobbyOpt.get();

        if (!lobby.isPublic()) {
            return ResponseEntity.status(403).body(null);
        }

        if (lobby.getPlayers().size() >= lobby.getMaxPlayers()) {
            return ResponseEntity.status(400).body(null);
        }

        LobbyPlayer player = new LobbyPlayer(user, sessionId);
        lobby.addPlayer(player);

        Lobby updatedLobby = lobbyRepository.save(lobby);

        notifyLobbyUpdate(updatedLobby);

        return ResponseEntity.ok(updatedLobby);
    }

    @PostMapping("/{lobbyId}/leave")
    public ResponseEntity<Map<String, String>> leaveLobby(@PathVariable long lobbyId, @RequestParam long userId) {
        Map<String, String> response = new HashMap<>();

        Optional<Lobby> lobbyOpt = lobbyRepository.findById(lobbyId);
        if (lobbyOpt.isEmpty()) {
            response.put("message", "Lobby not found");
            return ResponseEntity.status(404).body(response);
        }

        Optional<User> userOpt = userRepository.findById(userId);
        if (userOpt.isEmpty()) {
            response.put("message", "User not found");
            return ResponseEntity.status(404).body(response);
        }

        Lobby lobby = lobbyOpt.get();
        User user = userOpt.get();

        Optional<LobbyPlayer> playerOpt = lobby.getPlayers().stream()
                .filter(p -> p.getUser().getId() == userId)
                .findFirst();

        if (playerOpt.isEmpty()) {
            response.put("message", "Player not in this lobby");
            return ResponseEntity.status(404).body(response);
        }

        LobbyPlayer player = playerOpt.get();
        boolean wasLeader = player.isLeader();

        lobby.removePlayer(player);
        playerRepository.delete(player);

        if (lobby.getPlayers().isEmpty()) {
            lobbyRepository.delete(lobby);
            response.put("message", "Lobby deleted");
            return ResponseEntity.ok(response);
        }

        if (wasLeader) {
            lobby.promoteNextLeader();
        }

        Lobby updatedLobby = lobbyRepository.save(lobby);

        notifyLobbyUpdate(updatedLobby);

        response.put("message", "Left lobby successfully");
        return ResponseEntity.ok(response);
    }

    @PostMapping("/{lobbyId}/kick")
    public ResponseEntity<Map<String, String>> kickPlayer(@PathVariable long lobbyId, @RequestParam long leaderId, @RequestParam long targetUserId) {
        Map<String, String> response = new HashMap<>();

        Optional<Lobby> lobbyOpt = lobbyRepository.findById(lobbyId);
        if (lobbyOpt.isEmpty()) {
            response.put("message", "Lobby not found");
            return ResponseEntity.status(404).body(response);
        }

        Lobby lobby = lobbyOpt.get();

        if (!lobby.isLeader(leaderId)) {
            response.put("message", "Only the lobby leader can kick players");
            return ResponseEntity.status(403).body(response);
        }

        if (leaderId == targetUserId) {
            response.put("message", "Cannot kick yourself from the lobby");
            return ResponseEntity.status(400).body(response);
        }

        Optional<User> targetUserOpt = userRepository.findById(targetUserId);
        if (targetUserOpt.isEmpty()) {
            response.put("message", "Target user not found");
            return ResponseEntity.status(404).body(response);
        }

        User targetUser = targetUserOpt.get();

        Optional<LobbyPlayer> targetPlayerOpt = lobby.getPlayers().stream()
                .filter(p -> p.getUser().getId() == targetUserId)
                .findFirst();

        if (targetPlayerOpt.isEmpty()) {
            response.put("message", "Target player not in this lobby");
            return ResponseEntity.status(404).body(response);
        }

        LobbyPlayer targetPlayer = targetPlayerOpt.get();

        lobby.removePlayer(targetPlayer);
        playerRepository.delete(targetPlayer);

        Lobby updatedLobby = lobbyRepository.save(lobby);

        notifyLobbyUpdate(updatedLobby);

        messagingTemplate.convertAndSendToUser(
                targetUser.getUsername(),
                "/queue/lobby/kicked",
                "You have been kicked from the lobby"
        );

        response.put("message", "Player kicked successfully");
        return ResponseEntity.ok(response);
    }

    @PutMapping("/{lobbyId}/settings")
    public ResponseEntity<Lobby> updateLobbySettings(@PathVariable long lobbyId, @RequestParam long leaderId, @RequestBody Lobby updateData) {
        Optional<Lobby> lobbyOpt = lobbyRepository.findById(lobbyId);
        if (lobbyOpt.isEmpty()) {
            return ResponseEntity.status(404).body(null);
        }

        Lobby lobby = lobbyOpt.get();

        if (!lobby.isLeader(leaderId)) {
            return ResponseEntity.status(403).body(null);
        }

        lobby.setName(updateData.getName());
        lobby.setPublic(updateData.isPublic());
        lobby.setWithPoints(updateData.isWithPoints());
        lobby.setGameMode(updateData.getGameMode());

        Lobby updatedLobby = lobbyRepository.save(lobby);

        notifyLobbyUpdate(updatedLobby);

        return ResponseEntity.ok(updatedLobby);
    }

    @PostMapping("/{lobbyId}/ready")
    public ResponseEntity<Map<String, String>> toggleReady(@PathVariable long lobbyId, @RequestParam long userId) {
        Map<String, String> response = new HashMap<>();

        Optional<Lobby> lobbyOpt = lobbyRepository.findById(lobbyId);
        if (lobbyOpt.isEmpty()) {
            response.put("message", "Lobby not found");
            return ResponseEntity.status(404).body(response);
        }

        Lobby lobby = lobbyOpt.get();

        Optional<LobbyPlayer> playerOpt = lobby.getPlayers().stream()
                .filter(p -> p.getUser().getId() == userId)
                .findFirst();

        if (playerOpt.isEmpty()) {
            response.put("message", "Player not in this lobby");
            return ResponseEntity.status(404).body(response);
        }

        LobbyPlayer player = playerOpt.get();

        if (player.isLeader()) {
            response.put("message", "Lobby leader cannot ready up");
            return ResponseEntity.status(400).body(response);
        }

        player.setReady(!player.isReady());
        playerRepository.save(player);

        notifyLobbyUpdate(lobby);

        response.put("message", player.isReady() ? "Player is now ready" : "Player is no longer ready");
        return ResponseEntity.ok(response);
    }

    @GetMapping("/{lobbyId}/messages")
    public ResponseEntity<List<LobbyMessage>> getLobbyMessages(@PathVariable long lobbyId) {
        Optional<Lobby> lobbyOpt = lobbyRepository.findById(lobbyId);
        if (lobbyOpt.isEmpty()) {
            return ResponseEntity.status(404).body(null);
        }

        Lobby lobby = lobbyOpt.get();

        List<LobbyMessage> messages = new ArrayList<>(lobby.getMessages());
        messages.sort(Comparator.comparing(LobbyMessage::getSentAt));

        return ResponseEntity.ok(messages);
    }

    @PostMapping("/{lobbyId}/messages")
    public ResponseEntity<LobbyMessage> sendMessage(@PathVariable long lobbyId, @RequestParam long userId, @RequestBody Map<String, String> messageData) {
        Optional<Lobby> lobbyOpt = lobbyRepository.findById(lobbyId);
        if (lobbyOpt.isEmpty()) {
            return ResponseEntity.status(404).body(null);
        }

        Optional<User> userOpt = userRepository.findById(userId);
        if (userOpt.isEmpty()) {
            return ResponseEntity.status(404).body(null);
        }

        Lobby lobby = lobbyOpt.get();
        User user = userOpt.get();

        boolean isInLobby = lobby.getPlayers().stream()
                .anyMatch(p -> p.getUser().getId()== userId);

        if (!isInLobby) {
            return ResponseEntity.status(403).body(null);
        }

        String content = messageData.get("content");
        if (content == null || content.trim().isEmpty()) {
            return ResponseEntity.status(400).body(null);
        }

        LobbyMessage message = new LobbyMessage(lobby, user, content);
        LobbyMessage savedMessage = messageRepository.save(message);

        messagingTemplate.convertAndSend(
                "/topic/lobby/" + lobby.getId() + "/messages",
                savedMessage
        );

        return ResponseEntity.ok(savedMessage);
    }

    @PostMapping("/disconnect")
    public ResponseEntity<Map<String, String>> handleSessionDisconnect(@RequestHeader("X-Session-ID") String sessionId) {
        Map<String, String> response = new HashMap<>();

        Optional<LobbyPlayer> playerOpt = playerRepository.findAll().stream()
                .filter(p -> p.getSessionId().equals(sessionId))
                .findFirst();

        if (playerOpt.isEmpty()) {
            response.put("message", "No player found with this session");
            return ResponseEntity.status(404).body(response);
        }

        LobbyPlayer player = playerOpt.get();
        Lobby lobby = player.getLobby();

        lobby.removePlayer(player);
        playerRepository.delete(player);

        if (lobby.getPlayers().isEmpty()) {
            lobbyRepository.delete(lobby);
            response.put("message", "Lobby deleted");
            return ResponseEntity.ok(response);
        }

        Lobby updatedLobby = lobbyRepository.save(lobby);

        notifyLobbyUpdate(updatedLobby);

        response.put("message", "Player disconnected");
        return ResponseEntity.ok(response);
    }

    private void notifyLobbyUpdate(Lobby lobby) {
        messagingTemplate.convertAndSend(
                "/topic/lobby/" + lobby.getId() + "/updates",
                lobby
        );
    }
}
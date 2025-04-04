package hu.benkototh.cardgame.backend.rest.Data;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonManagedReference;
import jakarta.persistence.*;

import java.time.LocalDateTime;
import java.util.*;

@Entity
public class Lobby {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String name;
    private String code;
    private boolean isPublic;
    private String gameMode;
    private boolean withPoints;
    private int maxPlayers;
    private LocalDateTime createdAt;

    @OneToMany(mappedBy = "lobby", cascade = CascadeType.ALL, orphanRemoval = true)
    @JsonManagedReference // This annotation helps with JSON serialization
    private List<LobbyPlayer> players = new ArrayList<>();

    @OneToMany(mappedBy = "lobby", cascade = CascadeType.ALL, orphanRemoval = true)
    @JsonManagedReference // This annotation helps with JSON serialization
    private List<LobbyMessage> messages = new ArrayList<>();

    // Default constructor
    public Lobby() {
        this.createdAt = LocalDateTime.now();
        this.code = generateCode();
    }

    // Generate a random 6-character code
    private String generateCode() {
        String chars = "ABCDEFGHIJKLMNOPQRSTUVWXYZ0123456789";
        StringBuilder code = new StringBuilder();
        Random random = new Random();

        for (int i = 0; i < 6; i++) {
            code.append(chars.charAt(random.nextInt(chars.length())));
        }

        return code.toString();
    }

    // Getters and setters
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getCode() {
        return code;
    }

    public void setCode(String code) {
        this.code = code;
    }

    public boolean isPublic() {
        return isPublic;
    }

    public void setPublic(boolean isPublic) {
        this.isPublic = isPublic;
    }

    public String getGameMode() {
        return gameMode;
    }

    public void setGameMode(String gameMode) {
        this.gameMode = gameMode;
    }

    public boolean isWithPoints() {
        return withPoints;
    }

    public void setWithPoints(boolean withPoints) {
        this.withPoints = withPoints;
    }

    public int getMaxPlayers() {
        return maxPlayers;
    }

    public void setMaxPlayers(int maxPlayers) {
        this.maxPlayers = maxPlayers;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }

    public List<LobbyPlayer> getPlayers() {
        return players;
    }

    public void setPlayers(List<LobbyPlayer> players) {
        this.players = players;
    }

    public List<LobbyMessage> getMessages() {
        return messages;
    }

    public void setMessages(List<LobbyMessage> messages) {
        this.messages = messages;
    }

    // Helper methods
    public void addPlayer(LobbyPlayer player) {
        players.add(player);
        player.setLobby(this);
    }

    public void removePlayer(LobbyPlayer player) {
        players.remove(player);
        player.setLobby(null);
    }

    public void promoteNextLeader() {
        if (players.isEmpty()) return;

        players.get(0).setLeader(true);
    }

    public boolean isLeader(long userId) {
        return players.stream()
                .anyMatch(p -> p.getUser().getId() == userId && p.isLeader());
    }

    // Override hashCode and equals to avoid circular reference
    @Override
    public int hashCode() {
        return Objects.hash(id, name, code, isPublic, gameMode, withPoints, maxPlayers, createdAt);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Lobby lobby = (Lobby) o;
        return id != null && id.equals(lobby.id);
    }

    @Override
    public String toString() {
        return "Lobby{" +
                "id=" + id +
                ", name='" + name + '\'' +
                ", code='" + code + '\'' +
                ", isPublic=" + isPublic +
                ", gameMode='" + gameMode + '\'' +
                ", withPoints=" + withPoints +
                ", maxPlayers=" + maxPlayers +
                ", createdAt=" + createdAt +
                ", playersCount=" + (players != null ? players.size() : 0) +
                ", messagesCount=" + (messages != null ? messages.size() : 0) +
                '}';
    }
}
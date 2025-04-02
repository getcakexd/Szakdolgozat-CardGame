package hu.benkototh.cardgame.backend.rest.Data;

import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

@Entity
@Table(name = "lobbies")
@Data
@NoArgsConstructor
public class Lobby {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true)
    private String code;

    @Column(nullable = false)
    private String name;

    @Column(nullable = false)
    private boolean isPublic = false;

    @Column(nullable = false)
    private boolean withPoints = true;

    @Column(nullable = false)
    private String gameMode = "classic";

    @Column(nullable = false)
    private int maxPlayers = 4;

    @OneToMany(mappedBy = "lobby", cascade = CascadeType.ALL, orphanRemoval = true)
    private Set<LobbyPlayer> players = new HashSet<>();

    @OneToMany(mappedBy = "lobby", cascade = CascadeType.ALL, orphanRemoval = true)
    private Set<LobbyMessage> messages = new HashSet<>();

    @CreationTimestamp
    private LocalDateTime createdAt;

    @UpdateTimestamp
    private LocalDateTime updatedAt;

    @PrePersist
    public void prePersist() {
        if (code == null || code.isEmpty()) {
            code = generateUniqueCode();
        }
    }

    private String generateUniqueCode() {
        return UUID.randomUUID().toString().substring(0, 6).toUpperCase();
    }

    public boolean isLeader(Long userId) {
        return players.stream()
                .filter(player -> player.isLeader())
                .anyMatch(player -> player.getUser().getId() == userId);
    }

    public LobbyPlayer getLeader() {
        return players.stream()
                .filter(player -> player.isLeader())
                .findFirst()
                .orElse(null);
    }

    public void promoteNextLeader() {
        if (players.isEmpty()) return;

        LobbyPlayer currentLeader = getLeader();
        if (currentLeader != null) {
            currentLeader.setLeader(false);
        }

        players.stream()
                .findFirst()
                .ifPresent(player -> player.setLeader(true));
    }

    public boolean addPlayer(LobbyPlayer player) {
        if (players.size() >= maxPlayers) {
            return false;
        }

        player.setLobby(this);
        if (players.isEmpty()) {
            player.setLeader(true);
        }

        return players.add(player);
    }

    public boolean removePlayer(LobbyPlayer player) {
        boolean removed = players.remove(player);

        if (removed && player.isLeader() && !players.isEmpty()) {
            promoteNextLeader();
        }

        return removed;
    }
}

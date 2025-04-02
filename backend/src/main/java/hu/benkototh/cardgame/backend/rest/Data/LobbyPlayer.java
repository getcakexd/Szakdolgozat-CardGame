package hu.benkototh.cardgame.backend.rest.Data;

import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDateTime;

@Entity
@Table(name = "lobby_players")
@Data
@NoArgsConstructor
public class LobbyPlayer {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "lobby_id", nullable = false)
    private Lobby lobby;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @Column(nullable = false)
    private boolean isLeader = false;

    @Column(nullable = false)
    private boolean isReady = false;

    @Column(nullable = false)
    private String sessionId;

    @CreationTimestamp
    private LocalDateTime joinedAt;

    @UpdateTimestamp
    private LocalDateTime updatedAt;

    public LobbyPlayer(User user, String sessionId) {
        this.user = user;
        this.sessionId = sessionId;
    }
}
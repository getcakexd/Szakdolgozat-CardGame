package hu.benkototh.cardgame.backend.rest.Data;

import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDateTime;

@Entity
@Table(name = "lobby_messages")
@Data
@NoArgsConstructor
public class LobbyMessage {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "lobby_id", nullable = false)
    private Lobby lobby;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @Column(nullable = false, length = 500)
    private String content;

    @CreationTimestamp
    private LocalDateTime sentAt;

    public LobbyMessage(Lobby lobby, User user, String content) {
        this.lobby = lobby;
        this.user = user;
        this.content = content;
    }
}
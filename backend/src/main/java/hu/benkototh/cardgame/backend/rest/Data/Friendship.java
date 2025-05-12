package hu.benkototh.cardgame.backend.rest.Data;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.persistence.*;

@Entity
@Table(name = "friendships")
@Schema(description = "Represents a friendship connection between two users")
public class Friendship {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Schema(description = "Unique identifier for the friendship", example = "1")
    private long id;

    @ManyToOne
    @JoinColumn(name = "user1_id", nullable = false,
            foreignKey = @ForeignKey(name = "fk_friendship_user1",
                    foreignKeyDefinition = "FOREIGN KEY (user1_id) REFERENCES users(id) ON DELETE CASCADE"))
    @Schema(description = "First user in the friendship")
    private User user1;

    @ManyToOne
    @JoinColumn(name = "user2_id", nullable = false,
            foreignKey = @ForeignKey(name = "fk_friendship_user2",
                    foreignKeyDefinition = "FOREIGN KEY (user2_id) REFERENCES users(id) ON DELETE CASCADE"))
    @Schema(description = "Second user in the friendship")
    private User user2;

    public Friendship() {}

    public Friendship(User user1, User user2) {
        this.user1 = user1;
        this.user2 = user2;
    }

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public User getUser1() {
        return user1;
    }

    public void setUser1(User user1) {
        this.user1 = user1;
    }

    public User getUser2() {
        return user2;
    }

    public void setUser2(User user2) {
        this.user2 = user2;
    }
}
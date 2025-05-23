package hu.benkototh.cardgame.backend.rest.model;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.persistence.*;

@Entity
@Table(name = "club_invites")
@Schema(description = "Represents an invitation for a user to join a club")
public class ClubInvite {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Schema(description = "Unique identifier for the club invite", example = "1")
    private long id;

    @ManyToOne
    @JoinColumn(name = "club_id", nullable = false,
            foreignKey = @ForeignKey(name = "fk_clubinvite_club",
                    foreignKeyDefinition = "FOREIGN KEY (club_id) REFERENCES clubs(id) ON DELETE CASCADE"))
    @Schema(description = "Club that sent the invitation")
    private Club club;

    @ManyToOne
    @JoinColumn(name = "user_id", nullable = false,
            foreignKey = @ForeignKey(name = "fk_clubinvite_user",
                    foreignKeyDefinition = "FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE"))
    @Schema(description = "User who received the invitation")
    private User user;

    @Schema(description = "Current status of the invitation", example = "pending",
            allowableValues = {"pending", "accepted", "rejected", "canceled"})
    private String status;

    @Transient
    @Schema(description = "Name of the club (transient field, not stored in database)", example = "Card Masters")
    private String clubName;

    @Transient
    @Schema(description = "Username of the invited user (transient field, not stored in database)", example = "johndoe")
    private String username;

    public ClubInvite() {
    }

    public ClubInvite(Club club, User user) {
        this.club = club;
        this.user = user;
        this.status = "pending";
    }

    public long getId() {
        return id;
    }

    public Club getClub() {
        return club;
    }

    public User getUser() {
        return user;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public String getClubName() {
        return clubName;
    }

    public void setClubName(String clubName) {
        this.clubName = clubName;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }
}
package hu.benkototh.cardgame.backend.rest.model;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.persistence.*;

@Entity
@Table(name = "club_members")
@Schema(description = "Represents a member of a club with their role")
public class ClubMember {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Schema(description = "Unique identifier for the club membership", example = "1")
    private long id;

    @ManyToOne
    @JoinColumn(name = "club_id", nullable = false,
            foreignKey = @ForeignKey(name = "fk_clubmember_club",
                    foreignKeyDefinition = "FOREIGN KEY (club_id) REFERENCES clubs(id) ON DELETE CASCADE"))
    @Schema(description = "Club this membership belongs to")
    private Club club;

    @ManyToOne
    @JoinColumn(name = "user_id", nullable = false,
            foreignKey = @ForeignKey(name = "fk_clubmember_user",
                    foreignKeyDefinition = "FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE"))
    @Schema(description = "User who is a member of the club")
    private User user;

    @Column(nullable = false)
    @Schema(description = "Role of the user in the club", example = "member",
            allowableValues = {"owner", "admin", "moderator", "member"})
    private String role;

    @Transient
    @Schema(description = "Username of the club member (transient field, not stored in database)", example = "johndoe")
    private String username;

    public ClubMember() {

    }

    public ClubMember(Club club, User user, String role) {
        this.club = club;
        this.user = user;
        this.role = role;
    }

    public ClubMember(Club club, User user) {
        this.club = club;
        this.user = user;
        this.role = "member";
    }

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public Club getClub() {
        return club;
    }

    public void setClub(Club club) {
        this.club = club;
    }

    public User getUser() {
        return user;
    }

    public void setUser(User user) {
        this.user = user;
    }

    public String getRole() {
        return role;
    }

    public void setRole(String role) {
        this.role = role;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }
}
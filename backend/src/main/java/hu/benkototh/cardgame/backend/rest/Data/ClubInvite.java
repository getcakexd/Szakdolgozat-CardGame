package hu.benkototh.cardgame.backend.rest.Data;

import jakarta.persistence.*;

@Entity
@Table(name = "club_invites")
public class ClubInvite {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private long id;

    @ManyToOne(cascade = {CascadeType.PERSIST, CascadeType.MERGE})
    @JoinColumn(name = "club_id", nullable = false,
            foreignKey = @ForeignKey(name = "fk_clubinvite_club",
                    foreignKeyDefinition = "FOREIGN KEY (club_id) REFERENCES clubs(id) ON DELETE CASCADE"))
    private Club club;

    @ManyToOne(cascade = {CascadeType.PERSIST, CascadeType.MERGE})
    @JoinColumn(name = "user_id", nullable = false,
            foreignKey = @ForeignKey(name = "fk_clubinvite_user",
                    foreignKeyDefinition = "FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE"))
    private User user;

    private String status;

    @Transient
    private String clubName;

    @Transient
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

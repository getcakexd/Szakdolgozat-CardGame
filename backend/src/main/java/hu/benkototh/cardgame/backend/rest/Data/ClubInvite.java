package hu.benkototh.cardgame.backend.rest.Data;

import jakarta.persistence.*;

@Entity
@Table(name = "club_invites")
public class ClubInvite {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private long id;

    @ManyToOne
    @JoinColumn(name = "club_id", nullable = false)
    private Club club;

    @ManyToOne
    @JoinColumn(name = "user_id", nullable = false)
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

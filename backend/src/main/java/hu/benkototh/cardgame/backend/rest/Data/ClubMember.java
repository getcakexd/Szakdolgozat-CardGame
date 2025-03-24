package hu.benkototh.cardgame.backend.rest.Data;

import jakarta.persistence.*;

@Entity
@Table(name = "club_members")
public class ClubMember {
    @Id
    private long id;

    @ManyToOne
    @JoinColumn(name = "club_id", nullable = false)
    private Club club;

    @ManyToOne
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @Column(nullable = false)
    private String role;

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
}

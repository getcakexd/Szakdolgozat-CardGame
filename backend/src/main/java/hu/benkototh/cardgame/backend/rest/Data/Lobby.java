package hu.benkototh.cardgame.backend.rest.Data;

import jakarta.persistence.*;
import java.util.HashSet;
import java.util.Random;
import java.util.Set;

@Entity
@Table(name = "lobbies")
public class Lobby {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private long id;

    @Column(unique = true)
    private String code;

    @ManyToOne
    @JoinColumn(name = "leader_id", nullable = false)
    private User leader;

    @ManyToOne
    @JoinColumn(name = "game_id", nullable = false)
    private Game game;

    private boolean playWithPoints;

    private int minPlayers;

    @ManyToMany
    @JoinTable(
            name = "lobby_players",
            joinColumns = @JoinColumn(name = "lobby_id"),
            inverseJoinColumns = @JoinColumn(name = "user_id")
    )
    private Set<User> players = new HashSet<>();

    private String status = "WAITING";

    public Lobby() {
        this.code = generateCode();
    }

    private String generateCode() {
        Random random = new Random();
        StringBuilder code = new StringBuilder();
        for (int i = 0; i < 6; i++) {
            code.append((char) (random.nextInt(26) + 'A'));
        }
        return code.toString();
    }

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public String getCode() {
        return code;
    }

    public void setCode(String code) {
        this.code = code;
    }

    public User getLeader() {
        return leader;
    }

    public void setLeader(User leader) {
        this.leader = leader;
    }

    public Game getGame() {
        return game;
    }

    public void setGame(Game game) {
        this.game = game;
    }

    public boolean isPlayWithPoints() {
        return playWithPoints;
    }

    public void setPlayWithPoints(boolean playWithPoints) {
        this.playWithPoints = playWithPoints;
    }

    public Set<User> getPlayers() {
        return players;
    }

    public void setPlayers(Set<User> players) {
        this.players = players;
    }

    public void addPlayer(User player) {
        this.players.add(player);
    }

    public void removePlayer(User player) {
        this.players.remove(player);
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public int getMinPlayers() {
        return minPlayers;
    }

    public void setMinPlayers(int minPlayers) {
        this.minPlayers = minPlayers;
    }

    public boolean canStart() {
        return players.size() >= minPlayers;
    }
}
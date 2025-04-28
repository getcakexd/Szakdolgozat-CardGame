package hu.benkototh.cardgame.backend.rest.Data;

import jakarta.persistence.*;

import java.util.Date;
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

    @ManyToOne(cascade = {CascadeType.PERSIST, CascadeType.MERGE})
    @JoinColumn(name = "leader_id", nullable = false,
            foreignKey = @ForeignKey(name = "fk_lobby_leader",
                    foreignKeyDefinition = "FOREIGN KEY (leader_id) REFERENCES users(id) ON DELETE CASCADE"))
    private User leader;

    @ManyToOne(cascade = {CascadeType.PERSIST, CascadeType.MERGE})
    @JoinColumn(name = "game_id", nullable = false,
            foreignKey = @ForeignKey(name = "fk_lobby_game",
                    foreignKeyDefinition = "FOREIGN KEY (game_id) REFERENCES games(id) ON DELETE CASCADE"))
    private Game game;

    @ManyToMany(cascade = {CascadeType.PERSIST, CascadeType.MERGE})
    @JoinTable(
            name = "lobby_players",
            joinColumns = @JoinColumn(name = "lobby_id"),
            inverseJoinColumns = @JoinColumn(name = "user_id",
                    foreignKey = @ForeignKey(name = "fk_lobbyplayers_user",
                            foreignKeyDefinition = "FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE"))
    )
    private Set<User> players = new HashSet<>();

    private boolean playWithPoints;

    private int minPlayers;

    private String status = "WAITING";

    @Temporal(TemporalType.TIMESTAMP)
    private Date createdAt;

    private String cardGameId;

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

    public String getCardGameId() {
        return cardGameId;
    }

    public void setCardGameId(String cardGameId) {
        this.cardGameId = cardGameId;
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
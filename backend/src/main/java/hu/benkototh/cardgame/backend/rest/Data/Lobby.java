package hu.benkototh.cardgame.backend.rest.Data;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.persistence.*;

import java.util.Date;
import java.util.HashSet;
import java.util.Random;
import java.util.Set;

@Entity
@Table(name = "lobbies")
@Schema(description = "Represents a game lobby where players gather before starting a game")
public class Lobby {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Schema(description = "Unique identifier for the lobby", example = "1")
    private long id;

    @Column(unique = true)
    @Schema(description = "Unique join code for the lobby", example = "ABCDEF")
    private String code;

    @ManyToOne
    @JoinColumn(name = "leader_id", nullable = false,
            foreignKey = @ForeignKey(name = "fk_lobby_leader",
                    foreignKeyDefinition = "FOREIGN KEY (leader_id) REFERENCES users(id) ON DELETE CASCADE"))
    @Schema(description = "The user who created and leads the lobby")
    private User leader;

    @ManyToOne
    @JoinColumn(name = "game_id", nullable = false,
            foreignKey = @ForeignKey(name = "fk_lobby_game",
                    foreignKeyDefinition = "FOREIGN KEY (game_id) REFERENCES games(id) ON DELETE CASCADE"))
    @Schema(description = "The game selected for this lobby")
    private Game game;

    @ManyToMany
    @JoinTable(
            name = "lobby_players",
            joinColumns = @JoinColumn(name = "lobby_id"),
            inverseJoinColumns = @JoinColumn(name = "user_id",
                    foreignKey = @ForeignKey(name = "fk_lobbyplayers_user",
                            foreignKeyDefinition = "FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE"))
    )
    @Schema(description = "Set of players currently in the lobby")
    private Set<User> players = new HashSet<>();

    @ManyToOne
    @JoinColumn(
            name = "club_id",
            foreignKey = @ForeignKey(name = "fk_lobby_club",
                    foreignKeyDefinition = "FOREIGN KEY (club_id) REFERENCES clubs(id) ON DELETE CASCADE")
    )
    @Schema(description = "The club this lobby belongs to, if it's a club lobby")
    private Club club;

    @Schema(description = "Whether the lobby is publicly visible", example = "false")
    private boolean isPublic = false;

    @Schema(description = "Whether the game will track and award points", example = "true")
    private boolean playWithPoints;

    @Schema(description = "Minimum number of players required to start the game", example = "2")
    private int minPlayers;

    @Schema(description = "Current status of the lobby", example = "WAITING", allowableValues = {"WAITING", "IN_GAME", "FINISHED"})
    private String status = "WAITING";

    @Temporal(TemporalType.TIMESTAMP)
    @Schema(description = "When the lobby was created", example = "2023-05-20T14:30:00Z")
    private Date createdAt;

    @Schema(description = "ID of the active card game if the lobby is in game state", example = "04a420c1-e047-4530-8fe1-df22d07226cd")
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

    public Club getClub() {
        return club;
    }

    public void setClub(Club club) {
        this.club = club;
    }

    public boolean isPublic() {
        return isPublic;
    }

    public void setPublic(boolean aPublic) {
        isPublic = aPublic;
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
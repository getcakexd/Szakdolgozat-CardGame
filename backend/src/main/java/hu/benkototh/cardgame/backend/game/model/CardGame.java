package hu.benkototh.cardgame.backend.game.model;

import hu.benkototh.cardgame.backend.game.exception.GameException;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.persistence.*;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import com.fasterxml.jackson.annotation.JsonManagedReference;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Entity
@Table(name = "card_games")
@Inheritance(strategy = InheritanceType.SINGLE_TABLE)
@DiscriminatorColumn(name = "game_type", discriminatorType = DiscriminatorType.STRING)
@Schema(description = "Abstract base class representing a card game session")
public abstract class CardGame {
    private static final Logger logger = LoggerFactory.getLogger(CardGame.class);
    private static final ObjectMapper objectMapper = new ObjectMapper();

    @Id
    @Schema(description = "Unique identifier for the game session", example = "04a420c1-e047-4530-8fe1-df22d07226cd")
    private String id;

    @Schema(description = "ID of the game definition (game type)", example = "1")
    private long gameDefinitionId;

    @Schema(description = "Name of the game session", example = "John's Poker Game")
    private String name;

    @Enumerated(EnumType.STRING)
    @Schema(description = "Current status of the game", example = "ACTIVE", allowableValues = {"WAITING", "ACTIVE", "FINISHED", "ABANDONED"})
    private GameStatus status;

    @Temporal(TemporalType.TIMESTAMP)
    @Schema(description = "When the game was created", example = "2023-05-20T14:00:00Z")
    private Date createdAt;

    @Temporal(TemporalType.TIMESTAMP)
    @Schema(description = "When the game was started", example = "2023-05-20T14:05:00Z")
    private Date startedAt;

    @Temporal(TemporalType.TIMESTAMP)
    @Schema(description = "When the game ended", example = "2023-05-20T14:30:00Z")
    private Date endedAt;

    @Schema(description = "Whether this game should track statistics", example = "true")
    private boolean trackStatistics;

    @OneToMany(cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.EAGER)
    @JoinColumn(name = "game_id"
            , foreignKey = @ForeignKey(name = "fk_game_player",
            foreignKeyDefinition = "FOREIGN KEY (game_id) REFERENCES card_games(id) ON DELETE CASCADE"))
    @JsonManagedReference
    @Schema(description = "List of players participating in the game")
    private List<Player> players;

    @ManyToOne
    @JoinColumn(name = "current_player_id",
            foreignKey = @ForeignKey(name = "FK_CURRENT_PLAYER",
                    foreignKeyDefinition = "FOREIGN KEY (current_player_id) REFERENCES players(id) ON DELETE SET NULL"))
    @Schema(description = "Player whose turn it currently is")
    private Player currentPlayer;

    @ElementCollection
    @CollectionTable(
            name = "abandoned_users",
            joinColumns = @JoinColumn(name = "game_id")
    )
    @Column(name = "user_id")
    @Schema(description = "List of user IDs who have abandoned the game")
    private List<String> abandonedUsers = new ArrayList<>();

    @Transient
    @Schema(description = "Current state of the game (transient, not stored directly in database)")
    private Map<String, Object> gameState;

    @Column(columnDefinition = "TEXT")
    @Schema(description = "Serialized JSON representation of the game state")
    private String serializedGameState;

    public CardGame() {
        this.id = UUID.randomUUID().toString();
        this.players = new ArrayList<>();
        this.abandonedUsers = new ArrayList<>();
        this.status = GameStatus.WAITING;
        this.createdAt = new Date();
        this.gameState = new ConcurrentHashMap<>();
        this.trackStatistics = true;
        logger.debug("Created new CardGame with ID: {}", this.id);
    }

    @Schema(description = "Initialize the game with cards, players, and initial state")
    public abstract void initializeGame();

    @Schema(description = "Check if a move is valid for the given player")
    public abstract boolean isValidMove(String playerId, GameAction action);

    @Schema(description = "Execute a move for the given player")
    public abstract void executeMove(String playerId, GameAction action);

    @Schema(description = "Check if the game is over")
    public abstract boolean isGameOver();

    @Schema(description = "Calculate final scores for all players")
    public abstract Map<String, Integer> calculateScores();

    @Schema(description = "Get the minimum number of players required for this game")
    public abstract int getMinPlayers();

    @Schema(description = "Get the maximum number of players allowed for this game")
    public abstract int getMaxPlayers();

    @Schema(description = "Add a player to the game")
    public void addPlayer(Player player) {
        if (players.size() < getMaxPlayers() && status == GameStatus.WAITING) {
            players.add(player);
            logger.debug("Added player {} to game {}", player.getId(), this.id);
        } else {
            throw new GameException("Cannot add player to the game");
        }
    }

    @Schema(description = "Remove a player from the game")
    public void removePlayer(String playerId) {
        if (currentPlayer != null && currentPlayer.getId().equals(playerId)) {
            currentPlayer = null;
        }

        boolean removed = players.removeIf(player -> player.getId().equals(playerId));
        if (removed) {
            logger.debug("Removed player {} from game {}", playerId, this.id);
        } else {
            logger.warn("Player {} not found in game {}", playerId, this.id);
        }
    }

    @Schema(description = "Mark a user as having abandoned the game")
    public void addAbandonedUser(String userId) {
        if (!abandonedUsers.contains(userId)) {
            abandonedUsers.add(userId);
            logger.info("User {} added to abandoned users list for game {}", userId, this.id);
        }
    }

    @Schema(description = "Get the list of users who have abandoned the game")
    public List<String> fetchAbandonedUsers() {
        return abandonedUsers;
    }

    public void setAbandonedUsers(List<String> abandonedUsers) {
        this.abandonedUsers = abandonedUsers;
    }

    @Schema(description = "Check if a user has abandoned the game")
    public boolean hasUserAbandoned(String userId) {
        return abandonedUsers.contains(userId);
    }

    @Schema(description = "Start the game if enough players have joined")
    public void startGame() {
        if (players.size() >= getMinPlayers() && status == GameStatus.WAITING) {
            status = GameStatus.ACTIVE;
            startedAt = new Date();

            if (gameState == null) {
                gameState = new ConcurrentHashMap<>();
            }

            initializeGame();
            logger.info("Game {} started with {} players", this.id, players.size());
        } else {
            throw new GameException("Cannot start the game");
        }
    }

    @Schema(description = "End the game and set its status to FINISHED")
    public void endGame() {
        status = GameStatus.FINISHED;
        endedAt = new Date();
        logger.info("Game {} ended", this.id);
    }

    public String getId() {
        return id;
    }

    public long getGameDefinitionId() {
        return gameDefinitionId;
    }

    public void setGameDefinitionId(long gameDefinitionId) {
        this.gameDefinitionId = gameDefinitionId;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public List<Player> getPlayers() {
        return players;
    }

    public GameStatus getStatus() {
        return status;
    }

    public void setStatus(GameStatus status) {
        this.status = status;
    }

    public Date getCreatedAt() {
        return createdAt;
    }

    public Date getStartedAt() {
        return startedAt;
    }

    public Date getEndedAt() {
        return endedAt;
    }

    public Player getCurrentPlayer() {
        return currentPlayer;
    }

    public void setCurrentPlayer(Player currentPlayer) {
        this.currentPlayer = currentPlayer;
    }

    public boolean isTrackStatistics() {
        return trackStatistics;
    }

    public void setTrackStatistics(boolean trackStatistics) {
        this.trackStatistics = trackStatistics;
    }

    public Map<String, Object> getGameState() {
        if (gameState == null) {
            gameState = new ConcurrentHashMap<>();
        }
        return gameState;
    }

    @Schema(description = "Set a value in the game state")
    public void setGameState(String key, Object value) {
        if (gameState == null) {
            gameState = new ConcurrentHashMap<>();
        }

        if (value == null) {
            this.gameState.remove(key);
            logger.debug("Removed key {} from game state for game {}", key, this.id);
        } else {
            if (value instanceof List) {
                this.gameState.put(key, new ArrayList<>((List<?>) value));
            } else if (value instanceof Map) {
                this.gameState.put(key, new HashMap<>((Map<?, ?>) value));
            } else {
                this.gameState.put(key, value);
            }
            logger.debug("Set game state for game {}: {} = {}", this.id, key, value);
        }
    }

    @Schema(description = "Get a value from the game state")
    public Object getGameState(String key) {
        if (gameState == null) {
            return null;
        }
        return this.gameState.get(key);
    }

    @Schema(description = "Check if a key exists in the game state")
    public boolean hasGameState(String key) {
        return gameState != null && gameState.containsKey(key);
    }

    @Schema(description = "Remove a key from the game state")
    public void removeGameState(String key) {
        if (gameState != null) {
            gameState.remove(key);
            logger.debug("Removed key {} from game state for game {}", key, this.id);
        }
    }

    @PrePersist
    @PreUpdate
    public void serializeGameState() {
        try {
            if (this.gameState != null && !this.gameState.isEmpty()) {
                this.serializedGameState = objectMapper.writeValueAsString(this.gameState);
                logger.debug("Serialized game state for game {}: {}", this.id, this.serializedGameState);
            } else {
                this.serializedGameState = "{}";
                logger.debug("Serialized empty game state for game {}", this.id);
            }
        } catch (JsonProcessingException e) {
            logger.error("Error serializing game state for game {}: {}", this.id, e.getMessage());
            throw new RuntimeException("Error serializing game state", e);
        }
    }

    @PostLoad
    public void deserializeGameState() {
        try {
            if (this.serializedGameState != null && !this.serializedGameState.isEmpty()) {
                this.gameState = objectMapper.readValue(this.serializedGameState, Map.class);
                logger.debug("Deserialized game state for game {}: {}", this.id, this.gameState);

                processCommonGameStateObjects();

                processGameStateObjects();
            } else {
                this.gameState = new ConcurrentHashMap<>();
                logger.debug("Initialized empty game state for game {}", this.id);
            }
        } catch (JsonProcessingException e) {
            logger.error("Error deserializing game state for game {}: {}", this.id, e.getMessage());
            throw new RuntimeException("Error deserializing game state", e);
        }
    }

    private void processCommonGameStateObjects() {
        if (this.gameState.containsKey("deck")) {
            Object deckObj = this.gameState.get("deck");
            if (deckObj instanceof Map) {
                @SuppressWarnings("unchecked")
                Map<String, Object> deckMap = (Map<String, Object>) deckObj;
                Deck deck = Deck.fromMap(deckMap);
                this.gameState.put("deck", deck);
                logger.debug("Converted deck map to Deck object for game {}", this.id);
            }
        }
    }

    @Schema(description = "Process game-specific state objects after deserialization")
    protected void processGameStateObjects() {
    }
}
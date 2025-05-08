package hu.benkototh.cardgame.backend.game.model;

import hu.benkototh.cardgame.backend.game.exception.GameException;

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
public abstract class CardGame {
    private static final Logger logger = LoggerFactory.getLogger(CardGame.class);
    private static final ObjectMapper objectMapper = new ObjectMapper();

    @Id
    private String id;

    private long gameDefinitionId;

    private String name;

    @OneToMany(cascade = CascadeType.ALL, orphanRemoval = true)
    @JoinColumn(name = "game_id"
            , foreignKey = @ForeignKey(name = "fk_game_player",
            foreignKeyDefinition = "FOREIGN KEY (game_id) REFERENCES card_games(id) ON DELETE CASCADE"))
    @JsonManagedReference
    private List<Player> players;

    @ElementCollection
    @CollectionTable(
            name = "abandoned_users",
            joinColumns = @JoinColumn(name = "game_id")
    )
    @Column(name = "user_id")
    private List<String> abandonedUsers = new ArrayList<>();

    @Enumerated(EnumType.STRING)
    private GameStatus status;

    @Temporal(TemporalType.TIMESTAMP)
    private Date createdAt;

    @Temporal(TemporalType.TIMESTAMP)
    private Date startedAt;

    @Temporal(TemporalType.TIMESTAMP)
    private Date endedAt;

    @ManyToOne
    @JoinColumn(name = "current_player_id",
            foreignKey = @ForeignKey(name = "FK_CURRENT_PLAYER",
                    foreignKeyDefinition = "FOREIGN KEY (current_player_id) REFERENCES players(id) ON DELETE SET NULL"))
    private Player currentPlayer;

    private boolean trackStatistics;

    @Transient
    private Map<String, Object> gameState;

    @Column(columnDefinition = "TEXT")
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

    public abstract void initializeGame();
    public abstract boolean isValidMove(String playerId, GameAction action);
    public abstract void executeMove(String playerId, GameAction action);
    public abstract boolean isGameOver();
    public abstract Map<String, Integer> calculateScores();
    public abstract int getMinPlayers();
    public abstract int getMaxPlayers();

    public void addPlayer(Player player) {
        if (players.size() < getMaxPlayers() && status == GameStatus.WAITING) {
            players.add(player);
            logger.debug("Added player {} to game {}", player.getId(), this.id);
        } else {
            throw new GameException("Cannot add player to the game");
        }
    }

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

    public void addAbandonedUser(String userId) {
        if (!abandonedUsers.contains(userId)) {
            abandonedUsers.add(userId);
            logger.info("User {} added to abandoned users list for game {}", userId, this.id);
        }
    }

    public List<String> getAbandonedUsers() {
        return abandonedUsers;
    }

    public void setAbandonedUsers(List<String> abandonedUsers) {
        this.abandonedUsers = abandonedUsers;
    }

    public boolean hasUserAbandoned(String userId) {
        return abandonedUsers.contains(userId);
    }

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

    public Object getGameState(String key) {
        if (gameState == null) {
            return null;
        }
        return this.gameState.get(key);
    }

    public boolean hasGameState(String key) {
        return gameState != null && gameState.containsKey(key);
    }

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

    protected void processGameStateObjects() {
    }
}

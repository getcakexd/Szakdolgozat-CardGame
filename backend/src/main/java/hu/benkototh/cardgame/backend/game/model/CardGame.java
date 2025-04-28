package hu.benkototh.cardgame.backend.game.model;

import hu.benkototh.cardgame.backend.game.exception.GameException;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

public abstract class CardGame {
    private String id;
    private long gameDefinitionId;
    private String name;
    private List<Player> players;
    private GameStatus status;
    private Date createdAt;
    private Date startedAt;
    private Date endedAt;
    private Player currentPlayer;
    private boolean trackStatistics;
    private Map<String, Object> gameState;

    public CardGame() {
        this.id = UUID.randomUUID().toString();
        this.players = new ArrayList<>();
        this.status = GameStatus.WAITING;
        this.createdAt = new Date();
        this.gameState = new ConcurrentHashMap<>();
        this.trackStatistics = true;
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
        } else {
            throw new GameException("Cannot add player to the game");
        }
    }

    public void removePlayer(String playerId) {
        players.removeIf(player -> player.getId().equals(playerId));
    }

    public void startGame() {
        if (players.size() >= getMinPlayers() && status == GameStatus.WAITING) {
            status = GameStatus.ACTIVE;
            startedAt = new Date();

            if (gameState == null) {
                gameState = new ConcurrentHashMap<>();
            }

            initializeGame();
        } else {
            throw new GameException("Cannot start the game");
        }
    }

    public void endGame() {
        status = GameStatus.FINISHED;
        endedAt = new Date();
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
        return gameState;
    }

    public void setGameState(String key, Object value) {
        this.gameState.put(key, value);
    }

    public Object getGameState(String key) {
        return this.gameState.get(key);
    }
}
package hu.benkototh.cardgame.backend.game.model;

import java.util.Date;
import java.util.HashMap;
import java.util.Map;

public class GameEvent {
    private String eventType;
    private String gameId;
    private String playerId;
    private Date timestamp;
    private Map<String, Object> data;

    public GameEvent() {
        this.timestamp = new Date();
        this.data = new HashMap<>();
    }

    public GameEvent(String eventType, String gameId) {
        this();
        this.eventType = eventType;
        this.gameId = gameId;
    }

    public GameEvent(String eventType, String gameId, String playerId) {
        this(eventType, gameId);
        this.playerId = playerId;
    }

    public String getEventType() {
        return eventType;
    }

    public void setEventType(String eventType) {
        this.eventType = eventType;
    }

    public String getGameId() {
        return gameId;
    }

    public void setGameId(String gameId) {
        this.gameId = gameId;
    }

    public String getPlayerId() {
        return playerId;
    }

    public void setPlayerId(String playerId) {
        this.playerId = playerId;
    }

    public Date getTimestamp() {
        return timestamp;
    }

    public Map<String, Object> getData() {
        return data;
    }

    public void addData(String key, Object value) {
        this.data.put(key, value);
    }

    public Object getData(String key) {
        return this.data.get(key);
    }
}
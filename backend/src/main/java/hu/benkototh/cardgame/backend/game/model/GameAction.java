package hu.benkototh.cardgame.backend.game.model;

import java.util.HashMap;
import java.util.Map;

public class GameAction {
    private String actionType;
    private Map<String, Object> parameters;

    public GameAction() {
        this.parameters = new HashMap<>();
    }

    public GameAction(String actionType) {
        this.actionType = actionType;
        this.parameters = new HashMap<>();
    }

    public String getActionType() {
        return actionType;
    }

    public void setActionType(String actionType) {
        this.actionType = actionType;
    }

    public Map<String, Object> getParameters() {
        return parameters;
    }

    public void setParameters(Map<String, Object> parameters) {
        this.parameters = parameters;
    }

    public void addParameter(String key, Object value) {
        this.parameters.put(key, value);
    }

    public Object getParameter(String key) {
        return this.parameters.get(key);
    }

    public Card getCardParameter(String key) {
        return (Card) this.parameters.get(key);
    }

    public Integer getIntParameter(String key) {
        return (Integer) this.parameters.get(key);
    }

    public String getStringParameter(String key) {
        return (String) this.parameters.get(key);
    }

    public Boolean getBooleanParameter(String key) {
        return (Boolean) this.parameters.get(key);
    }
}
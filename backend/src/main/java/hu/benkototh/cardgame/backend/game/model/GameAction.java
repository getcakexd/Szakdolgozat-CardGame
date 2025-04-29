package hu.benkototh.cardgame.backend.game.model;

import java.util.HashMap;
import java.util.Map;
import com.fasterxml.jackson.annotation.JsonIgnore;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class GameAction {
    private static final Logger logger = LoggerFactory.getLogger(GameAction.class);

    private String actionType;
    private Map<String, Object> parameters;

    public GameAction() {
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

    @JsonIgnore
    public Card getCardParameter(String key) {
        Object value = parameters.get(key);

        if (value instanceof Card) {
            return (Card) value;
        }

        if (value instanceof Map) {
            try {
                @SuppressWarnings("unchecked")
                Map<String, Object> cardMap = (Map<String, Object>) value;
                Card card = Card.fromMap(cardMap);
                if (card != null) {
                    parameters.put(key, card);
                    return card;
                }
            } catch (Exception e) {
                logger.error("Error converting card parameter to Card: {}", e.getMessage());
            }
        }

        return null;
    }

    @Override
    public String toString() {
        return "GameAction{" +
                "actionType='" + actionType + '\'' +
                ", parameters=" + parameters +
                '}';
    }
}

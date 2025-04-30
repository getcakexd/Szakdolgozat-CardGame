package hu.benkototh.cardgame.backend.websocket.handler;

import hu.benkototh.cardgame.backend.game.controller.CardGameController;
import hu.benkototh.cardgame.backend.game.model.Card;
import hu.benkototh.cardgame.backend.game.model.GameAction;
import hu.benkototh.cardgame.backend.game.model.Rank;
import hu.benkototh.cardgame.backend.game.model.Suit;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.messaging.simp.SimpMessageHeaderAccessor;
import org.springframework.stereotype.Controller;

import java.util.Map;

@Controller
public class CardGameWebSocketHandler {

    @Autowired
    private CardGameController cardGameController;

    @MessageMapping("/game.action")
    public void executeGameActionWebSocket(@Payload Map<String, Object> payload) {
        try {
            String gameId = (String) payload.get("gameId");
            String userId = (String) payload.get("userId");

            @SuppressWarnings("unchecked")
            Map<String, Object> actionMap = (Map<String, Object>) payload.get("action");

            if (gameId == null || userId == null || actionMap == null) {
                return;
            }

            String actionType = (String) actionMap.get("actionType");
            @SuppressWarnings("unchecked")
            Map<String, Object> parameters = (Map<String, Object>) actionMap.get("parameters");

            if (actionType == null || parameters == null) {
                return;
            }

            GameAction action = new GameAction();
            action.setActionType(actionType);

            if (parameters.containsKey("card")) {
                @SuppressWarnings("unchecked")
                Map<String, Object> cardMap = (Map<String, Object>) parameters.get("card");

                if (cardMap != null) {
                    String suitStr = (String) cardMap.get("suit");
                    String rankStr = (String) cardMap.get("rank");

                    if (suitStr != null && rankStr != null) {
                        Suit suit = Suit.valueOf(suitStr);
                        Rank rank = Rank.valueOf(rankStr);
                        Card card = new Card(suit, rank);

                        action.addParameter("card", card);
                    }
                }
            }

            cardGameController.executeGameAction(gameId, userId, action);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @MessageMapping("/game.join")
    public void joinGameWebSocket(
            @Payload Map<String, String> payload,
            SimpMessageHeaderAccessor headerAccessor) {
        String gameId = payload.get("gameId");
        String userId = payload.get("userId");

        headerAccessor.getSessionAttributes().put("gameId", gameId);
        headerAccessor.getSessionAttributes().put("userId", userId);

        cardGameController.joinGame(gameId, userId);
    }

    @MessageMapping("/game.leave")
    public void leaveGameWebSocket(@Payload Map<String, String> payload) {
        String gameId = payload.get("gameId");
        String userId = payload.get("userId");

        cardGameController.leaveGame(gameId, userId);
    }

    @MessageMapping("/game.abandon")
    public void abandonGameWebSocket(@Payload Map<String, String> payload) {
        String gameId = payload.get("gameId");
        String userId = payload.get("userId");

        cardGameController.abandonGame(gameId, userId);
    }

    @MessageMapping("/game.start")
    public void startGameWebSocket(@Payload Map<String, String> payload) {
        String gameId = payload.get("gameId");
        String userId = payload.get("userId");

        cardGameController.startGame(gameId, userId);
    }
}

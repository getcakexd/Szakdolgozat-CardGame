package hu.benkototh.cardgame.backend.websocket.handler;

import hu.benkototh.cardgame.backend.game.controller.CardGameController;
import hu.benkototh.cardgame.backend.game.model.*;
import hu.benkototh.cardgame.backend.game.service.GameTimeoutService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.messaging.simp.SimpMessageHeaderAccessor;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Controller;
import org.springframework.transaction.annotation.Transactional;

import java.util.Map;

@Controller
public class CardGameWebSocketHandler {

    @Autowired
    private CardGameController cardGameController;

    @Autowired
    private GameTimeoutService gameTimeoutService;

    @Autowired
    SimpMessagingTemplate messagingTemplate;

    @MessageMapping("/game.action")
    @Transactional
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

            CardGame game = cardGameController.executeGameAction(gameId, userId, action);

            messagingTemplate.convertAndSend(
                    "/topic/game/" + game.getId(),
                    game
            );

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @MessageMapping("/game.join")
    @Transactional
    public void joinGameWebSocket(
            @Payload Map<String, String> payload,
            SimpMessageHeaderAccessor headerAccessor) {
        String gameId = payload.get("gameId");
        String userId = payload.get("userId");

        headerAccessor.getSessionAttributes().put("gameId", gameId);
        headerAccessor.getSessionAttributes().put("userId", userId);

        CardGame game = cardGameController.joinGame(gameId, userId);
        gameTimeoutService.recordActivity(gameId, userId);
        messagingTemplate.convertAndSend(
                "/topic/game/" + game.getId(),
                game
        );
    }

    @MessageMapping("/game.leave")
    @Transactional
    public void leaveGameWebSocket(@Payload Map<String, String> payload) {
        String gameId = payload.get("gameId");
        String userId = payload.get("userId");

        CardGame game = cardGameController.leaveGame(gameId, userId);
        messagingTemplate.convertAndSend(
                "/topic/game/" + gameId,
                game
        );
    }

    @MessageMapping("/game.abandon")
    @Transactional
    public void abandonGameWebSocket(@Payload Map<String, String> payload) {
        String gameId = payload.get("gameId");
        String userId = payload.get("userId");

        CardGame game = cardGameController.abandonGame(gameId, userId);
        messagingTemplate.convertAndSend(
                "/topic/game/" + gameId,
                game
        );
    }

    @MessageMapping("/game.start")
    @Transactional
    public void startGameWebSocket(@Payload Map<String, String> payload) {
        String gameId = payload.get("gameId");
        String userId = payload.get("userId");

        CardGame game = cardGameController.startGame(gameId, userId);
        messagingTemplate.convertAndSend(
                "/topic/game/" + gameId,
                game
        );
    }
}

package hu.benkototh.cardgame.backend.websocket.handler;

import hu.benkototh.cardgame.backend.game.controller.CardGameController;
import hu.benkototh.cardgame.backend.game.model.GameAction;
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
    public void processGameAction(@Payload GameAction action, SimpMessageHeaderAccessor headerAccessor) {
        String gameId = (String) headerAccessor.getSessionAttributes().get("gameId");
        String userId = (String) headerAccessor.getSessionAttributes().get("userId");

        cardGameController.executeGameAction(gameId, userId, action);
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

    @MessageMapping("/game.start")
    public void startGameWebSocket(@Payload Map<String, String> payload) {
        String gameId = payload.get("gameId");
        String userId = payload.get("userId");

        cardGameController.startGame(gameId, userId);
    }

    @MessageMapping("/game.message")
    public void sendGameMessage(@Payload Map<String, Object> payload) {
        String gameId = (String) payload.get("gameId");
        String userId = (String) payload.get("userId");
        String messageType = (String) payload.get("messageType");
        String content = (String) payload.get("content");

        cardGameController.sendGameMessage(gameId, userId, messageType, content);
    }
}
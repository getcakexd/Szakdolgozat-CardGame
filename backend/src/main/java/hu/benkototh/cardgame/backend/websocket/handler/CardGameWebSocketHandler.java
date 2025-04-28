package hu.benkototh.cardgame.backend.websocket.handler;

import hu.benkototh.cardgame.backend.game.model.CardGame;
import hu.benkototh.cardgame.backend.game.model.GameAction;
import hu.benkototh.cardgame.backend.game.service.CardGameService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.messaging.simp.SimpMessageHeaderAccessor;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Controller;

import java.util.Map;

@Controller
public class CardGameWebSocketHandler {

    @Autowired
    private SimpMessagingTemplate messagingTemplate;

    @Autowired
    private CardGameService cardGameService;

    @MessageMapping("/cardgame.join")
    public void joinGame(@Payload Map<String, String> payload, SimpMessageHeaderAccessor headerAccessor) {
        String gameId = payload.get("gameId");
        String userId = payload.get("userId");

        headerAccessor.getSessionAttributes().put("gameId", gameId);
        headerAccessor.getSessionAttributes().put("userId", userId);

        CardGame game = cardGameService.joinGame(gameId, userId);

        messagingTemplate.convertAndSendToUser(
                userId,
                "/queue/cardgame",
                game
        );

        messagingTemplate.convertAndSend(
                "/topic/cardgame/" + gameId,
                Map.of("type", "PLAYER_JOINED", "playerId", userId, "game", game)
        );
    }

    @MessageMapping("/cardgame.leave")
    public void leaveGame(@Payload Map<String, String> payload) {
        String gameId = payload.get("gameId");
        String userId = payload.get("userId");

        CardGame game = cardGameService.leaveGame(gameId, userId);

        messagingTemplate.convertAndSend(
                "/topic/cardgame/" + gameId,
                Map.of("type", "PLAYER_LEFT", "playerId", userId, "game", game)
        );
    }

    @MessageMapping("/cardgame.start")
    public void startGame(@Payload Map<String, String> payload) {
        String gameId = payload.get("gameId");
        String userId = payload.get("userId");

        CardGame game = cardGameService.startGame(gameId, userId);

        messagingTemplate.convertAndSend(
                "/topic/cardgame/" + gameId,
                Map.of("type", "GAME_STARTED", "game", game)
        );
    }

    @MessageMapping("/cardgame.action")
    public void executeAction(@Payload Map<String, Object> payload) {
        String gameId = (String) payload.get("gameId");
        String userId = (String) payload.get("userId");
        Map<String, Object> actionData = (Map<String, Object>) payload.get("action");

        GameAction action = new GameAction((String) actionData.get("actionType"));

        if (actionData.containsKey("parameters")) {
            Map<String, Object> parameters = (Map<String, Object>) actionData.get("parameters");
            for (Map.Entry<String, Object> entry : parameters.entrySet()) {
                action.addParameter(entry.getKey(), entry.getValue());
            }
        }

        CardGame game = cardGameService.executeGameAction(gameId, userId, action);

        messagingTemplate.convertAndSend(
                "/topic/cardgame/" + gameId,
                Map.of("type", "GAME_ACTION", "playerId", userId, "action", action, "game", game)
        );
    }

    @MessageMapping("/cardgame.message")
    public void sendGameMessage(@Payload Map<String, Object> payload) {
        String gameId = (String) payload.get("gameId");
        String userId = (String) payload.get("userId");
        String messageType = (String) payload.get("messageType");
        String content = (String) payload.get("content");

        if (messageType != null && messageType.equals("PARTNER_MESSAGE")) {
            messagingTemplate.convertAndSend(
                    "/topic/cardgame/" + gameId,
                    Map.of("type", "PARTNER_MESSAGE", "playerId", userId, "content", content)
            );
        }
    }
}
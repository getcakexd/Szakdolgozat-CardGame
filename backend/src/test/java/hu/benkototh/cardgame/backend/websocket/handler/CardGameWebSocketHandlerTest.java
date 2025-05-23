package hu.benkototh.cardgame.backend.websocket.handler;

import hu.benkototh.cardgame.backend.game.controller.CardGameController;
import hu.benkototh.cardgame.backend.game.model.*;
import hu.benkototh.cardgame.backend.game.service.GameTimeoutService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.messaging.simp.SimpMessageHeaderAccessor;
import org.springframework.messaging.simp.SimpMessagingTemplate;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

class CardGameWebSocketHandlerTest {

    @Mock
    private CardGameController cardGameController;

    @Mock
    private GameTimeoutService gameTimeoutService;

    @Mock
    private SimpMessagingTemplate messagingTemplate;

    @Mock
    private SimpMessageHeaderAccessor headerAccessor;

    @InjectMocks
    private CardGameWebSocketHandler webSocketHandler;

    private CardGame testCardGame;
    private Map<String, Object> sessionAttributes;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        
        testCardGame = mock(CardGame.class);
        when(testCardGame.getId()).thenReturn("game-123");
        
        sessionAttributes = new HashMap<>();
        when(headerAccessor.getSessionAttributes()).thenReturn(sessionAttributes);
    }

    @Test
    void testExecuteGameActionWebSocket() {
        Map<String, Object> payload = new HashMap<>();
        payload.put("gameId", "game-123");
        payload.put("userId", "1");
        
        Map<String, Object> actionMap = new HashMap<>();
        actionMap.put("actionType", "playCard");
        
        Map<String, Object> parameters = new HashMap<>();
        
        Map<String, Object> cardMap = new HashMap<>();
        cardMap.put("suit", "HEARTS");
        cardMap.put("rank", "ACE");
        
        parameters.put("card", cardMap);
        actionMap.put("parameters", parameters);
        
        payload.put("action", actionMap);
        
        when(cardGameController.executeGameAction(anyString(), anyString(), any(GameAction.class)))
                .thenReturn(testCardGame);
        
        webSocketHandler.executeGameActionWebSocket(payload);
        
        verify(gameTimeoutService).recordActivity("game-123", "1");
        verify(cardGameController).executeGameAction(eq("game-123"), eq("1"), any(GameAction.class));
        verify(messagingTemplate).convertAndSend(eq("/topic/game/game-123"), eq(testCardGame));
    }

    @Test
    void testExecuteGameActionWebSocket_InvalidPayload() {
        Map<String, Object> payload = new HashMap<>();

        webSocketHandler.executeGameActionWebSocket(payload);
        
        verify(gameTimeoutService, never()).recordActivity(anyString(), anyString());
        verify(cardGameController, never()).executeGameAction(anyString(), anyString(), any(GameAction.class));
        verify(messagingTemplate, never()).convertAndSend(anyString(), Optional.ofNullable(any()));
    }

    @Test
    void testExecuteGameActionWebSocket_InvalidAction() {
        Map<String, Object> payload = new HashMap<>();
        payload.put("gameId", "game-123");
        payload.put("userId", "1");
        
        Map<String, Object> actionMap = new HashMap<>();

        payload.put("action", actionMap);
        
        webSocketHandler.executeGameActionWebSocket(payload);
        
        verify(gameTimeoutService, never()).recordActivity(anyString(), anyString());
        verify(cardGameController, never()).executeGameAction(anyString(), anyString(), any(GameAction.class));
        verify(messagingTemplate, never()).convertAndSend(anyString(), Optional.ofNullable(any()));
    }

    @Test
    void testJoinGameWebSocket() {
        Map<String, String> payload = new HashMap<>();
        payload.put("gameId", "game-123");
        payload.put("userId", "1");
        
        when(cardGameController.joinGame("game-123", "1")).thenReturn(testCardGame);
        
        webSocketHandler.joinGameWebSocket(payload, headerAccessor);
        
        assertEquals("game-123", sessionAttributes.get("gameId"));
        assertEquals("1", sessionAttributes.get("userId"));
        
        verify(gameTimeoutService).recordActivity("game-123", "1");
        verify(cardGameController).joinGame("game-123", "1");
        verify(messagingTemplate).convertAndSend("/topic/game/game-123", testCardGame);
    }

    @Test
    void testLeaveGameWebSocket() {
        Map<String, String> payload = new HashMap<>();
        payload.put("gameId", "game-123");
        payload.put("userId", "1");
        
        when(cardGameController.leaveGame("game-123", "1")).thenReturn(testCardGame);
        
        webSocketHandler.leaveGameWebSocket(payload);
        
        verify(cardGameController).leaveGame("game-123", "1");
        verify(messagingTemplate).convertAndSend("/topic/game/game-123", testCardGame);
    }

    @Test
    void testAbandonGameWebSocket() {
        Map<String, String> payload = new HashMap<>();
        payload.put("gameId", "game-123");
        payload.put("userId", "1");
        
        when(cardGameController.abandonGame("game-123", "1")).thenReturn(testCardGame);
        
        webSocketHandler.abandonGameWebSocket(payload);
        
        verify(cardGameController).abandonGame("game-123", "1");
        verify(messagingTemplate).convertAndSend("/topic/game/game-123", testCardGame);
    }

    @Test
    void testStartGameWebSocket() {
        Map<String, String> payload = new HashMap<>();
        payload.put("gameId", "game-123");
        payload.put("userId", "1");
        
        when(cardGameController.startGame("game-123", "1")).thenReturn(testCardGame);
        
        webSocketHandler.startGameWebSocket(payload);
        
        verify(gameTimeoutService).recordActivity("game-123", "1");
        verify(cardGameController).startGame("game-123", "1");
        verify(messagingTemplate).convertAndSend("/topic/game/game-123", testCardGame);
    }
}

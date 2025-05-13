package hu.benkototh.cardgame.backend.game.service;

import hu.benkototh.cardgame.backend.game.controller.CardGameController;
import hu.benkototh.cardgame.backend.game.model.CardGame;
import hu.benkototh.cardgame.backend.game.model.GameStatus;
import hu.benkototh.cardgame.backend.game.model.Player;
import hu.benkototh.cardgame.backend.game.repository.ICardGameRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.context.event.ContextRefreshedEvent;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.test.util.ReflectionTestUtils;

import java.time.Instant;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

class GameTimeoutServiceTest {

    @Mock
    private ICardGameRepository cardGameRepository;

    @Mock
    private CardGameController cardGameController;

    @Mock
    private SimpMessagingTemplate messagingTemplate;

    @Mock
    private ContextRefreshedEvent contextRefreshedEvent;

    @InjectMocks
    private GameTimeoutService gameTimeoutService;

    private CardGame testCardGame;
    private Player player1;
    private Player player2;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);

        ReflectionTestUtils.setField(gameTimeoutService, "timeoutMinutes", 5L);
        ReflectionTestUtils.setField(gameTimeoutService, "checkIntervalMs", 30000L);

        testCardGame = mock(CardGame.class);
        when(testCardGame.getId()).thenReturn("game-123");
        when(testCardGame.getStatus()).thenReturn(GameStatus.ACTIVE);

        player1 = mock(Player.class);
        when(player1.getId()).thenReturn("1");

        player2 = mock(Player.class);
        when(player2.getId()).thenReturn("2");

        List<Player> players = new ArrayList<>();
        players.add(player1);
        players.add(player2);

        when(testCardGame.getPlayers()).thenReturn(players);
        when(testCardGame.getCurrentPlayer()).thenReturn(player1);

        List<CardGame> activeGames = new ArrayList<>();
        activeGames.add(testCardGame);
        when(cardGameRepository.findByStatus(GameStatus.ACTIVE)).thenReturn(activeGames);

        when(cardGameController.getGame("game-123")).thenReturn(testCardGame);
    }

    @Test
    void testOnApplicationEvent() {
        gameTimeoutService.onApplicationEvent(contextRefreshedEvent);

        verify(cardGameRepository).findByStatus(GameStatus.ACTIVE);
    }

    @Test
    void testInitializeActivityTracking() {
        gameTimeoutService.initializeActivityTracking();

        verify(cardGameRepository).findByStatus(GameStatus.ACTIVE);

        Map<String, Map<String, Instant>> gamePlayerActivityMap =
                (Map<String, Map<String, Instant>>) ReflectionTestUtils.getField(gameTimeoutService, "gamePlayerActivityMap");

        assertNotNull(gamePlayerActivityMap);
        assertTrue(gamePlayerActivityMap.containsKey("game-123"));
        assertTrue(gamePlayerActivityMap.get("game-123").containsKey("1"));
        assertTrue(gamePlayerActivityMap.get("game-123").containsKey("2"));
    }

    @Test
    void testRecordActivity() {
        Map<String, Map<String, Instant>> gamePlayerActivityMap = new ConcurrentHashMap<>();
        ReflectionTestUtils.setField(gameTimeoutService, "gamePlayerActivityMap", gamePlayerActivityMap);

        gameTimeoutService.recordActivity("game-123", "1");

        assertTrue(gamePlayerActivityMap.containsKey("game-123"));
        assertTrue(gamePlayerActivityMap.get("game-123").containsKey("1"));
        assertNotNull(gamePlayerActivityMap.get("game-123").get("1"));
    }

    @Test
    void testRecordActivity_NullInput() {
        Map<String, Map<String, Instant>> gamePlayerActivityMap = new ConcurrentHashMap<>();
        ReflectionTestUtils.setField(gameTimeoutService, "gamePlayerActivityMap", gamePlayerActivityMap);

        gameTimeoutService.recordActivity(null, "1");
        gameTimeoutService.recordActivity("game-123", null);

        assertFalse(gamePlayerActivityMap.containsKey("game-123"));
    }

    @Test
    void testCheckGameForTimeout_NoActivity() {
        Map<String, Map<String, Instant>> gamePlayerActivityMap = new ConcurrentHashMap<>();
        ReflectionTestUtils.setField(gameTimeoutService, "gamePlayerActivityMap", gamePlayerActivityMap);

        gameTimeoutService.checkGameForTimeout(testCardGame, Instant.now());

        assertTrue(gamePlayerActivityMap.containsKey("game-123"));
        assertTrue(gamePlayerActivityMap.get("game-123").containsKey("1"));
        assertTrue(gamePlayerActivityMap.get("game-123").containsKey("2"));
    }

    @Test
    void testCheckGameForTimeout_WithActivity_NotTimedOut() {
        Map<String, Map<String, Instant>> gamePlayerActivityMap = new ConcurrentHashMap<>();
        Map<String, Instant> playerMap = new ConcurrentHashMap<>();
        playerMap.put("1", Instant.now());
        gamePlayerActivityMap.put("game-123", playerMap);

        ReflectionTestUtils.setField(gameTimeoutService, "gamePlayerActivityMap", gamePlayerActivityMap);

        gameTimeoutService.checkGameForTimeout(testCardGame, Instant.now());

        verify(cardGameController, never()).abandonGame(anyString(), anyString());
    }

    @Test
    void testCheckGameForTimeout_WithActivity_TimedOut() {
        Map<String, Map<String, Instant>> gamePlayerActivityMap = new ConcurrentHashMap<>();
        Map<String, Instant> playerMap = new ConcurrentHashMap<>();

        Instant sixMinutesAgo = Instant.now().minusSeconds(360);
        playerMap.put("1", sixMinutesAgo);
        gamePlayerActivityMap.put("game-123", playerMap);

        ReflectionTestUtils.setField(gameTimeoutService, "gamePlayerActivityMap", gamePlayerActivityMap);

        gameTimeoutService.checkGameForTimeout(testCardGame, Instant.now());

        verify(cardGameController).abandonGame("game-123", "1");
    }

    @Test
    void testAbandonGameForInactivePlayer() {
        when(cardGameController.abandonGame("game-123", "1")).thenReturn(testCardGame);

        gameTimeoutService.abandonGameForInactivePlayer("game-123", "1");

        verify(cardGameController).abandonGame("game-123", "1");
        verify(messagingTemplate).convertAndSend(eq("/topic/game/game-123"), eq(testCardGame));
    }

    @Test
    void testAbandonGameForInactivePlayer_GameNotFound() {
        when(cardGameController.getGame("game-456")).thenReturn(null);

        gameTimeoutService.abandonGameForInactivePlayer("game-456", "1");

        verify(cardGameController, never()).abandonGame(anyString(), anyString());
        verify(messagingTemplate, never()).convertAndSend(anyString(), Optional.ofNullable(any()));
    }

    @Test
    void testAbandonGameForInactivePlayer_GameNotActive() {
        CardGame finishedGame = mock(CardGame.class);
        when(finishedGame.getId()).thenReturn("game-123");
        when(finishedGame.getStatus()).thenReturn(GameStatus.FINISHED);

        when(cardGameController.getGame("game-123")).thenReturn(finishedGame);

        gameTimeoutService.abandonGameForInactivePlayer("game-123", "1");

        verify(cardGameController, never()).abandonGame(anyString(), anyString());
        verify(messagingTemplate, never()).convertAndSend(anyString(), Optional.ofNullable(any()));
    }

    @Test
    void testAbandonGameForInactivePlayer_CurrentPlayerChanged() {
        CardGame gameWithDifferentCurrentPlayer = mock(CardGame.class);
        when(gameWithDifferentCurrentPlayer.getId()).thenReturn("game-123");
        when(gameWithDifferentCurrentPlayer.getStatus()).thenReturn(GameStatus.ACTIVE);
        when(gameWithDifferentCurrentPlayer.getCurrentPlayer()).thenReturn(player2);

        when(cardGameController.getGame("game-123")).thenReturn(gameWithDifferentCurrentPlayer);

        gameTimeoutService.abandonGameForInactivePlayer("game-123", "1");

        verify(cardGameController, never()).abandonGame(anyString(), anyString());
        verify(messagingTemplate, never()).convertAndSend(Optional.of(anyString()), any());
    }

    @Test
    void testCleanupFinishedGames() {
        Map<String, Map<String, Instant>> gamePlayerActivityMap = new ConcurrentHashMap<>();
        Map<String, Instant> playerMap1 = new ConcurrentHashMap<>();
        playerMap1.put("1", Instant.now());
        gamePlayerActivityMap.put("game-123", playerMap1);

        Map<String, Instant> playerMap2 = new ConcurrentHashMap<>();
        playerMap2.put("2", Instant.now());
        gamePlayerActivityMap.put("game-456", playerMap2);

        ReflectionTestUtils.setField(gameTimeoutService, "gamePlayerActivityMap", gamePlayerActivityMap);

        when(cardGameController.getGame("game-123")).thenReturn(testCardGame);

        CardGame finishedGame = mock(CardGame.class);
        when(finishedGame.getId()).thenReturn("game-456");
        when(finishedGame.getStatus()).thenReturn(GameStatus.FINISHED);
        when(cardGameController.getGame("game-456")).thenReturn(finishedGame);

        ReflectionTestUtils.invokeMethod(gameTimeoutService, "cleanupFinishedGames");

        assertFalse(gamePlayerActivityMap.containsKey("game-456"));
    }
}

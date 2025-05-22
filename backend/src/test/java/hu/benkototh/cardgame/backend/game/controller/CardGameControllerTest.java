package hu.benkototh.cardgame.backend.game.controller;

import hu.benkototh.cardgame.backend.game.exception.GameException;
import hu.benkototh.cardgame.backend.game.model.*;
import hu.benkototh.cardgame.backend.game.repository.ICardGameRepository;
import hu.benkototh.cardgame.backend.game.service.GameFactory;
import hu.benkototh.cardgame.backend.game.service.GameTimeoutService;
import hu.benkototh.cardgame.backend.rest.model.Game;
import hu.benkototh.cardgame.backend.rest.model.User;
import hu.benkototh.cardgame.backend.rest.controller.AuditLogController;
import hu.benkototh.cardgame.backend.rest.controller.LobbyController;
import hu.benkototh.cardgame.backend.rest.controller.UserController;
import hu.benkototh.cardgame.backend.rest.repository.IGameRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.mockito.MockedStatic;
import org.springframework.messaging.simp.SimpMessagingTemplate;

import java.util.*;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;
import static org.mockito.Mockito.mockStatic;

class CardGameControllerTest {

    @Mock
    private SimpMessagingTemplate messagingTemplate;

    @Mock
    private UserController userController;

    @Mock
    private ICardGameRepository cardGameRepository;

    @Mock
    private IGameRepository gameRepository;

    @Mock
    private StatisticsController statisticsController;

    @Mock
    private LobbyController lobbyController;

    @Mock
    private AuditLogController auditLogController;

    @Mock
    private GameTimeoutService gameTimeoutService;

    @InjectMocks
    private CardGameController cardGameController;

    private User testUser;
    private Game testGameDefinition;
    private CardGame testCardGame;

    private static class TestCardGame extends CardGame {
        @Override
        public void initializeGame() {}

        @Override
        public boolean isValidMove(String playerId, GameAction action) {
            return true;
        }

        @Override
        public void executeMove(String playerId, GameAction action) {}

        @Override
        public boolean isGameOver() {
            return false;
        }

        @Override
        public Map<String, Integer> calculateScores() {
            Map<String, Integer> scores = new HashMap<>();
            for (Player player : getPlayers()) {
                scores.put(player.getId(), player.getScore());
            }
            return scores;
        }

        @Override
        public int getMinPlayers() {
            return 2;
        }

        @Override
        public int getMaxPlayers() {
            return 4;
        }
    }

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);

        testUser = new User();
        testUser.setId(1L);
        testUser.setUsername("testuser");

        testGameDefinition = new Game();
        testGameDefinition.setId(1L);
        testGameDefinition.setName("Test Game");
        testGameDefinition.setFactorySign("TEST");
        testGameDefinition.setFactoryId(1);
        testGameDefinition.setMinPlayers(2);
        testGameDefinition.setMaxPlayers(4);

        testCardGame = new TestCardGame();
        testCardGame.setGameDefinitionId(1L);
        testCardGame.setName("Test Card Game");

        when(userController.getUser(1L)).thenReturn(testUser);
        when(gameRepository.findById(1L)).thenReturn(Optional.of(testGameDefinition));
        when(cardGameRepository.save(any(CardGame.class))).thenAnswer(invocation -> invocation.getArgument(0));
    }

    @Test
    void testCreateCardGame() {
        when(gameRepository.findById(1L)).thenReturn(Optional.of(testGameDefinition));
        when(userController.getUser(1L)).thenReturn(testUser);

        try (MockedStatic<GameFactory> mockedFactory = mockStatic(GameFactory.class)) {
            mockedFactory.when(() -> GameFactory.createGame(anyString(), anyInt())).thenReturn(testCardGame);

            CardGame result = cardGameController.createCardGame(1L, "1", "", true);

            assertNotNull(result);
            assertEquals(testCardGame, result);

            verify(cardGameRepository).save(testCardGame);
            verify(auditLogController).logAction(eq("GAME_CREATED"), eq(1L), anyString());
        }
    }

    @Test
    void testCreateCardGame_GameDefinitionNotFound() {
        when(gameRepository.findById(2L)).thenReturn(Optional.empty());

        assertThrows(GameException.class, () -> cardGameController.createCardGame(2L, "1", "Test Game", true));

        verify(cardGameRepository, never()).save(any(CardGame.class));
    }

    @Test
    void testCreateCardGame_UserNotFound() {
        when(userController.getUser(2L)).thenReturn(null);

        assertThrows(GameException.class, () -> cardGameController.createCardGame(1L, "2", "Test Game", true));

        verify(cardGameRepository, never()).save(any(CardGame.class));
    }

    @Test
    void testJoinGame() {
        when(cardGameRepository.findById("game-123")).thenReturn(Optional.of(testCardGame));

        CardGame result = cardGameController.joinGame("game-123", "1");

        assertNotNull(result);
        assertEquals(1, result.getPlayers().size());
        assertEquals("1", result.getPlayers().get(0).getId());
        assertEquals("testuser", result.getPlayers().get(0).getUsername());

        verify(cardGameRepository).save(any(CardGame.class));
        verify(auditLogController).logAction(eq("PLAYER_JOINED"), eq(1L), anyString());
        verify(messagingTemplate).convertAndSend(eq("/topic/game/" + result.getId()), any(GameEvent.class));
    }

    @Test
    void testJoinGame_GameNotFound() {
        when(cardGameRepository.findById("game-456")).thenReturn(Optional.empty());

        assertThrows(GameException.class, () -> cardGameController.joinGame("game-456", "1"));

        verify(cardGameRepository, never()).save(any(CardGame.class));
    }

    @Test
    void testJoinGame_UserNotFound() {
        when(cardGameRepository.findById("game-123")).thenReturn(Optional.of(testCardGame));
        when(userController.getUser(2L)).thenReturn(null);

        assertThrows(GameException.class, () -> cardGameController.joinGame("game-123", "2"));

        verify(cardGameRepository, never()).save(any(CardGame.class));
    }

    @Test
    void testJoinGame_PlayerAlreadyInGame() {
        Player existingPlayer = new Player();
        existingPlayer.setId("1");
        existingPlayer.setUsername("testuser");
        testCardGame.getPlayers().add(existingPlayer);

        when(cardGameRepository.findById("game-123")).thenReturn(Optional.of(testCardGame));

        CardGame result = cardGameController.joinGame("game-123", "1");

        assertNotNull(result);
        assertEquals(1, result.getPlayers().size());

        verify(cardGameRepository, never()).save(any(CardGame.class));
        verify(messagingTemplate, never()).convertAndSend(anyString(), any(GameEvent.class));
    }

    @Test
    void testLeaveGame() {
        Player player = new Player();
        player.setId("1");
        player.setUsername("testuser");
        testCardGame.getPlayers().add(player);
        testCardGame.setCurrentPlayer(player);

        Player player2 = new Player();
        player2.setId("2");
        player2.setUsername("testuser2");
        testCardGame.getPlayers().add(player2);

        when(cardGameRepository.findById("game-123")).thenReturn(Optional.of(testCardGame));

        CardGame result = cardGameController.leaveGame("game-123", "1");

        assertNotNull(result);
        assertEquals(1, result.getPlayers().size());
        assertNull(result.getCurrentPlayer());

        verify(cardGameRepository).save(testCardGame);
        verify(auditLogController).logAction(eq("PLAYER_LEFT"), eq(1L), anyString());
    }

    @Test
    void testLeaveGame_LastPlayer() {
        Player player = new Player();
        player.setId("1");
        player.setUsername("testuser");
        testCardGame.getPlayers().add(player);

        when(cardGameRepository.findById("game-123")).thenReturn(Optional.of(testCardGame));

        cardGameController.leaveGame("game-123", "1");

        verify(cardGameRepository).deleteById("game-123");
        verify(auditLogController).logAction(eq("GAME_DELETED"), eq(1L), anyString());
    }

    @Test
    void testAbandonGame() {
        Player player = new Player();
        player.setId("1");
        player.setUsername("testuser");
        testCardGame.getPlayers().add(player);
        testCardGame.setStatus(GameStatus.ACTIVE);

        when(cardGameRepository.findById("game-123")).thenReturn(Optional.of(testCardGame));

        CardGame result = cardGameController.abandonGame("game-123", "1");

        assertNotNull(result);
        assertEquals(GameStatus.FINISHED, result.getStatus());
        assertTrue(result.hasUserAbandoned("1"));

        verify(statisticsController).recordAbandonedGame(testCardGame, "1");
        verify(cardGameRepository).save(testCardGame);
        verify(lobbyController).endGame(testCardGame.getId());
        verify(auditLogController).logAction(eq("GAME_ABANDONED"), eq(1L), anyString());
    }

    @Test
    void testStartGame() {
        Player player1 = new Player();
        player1.setId("1");
        player1.setUsername("testuser");
        testCardGame.getPlayers().add(player1);

        Player player2 = new Player();
        player2.setId("2");
        player2.setUsername("testuser2");
        testCardGame.getPlayers().add(player2);

        when(cardGameRepository.findById("game-123")).thenReturn(Optional.of(testCardGame));

        CardGame result = cardGameController.startGame("game-123", "1");

        assertNotNull(result);
        assertEquals(GameStatus.ACTIVE, result.getStatus());

        verify(cardGameRepository).save(any(CardGame.class));
        verify(auditLogController).logAction(eq("GAME_STARTED"), eq(1L), anyString());
        verify(gameTimeoutService, times(2)).recordActivity(anyString(), anyString());
        verify(messagingTemplate).convertAndSend(eq("/topic/game/" + result.getId()), any(GameEvent.class));
    }

    @Test
    void testStartGame_PlayerNotInGame() {
        Player player = new Player();
        player.setId("2");
        player.setUsername("testuser2");
        testCardGame.getPlayers().add(player);

        when(cardGameRepository.findById("game-123")).thenReturn(Optional.of(testCardGame));

        assertThrows(GameException.class, () -> cardGameController.startGame("game-123", "1"));

        verify(cardGameRepository, never()).save(any(CardGame.class));
    }

    @Test
    void testExecuteGameAction() {
        Player player = new Player();
        player.setId("1");
        player.setUsername("testuser");
        testCardGame.getPlayers().add(player);
        testCardGame.setStatus(GameStatus.ACTIVE);
        testCardGame.setCurrentPlayer(player);

        GameAction action = new GameAction();
        action.setActionType("testAction");

        when(cardGameRepository.findById("game-123")).thenReturn(Optional.of(testCardGame));

        CardGame result = cardGameController.executeGameAction("game-123", "1", action);

        assertNotNull(result);

        verify(gameTimeoutService).recordActivity("game-123", "1");
        verify(cardGameRepository).save(any(CardGame.class));
        verify(auditLogController).logAction(eq("GAME_ACTION_EXECUTED"), eq(1L), anyString());
        verify(messagingTemplate).convertAndSend(eq("/topic/game/" + result.getId()), any(GameEvent.class));
    }

    @Test
    void testExecuteGameAction_GameOver() {
        Player player = new Player();
        player.setId("1");
        player.setUsername("testuser");
        testCardGame.getPlayers().add(player);
        testCardGame.setStatus(GameStatus.ACTIVE);
        testCardGame.setCurrentPlayer(player);

        GameAction action = new GameAction();
        action.setActionType("testAction");

        TestCardGame spyGame = spy((TestCardGame) testCardGame);
        when(spyGame.isGameOver()).thenReturn(true);
        when(spyGame.getId()).thenReturn("game-123");

        when(cardGameRepository.findById("game-123")).thenReturn(Optional.of(spyGame));

        CardGame result = cardGameController.executeGameAction("game-123", "1", action);

        assertNotNull(result);
        assertEquals(GameStatus.FINISHED, result.getStatus());

        verify(statisticsController).updateStatistics(any(CardGame.class), anyMap());
        verify(lobbyController).endGame("game-123");
        verify(cardGameRepository).save(any(CardGame.class));
        verify(auditLogController).logAction(eq("GAME_COMPLETED"), eq(1L), anyString());
        verify(auditLogController).logAction(eq("GAME_STATISTICS_UPDATED"), eq(1L), anyString());
    }

    @Test
    void testExecuteGameAction_GameNotFound() {
        when(cardGameRepository.findById("game-456")).thenReturn(Optional.empty());

        GameAction action = new GameAction();
        action.setActionType("testAction");

        assertThrows(GameException.class, () -> cardGameController.executeGameAction("game-456", "1", action));
    }

    @Test
    void testExecuteGameAction_GameNotActive() {
        testCardGame.setStatus(GameStatus.WAITING);

        when(cardGameRepository.findById("game-123")).thenReturn(Optional.of(testCardGame));

        GameAction action = new GameAction();
        action.setActionType("testAction");

        assertThrows(GameException.class, () -> cardGameController.executeGameAction("game-123", "1", action));
    }

    @Test
    void testExecuteGameAction_NotPlayersTurn() {
        Player player1 = new Player();
        player1.setId("1");
        player1.setUsername("testuser");

        Player player2 = new Player();
        player2.setId("2");
        player2.setUsername("testuser2");

        testCardGame.getPlayers().add(player1);
        testCardGame.getPlayers().add(player2);
        testCardGame.setStatus(GameStatus.ACTIVE);
        testCardGame.setCurrentPlayer(player2);

        when(cardGameRepository.findById("game-123")).thenReturn(Optional.of(testCardGame));

        GameAction action = new GameAction();
        action.setActionType("testAction");

        assertThrows(GameException.class, () -> cardGameController.executeGameAction("game-123", "1", action));
    }

    @Test
    void testExecuteGameAction_InvalidMove() {
        Player player = new Player();
        player.setId("1");
        player.setUsername("testuser");
        testCardGame.getPlayers().add(player);
        testCardGame.setStatus(GameStatus.ACTIVE);
        testCardGame.setCurrentPlayer(player);

        TestCardGame spyGame = spy((TestCardGame) testCardGame);
        when(spyGame.isValidMove(anyString(), any(GameAction.class))).thenReturn(false);

        when(cardGameRepository.findById("game-123")).thenReturn(Optional.of(spyGame));

        GameAction action = new GameAction();
        action.setActionType("testAction");

        assertThrows(GameException.class, () -> cardGameController.executeGameAction("game-123", "1", action));
    }

    @Test
    void testGetActiveGames() {
        List<CardGame> activeGames = new ArrayList<>();
        activeGames.add(testCardGame);

        when(cardGameRepository.findByStatus(GameStatus.ACTIVE)).thenReturn(activeGames);

        List<CardGame> result = cardGameController.getActiveGames();

        assertNotNull(result);
        assertEquals(1, result.size());
        assertEquals(testCardGame, result.get(0));
    }

    @Test
    void testGetWaitingGames() {
        List<CardGame> waitingGames = new ArrayList<>();
        waitingGames.add(testCardGame);

        when(cardGameRepository.findByStatus(GameStatus.WAITING)).thenReturn(waitingGames);

        List<CardGame> result = cardGameController.getWaitingGames();

        assertNotNull(result);
        assertEquals(1, result.size());
        assertEquals(testCardGame, result.get(0));
    }

    @Test
    void testGetGamesByDefinition() {
        List<CardGame> games = new ArrayList<>();
        games.add(testCardGame);

        when(cardGameRepository.findByGameDefinitionId(1L)).thenReturn(games);

        List<CardGame> result = cardGameController.getGamesByDefinition(1L);

        assertNotNull(result);
        assertEquals(1, result.size());
        assertEquals(testCardGame, result.get(0));
    }

    @Test
    void testGetGame() {
        when(cardGameRepository.findById("game-123")).thenReturn(Optional.of(testCardGame));

        CardGame result = cardGameController.getGame("game-123");

        assertNotNull(result);
        assertEquals(testCardGame, result);
    }

    @Test
    void testGetGame_NotFound() {
        when(cardGameRepository.findById("game-456")).thenReturn(Optional.empty());

        CardGame result = cardGameController.getGame("game-456");

        assertNull(result);
    }

    @Test
    void testGetGameDefinitions() {
        List<Game> gameDefinitions = new ArrayList<>();
        gameDefinitions.add(testGameDefinition);

        when(gameRepository.findAll()).thenReturn(gameDefinitions);

        List<Game> result = cardGameController.getGameDefinitions();

        assertNotNull(result);
        assertEquals(1, result.size());
        assertEquals(testGameDefinition, result.get(0));
    }

    @Test
    void testGetGameDefinition() {
        when(gameRepository.findById(1L)).thenReturn(Optional.of(testGameDefinition));

        Game result = cardGameController.getGameDefinition(1L);

        assertNotNull(result);
        assertEquals(testGameDefinition, result);
    }

    @Test
    void testGetGameDefinition_NotFound() {
        when(gameRepository.findById(2L)).thenReturn(Optional.empty());

        Game result = cardGameController.getGameDefinition(2L);

        assertNull(result);
    }

    @Test
    void testSaveGame() {
        cardGameController.saveGame(testCardGame);

        verify(cardGameRepository).save(testCardGame);
    }
}

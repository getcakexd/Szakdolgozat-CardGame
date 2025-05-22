package hu.benkototh.cardgame.backend.game.service;

import hu.benkototh.cardgame.backend.game.controller.CardGameController;
import hu.benkototh.cardgame.backend.game.controller.StatisticsController;
import hu.benkototh.cardgame.backend.game.model.CardGame;
import hu.benkototh.cardgame.backend.game.model.GameStatus;
import hu.benkototh.cardgame.backend.rest.model.Game;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import java.util.*;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class CardGameRestServiceTest {

    @Mock
    private CardGameController cardGameController;

    @Mock
    private StatisticsController statisticsController;

    @InjectMocks
    private CardGameRestService cardGameRestService;

    private Game testGameDefinition;
    private CardGame testCardGame;
    private List<Game> testGameDefinitions;
    private List<CardGame> testCardGames;
    private Map<String, Object> testStatistics;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        
        testGameDefinition = new Game();
        testGameDefinition.setId(1L);
        testGameDefinition.setName("Test Game");
        
        testGameDefinitions = new ArrayList<>();
        testGameDefinitions.add(testGameDefinition);
        
        testCardGame = mock(CardGame.class);
        when(testCardGame.getId()).thenReturn("game-123");
        when(testCardGame.getName()).thenReturn("Test Card Game");
        when(testCardGame.getStatus()).thenReturn(GameStatus.WAITING);
        
        testCardGames = new ArrayList<>();
        testCardGames.add(testCardGame);
        
        testStatistics = new HashMap<>();
        testStatistics.put("gamesPlayed", 10);
        testStatistics.put("gamesWon", 5);
        
        when(cardGameController.getGameDefinitions()).thenReturn(testGameDefinitions);
        when(cardGameController.getGameDefinition(1L)).thenReturn(testGameDefinition);
        when(cardGameController.getGame("game-123")).thenReturn(testCardGame);
        when(cardGameController.getActiveGames()).thenReturn(testCardGames);
        when(cardGameController.getWaitingGames()).thenReturn(testCardGames);
        when(cardGameController.getGamesByDefinition(1L)).thenReturn(testCardGames);
        
        when(statisticsController.getUserStatistics("1", null)).thenReturn(testStatistics);
        when(statisticsController.getUserStatisticsByGameDefinition("1", 1L)).thenReturn(testStatistics);
    }

    @Test
    void testGetGameDefinitions() {
        ResponseEntity<List<Game>> response = cardGameRestService.getGameDefinitions();
        
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals(testGameDefinitions, response.getBody());
    }

    @Test
    void testGetGameDefinition() {
        ResponseEntity<Game> response = cardGameRestService.getGameDefinition(1L);
        
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals(testGameDefinition, response.getBody());
    }

    @Test
    void testGetGameDefinition_NotFound() {
        when(cardGameController.getGameDefinition(2L)).thenReturn(null);
        
        ResponseEntity<Game> response = cardGameRestService.getGameDefinition(2L);
        
        assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
        assertNull(response.getBody());
    }

    @Test
    void testCreateGame() {
        Map<String, Object> request = new HashMap<>();
        request.put("gameDefinitionId", 1L);
        request.put("creatorId", "1");
        request.put("gameName", "New Game");
        request.put("trackStatistics", true);
        
        when(cardGameController.createCardGame(1L, "1", "New Game", true)).thenReturn(testCardGame);
        
        ResponseEntity<CardGame> response = cardGameRestService.createGame(request);
        
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals(testCardGame, response.getBody());
    }

    @Test
    void testJoinGame() {
        Map<String, String> request = new HashMap<>();
        request.put("userId", "1");
        
        when(cardGameController.joinGame("game-123", "1")).thenReturn(testCardGame);
        
        ResponseEntity<CardGame> response = cardGameRestService.joinGame("game-123", request);
        
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals(testCardGame, response.getBody());
    }

    @Test
    void testLeaveGame() {
        Map<String, String> request = new HashMap<>();
        request.put("userId", "1");
        
        when(cardGameController.leaveGame("game-123", "1")).thenReturn(testCardGame);
        
        ResponseEntity<CardGame> response = cardGameRestService.leaveGame("game-123", request);
        
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals(testCardGame, response.getBody());
    }

    @Test
    void testAbandonGame() {
        Map<String, String> request = new HashMap<>();
        request.put("userId", "1");
        
        when(cardGameController.abandonGame("game-123", "1")).thenReturn(testCardGame);
        
        ResponseEntity<CardGame> response = cardGameRestService.abandonGame("game-123", request);
        
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals(testCardGame, response.getBody());
    }

    @Test
    void testStartGame() {
        Map<String, String> request = new HashMap<>();
        request.put("userId", "1");
        
        when(cardGameController.startGame("game-123", "1")).thenReturn(testCardGame);
        
        ResponseEntity<CardGame> response = cardGameRestService.startGame("game-123", request);
        
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals(testCardGame, response.getBody());
    }

    @Test
    void testGetGames_Active() {
        ResponseEntity<List<CardGame>> response = cardGameRestService.getGames("active");
        
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals(testCardGames, response.getBody());
        verify(cardGameController).getActiveGames();
    }

    @Test
    void testGetGames_Waiting() {
        ResponseEntity<List<CardGame>> response = cardGameRestService.getGames("waiting");
        
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals(testCardGames, response.getBody());
        verify(cardGameController).getWaitingGames();
    }

    @Test
    void testGetGames_DefaultToActive() {
        ResponseEntity<List<CardGame>> response = cardGameRestService.getGames(null);
        
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals(testCardGames, response.getBody());
        verify(cardGameController).getActiveGames();
    }

    @Test
    void testGetGamesByDefinition() {
        ResponseEntity<List<CardGame>> response = cardGameRestService.getGamesByDefinition(1L);
        
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals(testCardGames, response.getBody());
    }

    @Test
    void testGetGame() {
        ResponseEntity<CardGame> response = cardGameRestService.getGame("game-123");
        
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals(testCardGame, response.getBody());
    }

    @Test
    void testGetGame_NotFound() {
        when(cardGameController.getGame("game-456")).thenReturn(null);
        
        ResponseEntity<CardGame> response = cardGameRestService.getGame("game-456");
        
        assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
        assertNull(response.getBody());
    }

    @Test
    void testGetUserStatistics() {
        ResponseEntity<Map<String, Object>> response = cardGameRestService.getUserStatistics("1", null);
        
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals(testStatistics, response.getBody());
    }

    @Test
    void testGetUserStatisticsByGameDefinition() {
        ResponseEntity<Map<String, Object>> response = cardGameRestService.getUserStatisticsByGameDefinition("1", 1L);
        
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals(testStatistics, response.getBody());
    }
}

package hu.benkototh.cardgame.backend.game.service;

import hu.benkototh.cardgame.backend.game.controller.StatsController;
import hu.benkototh.cardgame.backend.game.model.GameStatistics;
import hu.benkototh.cardgame.backend.game.model.UserGameStats;
import hu.benkototh.cardgame.backend.game.model.UserStats;
import hu.benkototh.cardgame.backend.rest.model.Game;
import hu.benkototh.cardgame.backend.rest.model.User;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class StatsRestServiceTest {

    @Mock
    private StatsController statsController;

    @InjectMocks
    private StatsRestService statsRestService;

    private UserStats testUserStats;
    private List<UserGameStats> testUserGameStatsList;
    private UserGameStats testUserGameStats;
    private List<GameStatistics> testRecentGames;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        
        User testUser = new User();
        testUser.setId(1L);
        testUser.setUsername("testuser");
        
        Game testGame = new Game();
        testGame.setId(1L);
        testGame.setName("Test Game");
        
        testUserStats = new UserStats();
        testUserStats.setId(1L);
        testUserStats.setUser(testUser);
        testUserStats.setGamesPlayed(10);
        testUserStats.setGamesWon(5);
        testUserStats.setGamesLost(3);
        testUserStats.setGamesDrawn(2);
        testUserStats.setTotalPoints(500);
        
        testUserGameStats = new UserGameStats();
        testUserGameStats.setId(1L);
        testUserGameStats.setUser(testUser);
        testUserGameStats.setGameDefinition(testGame);
        testUserGameStats.setGamesPlayed(5);
        testUserGameStats.setGamesWon(3);
        testUserGameStats.setTotalPoints(300);
        
        testUserGameStatsList = new ArrayList<>();
        testUserGameStatsList.add(testUserGameStats);
        
        GameStatistics gameStats = new GameStatistics();
        gameStats.setId(1L);
        gameStats.setUserId("1");
        gameStats.setGameId("game-123");
        gameStats.setGameDefinitionId(1L);
        gameStats.setScore(100);
        gameStats.setWon(true);
        gameStats.setPlayedAt(new Date());
        
        testRecentGames = new ArrayList<>();
        testRecentGames.add(gameStats);
    }

    @Test
    void testGetUserStats() {
        when(statsController.getUserStats(1L)).thenReturn(testUserStats);
        
        ResponseEntity<?> response = statsRestService.getUserStats(1L);
        
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals(testUserStats, response.getBody());
        
        when(statsController.getUserStats(2L)).thenReturn(null);
        
        response = statsRestService.getUserStats(2L);
        
        assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
        assertTrue(response.getBody() instanceof Map);
        @SuppressWarnings("unchecked")
        Map<String, String> errorResponse = (Map<String, String>) response.getBody();
        assertEquals("User not found", errorResponse.get("message"));
    }

    @Test
    void testGetUserGameStats() {
        when(statsController.getUserGameStats(1L)).thenReturn(testUserGameStatsList);
        
        ResponseEntity<?> response = statsRestService.getUserGameStats(1L);
        
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals(testUserGameStatsList, response.getBody());
    }

    @Test
    void testGetUserGameStatsByGame() {
        when(statsController.getUserGameStatsByGame(1L, 1L)).thenReturn(testUserGameStats);
        
        ResponseEntity<?> response = statsRestService.getUserGameStatsByGame(1L, 1L);
        
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals(testUserGameStats, response.getBody());
        
        when(statsController.getUserGameStatsByGame(2L, 1L)).thenReturn(null);
        
        response = statsRestService.getUserGameStatsByGame(2L, 1L);
        
        assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
        assertTrue(response.getBody() instanceof Map);
        @SuppressWarnings("unchecked")
        Map<String, String> errorResponse = (Map<String, String>) response.getBody();
        assertEquals("User or game not found", errorResponse.get("message"));
    }

    @Test
    void testGetLeaderboard() {
        when(statsController.getTopPlayersByPoints(10)).thenReturn(testUserGameStatsList);
        
        ResponseEntity<?> response = statsRestService.getLeaderboard(10);
        
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals(testUserGameStatsList, response.getBody());
    }

    @Test
    void testGetLeaderboardByGame() {
        when(statsController.getTopPlayersByGameAndPoints(1L, 10)).thenReturn(testUserGameStatsList);
        
        ResponseEntity<?> response = statsRestService.getLeaderboardByGame(1L, 10);
        
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals(testUserGameStatsList, response.getBody());
    }

    @Test
    void testGetRecentGames() {
        when(statsController.getRecentGames(1L, 10)).thenReturn(testRecentGames);
        
        ResponseEntity<?> response = statsRestService.getRecentGames(1L, 10);
        
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals(testRecentGames, response.getBody());
    }

    @Test
    void testGetUserDrawStats() {
        when(statsController.getUserStats(1L)).thenReturn(testUserStats);
        
        ResponseEntity<?> response = statsRestService.getUserDrawStats(1L);
        
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertTrue(response.getBody() instanceof Map);
        @SuppressWarnings("unchecked")
        Map<String, Object> drawStats = (Map<String, Object>) response.getBody();
        assertEquals(2, drawStats.get("gamesDrawn"));
        assertEquals(20.0, drawStats.get("drawPercentage"));
        
        when(statsController.getUserStats(2L)).thenReturn(null);
        
        response = statsRestService.getUserDrawStats(2L);
        
        assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
        assertTrue(response.getBody() instanceof Map);
        @SuppressWarnings("unchecked")
        Map<String, String> errorResponse = (Map<String, String>) response.getBody();
        assertEquals("User not found", errorResponse.get("message"));
    }
}

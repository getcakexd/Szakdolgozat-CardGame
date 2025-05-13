package hu.benkototh.cardgame.backend.game.service;

import hu.benkototh.cardgame.backend.game.controller.ClubStatsController;
import hu.benkototh.cardgame.backend.game.model.ClubGameStats;
import hu.benkototh.cardgame.backend.game.model.ClubMemberStats;
import hu.benkototh.cardgame.backend.game.model.ClubStats;
import hu.benkototh.cardgame.backend.rest.Data.Club;
import hu.benkototh.cardgame.backend.rest.Data.Game;
import hu.benkototh.cardgame.backend.rest.Data.User;
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

class ClubStatsRestServiceTest {

    @Mock
    private ClubStatsController clubStatsController;

    @InjectMocks
    private ClubStatsRestService clubStatsRestService;

    private ClubStats testClubStats;
    private List<ClubGameStats> testClubGameStatsList;
    private ClubGameStats testClubGameStats;
    private List<ClubMemberStats> testClubMemberStatsList;
    private ClubMemberStats testClubMemberStats;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        
        Club testClub = new Club();
        testClub.setId(1L);
        testClub.setName("Test Club");
        
        User testUser = new User();
        testUser.setId(1L);
        testUser.setUsername("testuser");
        
        Game testGame = new Game();
        testGame.setId(1L);
        testGame.setName("Test Game");
        
        testClubStats = new ClubStats();
        testClubStats.setId(1L);
        testClubStats.setClub(testClub);
        testClubStats.setGamesPlayed(20);
        testClubStats.setGamesDrawn(5);
        testClubStats.setTotalPoints(1000);
        testClubStats.setTotalFatsCollected(50);
        testClubStats.setUniquePlayersCount(10);
        
        testClubGameStats = new ClubGameStats();
        testClubGameStats.setId(1L);
        testClubGameStats.setClub(testClub);
        testClubGameStats.setGameDefinition(testGame);
        testClubGameStats.setGamesPlayed(10);
        testClubGameStats.setGamesDrawn(2);
        testClubGameStats.setTotalPoints(500);
        testClubGameStats.setHighestScore(120);
        testClubGameStats.setLastPlayed(new Date());
        
        testClubGameStatsList = new ArrayList<>();
        testClubGameStatsList.add(testClubGameStats);
        
        testClubMemberStats = new ClubMemberStats();
        testClubMemberStats.setId(1L);
        testClubMemberStats.setClub(testClub);
        testClubMemberStats.setUser(testUser);
        testClubMemberStats.setGamesPlayed(5);
        testClubMemberStats.setGamesWon(3);
        testClubMemberStats.setGamesDrawn(1);
        testClubMemberStats.setTotalPoints(250);
        testClubMemberStats.setLastPlayed(new Date());
        
        testClubMemberStatsList = new ArrayList<>();
        testClubMemberStatsList.add(testClubMemberStats);
    }

    @Test
    void testGetClubStats() {
        when(clubStatsController.getClubStats(1L)).thenReturn(testClubStats);
        
        ResponseEntity<?> response = clubStatsRestService.getClubStats(1L);
        
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals(testClubStats, response.getBody());
        
        when(clubStatsController.getClubStats(2L)).thenReturn(null);
        
        response = clubStatsRestService.getClubStats(2L);
        
        assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
        assertTrue(response.getBody() instanceof Map);
        @SuppressWarnings("unchecked")
        Map<String, String> errorResponse = (Map<String, String>) response.getBody();
        assertEquals("Club not found", errorResponse.get("message"));
    }

    @Test
    void testGetClubGameStats() {
        when(clubStatsController.getClubGameStats(1L)).thenReturn(testClubGameStatsList);
        
        ResponseEntity<?> response = clubStatsRestService.getClubGameStats(1L);
        
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals(testClubGameStatsList, response.getBody());
    }

    @Test
    void testGetClubGameStatsByGame() {
        when(clubStatsController.getClubGameStatsByGame(1L, 1L)).thenReturn(testClubGameStats);
        
        ResponseEntity<?> response = clubStatsRestService.getClubGameStatsByGame(1L, 1L);
        
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals(testClubGameStats, response.getBody());
        
        when(clubStatsController.getClubGameStatsByGame(2L, 1L)).thenReturn(null);
        
        response = clubStatsRestService.getClubGameStatsByGame(2L, 1L);
        
        assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
        assertTrue(response.getBody() instanceof Map);
        @SuppressWarnings("unchecked")
        Map<String, String> errorResponse = (Map<String, String>) response.getBody();
        assertEquals("Club or game not found", errorResponse.get("message"));
    }

    @Test
    void testGetClubMemberStats() {
        when(clubStatsController.getClubMemberStats(1L)).thenReturn(testClubMemberStatsList);
        
        ResponseEntity<?> response = clubStatsRestService.getClubMemberStats(1L);
        
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals(testClubMemberStatsList, response.getBody());
    }

    @Test
    void testGetClubMemberStatsByUser() {
        when(clubStatsController.getClubMemberStatsByUser(1L, 1L)).thenReturn(testClubMemberStats);
        
        ResponseEntity<?> response = clubStatsRestService.getClubMemberStatsByUser(1L, 1L);
        
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals(testClubMemberStats, response.getBody());
        
        when(clubStatsController.getClubMemberStatsByUser(2L, 1L)).thenReturn(null);
        
        response = clubStatsRestService.getClubMemberStatsByUser(2L, 1L);
        
        assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
        assertTrue(response.getBody() instanceof Map);
        @SuppressWarnings("unchecked")
        Map<String, String> errorResponse = (Map<String, String>) response.getBody();
        assertEquals("Club or user not found", errorResponse.get("message"));
    }

    @Test
    void testGetClubLeaderboard() {
        when(clubStatsController.getTopClubsByPoints(10)).thenReturn(testClubGameStatsList);
        
        ResponseEntity<?> response = clubStatsRestService.getClubLeaderboard(10);
        
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals(testClubGameStatsList, response.getBody());
    }

    @Test
    void testGetClubLeaderboardByGame() {
        when(clubStatsController.getTopClubsByGameAndPoints(1L, 10)).thenReturn(testClubGameStatsList);
        
        ResponseEntity<?> response = clubStatsRestService.getClubLeaderboardByGame(1L, 10);
        
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals(testClubGameStatsList, response.getBody());
    }

    @Test
    void testGetClubMemberLeaderboard() {
        when(clubStatsController.getTopMembersByClubAndPoints(1L, 10)).thenReturn(testClubMemberStatsList);
        
        ResponseEntity<?> response = clubStatsRestService.getClubMemberLeaderboard(1L, 10);
        
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals(testClubMemberStatsList, response.getBody());
    }

    @Test
    void testGetClubMemberLeaderboardByGame() {
        when(clubStatsController.getTopMembersByClubAndGameAndPoints(1L, 1L, 10)).thenReturn(testClubMemberStatsList);
        
        ResponseEntity<?> response = clubStatsRestService.getClubMemberLeaderboardByGame(1L, 1L, 10);
        
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals(testClubMemberStatsList, response.getBody());
    }
}

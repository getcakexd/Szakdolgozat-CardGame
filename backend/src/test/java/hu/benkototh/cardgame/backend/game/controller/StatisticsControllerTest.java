package hu.benkototh.cardgame.backend.game.controller;

import hu.benkototh.cardgame.backend.game.model.CardGame;
import hu.benkototh.cardgame.backend.game.model.GameStatistics;
import hu.benkototh.cardgame.backend.game.model.Player;
import hu.benkototh.cardgame.backend.game.repository.IGameStatisticsRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

class StatisticsControllerTest {

    @Mock
    private IGameStatisticsRepository gameStatisticsRepository;

    @Mock
    private StatsController statsController;

    @InjectMocks
    private StatisticsController statisticsController;

    private CardGame mockGame;
    private Player mockPlayer1;
    private Player mockPlayer2;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);

        mockGame = mock(CardGame.class);
        mockPlayer1 = mock(Player.class);
        mockPlayer2 = mock(Player.class);

        when(mockGame.getId()).thenReturn("game-123");
        when(mockGame.getGameDefinitionId()).thenReturn(1L);

        when(mockPlayer1.getId()).thenReturn("1");
        when(mockPlayer2.getId()).thenReturn("2");

        List<Player> players = new ArrayList<>();
        players.add(mockPlayer1);
        players.add(mockPlayer2);

        when(mockGame.getPlayers()).thenReturn(players);
    }

    @Test
    void testUpdateStatistics() {
        Map<String, Integer> scores = new HashMap<>();
        scores.put("1", 100);
        scores.put("2", 80);

        statisticsController.updateStatistics(mockGame, scores);

        verify(statsController).recordGameResult(eq(mockGame), eq(scores), eq(false), isNull(), eq(false), anyList());
        verify(gameStatisticsRepository, times(2)).save(any(GameStatistics.class));
    }

    @Test
    void testRecordAbandonedGame() {
        statisticsController.recordAbandonedGame(mockGame, "1");

        verify(statsController).recordAbandonedGame(mockGame, "1");
    }

    @Test
    void testGetUserStatistics() {
        Map<String, Object> mockStats = new HashMap<>();
        mockStats.put("gamesPlayed", 10);
        mockStats.put("gamesWon", 5);

        when(gameStatisticsRepository.getUserStatistics("1", "Poker")).thenReturn(mockStats);

        Map<String, Object> result = statisticsController.getUserStatistics("1", "Poker");

        assertEquals(10, result.get("gamesPlayed"));
        assertEquals(5, result.get("gamesWon"));

        when(gameStatisticsRepository.getUserStatistics("2", "Poker")).thenReturn(null);

        result = statisticsController.getUserStatistics("2", "Poker");

        assertEquals(0, result.get("gamesPlayed"));
        assertEquals(0, result.get("gamesWon"));
        assertEquals(0, result.get("gamesDrawn"));
        assertEquals(0, result.get("totalScore"));
        assertEquals(0, result.get("averageScore"));
    }

    @Test
    void testGetUserStatisticsByGameDefinition() {
        Map<String, Object> mockStats = new HashMap<>();
        mockStats.put("gamesPlayed", 10);
        mockStats.put("gamesWon", 5);

        when(gameStatisticsRepository.getUserStatisticsByGameDefinition("1", 1L)).thenReturn(mockStats);

        Map<String, Object> result = statisticsController.getUserStatisticsByGameDefinition("1", 1L);

        assertEquals(10, result.get("gamesPlayed"));
        assertEquals(5, result.get("gamesWon"));

        when(gameStatisticsRepository.getUserStatisticsByGameDefinition("2", 1L)).thenReturn(null);

        result = statisticsController.getUserStatisticsByGameDefinition("2", 1L);

        assertEquals(0, result.get("gamesPlayed"));
        assertEquals(0, result.get("gamesWon"));
        assertEquals(0, result.get("gamesDrawn"));
        assertEquals(0, result.get("totalScore"));
        assertEquals(0, result.get("averageScore"));
    }
}

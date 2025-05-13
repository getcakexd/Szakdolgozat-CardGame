package hu.benkototh.cardgame.backend.game.controller;

import hu.benkototh.cardgame.backend.game.model.*;
import hu.benkototh.cardgame.backend.game.repository.IGameStatisticsRepository;
import hu.benkototh.cardgame.backend.game.repository.IUserGameStatsRepository;
import hu.benkototh.cardgame.backend.game.repository.IUserStatsRepository;
import hu.benkototh.cardgame.backend.rest.Data.Game;
import hu.benkototh.cardgame.backend.rest.Data.Lobby;
import hu.benkototh.cardgame.backend.rest.Data.User;
import hu.benkototh.cardgame.backend.rest.controller.LobbyController;
import hu.benkototh.cardgame.backend.rest.controller.UserController;
import hu.benkototh.cardgame.backend.rest.repository.IGameRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.mockito.Spy;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.*;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

class StatsControllerTest {

    @Mock
    private IUserStatsRepository userStatsRepository;

    @Mock
    private IUserGameStatsRepository userGameStatsRepository;

    @Mock
    private IGameStatisticsRepository gameStatisticsRepository;

    @Mock
    private IGameRepository gameRepository;

    @Mock
    private UserController userController;

    @Mock
    private LobbyController lobbyController;

    @Mock
    private ClubStatsController clubStatsController;

    @Mock
    private CardGameController cardGameController;

    @InjectMocks
    @Spy
    private StatsController statsController;

    private User testUser;
    private Game testGame;
    private CardGame mockCardGame;
    private Player mockPlayer;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);

        testUser = new User();
        testUser.setId(1L);
        testUser.setUsername("testuser");

        testGame = new Game();
        testGame.setId(1L);
        testGame.setName("Test Game");

        mockCardGame = mock(CardGame.class);
        mockPlayer = mock(Player.class);

        when(mockCardGame.getId()).thenReturn("game-123");
        when(mockCardGame.getGameDefinitionId()).thenReturn(1L);
        when(mockCardGame.isTrackStatistics()).thenReturn(true);

        when(mockPlayer.getId()).thenReturn("1");
        when(mockPlayer.isAI()).thenReturn(false);

        List<Player> players = new ArrayList<>();
        players.add(mockPlayer);
        when(mockCardGame.getPlayers()).thenReturn(players);

        when(userController.getUser(1L)).thenReturn(testUser);
        when(gameRepository.findById(1L)).thenReturn(Optional.of(testGame));

        when(lobbyController.findLobbyByCardGameId(anyString())).thenReturn(null);

        ReflectionTestUtils.setField(statsController, "statsController", statsController);

        doNothing().when(statsController).recordGameResult(any(CardGame.class), anyMap(), anyBoolean(), anyString(), anyBoolean(), anyList());
    }

    @Test
    void testRecordGameResult() {
        Map<String, Integer> scores = new HashMap<>();
        scores.put("1", 100);

        List<Card> wonCards = new ArrayList<>();
        wonCards.add(new Card(Suit.HEARTS, Rank.ACE));
        wonCards.add(new Card(Suit.BELLS, Rank.TEN));

        when(mockPlayer.getWonCards()).thenReturn(wonCards);

        UserStats userStats = new UserStats();
        userStats.setUser(testUser);
        when(userStatsRepository.findByUser(testUser)).thenReturn(Optional.of(userStats));

        UserGameStats userGameStats = new UserGameStats();
        userGameStats.setUser(testUser);
        userGameStats.setGameDefinition(testGame);
        when(userGameStatsRepository.findByUserAndGameDefinition(testUser, testGame)).thenReturn(Optional.of(userGameStats));

        doCallRealMethod().when(statsController).updateUserStats(any(User.class), anyBoolean(), anyBoolean(), anyBoolean(), anyInt(), anyInt());
        doCallRealMethod().when(statsController).updateUserGameStats(any(User.class), any(Game.class), anyBoolean(), anyBoolean(), anyBoolean(), anyInt(), anyInt());

        statsController.updateUserStats(testUser, true, false, false, 100, 2);
        statsController.updateUserGameStats(testUser, testGame, true, false, false, 100, 2);

        verify(userStatsRepository).save(any(UserStats.class));
        verify(userGameStatsRepository).save(any(UserGameStats.class));
    }

    @Test
    void testRecordAbandonedGame() {
        Map<String, Integer> scores = new HashMap<>();
        scores.put("1", 0);

        when(mockCardGame.calculateScores()).thenReturn(scores);

        StatsController testStatsController = spy(new StatsController());
        ReflectionTestUtils.setField(testStatsController, "statsController", testStatsController);
        ReflectionTestUtils.setField(testStatsController, "cardGameController", cardGameController);

        doNothing().when(testStatsController).recordGameResult(any(CardGame.class), anyMap(), anyBoolean(), anyString(), anyBoolean(), anyList());

        testStatsController.recordAbandonedGame(mockCardGame, "2");

        verify(cardGameController).saveGame(mockCardGame);
    }

    @Test
    void testGetUserStats() {
        UserStats userStats = new UserStats();
        userStats.setUser(testUser);
        when(userStatsRepository.findByUser(testUser)).thenReturn(Optional.of(userStats));

        UserStats result = statsController.getUserStats(1L);

        assertNotNull(result);
        assertEquals(testUser, result.getUser());

        when(userController.getUser(2L)).thenReturn(null);

        result = statsController.getUserStats(2L);

        assertNull(result);
    }

    @Test
    void testGetUserGameStats() {
        List<UserGameStats> userGameStatsList = new ArrayList<>();
        UserGameStats userGameStats = new UserGameStats();
        userGameStats.setUser(testUser);
        userGameStats.setGameDefinition(testGame);
        userGameStatsList.add(userGameStats);

        when(userGameStatsRepository.findByUser(testUser)).thenReturn(userGameStatsList);

        List<UserGameStats> result = statsController.getUserGameStats(1L);

        assertNotNull(result);
        assertEquals(1, result.size());
        assertEquals(testUser, result.get(0).getUser());

        when(userController.getUser(2L)).thenReturn(null);

        result = statsController.getUserGameStats(2L);

        assertTrue(result.isEmpty());
    }

    @Test
    void testGetUserGameStatsByGame() {
        UserGameStats userGameStats = new UserGameStats();
        userGameStats.setUser(testUser);
        userGameStats.setGameDefinition(testGame);

        when(userGameStatsRepository.findByUserAndGameDefinition(testUser, testGame)).thenReturn(Optional.of(userGameStats));

        UserGameStats result = statsController.getUserGameStatsByGame(1L, 1L);

        assertNotNull(result);
        assertEquals(testUser, result.getUser());
        assertEquals(testGame, result.getGameDefinition());

        when(userController.getUser(2L)).thenReturn(null);

        result = statsController.getUserGameStatsByGame(2L, 1L);

        assertNull(result);

        when(gameRepository.findById(2L)).thenReturn(Optional.empty());

        result = statsController.getUserGameStatsByGame(1L, 2L);

        assertNull(result);
    }

    @Test
    void testGetTopPlayersByPoints() {
        List<UserGameStats> topPlayers = new ArrayList<>();
        UserGameStats stats1 = new UserGameStats();
        stats1.setUser(testUser);
        stats1.setTotalPoints(100);
        topPlayers.add(stats1);

        when(userGameStatsRepository.findTopPlayersByPoints()).thenReturn(topPlayers);

        List<UserGameStats> result = statsController.getTopPlayersByPoints(10);

        assertNotNull(result);
        assertEquals(1, result.size());
        assertEquals(100, result.get(0).getTotalPoints());
    }

    @Test
    void testGetTopPlayersByGameAndPoints() {
        List<UserGameStats> topPlayers = new ArrayList<>();
        UserGameStats stats1 = new UserGameStats();
        stats1.setUser(testUser);
        stats1.setGameDefinition(testGame);
        stats1.setTotalPoints(100);
        topPlayers.add(stats1);

        when(userGameStatsRepository.findTopPlayersByGameAndPoints(1L)).thenReturn(topPlayers);

        List<UserGameStats> result = statsController.getTopPlayersByGameAndPoints(1L, 10);

        assertNotNull(result);
        assertEquals(1, result.size());
        assertEquals(100, result.get(0).getTotalPoints());
    }

    @Test
    void testGetRecentGames() {
        List<GameStatistics> recentGames = new ArrayList<>();
        GameStatistics game1 = new GameStatistics();
        game1.setUserId("1");
        game1.setScore(100);
        game1.setPlayedAt(new Date());
        recentGames.add(game1);

        when(gameStatisticsRepository.findAll()).thenReturn(recentGames);

        List<GameStatistics> result = statsController.getRecentGames(1L, 10);

        assertNotNull(result);
        assertEquals(1, result.size());
        assertEquals("1", result.get(0).getUserId());
    }
}

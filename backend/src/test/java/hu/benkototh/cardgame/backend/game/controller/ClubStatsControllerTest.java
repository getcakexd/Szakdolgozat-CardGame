package hu.benkototh.cardgame.backend.game.controller;

import hu.benkototh.cardgame.backend.game.model.*;
import hu.benkototh.cardgame.backend.game.repository.IClubGameStatsRepository;
import hu.benkototh.cardgame.backend.game.repository.IClubMemberStatsRepository;
import hu.benkototh.cardgame.backend.game.repository.IClubStatsRepository;
import hu.benkototh.cardgame.backend.rest.model.Club;
import hu.benkototh.cardgame.backend.rest.model.Game;
import hu.benkototh.cardgame.backend.rest.model.Lobby;
import hu.benkototh.cardgame.backend.rest.model.User;
import hu.benkototh.cardgame.backend.rest.controller.ClubController;
import hu.benkototh.cardgame.backend.rest.controller.UserController;
import hu.benkototh.cardgame.backend.rest.repository.IGameRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.util.*;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

class ClubStatsControllerTest {

    @Mock
    private IClubStatsRepository clubStatsRepository;

    @Mock
    private IClubGameStatsRepository clubGameStatsRepository;

    @Mock
    private IClubMemberStatsRepository clubMemberStatsRepository;

    @Mock
    private IGameRepository gameRepository;

    @Mock
    private UserController userController;

    @Mock
    private ClubController clubController;

    @InjectMocks
    private ClubStatsController clubStatsController;

    private Club testClub;
    private User testUser;
    private Game testGame;
    private CardGame testCardGame;
    private Player testPlayer;
    private Lobby testLobby;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        
        testClub = new Club();
        testClub.setId(1L);
        testClub.setName("Test Club");
        
        testUser = new User();
        testUser.setId(1L);
        testUser.setUsername("testuser");
        
        testGame = new Game();
        testGame.setId(1L);
        testGame.setName("Test Game");
        
        testCardGame = mock(CardGame.class);
        when(testCardGame.getId()).thenReturn("game-123");
        when(testCardGame.getGameDefinitionId()).thenReturn(1L);
        
        testPlayer = mock(Player.class);
        when(testPlayer.getId()).thenReturn("1");
        
        List<Player> players = new ArrayList<>();
        players.add(testPlayer);
        when(testCardGame.getPlayers()).thenReturn(players);
        
        testLobby = new Lobby();
        testLobby.setClub(testClub);
        
        when(userController.getUser(1L)).thenReturn(testUser);
        when(gameRepository.findById(1L)).thenReturn(Optional.of(testGame));
        when(clubController.getClubById(1L)).thenReturn(Optional.of(testClub));
    }

    @Test
    void testRecordClubGameResult() {
        Map<String, Integer> scores = new HashMap<>();
        scores.put("1", 100);
        
        List<Card> wonCards = new ArrayList<>();
        wonCards.add(new Card(Suit.HEARTS, Rank.ACE));
        wonCards.add(new Card(Suit.BELLS, Rank.TEN));
        
        when(testPlayer.getWonCards()).thenReturn(wonCards);
        
        ClubStats clubStats = new ClubStats();
        clubStats.setClub(testClub);
        when(clubStatsRepository.findByClub(testClub)).thenReturn(Optional.of(clubStats));
        
        ClubGameStats clubGameStats = new ClubGameStats();
        clubGameStats.setClub(testClub);
        clubGameStats.setGameDefinition(testGame);
        when(clubGameStatsRepository.findByClubAndGameDefinition(testClub, testGame)).thenReturn(Optional.of(clubGameStats));
        
        ClubMemberStats clubMemberStats = new ClubMemberStats();
        clubMemberStats.setClub(testClub);
        clubMemberStats.setUser(testUser);
        when(clubMemberStatsRepository.findByClubAndUser(testClub, testUser)).thenReturn(Optional.of(clubMemberStats));
        
        clubStatsController.recordClubGameResult(testCardGame, testLobby, scores, false, null);
        
        verify(clubStatsRepository).save(any(ClubStats.class));
        verify(clubGameStatsRepository).save(any(ClubGameStats.class));
        verify(clubMemberStatsRepository).save(any(ClubMemberStats.class));
    }

    @Test
    void testGetClubStats() {
        ClubStats clubStats = new ClubStats();
        clubStats.setClub(testClub);
        when(clubStatsRepository.findByClub(testClub)).thenReturn(Optional.of(clubStats));
        
        ClubStats result = clubStatsController.getClubStats(1L);
        
        assertNotNull(result);
        assertEquals(testClub, result.getClub());
        
        when(clubController.getClubById(2L)).thenReturn(Optional.empty());
        
        result = clubStatsController.getClubStats(2L);
        
        assertNull(result);
    }

    @Test
    void testGetClubGameStats() {
        List<ClubGameStats> clubGameStatsList = new ArrayList<>();
        ClubGameStats clubGameStats = new ClubGameStats();
        clubGameStats.setClub(testClub);
        clubGameStats.setGameDefinition(testGame);
        clubGameStatsList.add(clubGameStats);
        
        when(clubGameStatsRepository.findByClub(testClub)).thenReturn(clubGameStatsList);
        
        List<ClubGameStats> result = clubStatsController.getClubGameStats(1L);
        
        assertNotNull(result);
        assertEquals(1, result.size());
        assertEquals(testClub, result.get(0).getClub());
        
        when(clubController.getClubById(2L)).thenReturn(Optional.empty());
        
        result = clubStatsController.getClubGameStats(2L);
        
        assertTrue(result.isEmpty());
    }

    @Test
    void testGetClubGameStatsByGame() {
        ClubGameStats clubGameStats = new ClubGameStats();
        clubGameStats.setClub(testClub);
        clubGameStats.setGameDefinition(testGame);
        
        when(clubGameStatsRepository.findByClubAndGameDefinition(testClub, testGame)).thenReturn(Optional.of(clubGameStats));
        
        ClubGameStats result = clubStatsController.getClubGameStatsByGame(1L, 1L);
        
        assertNotNull(result);
        assertEquals(testClub, result.getClub());
        assertEquals(testGame, result.getGameDefinition());
        
        when(clubController.getClubById(2L)).thenReturn(Optional.empty());
        
        result = clubStatsController.getClubGameStatsByGame(2L, 1L);
        
        assertNull(result);
        
        when(gameRepository.findById(2L)).thenReturn(Optional.empty());
        
        result = clubStatsController.getClubGameStatsByGame(1L, 2L);
        
        assertNull(result);
    }

    @Test
    void testGetClubMemberStats() {
        List<ClubMemberStats> clubMemberStatsList = new ArrayList<>();
        ClubMemberStats clubMemberStats = new ClubMemberStats();
        clubMemberStats.setClub(testClub);
        clubMemberStats.setUser(testUser);
        clubMemberStatsList.add(clubMemberStats);
        
        when(clubMemberStatsRepository.findByClub(testClub)).thenReturn(clubMemberStatsList);
        
        List<ClubMemberStats> result = clubStatsController.getClubMemberStats(1L);
        
        assertNotNull(result);
        assertEquals(1, result.size());
        assertEquals(testClub, result.get(0).getClub());
        
        when(clubController.getClubById(2L)).thenReturn(Optional.empty());
        
        result = clubStatsController.getClubMemberStats(2L);
        
        assertTrue(result.isEmpty());
    }

    @Test
    void testGetClubMemberStatsByUser() {
        ClubMemberStats clubMemberStats = new ClubMemberStats();
        clubMemberStats.setClub(testClub);
        clubMemberStats.setUser(testUser);
        
        when(clubMemberStatsRepository.findByClubAndUser(testClub, testUser)).thenReturn(Optional.of(clubMemberStats));
        
        ClubMemberStats result = clubStatsController.getClubMemberStatsByUser(1L, 1L);
        
        assertNotNull(result);
        assertEquals(testClub, result.getClub());
        assertEquals(testUser, result.getUser());
        
        when(clubController.getClubById(2L)).thenReturn(Optional.empty());
        
        result = clubStatsController.getClubMemberStatsByUser(2L, 1L);
        
        assertNull(result);
        
        when(userController.getUser(2L)).thenReturn(null);
        
        result = clubStatsController.getClubMemberStatsByUser(1L, 2L);
        
        assertNull(result);
    }

    @Test
    void testGetTopClubsByPoints() {
        List<ClubGameStats> topClubs = new ArrayList<>();
        ClubGameStats stats1 = new ClubGameStats();
        stats1.setClub(testClub);
        stats1.setTotalPoints(100);
        topClubs.add(stats1);
        
        when(clubGameStatsRepository.findTopClubsByPoints()).thenReturn(topClubs);
        
        List<ClubGameStats> result = clubStatsController.getTopClubsByPoints(10);
        
        assertNotNull(result);
        assertEquals(1, result.size());
        assertEquals(100, result.get(0).getTotalPoints());
    }

    @Test
    void testGetClubDrawStats() {
        ClubStats clubStats = new ClubStats();
        clubStats.setClub(testClub);
        clubStats.setGamesPlayed(10);
        clubStats.setGamesDrawn(2);
        
        when(clubStatsRepository.findByClub(testClub)).thenReturn(Optional.of(clubStats));
        
        Map<String, Object> result = clubStatsController.getClubDrawStats(1L);
        
        assertNotNull(result);
        assertEquals(2, result.get("gamesDrawn"));
        assertEquals(20.0, result.get("drawPercentage"));
        
        when(clubController.getClubById(2L)).thenReturn(Optional.empty());
        
        result = clubStatsController.getClubDrawStats(2L);
        
        assertTrue(result.isEmpty());
    }
}

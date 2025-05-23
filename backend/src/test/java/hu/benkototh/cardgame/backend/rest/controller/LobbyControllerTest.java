package hu.benkototh.cardgame.backend.rest.controller;

import hu.benkototh.cardgame.backend.game.controller.CardGameController;
import hu.benkototh.cardgame.backend.game.model.CardGame;
import hu.benkototh.cardgame.backend.game.model.game.ZsirGame;
import hu.benkototh.cardgame.backend.rest.model.Club;
import hu.benkototh.cardgame.backend.rest.model.Game;
import hu.benkototh.cardgame.backend.rest.model.Lobby;
import hu.benkototh.cardgame.backend.rest.model.User;
import hu.benkototh.cardgame.backend.rest.repository.IGameRepository;
import hu.benkototh.cardgame.backend.rest.repository.ILobbyRepository;
import hu.benkototh.cardgame.backend.rest.repository.IUserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class LobbyControllerTest {

    @Mock
    private ILobbyRepository lobbyRepository;

    @Mock
    private IUserRepository userRepository;

    @Mock
    private IGameRepository gameRepository;

    @Mock
    private CardGameController cardGameController;

    @Mock
    private ClubController clubController;

    @InjectMocks
    private LobbyController lobbyController;

    private User leader;
    private User player;
    private Game game;
    private Club club;
    private Lobby lobby;
    private CardGame cardGame;

    @BeforeEach
    void setUp() {
        leader = new User();
        leader.setId(1L);
        leader.setUsername("leader");

        player = new User();
        player.setId(2L);
        player.setUsername("player");

        game = new Game();
        game.setId(1L);
        game.setName("Test Game");
        game.setMinPlayers(2);
        game.setMaxPlayers(4);

        club = new Club();
        club.setId(1L);
        club.setName("Test Club");

        lobby = new Lobby();
        lobby.setId(1L);
        lobby.setCode("ABCDEF");
        lobby.setLeader(leader);
        lobby.setGame(game);
        lobby.setMinPlayers(game.getMinPlayers());
        lobby.setStatus(LobbyController.STATUS_WAITING);
        lobby.addPlayer(leader);

        cardGame = new ZsirGame();
    }

    @Test
    void testCreateLobby() {
        when(userRepository.findById(1L)).thenReturn(Optional.of(leader));
        when(gameRepository.findById(1L)).thenReturn(Optional.of(game));
        when(lobbyRepository.findAll()).thenReturn(new ArrayList<>());
        when(lobbyRepository.save(any(Lobby.class))).thenReturn(lobby);

        Lobby result = lobbyController.createLobby(1L, 1L, true, true);

        assertNotNull(result);
        assertEquals(leader, result.getLeader());
        assertEquals(game, result.getGame());
        assertTrue(result.isPlayWithPoints());
        assertTrue(result.isPublic());
        assertEquals(LobbyController.STATUS_WAITING, result.getStatus());
        assertTrue(result.getPlayers().contains(leader));
    }

    @Test
    void testCreateLobbyUserNotFound() {
        when(userRepository.findById(1L)).thenReturn(Optional.empty());

        Lobby result = lobbyController.createLobby(1L, 1L, true, true);

        assertNull(result);
    }

    @Test
    void testCreateLobbyGameNotFound() {
        when(userRepository.findById(1L)).thenReturn(Optional.of(leader));
        when(gameRepository.findById(1L)).thenReturn(Optional.empty());

        Lobby result = lobbyController.createLobby(1L, 1L, true, true);

        assertNull(result);
    }

    @Test
    void testCreateLobbyUserAlreadyInLobby() {
        when(userRepository.findById(1L)).thenReturn(Optional.of(leader));
        when(gameRepository.findById(1L)).thenReturn(Optional.of(game));
        when(lobbyRepository.findAll()).thenReturn(List.of(lobby));

        Lobby result = lobbyController.createLobby(1L, 1L, true, true);

        assertNull(result);
    }

    @Test
    void testCreateClubLobby() {
        when(userRepository.findById(1L)).thenReturn(Optional.of(leader));
        when(gameRepository.findById(1L)).thenReturn(Optional.of(game));
        when(clubController.getClubById(1L)).thenReturn(Optional.of(club));
        when(clubController.isUserMemberOfClub(1L, 1L)).thenReturn(true);
        when(lobbyRepository.findAll()).thenReturn(new ArrayList<>());

        Lobby clubLobby = new Lobby();
        clubLobby.setId(1L);
        clubLobby.setCode("ABCDEF");
        clubLobby.setLeader(leader);
        clubLobby.setGame(game);
        clubLobby.setClub(club);
        clubLobby.setMinPlayers(game.getMinPlayers());
        clubLobby.setStatus(LobbyController.STATUS_WAITING);
        clubLobby.addPlayer(leader);

        when(lobbyRepository.save(any(Lobby.class))).thenReturn(clubLobby);

        Lobby result = lobbyController.createClubLobby(1L, 1L, true, 1L);

        assertNotNull(result);
        assertEquals(leader, result.getLeader());
        assertEquals(game, result.getGame());
        assertEquals(club, result.getClub());
        assertTrue(result.isPlayWithPoints());
        assertFalse(result.isPublic());
        assertEquals(LobbyController.STATUS_WAITING, result.getStatus());
        assertTrue(result.getPlayers().contains(leader));
    }

    @Test
    void testCreateClubLobbyUserNotInClub() {
        when(userRepository.findById(1L)).thenReturn(Optional.of(leader));
        when(gameRepository.findById(1L)).thenReturn(Optional.of(game));
        when(clubController.getClubById(1L)).thenReturn(Optional.of(club));
        when(clubController.isUserMemberOfClub(1L, 1L)).thenReturn(false);

        Lobby result = lobbyController.createClubLobby(1L, 1L, true, 1L);

        assertNull(result);
    }

    @Test
    void testJoinLobby() {
        when(lobbyRepository.findAll()).thenReturn(List.of(lobby));
        when(userRepository.findById(2L)).thenReturn(Optional.of(player));
        when(lobbyRepository.save(any(Lobby.class))).thenReturn(lobby);

        Lobby result = lobbyController.joinLobby("ABCDEF", 2L);

        assertNotNull(result);
        assertTrue(result.getPlayers().contains(player));
    }

    @Test
    void testJoinLobbyNotFound() {
        when(lobbyRepository.findAll()).thenReturn(new ArrayList<>());

        Lobby result = lobbyController.joinLobby("ABCDEF", 2L);

        assertNull(result);
    }

    @Test
    void testJoinLobbyUserNotFound() {
        when(lobbyRepository.findAll()).thenReturn(List.of(lobby));
        when(userRepository.findById(2L)).thenReturn(Optional.empty());

        Lobby result = lobbyController.joinLobby("ABCDEF", 2L);

        assertNull(result);
    }

    @Test
    void testJoinLobbyNotWaiting() {
        lobby.setStatus(LobbyController.STATUS_IN_GAME);
        when(lobbyRepository.findAll()).thenReturn(List.of(lobby));
        when(userRepository.findById(2L)).thenReturn(Optional.of(player));

        Lobby result = lobbyController.joinLobby("ABCDEF", 2L);

        assertNull(result);
    }

    @Test
    void testJoinClubLobbyNotMember() {
        lobby.setClub(club);
        when(lobbyRepository.findAll()).thenReturn(List.of(lobby));
        when(userRepository.findById(2L)).thenReturn(Optional.of(player));
        when(clubController.isUserMemberOfClub(2L, 1L)).thenReturn(false);

        Lobby result = lobbyController.joinLobby("ABCDEF", 2L);

        assertNull(result);
    }

    @Test
    void testKickPlayer() {
        lobby.addPlayer(player);
        when(lobbyRepository.findById(1L)).thenReturn(Optional.of(lobby));
        when(userRepository.findById(2L)).thenReturn(Optional.of(player));
        when(lobbyRepository.save(any(Lobby.class))).thenReturn(lobby);

        Lobby result = lobbyController.kickPlayer(1L, 1L, 2L);

        assertNotNull(result);
        assertFalse(result.getPlayers().contains(player));
    }

    @Test
    void testKickPlayerNotLeader() {
        lobby.addPlayer(player);
        when(lobbyRepository.findById(1L)).thenReturn(Optional.of(lobby));
        when(userRepository.findById(2L)).thenReturn(Optional.of(player));

        Lobby result = lobbyController.kickPlayer(1L, 2L, 2L);

        assertNull(result);
    }

    @Test
    void testKickPlayerNotInLobby() {
        when(lobbyRepository.findById(1L)).thenReturn(Optional.of(lobby));
        when(userRepository.findById(2L)).thenReturn(Optional.of(player));

        Lobby result = lobbyController.kickPlayer(1L, 1L, 2L);

        assertNull(result);
    }

    @Test
    void testKickLeader() {
        when(lobbyRepository.findById(1L)).thenReturn(Optional.of(lobby));
        when(userRepository.findById(1L)).thenReturn(Optional.of(leader));

        Lobby result = lobbyController.kickPlayer(1L, 1L, 1L);

        assertNull(result);
    }

    @Test
    void testLeaveLobby() {
        lobby.addPlayer(player);
        when(lobbyRepository.findById(1L)).thenReturn(Optional.of(lobby));
        when(userRepository.findById(2L)).thenReturn(Optional.of(player));
        when(lobbyRepository.save(any(Lobby.class))).thenReturn(lobby);

        Lobby result = lobbyController.leaveLobby(1L, 2L);

        assertNotNull(result);
        assertFalse(result.getPlayers().contains(player));
    }

    @Test
    void testLeaveAsLeaderWithOtherPlayers() {
        lobby.addPlayer(player);
        when(lobbyRepository.findById(1L)).thenReturn(Optional.of(lobby));
        when(userRepository.findById(1L)).thenReturn(Optional.of(leader));
        when(lobbyRepository.save(any(Lobby.class))).thenReturn(lobby);

        Lobby result = lobbyController.leaveLobby(1L, 1L);

        assertNotNull(result);
        assertFalse(result.getPlayers().contains(leader));
        assertEquals(player, result.getLeader());
    }

    @Test
    void testLeaveAsLastPlayer() {
        when(lobbyRepository.findById(1L)).thenReturn(Optional.of(lobby));
        when(userRepository.findById(1L)).thenReturn(Optional.of(leader));

        Lobby result = lobbyController.leaveLobby(1L, 1L);

        assertNull(result);
        verify(lobbyRepository).delete(lobby);
    }

    @Test
    void testUpdateLobbySettings() {
        Game newGame = new Game();
        newGame.setId(2L);
        newGame.setMinPlayers(3);

        when(lobbyRepository.findById(1L)).thenReturn(Optional.of(lobby));
        when(gameRepository.findById(2L)).thenReturn(Optional.of(newGame));
        when(lobbyRepository.save(any(Lobby.class))).thenReturn(lobby);

        Lobby result = lobbyController.updateLobbySettings(1L, 1L, 2L, false, false);

        assertNotNull(result);
        assertEquals(newGame, result.getGame());
        assertFalse(result.isPlayWithPoints());
        assertFalse(result.isPublic());
        assertEquals(newGame.getMinPlayers(), result.getMinPlayers());
    }

    @Test
    void testUpdateLobbySettingsNotLeader() {
        when(lobbyRepository.findById(1L)).thenReturn(Optional.of(lobby));

        Lobby result = lobbyController.updateLobbySettings(1L, 2L, 2L, false, false);

        assertNull(result);
    }

    @Test
    void testStartGameNotLeader() {
        when(lobbyRepository.findById(1L)).thenReturn(Optional.of(lobby));

        Lobby result = lobbyController.startGame(1L, 2L);

        assertNull(result);
    }

    @Test
    void testStartGameNotEnoughPlayers() {
        when(lobbyRepository.findById(1L)).thenReturn(Optional.of(lobby));

        Lobby result = lobbyController.startGame(1L, 1L);

        assertNull(result);
    }

    @Test
    void testGetLobbyById() {
        when(lobbyRepository.findById(1L)).thenReturn(Optional.of(lobby));

        Lobby result = lobbyController.getLobbyById(1L);

        assertNotNull(result);
        assertEquals(1L, result.getId());
    }

    @Test
    void testGetLobbiesByPlayer() {
        when(userRepository.findById(1L)).thenReturn(Optional.of(leader));
        when(lobbyRepository.findAll()).thenReturn(List.of(lobby));

        List<Lobby> result = lobbyController.getLobbiesByPlayer(1L);

        assertEquals(1, result.size());
        assertEquals(1L, result.get(0).getId());
    }

    @Test
    void testGetAllGames() {
        when(gameRepository.findAll()).thenReturn(List.of(game));

        List<Game> result = lobbyController.getAllGames();

        assertEquals(1, result.size());
        assertEquals(1L, result.get(0).getId());
    }

    @Test
    void testGetLobbyByCode() {
        when(lobbyRepository.findAll()).thenReturn(List.of(lobby));

        Lobby result = lobbyController.getLobbyByCode("ABCDEF");

        assertNotNull(result);
        assertEquals("ABCDEF", result.getCode());
    }

    @Test
    void testEndGame() {
        lobby.setCardGameId("g-123456");
        lobby.setStatus(LobbyController.STATUS_IN_GAME);

        when(lobbyRepository.findAll()).thenReturn(List.of(lobby));
        when(lobbyRepository.save(any(Lobby.class))).thenReturn(lobby);

        lobbyController.endGame("g-123456");

        assertNull(lobby.getCardGameId());
        assertEquals(LobbyController.STATUS_WAITING, lobby.getStatus());
        verify(lobbyRepository).save(lobby);
    }

    @Test
    void testGetLobbyByPlayer() {
        when(lobbyRepository.findAll()).thenReturn(List.of(lobby));

        Lobby result = lobbyController.getLobbyByPlayer(1L);

        assertNotNull(result);
        assertEquals(1L, result.getId());
    }

    @Test
    void testGetPublicLobbies() {
        lobby.setPublic(true);
        when(lobbyRepository.findAll()).thenReturn(List.of(lobby));

        List<Lobby> result = lobbyController.getPublicLobbies();

        assertEquals(1, result.size());
        assertEquals(1L, result.get(0).getId());
    }

    @Test
    void testGetClubLobbies() {
        lobby.setClub(club);
        when(lobbyRepository.findAll()).thenReturn(List.of(lobby));

        List<Lobby> result = lobbyController.getClubLobbies(1L);

        assertEquals(1, result.size());
        assertEquals(1L, result.get(0).getId());
    }

    @Test
    void testFindLobbyByCardGameId() {
        lobby.setCardGameId("g-123456");
        when(lobbyRepository.findAll()).thenReturn(List.of(lobby));

        Lobby result = lobbyController.findLobbyByCardGameId("g-123456");

        assertNotNull(result);
        assertEquals(1L, result.getId());
    }
}
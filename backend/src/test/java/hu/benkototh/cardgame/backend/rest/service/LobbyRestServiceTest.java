package hu.benkototh.cardgame.backend.rest.service;

import hu.benkototh.cardgame.backend.rest.Data.Game;
import hu.benkototh.cardgame.backend.rest.Data.Lobby;
import hu.benkototh.cardgame.backend.rest.Data.User;
import hu.benkototh.cardgame.backend.rest.controller.LobbyController;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class LobbyRestServiceTest {

    @Mock
    private LobbyController lobbyController;

    @InjectMocks
    private LobbyRestService lobbyRestService;

    private Lobby lobby;
    private Game game;
    private User leader;

    @BeforeEach
    void setUp() {
        leader = new User();
        leader.setId(1L);
        leader.setUsername("leader");

        game = new Game();
        game.setId(1L);
        game.setName("Test Game");
        game.setMinPlayers(2);

        lobby = new Lobby();
        lobby.setId(1L);
        lobby.setCode("ABCDEF");
        lobby.setLeader(leader);
        lobby.setGame(game);
        lobby.setMinPlayers(game.getMinPlayers());
        lobby.setStatus(LobbyController.STATUS_WAITING);
        lobby.addPlayer(leader);
    }

    @Test
    void testCreateLobbySuccess() {
        when(lobbyController.createLobby(1L, 1L, true, true)).thenReturn(lobby);
        
        ResponseEntity<?> response = lobbyRestService.createLobby(1L, 1L, true, true);
        
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals(lobby, response.getBody());
    }

    @Test
    void testCreateLobbyFailure() {
        when(lobbyController.createLobby(1L, 1L, true, true)).thenReturn(null);
        
        ResponseEntity<?> response = lobbyRestService.createLobby(1L, 1L, true, true);
        
        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        assertTrue(response.getBody() instanceof Map);
        Map<String, String> responseBody = (Map<String, String>) response.getBody();
        assertTrue(responseBody.containsKey("message"));
    }

    @Test
    void testCreateClubLobbySuccess() {
        when(lobbyController.createClubLobby(1L, 1L, true, 1L)).thenReturn(lobby);
        
        ResponseEntity<?> response = lobbyRestService.createClubLobby(1L, 1L, true, 1L);
        
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals(lobby, response.getBody());
    }

    @Test
    void testCreateClubLobbyFailure() {
        when(lobbyController.createClubLobby(1L, 1L, true, 1L)).thenReturn(null);
        
        ResponseEntity<?> response = lobbyRestService.createClubLobby(1L, 1L, true, 1L);
        
        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        assertTrue(response.getBody() instanceof Map);
        Map<String, String> responseBody = (Map<String, String>) response.getBody();
        assertTrue(responseBody.containsKey("message"));
    }

    @Test
    void testJoinLobbySuccess() {
        when(lobbyController.joinLobby("ABCDEF", 2L)).thenReturn(lobby);
        
        ResponseEntity<?> response = lobbyRestService.joinLobby("ABCDEF", 2L);
        
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals(lobby, response.getBody());
    }

    @Test
    void testJoinLobbyFailure() {
        when(lobbyController.joinLobby("ABCDEF", 2L)).thenReturn(null);
        when(lobbyController.getLobbyByCode("ABCDEF")).thenReturn(null);
        
        ResponseEntity<?> response = lobbyRestService.joinLobby("ABCDEF", 2L);
        
        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        assertTrue(response.getBody() instanceof Map);
        Map<String, String> responseBody = (Map<String, String>) response.getBody();
        assertTrue(responseBody.containsKey("message"));
    }

    @Test
    void testKickPlayerSuccess() {
        when(lobbyController.kickPlayer(1L, 1L, 2L)).thenReturn(lobby);
        
        ResponseEntity<?> response = lobbyRestService.kickPlayer(1L, 1L, 2L);
        
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals(lobby, response.getBody());
    }

    @Test
    void testKickPlayerFailure() {
        when(lobbyController.kickPlayer(1L, 1L, 2L)).thenReturn(null);
        
        ResponseEntity<?> response = lobbyRestService.kickPlayer(1L, 1L, 2L);
        
        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        assertTrue(response.getBody() instanceof Map);
        Map<String, String> responseBody = (Map<String, String>) response.getBody();
        assertTrue(responseBody.containsKey("message"));
    }

    @Test
    void testLeaveLobbySuccess() {
        when(lobbyController.leaveLobby(1L, 2L)).thenReturn(lobby);
        
        ResponseEntity<?> response = lobbyRestService.leaveLobby(1L, 2L);
        
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertTrue(response.getBody() instanceof Map);
        Map<String, Object> responseBody = (Map<String, Object>) response.getBody();
        assertEquals("You have left the lobby.", responseBody.get("message"));
        assertEquals(false, responseBody.get("lobbyDeleted"));
        assertEquals(lobby, responseBody.get("lobby"));
    }

    @Test
    void testLeaveLobbyDeleted() {
        when(lobbyController.leaveLobby(1L, 2L)).thenReturn(null);
        
        ResponseEntity<?> response = lobbyRestService.leaveLobby(1L, 2L);
        
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertTrue(response.getBody() instanceof Map);
        Map<String, Object> responseBody = (Map<String, Object>) response.getBody();
        assertTrue(((String) responseBody.get("message")).contains("lobby might have been deleted"));
        assertEquals(true, responseBody.get("lobbyDeleted"));
    }

    @Test
    void testUpdateLobbySettingsSuccess() {
        when(lobbyController.updateLobbySettings(1L, 1L, 1L, true, true)).thenReturn(lobby);
        
        ResponseEntity<?> response = lobbyRestService.updateLobbySettings(1L, 1L, 1L, true, true);
        
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals(lobby, response.getBody());
    }

    @Test
    void testUpdateLobbySettingsFailure() {
        when(lobbyController.updateLobbySettings(1L, 1L, 1L, true, true)).thenReturn(null);
        
        ResponseEntity<?> response = lobbyRestService.updateLobbySettings(1L, 1L, 1L, true, true);
        
        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        assertTrue(response.getBody() instanceof Map);
        Map<String, String> responseBody = (Map<String, String>) response.getBody();
        assertTrue(responseBody.containsKey("message"));
    }

    @Test
    void testStartGameSuccess() {
        when(lobbyController.startGame(1L, 1L)).thenReturn(lobby);
        
        ResponseEntity<?> response = lobbyRestService.startGame(1L, 1L);
        
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals(lobby, response.getBody());
    }

    @Test
    void testStartGameFailure() {
        when(lobbyController.startGame(1L, 1L)).thenReturn(null);
        
        ResponseEntity<?> response = lobbyRestService.startGame(1L, 1L);
        
        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        assertTrue(response.getBody() instanceof Map);
        Map<String, String> responseBody = (Map<String, String>) response.getBody();
        assertTrue(responseBody.containsKey("message"));
    }

    @Test
    void testGetLobbySuccess() {
        when(lobbyController.getLobbyById(1L)).thenReturn(lobby);
        
        ResponseEntity<?> response = lobbyRestService.getLobby(1L);
        
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals(lobby, response.getBody());
    }

    @Test
    void testGetLobbyNotFound() {
        when(lobbyController.getLobbyById(1L)).thenReturn(null);
        
        ResponseEntity<?> response = lobbyRestService.getLobby(1L);
        
        assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
        assertTrue(response.getBody() instanceof Map);
        Map<String, String> responseBody = (Map<String, String>) response.getBody();
        assertEquals("Lobby not found.", responseBody.get("message"));
    }

    @Test
    void testGetLobbyByCodeSuccess() {
        when(lobbyController.getLobbyByCode("ABCDEF")).thenReturn(lobby);
        
        ResponseEntity<?> response = lobbyRestService.getLobbyByCode("ABCDEF");
        
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals(lobby, response.getBody());
    }

    @Test
    void testGetLobbyByCodeNotFound() {
        when(lobbyController.getLobbyByCode("ABCDEF")).thenReturn(null);
        
        ResponseEntity<?> response = lobbyRestService.getLobbyByCode("ABCDEF");
        
        assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
        assertTrue(response.getBody() instanceof Map);
        Map<String, String> responseBody = (Map<String, String>) response.getBody();
        assertEquals("Lobby not found.", responseBody.get("message"));
    }

    @Test
    void testGetLobbiesByPlayer() {
        List<Lobby> lobbies = new ArrayList<>();
        lobbies.add(lobby);
        
        when(lobbyController.getLobbiesByPlayer(1L)).thenReturn(lobbies);
        
        ResponseEntity<List<Lobby>> response = lobbyRestService.getLobbiesByPlayer(1L);
        
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals(1, response.getBody().size());
        assertEquals(1L, response.getBody().get(0).getId());
    }

    @Test
    void testGetLobbyByPlayer() {
        when(lobbyController.getLobbyByPlayer(1L)).thenReturn(lobby);
        
        ResponseEntity<Lobby> response = lobbyRestService.getLobbyByPlayer(1L);
        
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals(lobby, response.getBody());
    }

    @Test
    void testGetAllGames() {
        List<Game> games = new ArrayList<>();
        games.add(game);
        
        when(lobbyController.getAllGames()).thenReturn(games);
        
        ResponseEntity<List<Game>> response = lobbyRestService.getAllGames();
        
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals(1, response.getBody().size());
        assertEquals(1L, response.getBody().get(0).getId());
    }

    @Test
    void testGetPublicLobbies() {
        List<Lobby> lobbies = new ArrayList<>();
        lobbies.add(lobby);
        
        when(lobbyController.getPublicLobbies()).thenReturn(lobbies);
        
        ResponseEntity<List<Lobby>> response = lobbyRestService.getPublicLobbies();
        
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals(1, response.getBody().size());
        assertEquals(1L, response.getBody().get(0).getId());
    }

    @Test
    void testGetClubLobbies() {
        List<Lobby> lobbies = new ArrayList<>();
        lobbies.add(lobby);
        
        when(lobbyController.getClubLobbies(1L)).thenReturn(lobbies);
        
        ResponseEntity<List<Lobby>> response = lobbyRestService.getClubLobbies(1L);
        
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals(1, response.getBody().size());
        assertEquals(1L, response.getBody().get(0).getId());
    }
}
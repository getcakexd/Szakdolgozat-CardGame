package hu.benkototh.cardgame.backend.rest.service;

import hu.benkototh.cardgame.backend.rest.Data.Game;
import hu.benkototh.cardgame.backend.rest.Data.GameCreationDTO;
import hu.benkototh.cardgame.backend.rest.Data.User;
import hu.benkototh.cardgame.backend.rest.controller.AdminController;
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
class AdminRestServiceTest {

    @Mock
    private AdminController adminController;

    @InjectMocks
    private AdminRestService adminRestService;

    private User testUser;
    private Game testGame;
    private GameCreationDTO testGameDTO;

    @BeforeEach
    void setUp() {
        testUser = new User();
        testUser.setId(1L);
        testUser.setUsername("testuser");
        testUser.setEmail("test@example.com");

        testGame = new Game();
        testGame.setId(1L);
        testGame.setName("Test Game");
        testGame.setActive(true);

        testGameDTO = new GameCreationDTO();
        testGameDTO.setName("Test Game");
        testGameDTO.setActive(true);
    }

    @Test
    void testGetAllUsers() {
        List<User> users = new ArrayList<>();
        users.add(testUser);

        when(adminController.getAllUsers()).thenReturn(users);

        ResponseEntity<List<User>> response = adminRestService.getAllUsers();

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals(1, response.getBody().size());
        assertEquals(testUser.getUsername(), response.getBody().get(0).getUsername());
    }

    @Test
    void testCreateUserSuccess() {
        when(adminController.createUser(any(User.class))).thenReturn(testUser);

        ResponseEntity<Map<String, String>> response = adminRestService.createUser(testUser);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals("User created successfully.", response.getBody().get("message"));
    }

    @Test
    void testCreateUserUsernameExists() {
        when(adminController.createUser(any(User.class))).thenReturn(null);
        when(adminController.userExistsByUsername(anyString())).thenReturn(true);

        ResponseEntity<Map<String, String>> response = adminRestService.createUser(testUser);

        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        assertEquals("Username already in use.", response.getBody().get("message"));
    }

    @Test
    void testCreateUserEmailExists() {
        when(adminController.createUser(any(User.class))).thenReturn(null);
        when(adminController.userExistsByUsername(anyString())).thenReturn(false);
        when(adminController.userExistsByEmail(anyString())).thenReturn(true);

        ResponseEntity<Map<String, String>> response = adminRestService.createUser(testUser);

        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        assertEquals("Email already in use.", response.getBody().get("message"));
    }

    @Test
    void testDeleteUserSuccess() {
        when(adminController.deleteUser(1L)).thenReturn(true);

        ResponseEntity<Map<String, String>> response = adminRestService.deleteUser(1L);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals("User deleted successfully.", response.getBody().get("message"));
    }

    @Test
    void testDeleteUserNotFound() {
        when(adminController.deleteUser(1L)).thenReturn(false);

        ResponseEntity<Map<String, String>> response = adminRestService.deleteUser(1L);

        assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
        assertEquals("User not found.", response.getBody().get("message"));
    }

    @Test
    void testPromoteToAgentSuccess() {
        when(adminController.promoteToAgent(1L)).thenReturn(testUser);

        ResponseEntity<Map<String, String>> response = adminRestService.promoteToAgent(1L);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals("User promoted to agent successfully.", response.getBody().get("message"));
    }

    @Test
    void testPromoteToAgentFailed() {
        when(adminController.promoteToAgent(1L)).thenReturn(null);

        ResponseEntity<Map<String, String>> response = adminRestService.promoteToAgent(1L);

        assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
        assertEquals("User not found or only regular users can be promoted to agents.", response.getBody().get("message"));
    }

    @Test
    void testDemoteFromAgentSuccess() {
        when(adminController.demoteFromAgent(1L)).thenReturn(testUser);

        ResponseEntity<Map<String, String>> response = adminRestService.demoteFromAgent(1L);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals("User demoted from agent successfully.", response.getBody().get("message"));
    }

    @Test
    void testDemoteFromAgentFailed() {
        when(adminController.demoteFromAgent(1L)).thenReturn(null);

        ResponseEntity<Map<String, String>> response = adminRestService.demoteFromAgent(1L);

        assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
        assertEquals("User not found or only agents can be demoted to users.", response.getBody().get("message"));
    }

    @Test
    void testPromoteToAdminSuccess() {
        when(adminController.promoteToAdmin(1L)).thenReturn(testUser);

        ResponseEntity<Map<String, String>> response = adminRestService.promoteToAdmin(1L);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals("User promoted to admin successfully.", response.getBody().get("message"));
    }

    @Test
    void testPromoteToAdminFailed() {
        when(adminController.promoteToAdmin(1L)).thenReturn(null);

        ResponseEntity<Map<String, String>> response = adminRestService.promoteToAdmin(1L);

        assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
        assertEquals("User not found or user already has admin or higher privileges.", response.getBody().get("message"));
    }

    @Test
    void testDemoteFromAdminSuccess() {
        when(adminController.demoteFromAdmin(1L)).thenReturn(testUser);

        ResponseEntity<Map<String, String>> response = adminRestService.demoteFromAdmin(1L);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals("User demoted from admin successfully.", response.getBody().get("message"));
    }

    @Test
    void testDemoteFromAdminFailed() {
        when(adminController.demoteFromAdmin(1L)).thenReturn(null);

        ResponseEntity<Map<String, String>> response = adminRestService.demoteFromAdmin(1L);

        assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
        assertEquals("User not found or only admins can be demoted.", response.getBody().get("message"));
    }

    @Test
    void testGetAllGames() {
        List<Game> games = new ArrayList<>();
        games.add(testGame);

        when(adminController.getAllGames()).thenReturn(games);

        ResponseEntity<List<Game>> response = adminRestService.getAllGames();

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals(1, response.getBody().size());
        assertEquals(testGame.getName(), response.getBody().get(0).getName());
    }

    @Test
    void testCreateGameSuccess() {
        when(adminController.createGame(any(GameCreationDTO.class))).thenReturn(testGame);

        ResponseEntity<Map<String, String>> response = adminRestService.createGame(testGameDTO);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals("Game created successfully.", response.getBody().get("message"));
        assertEquals(String.valueOf(testGame.getId()), response.getBody().get("id"));
    }

    @Test
    void testCreateGameNameExists() {
        when(adminController.createGame(any(GameCreationDTO.class))).thenReturn(null);

        ResponseEntity<Map<String, String>> response = adminRestService.createGame(testGameDTO);

        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        assertEquals("Game with this name already exists.", response.getBody().get("message"));
    }

    @Test
    void testUpdateGameSuccess() {
        when(adminController.updateGame(any(GameCreationDTO.class), eq(1L))).thenReturn(testGame);

        ResponseEntity<Map<String, String>> response = adminRestService.updateGame(1L, testGameDTO);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals("Game updated successfully.", response.getBody().get("message"));
    }

    @Test
    void testUpdateGameFailed() {
        when(adminController.updateGame(any(GameCreationDTO.class), eq(1L))).thenReturn(null);

        ResponseEntity<Map<String, String>> response = adminRestService.updateGame(1L, testGameDTO);

        assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
        assertEquals("Game not found or name already exists.", response.getBody().get("message"));
    }

    @Test
    void testDeleteGameSuccess() {
        when(adminController.deleteGame(1L)).thenReturn(true);

        ResponseEntity<Map<String, String>> response = adminRestService.deleteGame(1L);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals("Game deleted successfully.", response.getBody().get("message"));
    }

    @Test
    void testDeleteGameNotFound() {
        when(adminController.deleteGame(1L)).thenReturn(false);

        ResponseEntity<Map<String, String>> response = adminRestService.deleteGame(1L);

        assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
        assertEquals("Game not found.", response.getBody().get("message"));
    }
}
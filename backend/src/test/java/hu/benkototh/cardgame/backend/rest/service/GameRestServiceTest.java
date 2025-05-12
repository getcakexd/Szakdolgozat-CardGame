package hu.benkototh.cardgame.backend.rest.service;

import hu.benkototh.cardgame.backend.rest.Data.Game;
import hu.benkototh.cardgame.backend.rest.Data.GameDescription;
import hu.benkototh.cardgame.backend.rest.Data.GameRules;
import hu.benkototh.cardgame.backend.rest.controller.GameController;
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
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class GameRestServiceTest {

    @Mock
    private GameController gameController;

    @InjectMocks
    private GameRestService gameRestService;

    private Game testGame;
    private GameDescription testDescription;
    private GameRules testRules;

    @BeforeEach
    void setUp() {
        testGame = new Game();
        testGame.setId(1L);
        testGame.setName("Test Game");
        testGame.setActive(true);
        testGame.setMinPlayers(2);
        testGame.setMaxPlayers(4);

        testDescription = new GameDescription("en", "Test description", false);
        testRules = new GameRules("en", "Test rules", false);
    }

    @Test
    void testGetAllGames() {
        List<Game> games = new ArrayList<>();
        games.add(testGame);

        when(gameController.getAllGames()).thenReturn(games);

        ResponseEntity<List<Game>> response = gameRestService.getAllGames();

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals(1, response.getBody().size());
        assertEquals(testGame.getName(), response.getBody().get(0).getName());
    }

    @Test
    void testGetActiveGames() {
        List<Game> games = new ArrayList<>();
        games.add(testGame);

        when(gameController.getActiveGames()).thenReturn(games);

        ResponseEntity<List<Game>> response = gameRestService.getActiveGames();

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals(1, response.getBody().size());
        assertEquals(testGame.getName(), response.getBody().get(0).getName());
    }

    @Test
    void testGetGameByIdSuccess() {
        when(gameController.getGameById(1L)).thenReturn(Optional.of(testGame));

        ResponseEntity<?> response = gameRestService.getGameById(1L);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals(testGame, response.getBody());
    }

    @Test
    void testGetGameByIdNotFound() {
        when(gameController.getGameById(1L)).thenReturn(Optional.empty());

        ResponseEntity<?> response = gameRestService.getGameById(1L);

        assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
        assertTrue(response.getBody() instanceof Map);
        assertEquals("Game not found", ((Map<String, String>) response.getBody()).get("message"));
    }

    @Test
    void testGetGameByNameSuccess() {
        when(gameController.getGameByName("Test Game")).thenReturn(Optional.of(testGame));

        ResponseEntity<?> response = gameRestService.getGameByName("Test Game");

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals(testGame, response.getBody());
    }

    @Test
    void testGetGameByNameNotFound() {
        when(gameController.getGameByName("Test Game")).thenReturn(Optional.empty());

        ResponseEntity<?> response = gameRestService.getGameByName("Test Game");

        assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
        assertTrue(response.getBody() instanceof Map);
        assertEquals("Game not found", ((Map<String, String>) response.getBody()).get("message"));
    }

    @Test
    void testCreateGameSuccess() {
        when(gameController.createGame(any(Game.class))).thenReturn(testGame);

        ResponseEntity<Map<String, String>> response = gameRestService.createGame(testGame);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals("Game created successfully.", response.getBody().get("message"));
        assertEquals(String.valueOf(testGame.getId()), response.getBody().get("id"));
    }

    @Test
    void testCreateGameNameExists() {
        when(gameController.createGame(any(Game.class))).thenReturn(null);

        ResponseEntity<Map<String, String>> response = gameRestService.createGame(testGame);

        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        assertEquals("Game with this name already exists.", response.getBody().get("message"));
    }

    @Test
    void testUpdateGameSuccess() {
        when(gameController.updateGame(any(Game.class))).thenReturn(testGame);

        ResponseEntity<Map<String, String>> response = gameRestService.updateGame(testGame);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals("Game updated successfully.", response.getBody().get("message"));
    }

    @Test
    void testUpdateGameNotFound() {
        when(gameController.updateGame(any(Game.class))).thenReturn(null);

        ResponseEntity<Map<String, String>> response = gameRestService.updateGame(testGame);

        assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
        assertEquals("Game not found.", response.getBody().get("message"));
    }

    @Test
    void testDeleteGameSuccess() {
        when(gameController.deleteGame(1L)).thenReturn(true);

        ResponseEntity<Map<String, String>> response = gameRestService.deleteGame(1L);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals("Game deleted successfully.", response.getBody().get("message"));
    }

    @Test
    void testDeleteGameNotFound() {
        when(gameController.deleteGame(1L)).thenReturn(false);

        ResponseEntity<Map<String, String>> response = gameRestService.deleteGame(1L);

        assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
        assertEquals("Game not found.", response.getBody().get("message"));
    }

    @Test
    void testAddGameDescriptionSuccess() {
        when(gameController.addGameDescription(eq(1L), any(GameDescription.class))).thenReturn(testDescription);

        ResponseEntity<Map<String, String>> response = gameRestService.addGameDescription(1L, testDescription);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals("Game description added/updated successfully.", response.getBody().get("message"));
    }

    @Test
    void testAddGameDescriptionGameNotFound() {
        when(gameController.addGameDescription(eq(1L), any(GameDescription.class))).thenReturn(null);

        ResponseEntity<Map<String, String>> response = gameRestService.addGameDescription(1L, testDescription);

        assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
        assertEquals("Game not found.", response.getBody().get("message"));
    }

    @Test
    void testAddGameRulesSuccess() {
        when(gameController.addGameRules(eq(1L), any(GameRules.class))).thenReturn(testRules);

        ResponseEntity<Map<String, String>> response = gameRestService.addGameRules(1L, testRules);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals("Game rules added/updated successfully.", response.getBody().get("message"));
    }

    @Test
    void testAddGameRulesGameNotFound() {
        when(gameController.addGameRules(eq(1L), any(GameRules.class))).thenReturn(null);

        ResponseEntity<Map<String, String>> response = gameRestService.addGameRules(1L, testRules);

        assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
        assertEquals("Game not found.", response.getBody().get("message"));
    }

    @Test
    void testRemoveGameDescriptionSuccess() {
        when(gameController.removeGameDescription(1L, "en")).thenReturn(true);

        ResponseEntity<Map<String, String>> response = gameRestService.removeGameDescription(1L, "en");

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals("Game description removed successfully.", response.getBody().get("message"));
    }

    @Test
    void testRemoveGameDescriptionNotFound() {
        when(gameController.removeGameDescription(1L, "en")).thenReturn(false);

        ResponseEntity<Map<String, String>> response = gameRestService.removeGameDescription(1L, "en");

        assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
        assertEquals("Game or description not found.", response.getBody().get("message"));
    }

    @Test
    void testRemoveGameRulesSuccess() {
        when(gameController.removeGameRules(1L, "en")).thenReturn(true);

        ResponseEntity<Map<String, String>> response = gameRestService.removeGameRules(1L, "en");

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals("Game rules removed successfully.", response.getBody().get("message"));
    }

    @Test
    void testRemoveGameRulesNotFound() {
        when(gameController.removeGameRules(1L, "en")).thenReturn(false);

        ResponseEntity<Map<String, String>> response = gameRestService.removeGameRules(1L, "en");

        assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
        assertEquals("Game or rules not found.", response.getBody().get("message"));
    }
}
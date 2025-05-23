package hu.benkototh.cardgame.backend.rest.controller;

import hu.benkototh.cardgame.backend.rest.model.Game;
import hu.benkototh.cardgame.backend.rest.model.GameDescription;
import hu.benkototh.cardgame.backend.rest.model.GameRules;
import hu.benkototh.cardgame.backend.rest.repository.IGameRepository;
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
class GameControllerTest {

    @Mock
    private IGameRepository gameRepository;

    @Mock
    private AuditLogController auditLogController;

    @InjectMocks
    private GameController gameController;

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
        testGame.setFactorySign("TestFactory");
        testGame.setFactoryId(1);

        testDescription = new GameDescription("en", "Test description", false);
        testRules = new GameRules("en", "Test rules", false);
    }

    @Test
    void testGetAllGames() {
        List<Game> games = new ArrayList<>();
        games.add(testGame);
        
        when(gameRepository.findAll()).thenReturn(games);
        
        List<Game> result = gameController.getAllGames();
        
        assertEquals(1, result.size());
        assertEquals(testGame.getName(), result.get(0).getName());
    }

    @Test
    void testGetActiveGames() {
        List<Game> games = new ArrayList<>();
        games.add(testGame);
        
        Game inactiveGame = new Game();
        inactiveGame.setId(2L);
        inactiveGame.setName("Inactive Game");
        inactiveGame.setActive(false);
        games.add(inactiveGame);
        
        when(gameRepository.findAll()).thenReturn(games);
        
        List<Game> result = gameController.getActiveGames();
        
        assertEquals(1, result.size());
        assertEquals(testGame.getName(), result.get(0).getName());
    }

    @Test
    void testGetGameById() {
        when(gameRepository.findById(1L)).thenReturn(Optional.of(testGame));
        
        Optional<Game> result = gameController.getGameById(1L);
        
        assertTrue(result.isPresent());
        assertEquals(testGame.getName(), result.get().getName());
    }

    @Test
    void testGetGameByName() {
        List<Game> games = new ArrayList<>();
        games.add(testGame);
        
        when(gameRepository.findAll()).thenReturn(games);
        
        Optional<Game> result = gameController.getGameByName("Test Game");
        
        assertTrue(result.isPresent());
        assertEquals(testGame.getId(), result.get().getId());
    }

    @Test
    void testCreateGameSuccess() {
        when(gameRepository.findAll()).thenReturn(new ArrayList<>());
        when(gameRepository.save(any(Game.class))).thenReturn(testGame);
        
        Game result = gameController.createGame(testGame);
        
        assertNotNull(result);
        assertEquals(testGame.getName(), result.getName());
        verify(gameRepository).save(testGame);
        verify(auditLogController).logAction(eq("CREATED_GAME"), eq(0L), anyString());
    }

    @Test
    void testCreateGameNameExists() {
        List<Game> existingGames = new ArrayList<>();
        existingGames.add(testGame);
        
        when(gameRepository.findAll()).thenReturn(existingGames);
        
        Game newGame = new Game();
        newGame.setName("Test Game");
        
        Game result = gameController.createGame(newGame);
        
        assertNull(result);
        verify(gameRepository, never()).save(any(Game.class));
        verify(auditLogController).logAction(eq("GAME_CREATION_FAILED"), eq(0L), anyString());
    }

    @Test
    void testUpdateGameSuccess() {
        when(gameRepository.existsById(1L)).thenReturn(true);
        when(gameRepository.save(any(Game.class))).thenReturn(testGame);
        
        Game result = gameController.updateGame(testGame);
        
        assertNotNull(result);
        assertEquals(testGame.getName(), result.getName());
        verify(gameRepository).save(testGame);
        verify(auditLogController).logAction(eq("UPDATED_GAME"), eq(0L), anyString());
    }

    @Test
    void testUpdateGameNotFound() {
        when(gameRepository.existsById(1L)).thenReturn(false);
        
        Game result = gameController.updateGame(testGame);
        
        assertNull(result);
        verify(gameRepository, never()).save(any(Game.class));
        verify(auditLogController).logAction(eq("GAME_UPDATE_FAILED"), eq(0L), anyString());
    }

    @Test
    void testDeleteGameSuccess() {
        when(gameRepository.findById(1L)).thenReturn(Optional.of(testGame));
        
        boolean result = gameController.deleteGame(1L);
        
        assertTrue(result);
        verify(gameRepository).delete(testGame);
        verify(auditLogController).logAction(eq("DELETED_GAME"), eq(0L), anyString());
    }

    @Test
    void testDeleteGameNotFound() {
        when(gameRepository.findById(1L)).thenReturn(Optional.empty());
        
        boolean result = gameController.deleteGame(1L);
        
        assertFalse(result);
        verify(gameRepository, never()).delete(any(Game.class));
        verify(auditLogController).logAction(eq("GAME_DELETION_FAILED"), eq(0L), anyString());
    }

    @Test
    void testAddGameDescriptionSuccess() {
        when(gameRepository.findById(1L)).thenReturn(Optional.of(testGame));
        when(gameRepository.save(any(Game.class))).thenReturn(testGame);
        
        GameDescription result = gameController.addGameDescription(1L, testDescription);
        
        assertNotNull(result);
        assertEquals(testDescription.getLanguage(), result.getLanguage());
        assertEquals(testDescription.getContent(), result.getContent());
        verify(gameRepository).save(testGame);
        verify(auditLogController).logAction(eq("ADDED_GAME_DESCRIPTION"), eq(0L), anyString());
    }

    @Test
    void testAddGameDescriptionGameNotFound() {
        when(gameRepository.findById(1L)).thenReturn(Optional.empty());
        
        GameDescription result = gameController.addGameDescription(1L, testDescription);
        
        assertNull(result);
        verify(gameRepository, never()).save(any(Game.class));
        verify(auditLogController).logAction(eq("GAME_DESCRIPTION_ADD_FAILED"), eq(0L), anyString());
    }

    @Test
    void testAddGameRulesSuccess() {
        when(gameRepository.findById(1L)).thenReturn(Optional.of(testGame));
        when(gameRepository.save(any(Game.class))).thenReturn(testGame);
        
        GameRules result = gameController.addGameRules(1L, testRules);
        
        assertNotNull(result);
        assertEquals(testRules.getLanguage(), result.getLanguage());
        assertEquals(testRules.getContent(), result.getContent());
        verify(gameRepository).save(testGame);
        verify(auditLogController).logAction(eq("ADDED_GAME_RULES"), eq(0L), anyString());
    }

    @Test
    void testAddGameRulesGameNotFound() {
        when(gameRepository.findById(1L)).thenReturn(Optional.empty());
        
        GameRules result = gameController.addGameRules(1L, testRules);
        
        assertNull(result);
        verify(gameRepository, never()).save(any(Game.class));
        verify(auditLogController).logAction(eq("GAME_RULES_ADD_FAILED"), eq(0L), anyString());
    }

    @Test
    void testRemoveGameDescriptionSuccess() {
        testGame.addDescription(testDescription);
        
        when(gameRepository.findById(1L)).thenReturn(Optional.of(testGame));
        when(gameRepository.save(any(Game.class))).thenReturn(testGame);
        
        boolean result = gameController.removeGameDescription(1L, "en");
        
        assertTrue(result);
        verify(gameRepository).save(testGame);
        verify(auditLogController).logAction(eq("REMOVED_GAME_DESCRIPTION"), eq(0L), anyString());
    }

    @Test
    void testRemoveGameDescriptionGameNotFound() {
        when(gameRepository.findById(1L)).thenReturn(Optional.empty());
        
        boolean result = gameController.removeGameDescription(1L, "en");
        
        assertFalse(result);
        verify(gameRepository, never()).save(any(Game.class));
        verify(auditLogController).logAction(eq("GAME_DESCRIPTION_REMOVE_FAILED"), eq(0L), anyString());
    }

    @Test
    void testRemoveGameRulesSuccess() {
        testGame.addRules(testRules);
        
        when(gameRepository.findById(1L)).thenReturn(Optional.of(testGame));
        when(gameRepository.save(any(Game.class))).thenReturn(testGame);
        
        boolean result = gameController.removeGameRules(1L, "en");
        
        assertTrue(result);
        verify(gameRepository).save(testGame);
        verify(auditLogController).logAction(eq("REMOVED_GAME_RULES"), eq(0L), anyString());
    }

    @Test
    void testRemoveGameRulesGameNotFound() {
        when(gameRepository.findById(1L)).thenReturn(Optional.empty());
        
        boolean result = gameController.removeGameRules(1L, "en");
        
        assertFalse(result);
        verify(gameRepository, never()).save(any(Game.class));
        verify(auditLogController).logAction(eq("GAME_RULES_REMOVE_FAILED"), eq(0L), anyString());
    }
}
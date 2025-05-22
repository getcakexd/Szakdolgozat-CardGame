package hu.benkototh.cardgame.backend.rest.controller;

import hu.benkototh.cardgame.backend.rest.model.*;
import hu.benkototh.cardgame.backend.rest.repository.IGameRepository;
import hu.benkototh.cardgame.backend.rest.repository.IUserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AdminControllerTest {

    @Mock
    private IUserRepository userRepository;

    @Mock
    private IGameRepository gameRepository;

    @Mock
    private AuditLogController auditLogController;

    @Spy
    private BCryptPasswordEncoder passwordEncoder = new BCryptPasswordEncoder();

    @InjectMocks
    private AdminController adminController;

    private User testUser;
    private Game testGame;
    private GameCreationDTO testGameDTO;

    @BeforeEach
    void setUp() {
        testUser = new User();
        testUser.setId(1L);
        testUser.setUsername("testuser");
        testUser.setEmail("test@example.com");
        testUser.setPassword("password");
        testUser.setRole("ROLE_USER");

        testGame = new Game();
        testGame.setId(1L);
        testGame.setName("Test Game");
        testGame.setActive(true);
        testGame.setMinPlayers(2);
        testGame.setMaxPlayers(4);
        testGame.setFactorySign("TestFactory");
        testGame.setFactoryId(1);

        testGameDTO = new GameCreationDTO();
        testGameDTO.setName("Test Game");
        testGameDTO.setActive(true);
        testGameDTO.setMinPlayers(2);
        testGameDTO.setMaxPlayers(4);
        testGameDTO.setFactorySign("TestFactory");
        testGameDTO.setFactoryId(1);
    }

    @Test
    void testGetAllUsers() {
        List<User> users = new ArrayList<>();
        users.add(testUser);

        when(userRepository.findAll()).thenReturn(users);

        List<User> result = adminController.getAllUsers();

        assertEquals(1, result.size());
        assertEquals(testUser.getUsername(), result.get(0).getUsername());
    }

    @Test
    void testCreateUserSuccess() {
        when(userRepository.findAll()).thenReturn(new ArrayList<>());
        when(userRepository.save(any(User.class))).thenReturn(testUser);

        User result = adminController.createUser(testUser);

        assertNotNull(result);
        assertEquals(testUser.getUsername(), result.getUsername());
        verify(userRepository).save(any(User.class));
        verify(auditLogController).logAction(eq("ADMIN_CREATED_USER"), eq(0L), anyString());
    }

    @Test
    void testCreateUserUsernameExists() {
        List<User> existingUsers = new ArrayList<>();
        existingUsers.add(testUser);

        when(userRepository.findAll()).thenReturn(existingUsers);

        User newUser = new User();
        newUser.setUsername("testuser");
        newUser.setEmail("new@example.com");

        User result = adminController.createUser(newUser);

        assertNull(result);
        verify(userRepository, never()).save(any(User.class));
        verify(auditLogController).logAction(eq("ADMIN_USER_CREATION_FAILED"), eq(0L), anyString());
    }

    @Test
    void testDeleteUserSuccess() {
        when(userRepository.findById(1L)).thenReturn(Optional.of(testUser));

        boolean result = adminController.deleteUser(1L);

        assertTrue(result);
        verify(userRepository).delete(testUser);
        verify(auditLogController).logAction(eq("ADMIN_DELETED_USER"), eq(0L), anyString());
    }

    @Test
    void testDeleteUserNotFound() {
        when(userRepository.findById(1L)).thenReturn(Optional.empty());

        boolean result = adminController.deleteUser(1L);

        assertFalse(result);
        verify(userRepository, never()).delete(any(User.class));
        verify(auditLogController).logAction(eq("ADMIN_USER_DELETION_FAILED"), eq(0L), anyString());
    }

    @Test
    void testPromoteToAgentSuccess() {
        testUser.setRole("ROLE_USER");
        when(userRepository.findById(1L)).thenReturn(Optional.of(testUser));
        when(userRepository.save(any(User.class))).thenReturn(testUser);

        User result = adminController.promoteToAgent(1L);

        assertNotNull(result);
        assertEquals("ROLE_AGENT", result.getRole());
        verify(userRepository).save(testUser);
        verify(auditLogController).logAction(eq("ADMIN_PROMOTED_TO_AGENT"), eq(0L), anyString());
    }

    @Test
    void testPromoteToAgentNotUser() {
        testUser.setRole("ROLE_ADMIN");
        when(userRepository.findById(1L)).thenReturn(Optional.of(testUser));

        User result = adminController.promoteToAgent(1L);

        assertNull(result);
        verify(userRepository, never()).save(any(User.class));
        verify(auditLogController).logAction(eq("ADMIN_PROMOTION_FAILED"), eq(0L), anyString());
    }

    @Test
    void testDemoteFromAgentSuccess() {
        testUser.setRole("ROLE_AGENT");
        when(userRepository.findById(1L)).thenReturn(Optional.of(testUser));
        when(userRepository.save(any(User.class))).thenReturn(testUser);

        User result = adminController.demoteFromAgent(1L);

        assertNotNull(result);
        assertEquals("ROLE_USER", result.getRole());
        verify(userRepository).save(testUser);
        verify(auditLogController).logAction(eq("ADMIN_DEMOTED_FROM_AGENT"), eq(0L), anyString());
    }

    @Test
    void testPromoteToAdminSuccess() {
        testUser.setRole("ROLE_USER");
        when(userRepository.findById(1L)).thenReturn(Optional.of(testUser));
        when(userRepository.save(any(User.class))).thenReturn(testUser);

        User result = adminController.promoteToAdmin(1L);

        assertNotNull(result);
        assertEquals("ROLE_ADMIN", result.getRole());
        verify(userRepository).save(testUser);
        verify(auditLogController).logAction(eq("ADMIN_PROMOTED_TO_ADMIN"), eq(0L), anyString());
    }

    @Test
    void testDemoteFromAdminSuccess() {
        testUser.setRole("ROLE_ADMIN");
        when(userRepository.findById(1L)).thenReturn(Optional.of(testUser));
        when(userRepository.save(any(User.class))).thenReturn(testUser);

        User result = adminController.demoteFromAdmin(1L);

        assertNotNull(result);
        assertEquals("ROLE_AGENT", result.getRole());
        verify(userRepository).save(testUser);
        verify(auditLogController).logAction(eq("ADMIN_DEMOTED_FROM_ADMIN"), eq(0L), anyString());
    }

    @Test
    void testGetAllGames() {
        List<Game> games = new ArrayList<>();
        games.add(testGame);

        when(gameRepository.findAll()).thenReturn(games);

        List<Game> result = adminController.getAllGames();

        assertEquals(1, result.size());
        assertEquals(testGame.getName(), result.get(0).getName());
    }

    @Test
    void testCreateGameSuccess() {
        when(gameRepository.findAll()).thenReturn(new ArrayList<>());
        when(gameRepository.save(any(Game.class))).thenReturn(testGame);

        Game result = adminController.createGame(testGameDTO);

        assertNotNull(result);
        assertEquals(testGame.getName(), result.getName());
        verify(gameRepository).save(any(Game.class));
        verify(auditLogController).logAction(eq("ADMIN_CREATED_GAME"), eq(0L), anyString());
    }

    @Test
    void testCreateGameNameExists() {
        List<Game> existingGames = new ArrayList<>();
        existingGames.add(testGame);

        when(gameRepository.findAll()).thenReturn(existingGames);

        Game result = adminController.createGame(testGameDTO);

        assertNull(result);
        verify(gameRepository, never()).save(any(Game.class));
        verify(auditLogController).logAction(eq("ADMIN_GAME_CREATION_FAILED"), eq(0L), anyString());
    }

    @Test
    void testDeleteGameSuccess() {
        when(gameRepository.findById(1L)).thenReturn(Optional.of(testGame));

        boolean result = adminController.deleteGame(1L);

        assertTrue(result);
        verify(gameRepository).delete(testGame);
        verify(auditLogController).logAction(eq("ADMIN_DELETED_GAME"), eq(0L), anyString());
    }

    @Test
    void testDeleteGameNotFound() {
        when(gameRepository.findById(1L)).thenReturn(Optional.empty());

        boolean result = adminController.deleteGame(1L);

        assertFalse(result);
        verify(gameRepository, never()).delete(any(Game.class));
        verify(auditLogController).logAction(eq("ADMIN_GAME_DELETION_FAILED"), eq(0L), anyString());
    }

    @Test
    void testUpdateGameSuccess() {
        when(gameRepository.findById(1L)).thenReturn(Optional.of(testGame));
        when(gameRepository.save(any(Game.class))).thenReturn(testGame);

        Game result = adminController.updateGame(testGameDTO, 1L);

        assertNotNull(result);
        assertEquals(testGame.getName(), result.getName());
        verify(gameRepository).save(any(Game.class));
        verify(auditLogController).logAction(eq("ADMIN_UPDATED_GAME"), eq(0L), anyString());
    }

    @Test
    void testUpdateGameNotFound() {
        when(gameRepository.findById(1L)).thenReturn(Optional.empty());

        Game result = adminController.updateGame(testGameDTO, 1L);

        assertNull(result);
        verify(gameRepository, never()).save(any(Game.class));
        verify(auditLogController).logAction(eq("ADMIN_GAME_UPDATE_FAILED"), eq(0L), anyString());
    }

    @Test
    void testUserExistsByUsername() {
        List<User> users = new ArrayList<>();
        users.add(testUser);

        when(userRepository.findAll()).thenReturn(users);

        assertTrue(adminController.userExistsByUsername("testuser"));
        assertFalse(adminController.userExistsByUsername("nonexistent"));
    }

    @Test
    void testUserExistsByEmail() {
        List<User> users = new ArrayList<>();
        users.add(testUser);

        when(userRepository.findAll()).thenReturn(users);

        assertTrue(adminController.userExistsByEmail("test@example.com"));
        assertFalse(adminController.userExistsByEmail("nonexistent@example.com"));
    }

    @Test
    void testGameExistsByName() {
        List<Game> games = new ArrayList<>();
        games.add(testGame);

        when(gameRepository.findAll()).thenReturn(games);

        assertTrue(adminController.gameExistsByName("Test Game"));
        assertFalse(adminController.gameExistsByName("Nonexistent Game"));
    }
}
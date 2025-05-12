package hu.benkototh.cardgame.backend.rest.controller;

import hu.benkototh.cardgame.backend.rest.Data.LobbyMessage;
import hu.benkototh.cardgame.backend.rest.Data.User;
import hu.benkototh.cardgame.backend.rest.repository.ILobbyMessageRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class LobbyChatControllerTest {

    @Mock
    private ILobbyMessageRepository lobbyMessageRepository;

    @Mock
    private UserController userController;

    @Mock
    private AuditLogController auditLogController;

    @InjectMocks
    private LobbyChatController lobbyChatController;

    private User user;
    private LobbyMessage lobbyMessage;
    private LobbyMessage gameMessage;

    @BeforeEach
    void setUp() {
        user = new User();
        user.setId(1L);
        user.setUsername("testuser");

        lobbyMessage = new LobbyMessage();
        lobbyMessage.setId(1L);
        lobbyMessage.setUser(user);
        lobbyMessage.setContent("Test lobby message");
        lobbyMessage.setLobbyId(1L);
        lobbyMessage.setLobbyMessage(true);
        lobbyMessage.setTimestamp(new Date());
        lobbyMessage.setStatus("active");

        gameMessage = new LobbyMessage();
        gameMessage.setId(2L);
        gameMessage.setUser(user);
        gameMessage.setContent("Test game message");
        gameMessage.setGameId("g-123456");
        gameMessage.setLobbyMessage(false);
        gameMessage.setTimestamp(new Date());
        gameMessage.setStatus("active");
    }

    @Test
    void testGetLobbyMessages() {
        List<LobbyMessage> messages = new ArrayList<>();
        messages.add(lobbyMessage);
        
        when(lobbyMessageRepository.findTop100ByLobbyIdOrderByTimestampDesc(1L)).thenReturn(messages);
        
        List<LobbyMessage> result = lobbyChatController.getLobbyMessages(1L);
        
        assertEquals(1, result.size());
        assertEquals(1L, result.get(0).getId());
    }

    @Test
    void testGetGameMessages() {
        List<LobbyMessage> messages = new ArrayList<>();
        messages.add(gameMessage);
        
        when(lobbyMessageRepository.findTop100ByGameIdOrderByTimestampDesc("g-123456")).thenReturn(messages);
        
        List<LobbyMessage> result = lobbyChatController.getGameMessages("g-123456");
        
        assertEquals(1, result.size());
        assertEquals(2L, result.get(0).getId());
    }

    @Test
    void testSendLobbyMessage() {
        when(userController.getUser(1L)).thenReturn(user);
        when(lobbyMessageRepository.save(any(LobbyMessage.class))).thenReturn(lobbyMessage);
        
        LobbyMessage result = lobbyChatController.sendLobbyMessage(1L, 1L, "Test lobby message");
        
        assertNotNull(result);
        assertEquals(1L, result.getId());
        assertEquals(user, result.getUser());
        assertEquals("Test lobby message", result.getContent());
        assertEquals(1L, result.getLobbyId());
        assertTrue(result.isLobbyMessage());
        assertEquals("active", result.getStatus());
        
        verify(auditLogController).logAction(eq("LOBBY_MESSAGE_SENT"), eq(1L), anyString());
    }

    @Test
    void testSendLobbyMessageUserNotFound() {
        when(userController.getUser(1L)).thenReturn(null);
        
        LobbyMessage result = lobbyChatController.sendLobbyMessage(1L, 1L, "Test lobby message");
        
        assertNull(result);
    }

    @Test
    void testSendGameMessage() {
        when(userController.getUser(1L)).thenReturn(user);
        when(lobbyMessageRepository.save(any(LobbyMessage.class))).thenReturn(gameMessage);
        
        LobbyMessage result = lobbyChatController.sendGameMessage(1L, "g-123456", "Test game message");
        
        assertNotNull(result);
        assertEquals(2L, result.getId());
        assertEquals(user, result.getUser());
        assertEquals("Test game message", result.getContent());
        assertEquals("g-123456", result.getGameId());
        assertFalse(result.isLobbyMessage());
        assertEquals("active", result.getStatus());
        
        verify(auditLogController).logAction(eq("GAME_MESSAGE_SENT"), eq(1L), anyString());
    }

    @Test
    void testSendGameMessageUserNotFound() {
        when(userController.getUser(1L)).thenReturn(null);
        
        LobbyMessage result = lobbyChatController.sendGameMessage(1L, "g-123456", "Test game message");
        
        assertNull(result);
    }

    @Test
    void testUnsendLobbyMessage() {
        when(lobbyMessageRepository.findById(1L)).thenReturn(Optional.of(lobbyMessage));
        when(lobbyMessageRepository.save(any(LobbyMessage.class))).thenAnswer(invocation -> invocation.getArgument(0));
        
        LobbyMessage result = lobbyChatController.unsendLobbyMessage(1L);
        
        assertNotNull(result);
        assertEquals(1L, result.getId());
        assertEquals(user, result.getUser());
        assertEquals("This message has been unsent.", result.getContent());
        assertEquals("unsent", result.getStatus());
        
        verify(auditLogController).logAction(eq("LOBBY_MESSAGE_UNSENT"), eq(1L), anyString());
    }

    @Test
    void testUnsendLobbyMessageNotFound() {
        when(lobbyMessageRepository.findById(1L)).thenReturn(Optional.empty());
        
        LobbyMessage result = lobbyChatController.unsendLobbyMessage(1L);
        
        assertNull(result);
    }

    @Test
    void testRemoveMessage() {
        when(lobbyMessageRepository.findById(1L)).thenReturn(Optional.of(lobbyMessage));
        when(lobbyMessageRepository.save(any(LobbyMessage.class))).thenAnswer(invocation -> invocation.getArgument(0));
        
        LobbyMessage result = lobbyChatController.removeMessage(1L);
        
        assertNotNull(result);
        assertEquals(1L, result.getId());
        assertEquals(user, result.getUser());
        assertEquals("This message has been removed by a moderator.", result.getContent());
        assertEquals("removed", result.getStatus());
        
        verify(auditLogController).logAction(eq("LOBBY_MESSAGE_REMOVED"), eq(0L), anyString());
    }

    @Test
    void testRemoveMessageNotFound() {
        when(lobbyMessageRepository.findById(1L)).thenReturn(Optional.empty());
        
        LobbyMessage result = lobbyChatController.removeMessage(1L);
        
        assertNull(result);
    }
}
package hu.benkototh.cardgame.backend.rest.service;

import hu.benkototh.cardgame.backend.rest.Data.LobbyMessage;
import hu.benkototh.cardgame.backend.rest.Data.User;
import hu.benkototh.cardgame.backend.rest.controller.LobbyChatController;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class LobbyChatRestServiceTest {

    @Mock
    private LobbyChatController lobbyChatController;

    @InjectMocks
    private LobbyChatRestService lobbyChatRestService;

    private User user;
    private LobbyMessage lobbyMessage;
    private LobbyMessage gameMessage;
    private List<LobbyMessage> lobbyMessages;
    private List<LobbyMessage> gameMessages;

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

        lobbyMessages = new ArrayList<>();
        lobbyMessages.add(lobbyMessage);

        gameMessages = new ArrayList<>();
        gameMessages.add(gameMessage);
    }

    @Test
    void testGetLobbyMessages() {
        when(lobbyChatController.getLobbyMessages(1L)).thenReturn(lobbyMessages);

        ResponseEntity<List<LobbyMessage>> response = lobbyChatRestService.getLobbyMessages(1L);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals(1, response.getBody().size());
        assertEquals(1L, response.getBody().get(0).getId());
    }

    @Test
    void testGetGameMessages() {
        when(lobbyChatController.getGameMessages("g-123456")).thenReturn(gameMessages);

        ResponseEntity<List<LobbyMessage>> response = lobbyChatRestService.getGameMessages("g-123456");

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals(1, response.getBody().size());
        assertEquals(2L, response.getBody().get(0).getId());
    }

    @Test
    void testSendMessageSuccess() {
        when(lobbyChatController.sendLobbyMessage(1L, 1L, "Test message")).thenReturn(lobbyMessage);

        ResponseEntity<LobbyMessage> response = lobbyChatRestService.sendMessage(1L, 1L, "Test message");

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals(1L, response.getBody().getId());
    }

    @Test
    void testSendMessageNotFound() {
        when(lobbyChatController.sendLobbyMessage(1L, 1L, "Test message")).thenReturn(null);

        ResponseEntity<LobbyMessage> response = lobbyChatRestService.sendMessage(1L, 1L, "Test message");

        assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
        assertNull(response.getBody());
    }

    @Test
    void testSendGameMessageSuccess() {
        when(lobbyChatController.sendGameMessage(1L, "g-123456", "Test message")).thenReturn(gameMessage);

        ResponseEntity<LobbyMessage> response = lobbyChatRestService.sendGameMessage("g-123456", 1L, "Test message");

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals(2L, response.getBody().getId());
    }

    @Test
    void testSendGameMessageNotFound() {
        when(lobbyChatController.sendGameMessage(1L, "g-123456", "Test message")).thenReturn(null);

        ResponseEntity<LobbyMessage> response = lobbyChatRestService.sendGameMessage("g-123456", 1L, "Test message");

        assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
        assertNull(response.getBody());
    }

    @Test
    void testUnsendMessageSuccess() {
        when(lobbyChatController.unsendLobbyMessage(1L)).thenReturn(lobbyMessage);

        ResponseEntity<LobbyMessage> response = lobbyChatRestService.unsendMessage(1L);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals(1L, response.getBody().getId());
    }

    @Test
    void testUnsendMessageNotFound() {
        when(lobbyChatController.unsendLobbyMessage(1L)).thenReturn(null);

        ResponseEntity<LobbyMessage> response = lobbyChatRestService.unsendMessage(1L);

        assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
        assertNull(response.getBody());
    }

    @Test
    void testRemoveMessageSuccess() {
        when(lobbyChatController.removeMessage(1L)).thenReturn(lobbyMessage);

        ResponseEntity<LobbyMessage> response = lobbyChatRestService.removeMessage(1L);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals(1L, response.getBody().getId());
    }

    @Test
    void testRemoveMessageNotFound() {
        when(lobbyChatController.removeMessage(1L)).thenReturn(null);

        ResponseEntity<LobbyMessage> response = lobbyChatRestService.removeMessage(1L);

        assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
        assertNull(response.getBody());
    }
}
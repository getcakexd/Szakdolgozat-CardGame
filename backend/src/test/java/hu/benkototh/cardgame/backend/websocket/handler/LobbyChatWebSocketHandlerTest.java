package hu.benkototh.cardgame.backend.websocket.handler;

import hu.benkototh.cardgame.backend.rest.model.LobbyMessage;
import hu.benkototh.cardgame.backend.rest.model.User;
import hu.benkototh.cardgame.backend.rest.controller.LobbyChatController;
import hu.benkototh.cardgame.backend.websocket.dto.lobby.GetLobbyMessagesDTO;
import hu.benkototh.cardgame.backend.websocket.dto.lobby.LobbyChatMessageDTO;
import hu.benkototh.cardgame.backend.websocket.dto.lobby.RemoveLobbyMessageDTO;
import hu.benkototh.cardgame.backend.websocket.dto.lobby.UnsendLobbyMessageDTO;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.messaging.simp.SimpMessagingTemplate;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Optional;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class LobbyChatWebSocketHandlerTest {

    @Mock
    private SimpMessagingTemplate messagingTemplate;

    @Mock
    private LobbyChatController lobbyChatController;

    @InjectMocks
    private LobbyChatWebSocketHandler lobbyChatWebSocketHandler;

    private User sender;
    private LobbyMessage lobbyMessage;
    private LobbyMessage gameMessage;
    private List<LobbyMessage> lobbyMessages;
    private List<LobbyMessage> gameMessages;

    @BeforeEach
    void setUp() {
        sender = new User();
        sender.setId(1L);
        sender.setUsername("sender");

        lobbyMessage = new LobbyMessage();
        lobbyMessage.setId(123L);
        lobbyMessage.setUser(sender);
        lobbyMessage.setContent("Hello everyone in the lobby!");
        lobbyMessage.setTimestamp(new Date());
        lobbyMessage.setLobbyId(42L);
        lobbyMessage.setGameId(null);
        lobbyMessage.setLobbyMessage(true);
        lobbyMessage.setStatus("sent");

        gameMessage = new LobbyMessage();
        gameMessage.setId(456L);
        gameMessage.setUser(sender);
        gameMessage.setContent("Hello everyone in the game!");
        gameMessage.setTimestamp(new Date());
        gameMessage.setLobbyId(42L);
        gameMessage.setGameId("g-123456");
        gameMessage.setLobbyMessage(false);
        lobbyMessage.setStatus("sent");

        lobbyMessages = new ArrayList<>();
        lobbyMessages.add(lobbyMessage);

        gameMessages = new ArrayList<>();
        gameMessages.add(gameMessage);
    }

    @Test
    void testSendLobbyMessage() {
        LobbyChatMessageDTO chatMessageDTO = new LobbyChatMessageDTO();
        chatMessageDTO.setSenderId(1L);
        chatMessageDTO.setLobbyId(42L);
        chatMessageDTO.setContent("Hello everyone in the lobby!");
        
        when(lobbyChatController.sendLobbyMessage(1L, 42L, "Hello everyone in the lobby!")).thenReturn(lobbyMessage);
        
        lobbyChatWebSocketHandler.sendLobbyMessage(chatMessageDTO);
        
        verify(lobbyChatController).sendLobbyMessage(1L, 42L, "Hello everyone in the lobby!");
        verify(messagingTemplate).convertAndSend("/topic/lobby/42", lobbyMessage);
    }

    @Test
    void testSendLobbyMessageNull() {
        LobbyChatMessageDTO chatMessageDTO = new LobbyChatMessageDTO();
        chatMessageDTO.setSenderId(1L);
        chatMessageDTO.setLobbyId(42L);
        chatMessageDTO.setContent("Hello everyone in the lobby!");
        
        when(lobbyChatController.sendLobbyMessage(1L, 42L, "Hello everyone in the lobby!")).thenReturn(null);
        
        lobbyChatWebSocketHandler.sendLobbyMessage(chatMessageDTO);
        
        verify(lobbyChatController).sendLobbyMessage(1L, 42L, "Hello everyone in the lobby!");
        verify(messagingTemplate, never()).convertAndSend(anyString(), Optional.ofNullable(any()));
    }

    @Test
    void testSendGameMessage() {
        LobbyChatMessageDTO chatMessageDTO = new LobbyChatMessageDTO();
        chatMessageDTO.setSenderId(1L);
        chatMessageDTO.setLobbyId(0L);
        chatMessageDTO.setGameId("g-123456");
        chatMessageDTO.setContent("Hello everyone in the game!");
        
        when(lobbyChatController.sendGameMessage(1L, "g-123456", "Hello everyone in the game!")).thenReturn(gameMessage);
        
        lobbyChatWebSocketHandler.sendLobbyMessage(chatMessageDTO);
        
        verify(lobbyChatController).sendGameMessage(1L, "g-123456", "Hello everyone in the game!");
        verify(messagingTemplate).convertAndSend("/topic/game/g-123456", gameMessage);
    }

    @Test
    void testSendGameMessageNull() {
        LobbyChatMessageDTO chatMessageDTO = new LobbyChatMessageDTO();
        chatMessageDTO.setSenderId(1L);
        chatMessageDTO.setLobbyId(0L);
        chatMessageDTO.setGameId("g-123456");
        chatMessageDTO.setContent("Hello everyone in the game!");
        
        when(lobbyChatController.sendGameMessage(1L, "g-123456", "Hello everyone in the game!")).thenReturn(null);
        
        lobbyChatWebSocketHandler.sendLobbyMessage(chatMessageDTO);
        
        verify(lobbyChatController).sendGameMessage(1L, "g-123456", "Hello everyone in the game!");
        verify(messagingTemplate, never()).convertAndSend(anyString(), Optional.ofNullable(any()));
    }

    @Test
    void testUnsendLobbyMessage() {
        UnsendLobbyMessageDTO unsendMessageDTO = new UnsendLobbyMessageDTO();
        unsendMessageDTO.setMessageId(123L);
        
        when(lobbyChatController.unsendLobbyMessage(123L)).thenReturn(lobbyMessage);
        
        lobbyChatWebSocketHandler.unsendMessage(unsendMessageDTO);
        
        verify(lobbyChatController).unsendLobbyMessage(123L);
        verify(messagingTemplate).convertAndSend("/topic/lobby/42", lobbyMessage);
    }

    @Test
    void testUnsendGameMessage() {
        UnsendLobbyMessageDTO unsendMessageDTO = new UnsendLobbyMessageDTO();
        unsendMessageDTO.setMessageId(456L);
        
        when(lobbyChatController.unsendLobbyMessage(456L)).thenReturn(gameMessage);
        
        lobbyChatWebSocketHandler.unsendMessage(unsendMessageDTO);
        
        verify(lobbyChatController).unsendLobbyMessage(456L);
        verify(messagingTemplate).convertAndSend("/topic/game/g-123456", gameMessage);
    }

    @Test
    void testUnsendMessageNull() {
        UnsendLobbyMessageDTO unsendMessageDTO = new UnsendLobbyMessageDTO();
        unsendMessageDTO.setMessageId(123L);
        
        when(lobbyChatController.unsendLobbyMessage(123L)).thenReturn(null);
        
        lobbyChatWebSocketHandler.unsendMessage(unsendMessageDTO);
        
        verify(lobbyChatController).unsendLobbyMessage(123L);
        verify(messagingTemplate, never()).convertAndSend(anyString(), Optional.ofNullable(any()));
    }

    @Test
    void testRemoveLobbyMessage() {
        RemoveLobbyMessageDTO removeMessageDTO = new RemoveLobbyMessageDTO();
        removeMessageDTO.setMessageId(123L);
        
        when(lobbyChatController.removeMessage(123L)).thenReturn(lobbyMessage);
        
        lobbyChatWebSocketHandler.removeMessage(removeMessageDTO);
        
        verify(lobbyChatController).removeMessage(123L);
        verify(messagingTemplate).convertAndSend("/topic/lobby/42", lobbyMessage);
    }

    @Test
    void testRemoveGameMessage() {
        RemoveLobbyMessageDTO removeMessageDTO = new RemoveLobbyMessageDTO();
        removeMessageDTO.setMessageId(456L);
        
        when(lobbyChatController.removeMessage(456L)).thenReturn(gameMessage);
        
        lobbyChatWebSocketHandler.removeMessage(removeMessageDTO);
        
        verify(lobbyChatController).removeMessage(456L);
        verify(messagingTemplate).convertAndSend("/topic/game/g-123456", gameMessage);
    }

    @Test
    void testRemoveMessageNull() {
        RemoveLobbyMessageDTO removeMessageDTO = new RemoveLobbyMessageDTO();
        removeMessageDTO.setMessageId(123L);
        
        when(lobbyChatController.removeMessage(123L)).thenReturn(null);
        
        lobbyChatWebSocketHandler.removeMessage(removeMessageDTO);
        
        verify(lobbyChatController).removeMessage(123L);
        verify(messagingTemplate, never()).convertAndSend(anyString(), Optional.ofNullable(any()));
    }

    @Test
    void testGetLobbyMessages() {
        GetLobbyMessagesDTO getMessagesDTO = new GetLobbyMessagesDTO();
        getMessagesDTO.setLobbyId(42L);
        
        when(lobbyChatController.getLobbyMessages(42L)).thenReturn(lobbyMessages);
        
        lobbyChatWebSocketHandler.getMessages(getMessagesDTO);
        
        verify(lobbyChatController).getLobbyMessages(42L);
        verify(messagingTemplate).convertAndSend("/topic/lobby/42", lobbyMessages);
    }

    @Test
    void testGetGameMessages() {
        GetLobbyMessagesDTO getMessagesDTO = new GetLobbyMessagesDTO();
        getMessagesDTO.setLobbyId(0L);
        getMessagesDTO.setGameId("g-123456");
        
        when(lobbyChatController.getGameMessages("g-123456")).thenReturn(gameMessages);
        
        lobbyChatWebSocketHandler.getMessages(getMessagesDTO);
        
        verify(lobbyChatController).getGameMessages("g-123456");
        verify(messagingTemplate).convertAndSend("/topic/game/g-123456", gameMessages);
    }
}
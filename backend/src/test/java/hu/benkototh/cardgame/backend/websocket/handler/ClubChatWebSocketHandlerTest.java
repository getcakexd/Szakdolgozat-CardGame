package hu.benkototh.cardgame.backend.websocket.handler;

import hu.benkototh.cardgame.backend.rest.model.Club;
import hu.benkototh.cardgame.backend.rest.model.ClubMessage;
import hu.benkototh.cardgame.backend.rest.model.User;
import hu.benkototh.cardgame.backend.rest.controller.ClubChatController;
import hu.benkototh.cardgame.backend.websocket.dto.club.ClubChatMessageDTO;
import hu.benkototh.cardgame.backend.websocket.dto.club.GetClubMessagesDTO;
import hu.benkototh.cardgame.backend.websocket.dto.club.RemoveClubMessageDTO;
import hu.benkototh.cardgame.backend.websocket.dto.club.UnsendClubMessageDTO;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.messaging.simp.SimpMessagingTemplate;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ClubChatWebSocketHandlerTest {

    @Mock
    private SimpMessagingTemplate messagingTemplate;

    @Mock
    private ClubChatController clubChatController;

    @InjectMocks
    private ClubChatWebSocketHandler clubChatWebSocketHandler;

    private Club club;
    private User sender;
    private ClubMessage message;
    private List<ClubMessage> messages;

    @BeforeEach
    void setUp() {
        club = new Club();
        club.setId(42L);
        club.setName("Test Club");

        sender = new User();
        sender.setId(1L);
        sender.setUsername("sender");

        message = new ClubMessage();
        message.setId(123L);
        message.setClub(club);
        message.setSender(sender);
        message.setContent("Hello club members!");
        message.setTimestamp(LocalDateTime.now());
        message.setStatus("sent");

        messages = new ArrayList<>();
        messages.add(message);
    }

    @Test
    void testSendMessage() {
        ClubChatMessageDTO chatMessageDTO = new ClubChatMessageDTO(42L, 1L, "Hello club members!");
        
        when(clubChatController.sendClubMessage(42L, 1L, "Hello club members!")).thenReturn(message);
        
        clubChatWebSocketHandler.sendMessage(chatMessageDTO);
        
        verify(clubChatController).sendClubMessage(42L, 1L, "Hello club members!");
        verify(messagingTemplate).convertAndSend("/topic/club/42", message);
    }

    @Test
    void testSendMessageNull() {
        ClubChatMessageDTO chatMessageDTO = new ClubChatMessageDTO(42L, 1L, "Hello club members!");
        
        when(clubChatController.sendClubMessage(42L, 1L, "Hello club members!")).thenReturn(null);
        
        clubChatWebSocketHandler.sendMessage(chatMessageDTO);
        
        verify(clubChatController).sendClubMessage(42L, 1L, "Hello club members!");
        verify(messagingTemplate, never()).convertAndSend(anyString(), Optional.ofNullable(any()));
    }

    @Test
    void testUnsendMessage() {
        UnsendClubMessageDTO unsendMessageDTO = new UnsendClubMessageDTO(123L);
        
        when(clubChatController.unsendClubMessage(123L)).thenReturn(message);
        
        clubChatWebSocketHandler.unsendMessage(unsendMessageDTO);
        
        verify(clubChatController).unsendClubMessage(123L);
        verify(messagingTemplate).convertAndSend("/topic/club/42", message);
    }

    @Test
    void testUnsendMessageNull() {
        UnsendClubMessageDTO unsendMessageDTO = new UnsendClubMessageDTO(123L);
        
        when(clubChatController.unsendClubMessage(123L)).thenReturn(null);
        
        clubChatWebSocketHandler.unsendMessage(unsendMessageDTO);
        
        verify(clubChatController).unsendClubMessage(123L);
        verify(messagingTemplate, never()).convertAndSend(anyString(), Optional.ofNullable(any()));
    }

    @Test
    void testRemoveMessage() {
        RemoveClubMessageDTO removeMessageDTO = new RemoveClubMessageDTO(123L);
        
        when(clubChatController.removeClubMessage(123L)).thenReturn(message);
        
        clubChatWebSocketHandler.removeMessage(removeMessageDTO);
        
        verify(clubChatController).removeClubMessage(123L);
        verify(messagingTemplate).convertAndSend("/topic/club/42", message);
    }

    @Test
    void testRemoveMessageNull() {
        RemoveClubMessageDTO removeMessageDTO = new RemoveClubMessageDTO(123L);
        
        when(clubChatController.removeClubMessage(123L)).thenReturn(null);
        
        clubChatWebSocketHandler.removeMessage(removeMessageDTO);
        
        verify(clubChatController).removeClubMessage(123L);
        verify(messagingTemplate, never()).convertAndSend(anyString(), Optional.ofNullable(any()));
    }

    @Test
    void testGetMessages() {
        GetClubMessagesDTO getMessagesDTO = new GetClubMessagesDTO(42L);
        
        when(clubChatController.getClubChatHistory(42L)).thenReturn(messages);
        
        clubChatWebSocketHandler.getMessages(getMessagesDTO);
        
        verify(clubChatController).getClubChatHistory(42L);
        verify(messagingTemplate).convertAndSend("/topic/club/42", messages);
    }
}
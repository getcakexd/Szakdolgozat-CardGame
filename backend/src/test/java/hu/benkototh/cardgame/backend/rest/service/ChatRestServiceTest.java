package hu.benkototh.cardgame.backend.rest.service;

import hu.benkototh.cardgame.backend.rest.model.Message;
import hu.benkototh.cardgame.backend.rest.model.User;
import hu.benkototh.cardgame.backend.rest.controller.ChatController;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ChatRestServiceTest {

    @Mock
    private ChatController chatController;

    @InjectMocks
    private ChatRestService chatRestService;

    private User sender;
    private User receiver;
    private Message message;
    private List<Message> messages;

    @BeforeEach
    void setUp() {
        sender = new User();
        sender.setId(1L);
        sender.setUsername("sender");

        receiver = new User();
        receiver.setId(2L);
        receiver.setUsername("receiver");

        message = new Message(sender, receiver, "Hello");
        message.setId(1L);
        message.setTimestamp(LocalDateTime.now());
        message.setStatus("unread");

        messages = new ArrayList<>();
        messages.add(message);
    }

    @Test
    void testGetMessages() {
        when(chatController.getMessages(1L, 2L)).thenReturn(messages);
        
        ResponseEntity<List<Message>> response = chatRestService.getMessages(1L, 2L);
        
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals(1, response.getBody().size());
        assertEquals(1L, response.getBody().get(0).getId());
    }

    @Test
    void testSendMessage() {
        when(chatController.sendMessage(1L, 2L, "Hello")).thenReturn(message);
        
        ResponseEntity<Message> response = chatRestService.sendMessage(1L, 2L, "Hello");
        
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals(1L, response.getBody().getId());
    }

    @Test
    void testUnsendMessageSuccess() {
        when(chatController.unsendMessage(1L)).thenReturn(message);
        
        ResponseEntity<Map<String, String>> response = chatRestService.deleteMessage(1L);
        
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals("Message unsent", response.getBody().get("message"));
    }

    @Test
    void testUnsendMessageNotFound() {
        when(chatController.unsendMessage(1L)).thenReturn(null);
        
        ResponseEntity<Map<String, String>> response = chatRestService.deleteMessage(1L);
        
        assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
        assertEquals("Message not found", response.getBody().get("message"));
    }

    @Test
    void testGetUnreadMessageCounts() {
        Map<Long, Integer> unreadCounts = new HashMap<>();
        unreadCounts.put(2L, 3);
        
        when(chatController.getUnreadMessageCounts(1L)).thenReturn(unreadCounts);
        
        ResponseEntity<Map<Long, Integer>> response = chatRestService.getUnreadMessageCounts(1L);
        
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals(1, response.getBody().size());
        assertEquals(3, response.getBody().get(2L));
    }
}
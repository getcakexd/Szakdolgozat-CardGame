package hu.benkototh.cardgame.backend.rest.controller;

import hu.benkototh.cardgame.backend.rest.model.Message;
import hu.benkototh.cardgame.backend.rest.model.User;
import hu.benkototh.cardgame.backend.rest.repository.IMessageRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.web.server.ResponseStatusException;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ChatControllerTest {

    @Mock
    private IMessageRepository messageRepository;

    @Mock
    private UserController userController;

    @Mock
    private AuditLogController auditLogController;

    @InjectMocks
    private ChatController chatController;

    private User sender;
    private User receiver;
    private Message message1;
    private Message message2;
    private List<Message> messages;

    @BeforeEach
    void setUp() {
        sender = new User();
        sender.setId(1L);
        sender.setUsername("sender");

        receiver = new User();
        receiver.setId(2L);
        receiver.setUsername("receiver");

        message1 = new Message(sender, receiver, "Hello");
        message1.setId(1L);
        message1.setTimestamp(LocalDateTime.now().minusHours(1));
        message1.setStatus("unread");

        message2 = new Message(receiver, sender, "Hi there");
        message2.setId(2L);
        message2.setTimestamp(LocalDateTime.now());
        message2.setStatus("unread");

        messages = new ArrayList<>();
        messages.add(message1);
        messages.add(message2);
    }

    @Test
    void testGetMessages() {
        when(messageRepository.findAll()).thenReturn(messages);
        
        List<Message> result = chatController.getMessages(1L, 2L);
        
        assertEquals(2, result.size());

        verify(messageRepository).save(message2);
        assertEquals("read", message2.getStatus());

        verify(auditLogController).logAction(eq("MESSAGES_VIEWED"), eq(1L), anyString());
    }

    @Test
    void testSendMessage() {
        when(userController.getUser(1L)).thenReturn(sender);
        when(userController.getUser(2L)).thenReturn(receiver);
        when(messageRepository.save(any(Message.class))).thenReturn(message1);
        
        Message result = chatController.sendMessage(1L, 2L, "Hello");
        
        assertNotNull(result);
        assertEquals(message1, result);

        verify(auditLogController).logAction(eq("MESSAGE_SENT"), eq(1L), anyString());
    }

    @Test
    void testSendMessageSenderNotFound() {
        when(userController.getUser(1L)).thenReturn(null);
        
        assertThrows(ResponseStatusException.class, () -> {
            chatController.sendMessage(1L, 2L, "Hello");
        });
    }

    @Test
    void testSendMessageReceiverNotFound() {
        when(userController.getUser(1L)).thenReturn(sender);
        when(userController.getUser(2L)).thenReturn(null);
        
        assertThrows(ResponseStatusException.class, () -> {
            chatController.sendMessage(1L, 2L, "Hello");
        });
    }

    @Test
    void testUnsendMessage() {
        when(messageRepository.findById(1L)).thenReturn(Optional.of(message1));
        when(messageRepository.save(any(Message.class))).thenAnswer(invocation -> invocation.getArgument(0));
        
        Message result = chatController.unsendMessage(1L);
        
        assertNotNull(result);
        assertEquals("unsent", result.getStatus());
        assertEquals("This message has been unsent", result.getContent());

        verify(auditLogController).logAction(eq("MESSAGE_UNSENT"), eq(1L), anyString());
    }

    @Test
    void testUnsendMessageNotFound() {
        when(messageRepository.findById(1L)).thenReturn(Optional.empty());
        
        Message result = chatController.unsendMessage(1L);
        
        assertNull(result);
    }

    @Test
    void testGetUnreadMessageCounts() {
        when(messageRepository.findAll()).thenReturn(messages);
        
        Map<Long, Integer> result = chatController.getUnreadMessageCounts(1L);
        
        assertEquals(1, result.size());
        assertEquals(1, result.get(2L));
    }

    @Test
    void testFindBySenderAndReceiver() {
        when(messageRepository.findAll()).thenReturn(messages);
        
        List<Message> result = chatController.findBySenderAndReceiver(1L, 2L);
        
        assertEquals(2, result.size());
    }

    @Test
    void testGetMessagesByUser() {
        when(messageRepository.findAll()).thenReturn(messages);
        
        List<Message> result = chatController.getMessagesByUser(1L);
        
        assertEquals(2, result.size());
    }

    @Test
    void testGetSentMessagesByUser() {
        when(messageRepository.findAll()).thenReturn(messages);
        
        List<Message> result = chatController.getSentMessagesByUser(1L);
        
        assertEquals(1, result.size());
        assertEquals(1L, result.get(0).getSender().getId());
    }
}
package hu.benkototh.cardgame.backend.websocket.handler;

import hu.benkototh.cardgame.backend.rest.model.Message;
import hu.benkototh.cardgame.backend.rest.model.User;
import hu.benkototh.cardgame.backend.rest.controller.ChatController;
import hu.benkototh.cardgame.backend.websocket.dto.UserIdDTO;
import hu.benkototh.cardgame.backend.websocket.dto.friends.ChatMessageDTO;
import hu.benkototh.cardgame.backend.websocket.dto.friends.GetMessagesDTO;
import hu.benkototh.cardgame.backend.websocket.dto.friends.UnsendMessageDTO;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.messaging.simp.SimpMessagingTemplate;

import java.time.LocalDateTime;
import java.util.*;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ChatWebSocketHandlerTest {

    @Mock
    private SimpMessagingTemplate messagingTemplate;

    @Mock
    private ChatController chatController;

    @InjectMocks
    private ChatWebSocketHandler chatWebSocketHandler;

    private User sender;
    private User receiver;
    private Message message;
    private List<Message> messages;
    private Map<Long, Integer> unreadCounts;

    @BeforeEach
    void setUp() {
        sender = new User();
        sender.setId(1L);
        sender.setUsername("sender");

        receiver = new User();
        receiver.setId(2L);
        receiver.setUsername("receiver");

        message = new Message();
        message.setId(123L);
        message.setSender(sender);
        message.setReceiver(receiver);
        message.setContent("Hello, world!");
        message.setTimestamp(LocalDateTime.now());
        message.setStatus("unread");

        messages = new ArrayList<>();
        messages.add(message);

        unreadCounts = new HashMap<>();
        unreadCounts.put(2L, 5);
    }

    @Test
    void testSendMessage() {
        ChatMessageDTO chatMessageDTO = new ChatMessageDTO(1L, 2L, "Hello, world!");
        
        when(chatController.sendMessage(1L, 2L, "Hello, world!")).thenReturn(message);
        when(chatController.getUnreadMessageCounts(2L)).thenReturn(unreadCounts);
        
        chatWebSocketHandler.sendMessage(chatMessageDTO);
        
        verify(chatController).sendMessage(1L, 2L, "Hello, world!");
        verify(messagingTemplate).convertAndSend("/topic/user/1", message);
        verify(messagingTemplate).convertAndSend("/topic/user/2", message);
        verify(messagingTemplate).convertAndSend("/topic/unread/2", unreadCounts);
    }

    @Test
    void testUnsendMessage() {
        UnsendMessageDTO unsendMessageDTO = new UnsendMessageDTO(123L);
        
        when(chatController.unsendMessage(123L)).thenReturn(message);
        
        chatWebSocketHandler.unsendMessage(unsendMessageDTO);
        
        verify(chatController).unsendMessage(123L);
        verify(messagingTemplate).convertAndSend("/topic/user/1", message);
        verify(messagingTemplate).convertAndSend("/topic/user/2", message);
    }

    @Test
    void testUnsendMessageNotFound() {
        UnsendMessageDTO unsendMessageDTO = new UnsendMessageDTO(123L);
        
        when(chatController.unsendMessage(123L)).thenReturn(null);
        
        chatWebSocketHandler.unsendMessage(unsendMessageDTO);
        
        verify(chatController).unsendMessage(123L);
        verify(messagingTemplate, never()).convertAndSend(anyString(), Optional.ofNullable(any()));
    }

    @Test
    void testGetMessages() {
        GetMessagesDTO getMessagesDTO = new GetMessagesDTO(1L, 2L);
        
        when(chatController.getMessages(1L, 2L)).thenReturn(messages);
        when(chatController.getUnreadMessageCounts(1L)).thenReturn(unreadCounts);
        
        chatWebSocketHandler.getMessages(getMessagesDTO);
        
        verify(chatController).getMessages(1L, 2L);
        verify(messagingTemplate).convertAndSend("/topic/chat/1/2", messages);
        verify(messagingTemplate).convertAndSend("/topic/unread/1", unreadCounts);
    }

    @Test
    void testGetUnreadCounts() {
        UserIdDTO userIdDTO = new UserIdDTO(1L);
        
        when(chatController.getUnreadMessageCounts(1L)).thenReturn(unreadCounts);
        
        chatWebSocketHandler.getUnreadCounts(userIdDTO);
        
        verify(chatController).getUnreadMessageCounts(1L);
        verify(messagingTemplate).convertAndSend("/topic/unread/1", unreadCounts);
    }
}
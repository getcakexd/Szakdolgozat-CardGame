package hu.benkototh.cardgame.backend.rest.controller;

import hu.benkototh.cardgame.backend.rest.Data.Club;
import hu.benkototh.cardgame.backend.rest.Data.ClubMessage;
import hu.benkototh.cardgame.backend.rest.Data.User;
import hu.benkototh.cardgame.backend.rest.repository.IClubMessageRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ClubChatControllerTest {

    @Mock
    private IClubMessageRepository clubMessageRepository;

    @Mock
    private UserController userController;

    @Mock
    private ClubController clubController;

    @Mock
    private AuditLogController auditLogController;

    @InjectMocks
    private ClubChatController clubChatController;

    private Club testClub;
    private User testUser;
    private ClubMessage testMessage;
    private List<ClubMessage> messages;

    @BeforeEach
    void setUp() {
        testClub = new Club("Test Club", "Test Description", true);
        testClub.setId(1L);

        testUser = new User();
        testUser.setId(1L);
        testUser.setUsername("testuser");

        testMessage = new ClubMessage(testClub, testUser, "Test message", LocalDateTime.now());
        testMessage.setId(1L);

        messages = new ArrayList<>();
        messages.add(testMessage);
    }

    @Test
    void testGetClubChatHistory() {
        when(clubMessageRepository.findAll()).thenReturn(messages);
        
        List<ClubMessage> result = clubChatController.getClubChatHistory(1L);
        
        assertEquals(1, result.size());
        assertEquals(testMessage.getId(), result.get(0).getId());
    }

    @Test
    void testSendClubMessage() {
        when(userController.getUser(1L)).thenReturn(testUser);
        when(clubController.getClub(1L)).thenReturn(testClub);
        when(clubMessageRepository.save(any(ClubMessage.class))).thenReturn(testMessage);
        
        ClubMessage result = clubChatController.sendClubMessage(1L, 1L, "Test message");
        
        assertNotNull(result);
        assertEquals(testMessage.getId(), result.getId());
        verify(auditLogController).logAction(eq("CLUB_MESSAGE_SENT"), eq(1L), anyString());
    }

    @Test
    void testSendClubMessageUserNotFound() {
        when(userController.getUser(1L)).thenReturn(null);
        when(clubController.getClub(1L)).thenReturn(testClub);
        
        ClubMessage result = clubChatController.sendClubMessage(1L, 1L, "Test message");
        
        assertNull(result);
    }

    @Test
    void testSendClubMessageClubNotFound() {
        when(userController.getUser(1L)).thenReturn(testUser);
        when(clubController.getClub(1L)).thenReturn(null);
        
        ClubMessage result = clubChatController.sendClubMessage(1L, 1L, "Test message");
        
        assertNull(result);
    }

    @Test
    void testUnsendClubMessage() {
        when(clubMessageRepository.findById(1L)).thenReturn(Optional.of(testMessage));
        when(clubMessageRepository.save(any(ClubMessage.class))).thenReturn(testMessage);
        
        ClubMessage result = clubChatController.unsendClubMessage(1L);
        
        assertNotNull(result);
        assertEquals("unsent", testMessage.getStatus());
        assertEquals("This message has been unsent.", testMessage.getContent());
        verify(auditLogController).logAction(eq("CLUB_MESSAGE_UNSENT"), eq(1L), anyString());
    }

    @Test
    void testUnsendClubMessageNotFound() {
        when(clubMessageRepository.findById(1L)).thenReturn(Optional.empty());
        
        ClubMessage result = clubChatController.unsendClubMessage(1L);
        
        assertNull(result);
    }

    @Test
    void testRemoveClubMessage() {
        when(clubMessageRepository.findById(1L)).thenReturn(Optional.of(testMessage));
        when(clubMessageRepository.save(any(ClubMessage.class))).thenReturn(testMessage);
        
        ClubMessage result = clubChatController.removeClubMessage(1L);
        
        assertNotNull(result);
        assertEquals("removed", testMessage.getStatus());
        assertEquals("This message has been removed by a moderator.", testMessage.getContent());
        verify(auditLogController).logAction(eq("CLUB_MESSAGE_REMOVED"), eq(0L), anyString());
    }

    @Test
    void testRemoveClubMessageNotFound() {
        when(clubMessageRepository.findById(1L)).thenReturn(Optional.empty());
        
        ClubMessage result = clubChatController.removeClubMessage(1L);
        
        assertNull(result);
    }

    @Test
    void testGetMessagesByUser() {
        when(clubMessageRepository.findAll()).thenReturn(messages);
        
        List<ClubMessage> result = clubChatController.getMessagesByUser(1L);
        
        assertEquals(1, result.size());
        assertEquals(testMessage.getId(), result.get(0).getId());
    }
}
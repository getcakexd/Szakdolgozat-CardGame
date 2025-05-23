package hu.benkototh.cardgame.backend.rest.service;

import hu.benkototh.cardgame.backend.rest.model.Club;
import hu.benkototh.cardgame.backend.rest.model.ClubMessage;
import hu.benkototh.cardgame.backend.rest.model.User;
import hu.benkototh.cardgame.backend.rest.controller.ClubChatController;
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
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ClubChatRestServiceTest {

    @Mock
    private ClubChatController clubChatController;

    @InjectMocks
    private ClubChatRestService clubChatRestService;

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
        when(clubChatController.getClubChatHistory(1L)).thenReturn(messages);
        
        ResponseEntity<List<ClubMessage>> response = clubChatRestService.getClubChatHistory(1L);
        
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals(1, response.getBody().size());
        assertEquals(testMessage.getId(), response.getBody().get(0).getId());
    }

    @Test
    void testSendClubMessageSuccess() {
        when(clubChatController.sendClubMessage(1L, 1L, "Test message")).thenReturn(testMessage);
        
        ResponseEntity<ClubMessage> response = clubChatRestService.sendClubMessage(1L, 1L, "Test message");
        
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals(testMessage.getId(), response.getBody().getId());
    }

    @Test
    void testSendClubMessageFailure() {
        when(clubChatController.sendClubMessage(1L, 1L, "Test message")).thenReturn(null);
        
        ResponseEntity<ClubMessage> response = clubChatRestService.sendClubMessage(1L, 1L, "Test message");
        
        assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
        assertNull(response.getBody());
    }

    @Test
    void testUnsendClubMessageSuccess() {
        when(clubChatController.unsendClubMessage(1L)).thenReturn(testMessage);
        
        ResponseEntity<ClubMessage> response = clubChatRestService.unsendClubMessage(1L);
        
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals(testMessage.getId(), response.getBody().getId());
    }

    @Test
    void testUnsendClubMessageFailure() {
        when(clubChatController.unsendClubMessage(1L)).thenReturn(null);
        
        ResponseEntity<ClubMessage> response = clubChatRestService.unsendClubMessage(1L);
        
        assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
        assertNull(response.getBody());
    }

    @Test
    void testRemoveClubMessageSuccess() {
        when(clubChatController.removeClubMessage(1L)).thenReturn(testMessage);
        
        ResponseEntity<ClubMessage> response = clubChatRestService.removeClubMessage(1L);
        
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals(testMessage.getId(), response.getBody().getId());
    }

    @Test
    void testRemoveClubMessageFailure() {
        when(clubChatController.removeClubMessage(1L)).thenReturn(null);
        
        ResponseEntity<ClubMessage> response = clubChatRestService.removeClubMessage(1L);
        
        assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
        assertNull(response.getBody());
    }
}
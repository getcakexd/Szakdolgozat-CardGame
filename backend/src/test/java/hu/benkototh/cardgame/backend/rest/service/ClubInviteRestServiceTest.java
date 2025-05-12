package hu.benkototh.cardgame.backend.rest.service;

import hu.benkototh.cardgame.backend.rest.Data.Club;
import hu.benkototh.cardgame.backend.rest.Data.ClubInvite;
import hu.benkototh.cardgame.backend.rest.Data.User;
import hu.benkototh.cardgame.backend.rest.controller.ClubInviteController;
import hu.benkototh.cardgame.backend.rest.controller.UserController;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ClubInviteRestServiceTest {

    @Mock
    private ClubInviteController clubInviteController;

    @Mock
    private UserController userController;

    @InjectMocks
    private ClubInviteRestService clubInviteRestService;

    private Club testClub;
    private User testUser;
    private ClubInvite testInvite;
    private List<ClubInvite> invites;

    @BeforeEach
    void setUp() {
        testClub = new Club("Test Club", "Test Description", true);
        testClub.setId(1L);

        testUser = new User();
        testUser.setId(1L);
        testUser.setUsername("testuser");

        testInvite = new ClubInvite(testClub, testUser);

        invites = new ArrayList<>();
        invites.add(testInvite);
    }

    @Test
    void testGetClubInvites() {
        when(clubInviteController.getClubInvites(1L)).thenReturn(invites);
        
        ResponseEntity<List<ClubInvite>> response = clubInviteRestService.getClubInvites(1L);
        
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals(1, response.getBody().size());
        assertEquals(testInvite.getId(), response.getBody().get(0).getId());
    }

    @Test
    void testGetPendingInvites() {
        when(clubInviteController.getPendingInvites(1L)).thenReturn(invites);
        
        ResponseEntity<List<ClubInvite>> response = clubInviteRestService.getClubInvite(1L);
        
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals(1, response.getBody().size());
        assertEquals(testInvite.getId(), response.getBody().get(0).getId());
    }

    @Test
    void testGetInviteHistory() {
        when(clubInviteController.getInviteHistory(1L)).thenReturn(invites);
        
        ResponseEntity<List<ClubInvite>> response = clubInviteRestService.getInviteHistory(1L);
        
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals(1, response.getBody().size());
        assertEquals(testInvite.getId(), response.getBody().get(0).getId());
    }

    @Test
    void testAddClubInviteSuccess() {
        when(clubInviteController.addClubInvite(1L, "testuser")).thenReturn(testInvite);
        
        ResponseEntity<?> response = clubInviteRestService.addClubInvite(1L, "testuser");
        
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals(testInvite, response.getBody());
    }

    @Test
    void testAddClubInviteUserNotFound() {
        when(clubInviteController.addClubInvite(1L, "testuser")).thenReturn(null);
        when(userController.findByUsername("testuser")).thenReturn(null);
        
        ResponseEntity<?> response = clubInviteRestService.addClubInvite(1L, "testuser");
        
        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        assertTrue(response.getBody() instanceof Map);
        Map<String, String> responseBody = (Map<String, String>) response.getBody();
        assertEquals("User not found.", responseBody.get("message"));
    }

    @Test
    void testAddClubInviteAlreadyMember() {
        when(clubInviteController.addClubInvite(1L, "testuser")).thenReturn(null);
        when(userController.findByUsername("testuser")).thenReturn(testUser);
        
        ResponseEntity<?> response = clubInviteRestService.addClubInvite(1L, "testuser");
        
        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        assertTrue(response.getBody() instanceof Map);
        Map<String, String> responseBody = (Map<String, String>) response.getBody();
        assertEquals("User is already a member of this club or invite already sent.", responseBody.get("message"));
    }

    @Test
    void testAcceptClubInviteSuccess() {
        when(clubInviteController.acceptClubInvite(1L)).thenReturn(testInvite);
        
        ResponseEntity<ClubInvite> response = clubInviteRestService.acceptClubInvite(1L);
        
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals(testInvite.getId(), response.getBody().getId());
    }

    @Test
    void testAcceptClubInviteFailure() {
        when(clubInviteController.acceptClubInvite(1L)).thenReturn(null);
        
        ResponseEntity<ClubInvite> response = clubInviteRestService.acceptClubInvite(1L);
        
        assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
        assertNull(response.getBody());
    }

    @Test
    void testDeclineClubInviteSuccess() {
        when(clubInviteController.declineClubInvite(1L)).thenReturn(testInvite);
        
        ResponseEntity<ClubInvite> response = clubInviteRestService.declineClubInvite(1L);
        
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals(testInvite.getId(), response.getBody().getId());
    }

    @Test
    void testDeclineClubInviteFailure() {
        when(clubInviteController.declineClubInvite(1L)).thenReturn(null);
        
        ResponseEntity<ClubInvite> response = clubInviteRestService.declineClubInvite(1L);
        
        assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
        assertNull(response.getBody());
    }

    @Test
    void testDeleteClubInviteSuccess() {
        when(clubInviteController.deleteClubInvite(1L)).thenReturn(true);
        
        ResponseEntity<?> response = clubInviteRestService.deleteClubInvite(1L);
        
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNull(response.getBody());
    }

    @Test
    void testDeleteClubInviteFailure() {
        when(clubInviteController.deleteClubInvite(1L)).thenReturn(false);
        
        ResponseEntity<?> response = clubInviteRestService.deleteClubInvite(1L);
        
        assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
        assertNull(response.getBody());
    }
}
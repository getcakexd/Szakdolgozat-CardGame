package hu.benkototh.cardgame.backend.rest.service;

import hu.benkototh.cardgame.backend.rest.model.Club;
import hu.benkototh.cardgame.backend.rest.model.ClubMember;
import hu.benkototh.cardgame.backend.rest.model.User;
import hu.benkototh.cardgame.backend.rest.controller.ClubMemberController;
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
class ClubMemberRestServiceTest {

    @Mock
    private ClubMemberController clubMemberController;

    @InjectMocks
    private ClubMemberRestService clubMemberRestService;

    private Club testClub;
    private User testUser;
    private ClubMember testMember;
    private List<ClubMember> members;

    @BeforeEach
    void setUp() {
        testClub = new Club("Test Club", "Test Description", true);
        testClub.setId(1L);

        testUser = new User();
        testUser.setId(1L);
        testUser.setUsername("testuser");

        testMember = new ClubMember(testClub, testUser, "member");
        testMember.setId(1L);

        members = new ArrayList<>();
        members.add(testMember);
    }

    @Test
    void testGetClubMembersSuccess() {
        when(clubMemberController.getClubMembers(1L)).thenReturn(members);
        
        ResponseEntity<List<ClubMember>> response = clubMemberRestService.getClubMembers(1L);
        
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals(1, response.getBody().size());
        assertEquals(testMember.getId(), response.getBody().get(0).getId());
    }

    @Test
    void testGetClubMembersNotFound() {
        when(clubMemberController.getClubMembers(1L)).thenReturn(null);
        
        ResponseEntity<List<ClubMember>> response = clubMemberRestService.getClubMembers(1L);
        
        assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
        assertNull(response.getBody());
    }

    @Test
    void testGetClubMemberSuccess() {
        when(clubMemberController.getClubMember(1L, 1L)).thenReturn(testMember);
        
        ResponseEntity<ClubMember> response = clubMemberRestService.getClubMember(1L, 1L);
        
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals(testMember.getId(), response.getBody().getId());
    }

    @Test
    void testGetClubMemberNotFound() {
        when(clubMemberController.getClubMember(1L, 1L)).thenReturn(null);
        
        ResponseEntity<ClubMember> response = clubMemberRestService.getClubMember(1L, 1L);
        
        assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
        assertNull(response.getBody());
    }

    @Test
    void testAddClubMemberSuccess() {
        when(clubMemberController.addClubMember(1L, 1L)).thenReturn(testMember);
        
        ResponseEntity<ClubMember> response = clubMemberRestService.addClubMember(1L, 1L);
        
        assertEquals(HttpStatus.CREATED, response.getStatusCode());
        assertEquals(testMember.getId(), response.getBody().getId());
    }

    @Test
    void testAddClubMemberNotFound() {
        when(clubMemberController.addClubMember(1L, 1L)).thenReturn(null);
        
        ResponseEntity<ClubMember> response = clubMemberRestService.addClubMember(1L, 1L);
        
        assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
        assertNull(response.getBody());
    }

    @Test
    void testGetClubMemberRole() {
        when(clubMemberController.getClubMemberRole(1L, 1L)).thenReturn("member");
        
        ResponseEntity<Map<String, String>> response = clubMemberRestService.getClubMemberRole(1L, 1L);
        
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals("member", response.getBody().get("role"));
    }

    @Test
    void testGetClubMemberRoleNotFound() {
        when(clubMemberController.getClubMemberRole(1L, 1L)).thenReturn(null);
        
        ResponseEntity<Map<String, String>> response = clubMemberRestService.getClubMemberRole(1L, 1L);
        
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals("User is not a member of the club.", response.getBody().get("message"));
        assertNull(response.getBody().get("role"));
    }

    @Test
    void testModifyClubMemberSuccess() {
        when(clubMemberController.modifyClubMember(1L, 1L, "moderator")).thenReturn(testMember);
        
        ResponseEntity<ClubMember> response = clubMemberRestService.promoteClubMember(1L, 1L, "moderator");
        
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals(testMember.getId(), response.getBody().getId());
    }

    @Test
    void testModifyClubMemberFailure() {
        when(clubMemberController.modifyClubMember(1L, 1L, "moderator")).thenReturn(null);
        
        ResponseEntity<ClubMember> response = clubMemberRestService.promoteClubMember(1L, 1L, "moderator");
        
        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        assertNull(response.getBody());
    }

    @Test
    void testRemoveClubMemberSuccess() {
        when(clubMemberController.removeClubMember(1L, 1L)).thenReturn(true);
        
        ResponseEntity<Map<String, String>> response = clubMemberRestService.removeClubMember(1L, 1L);
        
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals("User removed from the club.", response.getBody().get("message"));
    }

    @Test
    void testRemoveClubMemberFailure() {
        when(clubMemberController.removeClubMember(1L, 1L)).thenReturn(false);
        
        ResponseEntity<Map<String, String>> response = clubMemberRestService.removeClubMember(1L, 1L);
        
        assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
        assertEquals("User is not a member of the club.", response.getBody().get("message"));
    }

    @Test
    void testLeaveClubSuccess() {
        when(clubMemberController.leaveClub(1L, 1L)).thenReturn(true);
        
        ResponseEntity<Map<String, String>> response = clubMemberRestService.leaveClub(1L, 1L);
        
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals("User left the club.", response.getBody().get("message"));
    }

    @Test
    void testLeaveClubFailure() {
        when(clubMemberController.leaveClub(1L, 1L)).thenReturn(false);
        
        ResponseEntity<Map<String, String>> response = clubMemberRestService.leaveClub(1L, 1L);
        
        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        assertEquals("Admin cannot leave the club.", response.getBody().get("message"));
    }
}
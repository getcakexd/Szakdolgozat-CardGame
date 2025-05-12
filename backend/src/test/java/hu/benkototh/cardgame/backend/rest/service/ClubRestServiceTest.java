package hu.benkototh.cardgame.backend.rest.service;

import hu.benkototh.cardgame.backend.rest.Data.Club;
import hu.benkototh.cardgame.backend.rest.Data.ClubMember;
import hu.benkototh.cardgame.backend.rest.Data.User;
import hu.benkototh.cardgame.backend.rest.controller.ClubController;
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
class ClubRestServiceTest {

    @Mock
    private ClubController clubController;

    @Mock
    private ClubMemberController clubMemberController;

    @InjectMocks
    private ClubRestService clubRestService;

    private Club testClub;
    private User testUser;
    private ClubMember testMember;
    private List<Club> clubs;
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

        clubs = new ArrayList<>();
        clubs.add(testClub);

        members = new ArrayList<>();
        members.add(testMember);
    }

    @Test
    void testGetClubs() {
        when(clubController.getAllClubs()).thenReturn(clubs);
        
        ResponseEntity<List<Club>> response = clubRestService.getClubs();
        
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals(1, response.getBody().size());
        assertEquals(testClub.getId(), response.getBody().get(0).getId());
    }

    @Test
    void testGetPublicClubs() {
        when(clubController.getPublicClubs()).thenReturn(clubs);
        
        ResponseEntity<List<Club>> response = clubRestService.getPublicClubs();
        
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals(1, response.getBody().size());
        assertEquals(testClub.getId(), response.getBody().get(0).getId());
    }

    @Test
    void testGetJoinableClubsSuccess() {
        when(clubController.getJoinableClubs(1L)).thenReturn(clubs);
        
        ResponseEntity<List<Club>> response = clubRestService.getPublicClubs(1L);
        
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals(1, response.getBody().size());
        assertEquals(testClub.getId(), response.getBody().get(0).getId());
    }

    @Test
    void testGetJoinableClubsUserNotFound() {
        when(clubController.getJoinableClubs(1L)).thenReturn(null);
        
        ResponseEntity<List<Club>> response = clubRestService.getPublicClubs(1L);
        
        assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
        assertNull(response.getBody());
    }

    @Test
    void testGetClubSuccess() {
        when(clubController.getClub(1L)).thenReturn(testClub);
        
        ResponseEntity<Club> response = clubRestService.getClub(1L);
        
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals(testClub.getId(), response.getBody().getId());
    }

    @Test
    void testGetClubNotFound() {
        when(clubController.getClub(1L)).thenReturn(null);
        
        ResponseEntity<Club> response = clubRestService.getClub(1L);
        
        assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
        assertNull(response.getBody());
    }

    @Test
    void testGetClubsByUserSuccess() {
        when(clubController.getClubsByUser(1L)).thenReturn(clubs);
        
        ResponseEntity<List<Club>> response = clubRestService.getClubsByUser(1L);
        
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals(1, response.getBody().size());
        assertEquals(testClub.getId(), response.getBody().get(0).getId());
    }

    @Test
    void testGetClubsByUserNotFound() {
        when(clubController.getClubsByUser(1L)).thenReturn(null);
        
        ResponseEntity<List<Club>> response = clubRestService.getClubsByUser(1L);
        
        assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
        assertNull(response.getBody());
    }

    @Test
    void testGetClubMembersSuccess() {
        when(clubMemberController.getClubMembers(1L)).thenReturn(members);
        
        ResponseEntity<?> response = clubRestService.getClubMembers(1L);
        
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertTrue(response.getBody() instanceof List);
        List<ClubMember> responseBody = (List<ClubMember>) response.getBody();
        assertEquals(1, responseBody.size());
        assertEquals(testMember.getId(), responseBody.get(0).getId());
    }

    @Test
    void testGetClubMembersNotFound() {
        when(clubMemberController.getClubMembers(1L)).thenReturn(null);
        
        ResponseEntity<?> response = clubRestService.getClubMembers(1L);
        
        assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
        assertEquals("Club not found", response.getBody());
    }

    @Test
    void testCreateClubSuccess() {
        when(clubController.createClub("Test Club", "Test Description", true, 1L)).thenReturn(testClub);
        
        ResponseEntity<?> response = clubRestService.createClub("Test Club", "Test Description", true, 1L);
        
        assertEquals(HttpStatus.CREATED, response.getStatusCode());
        assertEquals(testClub, response.getBody());
    }

    @Test
    void testCreateClubUserNotFound() {
        when(clubController.createClub("Test Club", "Test Description", true, 1L)).thenReturn(null);
        
        ResponseEntity<?> response = clubRestService.createClub("Test Club", "Test Description", true, 1L);
        
        assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
        assertEquals("User not found", response.getBody());
    }

    @Test
    void testUpdateClubSuccess() {
        when(clubController.updateClub(1L, "Updated Club", "Updated Description", false)).thenReturn(testClub);
        
        ResponseEntity<?> response = clubRestService.updateClub(1L, "Updated Club", "Updated Description", false);
        
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals(testClub, response.getBody());
    }

    @Test
    void testUpdateClubNotFound() {
        when(clubController.updateClub(1L, "Updated Club", "Updated Description", false)).thenReturn(null);
        
        ResponseEntity<?> response = clubRestService.updateClub(1L, "Updated Club", "Updated Description", false);
        
        assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
        assertEquals("Club not found", response.getBody());
    }

    @Test
    void testDeleteClubSuccess() {
        when(clubController.deleteClub(1L)).thenReturn(true);
        
        ResponseEntity<Map<String, String>> response = clubRestService.deleteClub(1L);
        
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals("Club deleted", response.getBody().get("message"));
    }

    @Test
    void testDeleteClubNotFound() {
        when(clubController.deleteClub(1L)).thenReturn(false);
        
        ResponseEntity<Map<String, String>> response = clubRestService.deleteClub(1L);
        
        assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
        assertEquals("Club not found", response.getBody().get("message"));
    }

    @Test
    void testGetClubMemberCountSuccess() {
        when(clubMemberController.getClubMemberCount(1L)).thenReturn(5);
        
        ResponseEntity<Map<String, Integer>> response = clubRestService.getClubMemberCount(1L);
        
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals(Integer.valueOf(5), response.getBody().get("memberCount"));
    }

    @Test
    void testGetClubMemberCountNotFound() {
        when(clubMemberController.getClubMemberCount(1L)).thenReturn(-1);
        
        ResponseEntity<Map<String, Integer>> response = clubRestService.getClubMemberCount(1L);
        
        assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
        assertEquals(Integer.valueOf(0), response.getBody().get("message"));
    }
}
package hu.benkototh.cardgame.backend.rest.controller;

import hu.benkototh.cardgame.backend.rest.model.Club;
import hu.benkototh.cardgame.backend.rest.model.User;
import hu.benkototh.cardgame.backend.rest.repository.IClubRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ClubControllerTest {

    @Mock
    private IClubRepository clubRepository;

    @Mock
    private UserController userController;

    @Mock
    private ClubMemberController clubMemberController;

    @Mock
    private ClubInviteController clubInviteController;

    @Mock
    private AuditLogController auditLogController;

    @InjectMocks
    private ClubController clubController;

    private Club testClub;
    private User testUser;
    private List<Club> clubs;

    @BeforeEach
    void setUp() {
        testClub = new Club("Test Club", "Test Description", true);
        testClub.setId(1L);

        testUser = new User();
        testUser.setId(1L);
        testUser.setUsername("testuser");

        clubs = new ArrayList<>();
        clubs.add(testClub);
    }

    @Test
    void testGetAllClubs() {
        when(clubRepository.findAll()).thenReturn(clubs);
        
        List<Club> result = clubController.getAllClubs();
        
        assertEquals(1, result.size());
        assertEquals(testClub.getName(), result.get(0).getName());
    }

    @Test
    void testGetPublicClubs() {
        when(clubRepository.findAll()).thenReturn(clubs);
        
        List<Club> result = clubController.getPublicClubs();
        
        assertEquals(1, result.size());
        assertEquals(testClub.getName(), result.get(0).getName());
    }

    @Test
    void testGetJoinableClubsByUserId() {
        when(userController.getUser(1L)).thenReturn(testUser);
        when(clubRepository.findAll()).thenReturn(clubs);
        when(clubMemberController.isMember(testUser, testClub)).thenReturn(false);
        
        List<Club> result = clubController.getJoinableClubs(1L);
        
        assertEquals(1, result.size());
        assertEquals(testClub.getName(), result.get(0).getName());
    }

    @Test
    void testGetJoinableClubsByUserIdUserNotFound() {
        when(userController.getUser(1L)).thenReturn(null);
        
        List<Club> result = clubController.getJoinableClubs(1L);
        
        assertNull(result);
    }

    @Test
    void testGetJoinableClubsByUser() {
        when(clubRepository.findAll()).thenReturn(clubs);
        when(clubMemberController.isMember(testUser, testClub)).thenReturn(false);
        
        List<Club> result = clubController.getJoinableClubs(testUser);
        
        assertEquals(1, result.size());
        assertEquals(testClub.getName(), result.get(0).getName());
    }

    @Test
    void testGetClub() {
        when(clubRepository.findById(1L)).thenReturn(Optional.of(testClub));
        
        Club result = clubController.getClub(1L);
        
        assertNotNull(result);
        assertEquals(testClub.getName(), result.getName());
    }

    @Test
    void testGetClubNotFound() {
        when(clubRepository.findById(1L)).thenReturn(Optional.empty());
        
        Club result = clubController.getClub(1L);
        
        assertNull(result);
    }

    @Test
    void testGetClubsByUser() {
        when(userController.getUser(1L)).thenReturn(testUser);
        when(clubMemberController.getClubsByUser(testUser)).thenReturn(clubs);
        
        List<Club> result = clubController.getClubsByUser(1L);
        
        assertEquals(1, result.size());
        assertEquals(testClub.getName(), result.get(0).getName());
    }

    @Test
    void testGetClubsByUserUserNotFound() {
        when(userController.getUser(1L)).thenReturn(null);
        
        List<Club> result = clubController.getClubsByUser(1L);
        
        assertNull(result);
    }

    @Test
    void testCreateClub() {
        when(userController.getUser(1L)).thenReturn(testUser);
        when(clubRepository.save(any(Club.class))).thenReturn(testClub);
        
        Club result = clubController.createClub("Test Club", "Test Description", true, 1L);
        
        assertNotNull(result);
        assertEquals(testClub.getName(), result.getName());
        verify(clubMemberController).addClubMember(testClub, testUser, "admin");
        verify(auditLogController).logAction(eq("CLUB_CREATED"), eq(1L), anyString());
    }

    @Test
    void testCreateClubUserNotFound() {
        when(userController.getUser(1L)).thenReturn(null);
        
        Club result = clubController.createClub("Test Club", "Test Description", true, 1L);
        
        assertNull(result);
    }

    @Test
    void testUpdateClub() {
        when(clubRepository.findById(1L)).thenReturn(Optional.of(testClub));
        when(clubRepository.save(any(Club.class))).thenReturn(testClub);
        
        Club result = clubController.updateClub(1L, "Updated Club", "Updated Description", false);
        
        assertNotNull(result);
        assertEquals("Updated Club", testClub.getName());
        assertEquals("Updated Description", testClub.getDescription());
        assertFalse(testClub.isPublic());
        verify(auditLogController).logAction(eq("CLUB_UPDATED"), eq(0L), anyString());
    }

    @Test
    void testUpdateClubNotFound() {
        when(clubRepository.findById(1L)).thenReturn(Optional.empty());
        
        Club result = clubController.updateClub(1L, "Updated Club", "Updated Description", false);
        
        assertNull(result);
    }

    @Test
    void testDeleteClub() {
        when(clubRepository.findById(1L)).thenReturn(Optional.of(testClub));
        
        boolean result = clubController.deleteClub(1L);
        
        assertTrue(result);
        verify(clubRepository).deleteById(1L);
        verify(auditLogController).logAction(eq("CLUB_DELETED"), eq(0L), anyString());
    }

    @Test
    void testDeleteClubNotFound() {
        when(clubRepository.findById(1L)).thenReturn(Optional.empty());
        
        boolean result = clubController.deleteClub(1L);
        
        assertFalse(result);
    }

    @Test
    void testGetClubById() {
        when(clubRepository.findById(1L)).thenReturn(Optional.of(testClub));
        
        Optional<Club> result = clubController.getClubById(1L);
        
        assertTrue(result.isPresent());
        assertEquals(testClub.getName(), result.get().getName());
    }

    @Test
    void testIsUserMemberOfClub() {
        when(userController.getUser(1L)).thenReturn(testUser);
        when(clubRepository.findById(1L)).thenReturn(Optional.of(testClub));
        when(clubMemberController.isMember(testUser, testClub)).thenReturn(true);
        
        boolean result = clubController.isUserMemberOfClub(1L, 1L);
        
        assertTrue(result);
    }

    @Test
    void testIsUserMemberOfClubUserNotFound() {
        when(userController.getUser(1L)).thenReturn(null);
        
        boolean result = clubController.isUserMemberOfClub(1L, 1L);
        
        assertFalse(result);
    }

    @Test
    void testIsUserMemberOfClubClubNotFound() {
        when(userController.getUser(1L)).thenReturn(testUser);
        when(clubRepository.findById(1L)).thenReturn(Optional.empty());
        
        boolean result = clubController.isUserMemberOfClub(1L, 1L);
        
        assertFalse(result);
    }
}
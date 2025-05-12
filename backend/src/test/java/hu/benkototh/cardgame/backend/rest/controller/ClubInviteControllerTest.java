package hu.benkototh.cardgame.backend.rest.controller;

import hu.benkototh.cardgame.backend.rest.Data.Club;
import hu.benkototh.cardgame.backend.rest.Data.ClubInvite;
import hu.benkototh.cardgame.backend.rest.Data.User;
import hu.benkototh.cardgame.backend.rest.repository.IClubInviteRepository;
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
class ClubInviteControllerTest {

    @Mock
    private IClubInviteRepository clubInviteRepository;

    @Mock
    private UserController userController;

    @Mock
    private ClubController clubController;

    @Mock
    private ClubMemberController clubMemberController;

    @Mock
    private AuditLogController auditLogController;

    @InjectMocks
    private ClubInviteController clubInviteController;

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
        when(clubInviteRepository.findAll()).thenReturn(invites);
        
        List<ClubInvite> result = clubInviteController.getClubInvites(1L);
        
        assertEquals(1, result.size());
        assertEquals(testInvite.getId(), result.get(0).getId());
    }

    @Test
    void testGetPendingInvites() {
        when(clubInviteRepository.findAll()).thenReturn(invites);
        
        List<ClubInvite> result = clubInviteController.getPendingInvites(1L);
        
        assertEquals(1, result.size());
        assertEquals(testInvite.getId(), result.get(0).getId());
    }

    @Test
    void testGetInviteHistory() {
        testInvite.setStatus("accepted");
        when(clubInviteRepository.findAll()).thenReturn(invites);
        
        List<ClubInvite> result = clubInviteController.getInviteHistory(1L);
        
        assertEquals(1, result.size());
        assertEquals(testInvite.getId(), result.get(0).getId());
    }

    @Test
    void testAddClubInviteSuccess() {
        when(clubController.getClub(1L)).thenReturn(testClub);
        when(userController.findByUsername("testuser")).thenReturn(testUser);
        when(clubMemberController.isMember(testUser, testClub)).thenReturn(false);
        when(clubInviteRepository.findAll()).thenReturn(new ArrayList<>());
        when(clubInviteRepository.save(any(ClubInvite.class))).thenReturn(testInvite);
        
        ClubInvite result = clubInviteController.addClubInvite(1L, "testuser");
        
        assertNotNull(result);
        assertEquals(testInvite.getId(), result.getId());
        verify(auditLogController).logAction(eq("CLUB_INVITE_SENT"), eq(0L), anyString());
    }

    @Test
    void testAddClubInviteUserNotFound() {
        when(clubController.getClub(1L)).thenReturn(testClub);
        when(userController.findByUsername("testuser")).thenReturn(null);
        
        ClubInvite result = clubInviteController.addClubInvite(1L, "testuser");
        
        assertNull(result);
        verify(auditLogController).logAction(eq("CLUB_INVITE_FAILED"), eq(0L), anyString());
    }

    @Test
    void testAddClubInviteClubNotFound() {
        when(clubController.getClub(1L)).thenReturn(null);
        
        ClubInvite result = clubInviteController.addClubInvite(1L, "testuser");
        
        assertNull(result);
        verify(auditLogController).logAction(eq("CLUB_INVITE_FAILED"), eq(0L), anyString());
    }

    @Test
    void testAddClubInviteAlreadyMember() {
        when(clubController.getClub(1L)).thenReturn(testClub);
        when(userController.findByUsername("testuser")).thenReturn(testUser);
        when(clubMemberController.isMember(testUser, testClub)).thenReturn(true);
        
        ClubInvite result = clubInviteController.addClubInvite(1L, "testuser");
        
        assertNull(result);
        verify(auditLogController).logAction(eq("CLUB_INVITE_FAILED"), eq(0L), anyString());
    }

    @Test
    void testAddClubInviteAlreadyInvited() {
        when(clubController.getClub(1L)).thenReturn(testClub);
        when(userController.findByUsername("testuser")).thenReturn(testUser);
        when(clubMemberController.isMember(testUser, testClub)).thenReturn(false);
        when(clubInviteRepository.findAll()).thenReturn(invites);
        
        ClubInvite result = clubInviteController.addClubInvite(1L, "testuser");
        
        assertNull(result);
        verify(auditLogController).logAction(eq("CLUB_INVITE_FAILED"), eq(0L), anyString());
    }

    @Test
    void testAcceptClubInviteSuccess() {
        when(clubInviteRepository.findById(1L)).thenReturn(Optional.of(testInvite));
        when(clubInviteRepository.save(any(ClubInvite.class))).thenReturn(testInvite);
        
        ClubInvite result = clubInviteController.acceptClubInvite(1L);
        
        assertNotNull(result);
        assertEquals("accepted", result.getStatus());
        verify(clubMemberController).addClubMember(testClub, testUser, "member");
        verify(auditLogController).logAction(eq("CLUB_INVITE_ACCEPTED"), eq(1L), anyString());
    }

    @Test
    void testAcceptClubInviteNotFound() {
        when(clubInviteRepository.findById(1L)).thenReturn(Optional.empty());
        
        ClubInvite result = clubInviteController.acceptClubInvite(1L);
        
        assertNull(result);
        verify(auditLogController).logAction(eq("CLUB_INVITE_ACCEPT_FAILED"), eq(0L), anyString());
    }

    @Test
    void testDeclineClubInviteSuccess() {
        when(clubInviteRepository.findById(1L)).thenReturn(Optional.of(testInvite));
        when(clubInviteRepository.save(any(ClubInvite.class))).thenReturn(testInvite);
        
        ClubInvite result = clubInviteController.declineClubInvite(1L);
        
        assertNotNull(result);
        assertEquals("declined", result.getStatus());
        verify(auditLogController).logAction(eq("CLUB_INVITE_DECLINED"), eq(1L), anyString());
    }

    @Test
    void testDeclineClubInviteNotFound() {
        when(clubInviteRepository.findById(1L)).thenReturn(Optional.empty());
        
        ClubInvite result = clubInviteController.declineClubInvite(1L);
        
        assertNull(result);
        verify(auditLogController).logAction(eq("CLUB_INVITE_DECLINE_FAILED"), eq(0L), anyString());
    }

    @Test
    void testDeleteClubInviteSuccess() {
        when(clubInviteRepository.findById(1L)).thenReturn(Optional.of(testInvite));
        
        boolean result = clubInviteController.deleteClubInvite(1L);
        
        assertTrue(result);
        verify(clubInviteRepository).delete(testInvite);
        verify(auditLogController).logAction(eq("CLUB_INVITE_DELETED"), eq(0L), anyString());
    }

    @Test
    void testDeleteClubInviteNotFound() {
        when(clubInviteRepository.findById(1L)).thenReturn(Optional.empty());
        
        boolean result = clubInviteController.deleteClubInvite(1L);
        
        assertFalse(result);
        verify(auditLogController).logAction(eq("CLUB_INVITE_DELETE_FAILED"), eq(0L), anyString());
    }
}
package hu.benkototh.cardgame.backend.rest.controller;

import hu.benkototh.cardgame.backend.rest.model.Club;
import hu.benkototh.cardgame.backend.rest.model.ClubMember;
import hu.benkototh.cardgame.backend.rest.model.User;
import hu.benkototh.cardgame.backend.rest.repository.IClubMemberRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ClubMemberControllerTest {

    @Mock
    private IClubMemberRepository clubMemberRepository;

    @Mock
    private UserController userController;

    @Mock
    private ClubController clubController;

    @Mock
    private AuditLogController auditLogController;

    @InjectMocks
    private ClubMemberController clubMemberController;

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
    void testGetClubMembers() {
        when(clubMemberRepository.findAll()).thenReturn(members);
        
        List<ClubMember> result = clubMemberController.getClubMembers(1L);
        
        assertNotNull(result);
        assertEquals(1, result.size());
        assertEquals(testMember.getId(), result.get(0).getId());
        assertEquals(testUser.getUsername(), result.get(0).getUsername());
    }

    @Test
    void testGetClubMembersEmpty() {
        when(clubMemberRepository.findAll()).thenReturn(new ArrayList<>());
        
        List<ClubMember> result = clubMemberController.getClubMembers(1L);
        
        assertNull(result);
    }

    @Test
    void testGetClubMember() {
        when(userController.getUser(1L)).thenReturn(testUser);
        when(clubController.getClub(1L)).thenReturn(testClub);
        when(clubMemberRepository.findAll()).thenReturn(members);
        
        ClubMember result = clubMemberController.getClubMember(1L, 1L);
        
        assertNotNull(result);
        assertEquals(testMember.getId(), result.getId());
    }

    @Test
    void testGetClubMemberUserNotFound() {
        when(userController.getUser(1L)).thenReturn(null);
        
        ClubMember result = clubMemberController.getClubMember(1L, 1L);
        
        assertNull(result);
    }

    @Test
    void testGetClubMemberClubNotFound() {
        when(userController.getUser(1L)).thenReturn(testUser);
        when(clubController.getClub(1L)).thenReturn(null);
        
        ClubMember result = clubMemberController.getClubMember(1L, 1L);
        
        assertNull(result);
    }

    @Test
    void testGetClubMemberByUser() {
        when(clubMemberRepository.findAll()).thenReturn(members);
        
        ClubMember result = clubMemberController.getClubMemberByUser(testUser);
        
        assertNotNull(result);
        assertEquals(testMember.getId(), result.getId());
    }

    @Test
    void testAddClubMember() {
        when(userController.getUser(1L)).thenReturn(testUser);
        when(clubController.getClub(1L)).thenReturn(testClub);
        when(clubMemberRepository.save(any(ClubMember.class))).thenReturn(testMember);
        
        ClubMember result = clubMemberController.addClubMember(1L, 1L);
        
        assertNotNull(result);
        assertEquals(testMember.getId(), result.getId());
        verify(auditLogController).logAction(eq("CLUB_MEMBER_ADDED"), eq(1L), anyString());
    }

    @Test
    void testAddClubMemberUserNotFound() {
        when(userController.getUser(1L)).thenReturn(null);
        
        ClubMember result = clubMemberController.addClubMember(1L, 1L);
        
        assertNull(result);
    }

    @Test
    void testAddClubMemberClubNotFound() {
        when(userController.getUser(1L)).thenReturn(testUser);
        when(clubController.getClub(1L)).thenReturn(null);
        
        ClubMember result = clubMemberController.addClubMember(1L, 1L);
        
        assertNull(result);
    }

    @Test
    void testAddClubMemberWithRole() {
        when(clubMemberRepository.save(any(ClubMember.class))).thenReturn(testMember);
        
        ClubMember result = clubMemberController.addClubMember(testClub, testUser, "admin");
        
        assertNotNull(result);
        assertEquals(testMember.getId(), result.getId());
        verify(auditLogController).logAction(eq("CLUB_MEMBER_ADDED_WITH_ROLE"), eq(1L), anyString());
    }

    @Test
    void testGetClubMemberRole() {
        when(clubMemberRepository.findAll()).thenReturn(members);
        
        String result = clubMemberController.getClubMemberRole(1L, 1L);
        
        assertEquals("member", result);
    }

    @Test
    void testModifyClubMember() {
        when(clubMemberRepository.findAll()).thenReturn(members);
        when(clubMemberRepository.save(any(ClubMember.class))).thenReturn(testMember);
        
        ClubMember result = clubMemberController.modifyClubMember(1L, 1L, "moderator");
        
        assertNotNull(result);
        assertEquals("moderator", testMember.getRole());
        verify(auditLogController).logAction(eq("CLUB_MEMBER_ROLE_MODIFIED"), eq(0L), anyString());
    }

    @Test
    void testModifyClubMemberNotFound() {
        when(clubMemberRepository.findAll()).thenReturn(new ArrayList<>());
        
        ClubMember result = clubMemberController.modifyClubMember(1L, 1L, "moderator");
        
        assertNull(result);
    }

    @Test
    void testModifyClubMemberInvalidRole() {
        when(clubMemberRepository.findAll()).thenReturn(members);
        
        ClubMember result = clubMemberController.modifyClubMember(1L, 1L, "invalid");
        
        assertNull(result);
    }

    @Test
    void testRemoveClubMember() {
        when(clubMemberRepository.findAll()).thenReturn(members);
        
        boolean result = clubMemberController.removeClubMember(1L, 1L);
        
        assertTrue(result);
        verify(clubMemberRepository).delete(testMember);
        verify(auditLogController).logAction(eq("CLUB_MEMBER_REMOVED"), eq(0L), anyString());
    }

    @Test
    void testRemoveClubMemberNotFound() {
        when(clubMemberRepository.findAll()).thenReturn(new ArrayList<>());
        
        boolean result = clubMemberController.removeClubMember(1L, 1L);
        
        assertFalse(result);
    }

    @Test
    void testLeaveClub() {
        when(clubMemberRepository.findAll()).thenReturn(members);
        
        boolean result = clubMemberController.leaveClub(1L, 1L);
        
        assertTrue(result);
        verify(clubMemberRepository).delete(testMember);
        verify(auditLogController).logAction(eq("CLUB_LEFT"), eq(1L), anyString());
    }

    @Test
    void testLeaveClubNotFound() {
        when(clubMemberRepository.findAll()).thenReturn(new ArrayList<>());
        
        boolean result = clubMemberController.leaveClub(1L, 1L);
        
        assertFalse(result);
    }

    @Test
    void testLeaveClubAsAdmin() {
        testMember.setRole("admin");
        when(clubMemberRepository.findAll()).thenReturn(members);
        
        boolean result = clubMemberController.leaveClub(1L, 1L);
        
        assertFalse(result);
    }

    @Test
    void testFindByClubId() {
        when(clubMemberRepository.findAll()).thenReturn(members);
        
        List<ClubMember> result = clubMemberController.findByClubId(1L);
        
        assertEquals(1, result.size());
        assertEquals(testMember.getId(), result.get(0).getId());
    }

    @Test
    void testIsMember() {
        when(clubMemberRepository.findAll()).thenReturn(members);
        
        boolean result = clubMemberController.isMember(testUser, testClub);
        
        assertTrue(result);
    }

    @Test
    void testGetClubsByUser() {
        when(clubMemberRepository.findAll()).thenReturn(members);
        
        List<Club> result = clubMemberController.getClubsByUser(testUser);
        
        assertEquals(1, result.size());
        assertEquals(testClub.getId(), result.get(0).getId());
    }

    @Test
    void testGetClubMemberCount() {
        when(clubMemberRepository.findAll()).thenReturn(members);
        
        int result = clubMemberController.getClubMemberCount(1L);
        
        assertEquals(1, result);
    }
}
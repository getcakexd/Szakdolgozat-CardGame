package hu.benkototh.cardgame.backend.rest.data;

import hu.benkototh.cardgame.backend.rest.Data.Club;
import hu.benkototh.cardgame.backend.rest.Data.Game;
import hu.benkototh.cardgame.backend.rest.Data.Lobby;
import hu.benkototh.cardgame.backend.rest.Data.User;
import org.junit.jupiter.api.Test;
import java.util.Date;
import java.util.HashSet;
import java.util.Set;
import static org.junit.jupiter.api.Assertions.*;

class LobbyTest {

    @Test
    void testLobbyCreation() {
        Lobby lobby = new Lobby();
        assertNotNull(lobby);
        assertNotNull(lobby.getCode());
        assertEquals(6, lobby.getCode().length());
        assertTrue(lobby.getCode().matches("[A-Z]{6}"));
    }

    @Test
    void testGettersAndSetters() {
        Lobby lobby = new Lobby();
        
        long id = 1L;
        String code = "ABCDEF";
        User leader = new User();
        leader.setId(1L);
        Game game = new Game();
        game.setId(1L);
        game.setMinPlayers(2);
        Set<User> players = new HashSet<>();
        players.add(leader);
        Club club = new Club();
        club.setId(1L);
        boolean isPublic = true;
        boolean playWithPoints = true;
        int minPlayers = 2;
        String status = "WAITING";
        Date createdAt = new Date();
        String cardGameId = "g-123456";
        
        lobby.setId(id);
        lobby.setCode(code);
        lobby.setLeader(leader);
        lobby.setGame(game);
        lobby.setPlayers(players);
        lobby.setClub(club);
        lobby.setPublic(isPublic);
        lobby.setPlayWithPoints(playWithPoints);
        lobby.setMinPlayers(minPlayers);
        lobby.setStatus(status);
        lobby.setCardGameId(cardGameId);
        
        assertEquals(id, lobby.getId());
        assertEquals(code, lobby.getCode());
        assertEquals(leader, lobby.getLeader());
        assertEquals(game, lobby.getGame());
        assertEquals(players, lobby.getPlayers());
        assertEquals(club, lobby.getClub());
        assertEquals(isPublic, lobby.isPublic());
        assertEquals(playWithPoints, lobby.isPlayWithPoints());
        assertEquals(minPlayers, lobby.getMinPlayers());
        assertEquals(status, lobby.getStatus());
        assertEquals(cardGameId, lobby.getCardGameId());
    }

    @Test
    void testPlayerManagement() {
        Lobby lobby = new Lobby();
        
        User player1 = new User();
        player1.setId(1L);
        
        User player2 = new User();
        player2.setId(2L);
        
        lobby.addPlayer(player1);
        assertEquals(1, lobby.getPlayers().size());
        assertTrue(lobby.getPlayers().contains(player1));
        
        lobby.addPlayer(player2);
        assertEquals(2, lobby.getPlayers().size());
        assertTrue(lobby.getPlayers().contains(player2));
        
        lobby.removePlayer(player1);
        assertEquals(1, lobby.getPlayers().size());
        assertFalse(lobby.getPlayers().contains(player1));
        assertTrue(lobby.getPlayers().contains(player2));
    }

    @Test
    void testCanStart() {
        Lobby lobby = new Lobby();
        
        Game game = new Game();
        game.setMinPlayers(2);
        
        User player1 = new User();
        player1.setId(1L);
        
        User player2 = new User();
        player2.setId(2L);
        
        lobby.setGame(game);
        lobby.setMinPlayers(game.getMinPlayers());
        
        lobby.addPlayer(player1);
        assertFalse(lobby.canStart());
        
        lobby.addPlayer(player2);
        assertTrue(lobby.canStart());
    }
}
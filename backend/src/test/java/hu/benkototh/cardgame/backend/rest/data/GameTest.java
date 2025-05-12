package hu.benkototh.cardgame.backend.rest.data;

import hu.benkototh.cardgame.backend.rest.Data.Game;
import hu.benkototh.cardgame.backend.rest.Data.GameDescription;
import hu.benkototh.cardgame.backend.rest.Data.GameRules;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

class GameTest {

    @Test
    void testGameCreation() {
        Game game = new Game();
        assertNotNull(game);
    }

    @Test
    void testGettersAndSetters() {
        Game game = new Game();
        
        game.setId(1L);
        game.setName("Poker");
        game.setActive(true);
        game.setMinPlayers(2);
        game.setMaxPlayers(8);
        game.setFactorySign("PokerGameFactory");
        game.setFactoryId(1);
        
        assertEquals(1L, game.getId());
        assertEquals("Poker", game.getName());
        assertTrue(game.isActive());
        assertEquals(2, game.getMinPlayers());
        assertEquals(8, game.getMaxPlayers());
        assertEquals("PokerGameFactory", game.getFactorySign());
        assertEquals(1, game.getFactoryId());
    }
    
    @Test
    void testDescriptionManagement() {
        Game game = new Game();
        GameDescription description = new GameDescription();
        description.setLanguage("en");
        description.setContent("Test description");
        
        game.addDescription(description);
        
        assertEquals(1, game.getDescriptions().size());
        assertEquals(game, description.getGame());
        
        game.removeDescription(description);
        
        assertEquals(0, game.getDescriptions().size());
        assertNull(description.getGame());
    }
    
    @Test
    void testRulesManagement() {
        Game game = new Game();
        GameRules rules = new GameRules();
        rules.setLanguage("en");
        rules.setContent("Test rules");
        
        game.addRules(rules);
        
        assertEquals(1, game.getRules().size());
        assertEquals(game, rules.getGame());
        
        game.removeRules(rules);
        
        assertEquals(0, game.getRules().size());
        assertNull(rules.getGame());
    }
}
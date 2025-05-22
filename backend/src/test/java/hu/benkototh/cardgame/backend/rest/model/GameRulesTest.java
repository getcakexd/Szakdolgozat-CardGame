package hu.benkototh.cardgame.backend.rest.model;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

class GameRulesTest {

    @Test
    void testGameRulesCreation() {
        GameRules rules = new GameRules();
        assertNotNull(rules);
        assertFalse(rules.isMarkdown());
    }

    @Test
    void testGameRulesCreationWithParameters() {
        String language = "en";
        String content = "Test rules";
        
        GameRules rules = new GameRules(language, content);
        
        assertNotNull(rules);
        assertEquals(language, rules.getLanguage());
        assertEquals(content, rules.getContent());
        assertFalse(rules.isMarkdown());
    }

    @Test
    void testGameRulesCreationWithMarkdown() {
        String language = "en";
        String content = "Test rules";
        boolean isMarkdown = true;
        
        GameRules rules = new GameRules(language, content, isMarkdown);
        
        assertNotNull(rules);
        assertEquals(language, rules.getLanguage());
        assertEquals(content, rules.getContent());
        assertTrue(rules.isMarkdown());
    }

    @Test
    void testGettersAndSetters() {
        GameRules rules = new GameRules();
        
        long id = 1L;
        String language = "fr";
        String content = "French rules";
        boolean isMarkdown = true;
        Game game = new Game();
        
        rules.setId(id);
        rules.setLanguage(language);
        rules.setContent(content);
        rules.setMarkdown(isMarkdown);
        rules.setGame(game);
        
        assertEquals(id, rules.getId());
        assertEquals(language, rules.getLanguage());
        assertEquals(content, rules.getContent());
        assertEquals(isMarkdown, rules.isMarkdown());
        assertEquals(game, rules.getGame());
    }
}
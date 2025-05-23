package hu.benkototh.cardgame.backend.rest.model;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

class GameDescriptionTest {

    @Test
    void testGameDescriptionCreation() {
        GameDescription description = new GameDescription();
        assertNotNull(description);
        assertFalse(description.isMarkdown());
    }

    @Test
    void testGameDescriptionCreationWithParameters() {
        String language = "en";
        String content = "Test description";
        
        GameDescription description = new GameDescription(language, content);
        
        assertNotNull(description);
        assertEquals(language, description.getLanguage());
        assertEquals(content, description.getContent());
        assertFalse(description.isMarkdown());
    }

    @Test
    void testGameDescriptionCreationWithMarkdown() {
        String language = "en";
        String content = "Test description";
        boolean isMarkdown = true;
        
        GameDescription description = new GameDescription(language, content, isMarkdown);
        
        assertNotNull(description);
        assertEquals(language, description.getLanguage());
        assertEquals(content, description.getContent());
        assertTrue(description.isMarkdown());
    }

    @Test
    void testGettersAndSetters() {
        GameDescription description = new GameDescription();
        
        long id = 1L;
        String language = "fr";
        String content = "French description";
        boolean isMarkdown = true;
        Game game = new Game();
        
        description.setId(id);
        description.setLanguage(language);
        description.setContent(content);
        description.setMarkdown(isMarkdown);
        description.setGame(game);
        
        assertEquals(id, description.getId());
        assertEquals(language, description.getLanguage());
        assertEquals(content, description.getContent());
        assertEquals(isMarkdown, description.isMarkdown());
        assertEquals(game, description.getGame());
    }
}
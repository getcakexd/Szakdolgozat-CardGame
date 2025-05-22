package hu.benkototh.cardgame.backend.rest.model;

import org.junit.jupiter.api.Test;
import java.util.ArrayList;
import java.util.List;
import static org.junit.jupiter.api.Assertions.*;

class GameCreationDTOTest {

    @Test
    void testGameCreationDTOCreation() {
        GameCreationDTO dto = new GameCreationDTO();
        assertNotNull(dto);
    }

    @Test
    void testGettersAndSetters() {
        GameCreationDTO dto = new GameCreationDTO();
        
        String name = "Poker";
        boolean active = true;
        int minPlayers = 2;
        int maxPlayers = 8;
        String factorySign = "PokerGameFactory";
        int factoryId = 1;
        
        dto.setName(name);
        dto.setActive(active);
        dto.setMinPlayers(minPlayers);
        dto.setMaxPlayers(maxPlayers);
        dto.setFactorySign(factorySign);
        dto.setFactoryId(factoryId);
        
        assertEquals(name, dto.getName());
        assertEquals(active, dto.isActive());
        assertEquals(minPlayers, dto.getMinPlayers());
        assertEquals(maxPlayers, dto.getMaxPlayers());
        assertEquals(factorySign, dto.getFactorySign());
        assertEquals(factoryId, dto.getFactoryId());
    }

    @Test
    void testDescriptionsAndRules() {
        GameCreationDTO dto = new GameCreationDTO();
        
        List<GameCreationDTO.LocalizedContent> descriptions = new ArrayList<>();
        GameCreationDTO.LocalizedContent enDesc = new GameCreationDTO.LocalizedContent();
        enDesc.setLanguage("en");
        enDesc.setContent("English description");
        enDesc.setMarkdown(true);
        descriptions.add(enDesc);
        
        List<GameCreationDTO.LocalizedContent> rules = new ArrayList<>();
        GameCreationDTO.LocalizedContent enRules = new GameCreationDTO.LocalizedContent();
        enRules.setLanguage("en");
        enRules.setContent("English rules");
        enRules.setMarkdown(false);
        rules.add(enRules);
        
        dto.setDescriptions(descriptions);
        dto.setRules(rules);
        
        assertEquals(descriptions, dto.getDescriptions());
        assertEquals(rules, dto.getRules());
        assertEquals(1, dto.getDescriptions().size());
        assertEquals(1, dto.getRules().size());
        
        assertEquals("en", dto.getDescriptions().get(0).getLanguage());
        assertEquals("English description", dto.getDescriptions().get(0).getContent());
        assertTrue(dto.getDescriptions().get(0).isMarkdown());
        
        assertEquals("en", dto.getRules().get(0).getLanguage());
        assertEquals("English rules", dto.getRules().get(0).getContent());
        assertFalse(dto.getRules().get(0).isMarkdown());
    }

    @Test
    void testLocalizedContentGettersAndSetters() {
        GameCreationDTO.LocalizedContent content = new GameCreationDTO.LocalizedContent();
        
        String language = "fr";
        String contentText = "French content";
        boolean isMarkdown = true;
        
        content.setLanguage(language);
        content.setContent(contentText);
        content.setMarkdown(isMarkdown);
        
        assertEquals(language, content.getLanguage());
        assertEquals(contentText, content.getContent());
        assertEquals(isMarkdown, content.isMarkdown());
    }
}
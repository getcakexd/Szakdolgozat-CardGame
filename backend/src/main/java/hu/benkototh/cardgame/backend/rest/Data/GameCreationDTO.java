package hu.benkototh.cardgame.backend.rest.Data;

import io.swagger.v3.oas.annotations.media.Schema;
import java.util.List;

@Schema(description = "DTO for creating a new game definition")
public class GameCreationDTO {

    @Schema(description = "Name of the game", example = "Poker")
    private String name;

    @Schema(description = "Whether the game is active and available to play", example = "true")
    private boolean active;

    @Schema(description = "Minimum number of players required", example = "2")
    private int minPlayers;

    @Schema(description = "Maximum number of players allowed", example = "8")
    private int maxPlayers;

    @Schema(description = "Identifier for the game factory implementation", example = "PokerGameFactory")
    private String factorySign;

    @Schema(description = "Numeric identifier for the game factory", example = "1")
    private int factoryId;

    @Schema(description = "List of game descriptions in different languages")
    private List<LocalizedContent> descriptions;

    @Schema(description = "List of game rules in different languages")
    private List<LocalizedContent> rules;

    @Schema(description = "Represents localized content in a specific language")
    public static class LocalizedContent {

        @Schema(description = "Language code", example = "en", allowableValues = {"en", "hu", "de", "fr"})
        private String language;

        @Schema(description = "Content text in the specified language", example = "A classic card game where players bet on who has the best hand.")
        private String content;

        @Schema(description = "Whether the content is formatted as Markdown", example = "true")
        private boolean isMarkdown;

        public String getLanguage() {
            return language;
        }

        public void setLanguage(String language) {
            this.language = language;
        }

        public String getContent() {
            return content;
        }

        public void setContent(String content) {
            this.content = content;
        }

        public boolean isMarkdown() {
            return isMarkdown;
        }

        public void setMarkdown(boolean markdown) {
            isMarkdown = markdown;
        }
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public boolean isActive() {
        return active;
    }

    public void setActive(boolean active) {
        this.active = active;
    }

    public int getMinPlayers() {
        return minPlayers;
    }

    public void setMinPlayers(int minPlayers) {
        this.minPlayers = minPlayers;
    }

    public int getMaxPlayers() {
        return maxPlayers;
    }

    public void setMaxPlayers(int maxPlayers) {
        this.maxPlayers = maxPlayers;
    }

    public String getFactorySign() {
        return factorySign;
    }

    public void setFactorySign(String factorySign) {
        this.factorySign = factorySign;
    }

    public int getFactoryId() {
        return factoryId;
    }

    public void setFactoryId(int factoryId) {
        this.factoryId = factoryId;
    }

    public List<LocalizedContent> getDescriptions() {
        return descriptions;
    }

    public void setDescriptions(List<LocalizedContent> descriptions) {
        this.descriptions = descriptions;
    }

    public List<LocalizedContent> getRules() {
        return rules;
    }

    public void setRules(List<LocalizedContent> rules) {
        this.rules = rules;
    }
}
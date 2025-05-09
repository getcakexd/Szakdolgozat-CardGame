package hu.benkototh.cardgame.backend.rest.Data;

import java.util.List;

public class GameCreationDTO {
    private String name;
    private boolean active;
    private int minPlayers;
    private int maxPlayers;
    private String factorySign;
    private int factoryId;
    private List<LocalizedContent> descriptions;
    private List<LocalizedContent> rules;

    public static class LocalizedContent {
        private String language;
        private String content;
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
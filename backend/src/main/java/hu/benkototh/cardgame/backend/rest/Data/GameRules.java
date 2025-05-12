package hu.benkototh.cardgame.backend.rest.Data;

import com.fasterxml.jackson.annotation.JsonBackReference;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.persistence.*;

@Entity
@Table(name = "game_rules")
@Schema(description = "Represents the rules of a card game in different languages")
public class GameRules {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Schema(description = "Unique identifier for the game rules", example = "1")
    private long id;

    @Column(nullable = false)
    @Schema(description = "Language code for the rules content", example = "en", allowableValues = {"en", "hu", "de", "fr"})
    private String language;

    @Column(columnDefinition = "TEXT")
    @Schema(description = "The actual rules content in the specified language", example = "The game is played with a standard 52-card deck...")
    private String content;

    @Column(nullable = false, columnDefinition = "BOOLEAN DEFAULT false")
    @Schema(description = "Whether the content is formatted as Markdown", example = "true")
    private boolean isMarkdown;

    @JsonBackReference
    @ManyToOne(fetch = FetchType.LAZY, cascade = {CascadeType.PERSIST, CascadeType.MERGE})
    @JoinColumn(name = "game_id",
            foreignKey = @ForeignKey(name = "fk_gamerules_game",
                    foreignKeyDefinition = "FOREIGN KEY (game_id) REFERENCES games(id) ON DELETE CASCADE"))
    @Schema(description = "The game these rules belong to")
    private Game game;

    public GameRules() {
        this.isMarkdown = false;
    }

    public GameRules(String language, String content) {
        this.language = language;
        this.content = content;
        this.isMarkdown = false;
    }

    public GameRules(String language, String content, boolean isMarkdown) {
        this.language = language;
        this.content = content;
        this.isMarkdown = isMarkdown;
    }

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

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

    public Game getGame() {
        return game;
    }

    public void setGame(Game game) {
        this.game = game;
    }
}
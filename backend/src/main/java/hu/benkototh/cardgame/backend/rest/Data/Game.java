package hu.benkototh.cardgame.backend.rest.Data;

import com.fasterxml.jackson.annotation.JsonManagedReference;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.persistence.*;
import java.util.HashSet;
import java.util.Set;

@Entity
@Table(name = "games")
@Schema(description = "Represents a card game definition")
public class Game {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Schema(description = "Unique identifier for the game", example = "1")
    private long id;

    @Schema(description = "Name of the game", example = "Poker")
    private String name;

    @Schema(description = "Whether the game is currently active and available to play", example = "true")
    private boolean active;

    @Schema(description = "Minimum number of players required", example = "2")
    private int minPlayers;

    @Schema(description = "Maximum number of players allowed", example = "8")
    private int maxPlayers;

    @Schema(description = "Identifier for the game factory implementation", example = "PokerGameFactory")
    private String factorySign;

    @Schema(description = "Numeric identifier for the game factory", example = "1")
    private int factoryId;

    @JsonManagedReference
    @OneToMany(mappedBy = "game", cascade = CascadeType.ALL, orphanRemoval = true)
    @Schema(description = "Set of game descriptions in different languages")
    private Set<GameDescription> descriptions = new HashSet<>();

    @JsonManagedReference
    @OneToMany(mappedBy = "game", cascade = CascadeType.ALL, orphanRemoval = true)
    @Schema(description = "Set of game rules in different languages")
    private Set<GameRules> rules = new HashSet<>();

    public Game() {
    }

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
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

    public Set<GameDescription> getDescriptions() {
        return descriptions;
    }

    public void setDescriptions(Set<GameDescription> descriptions) {
        this.descriptions = descriptions;
    }

    public Set<GameRules> getRules() {
        return rules;
    }

    public void setRules(Set<GameRules> rules) {
        this.rules = rules;
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

    public void addDescription(GameDescription description) {
        descriptions.add(description);
        description.setGame(this);
    }

    public void removeDescription(GameDescription description) {
        descriptions.remove(description);
        description.setGame(null);
    }

    public void addRules(GameRules rules) {
        this.rules.add(rules);
        rules.setGame(this);
    }

    public void removeRules(GameRules rules) {
        this.rules.remove(rules);
        rules.setGame(null);
    }
}
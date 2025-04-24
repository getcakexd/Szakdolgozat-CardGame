package hu.benkototh.cardgame.backend.rest.Data;

import com.fasterxml.jackson.annotation.JsonManagedReference;
import jakarta.persistence.*;
import java.util.HashSet;
import java.util.Set;

@Entity
@Table(name = "games")
public class Game {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private long id;
    private String name;
    private boolean active;
    private int minPlayers;
    private int maxPlayers;

    @JsonManagedReference
    @OneToMany(mappedBy = "game", cascade = CascadeType.ALL, orphanRemoval = true)
    private Set<GameDescription> descriptions = new HashSet<>();

    @JsonManagedReference
    @OneToMany(mappedBy = "game", cascade = CascadeType.ALL, orphanRemoval = true)
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
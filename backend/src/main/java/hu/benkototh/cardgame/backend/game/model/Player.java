package hu.benkototh.cardgame.backend.game.model;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.persistence.*;
import java.util.ArrayList;
import java.util.List;
import com.fasterxml.jackson.annotation.JsonBackReference;

@Entity
@Table(name = "players")
@Schema(description = "Represents a player in a card game")
public class Player {
    @Id
    @Schema(description = "Unique identifier for the player", example = "1")
    private String id;

    @Schema(description = "Username of the player", example = "johndoe")
    private String username;

    @ElementCollection(fetch = FetchType.EAGER)
    @CollectionTable(name = "player_hand", joinColumns = @JoinColumn(name = "player_id"
            , foreignKey = @ForeignKey(name = "fk_player_hand",
            foreignKeyDefinition = "FOREIGN KEY (player_id) REFERENCES players(id) ON DELETE CASCADE")
    ))
    @Schema(description = "Cards currently in the player's hand")
    private List<Card> hand;

    @ElementCollection(fetch = FetchType.EAGER)
    @CollectionTable(name = "player_won_cards", joinColumns = @JoinColumn(name = "player_id"
            , foreignKey = @ForeignKey(name = "fk_player_won_cards",
            foreignKeyDefinition = "FOREIGN KEY (player_id) REFERENCES players(id) ON DELETE CASCADE")
    ))
    @Schema(description = "Cards the player has won during the game")
    private List<Card> wonCards;

    @Schema(description = "Whether the player is still active in the game", example = "true")
    private boolean active;

    @Schema(description = "Current score of the player in the game", example = "42")
    private int score;

    @ManyToOne
    @JoinColumn(name = "game_id"
            , foreignKey = @ForeignKey(name = "fk_player_game",
            foreignKeyDefinition = "FOREIGN KEY (game_id) REFERENCES card_games(id) ON DELETE CASCADE"))
    @JsonBackReference
    @Schema(description = "The card game this player is participating in")
    private CardGame game;

    @Schema(description = "Whether this player is controlled by AI", example = "false")
    private boolean isAI;

    public Player() {
        this.hand = new ArrayList<>();
        this.wonCards = new ArrayList<>();
        this.active = true;
        this.score = 0;
        this.isAI = false;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public List<Card> getHand() {
        return hand;
    }

    public void setHand(List<Card> hand) {
        this.hand = hand;
    }

    public List<Card> getWonCards() {
        return wonCards;
    }

    public void setWonCards(List<Card> wonCards) {
        this.wonCards = wonCards;
    }

    public boolean isActive() {
        return active;
    }

    public void setActive(boolean active) {
        this.active = active;
    }

    public int getScore() {
        return score;
    }

    public void setScore(int score) {
        this.score = score;
    }

    public CardGame getGame() {
        return game;
    }

    public void setGame(CardGame game) {
        this.game = game;
    }

    public boolean isAI() {
        return isAI;
    }

    public void setAI(boolean AI) {
        isAI = AI;
    }

    @Schema(description = "Adds a card to the player's hand")
    public void addCardToHand(Card card) {
        if (hand == null) {
            hand = new ArrayList<>();
        }
        hand.add(card);
    }

    @Schema(description = "Removes a card from the player's hand")
    public void removeCardFromHand(Card card) {
        if (hand != null) {
            hand.removeIf(c -> c.getSuit() == card.getSuit() && c.getRank() == card.getRank());
        }
    }
}
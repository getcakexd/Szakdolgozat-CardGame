package hu.benkototh.cardgame.backend.game.model;

import jakarta.persistence.*;
import java.util.ArrayList;
import java.util.List;
import com.fasterxml.jackson.annotation.JsonBackReference;

@Entity
@Table(name = "players")
public class Player {
    @Id
    private String id;

    private String username;

    @ElementCollection(fetch = FetchType.EAGER)
    @CollectionTable(name = "player_hand", joinColumns = @JoinColumn(name = "player_id"
            , foreignKey = @ForeignKey(name = "fk_player_hand",
            foreignKeyDefinition = "FOREIGN KEY (player_id) REFERENCES players(id) ON DELETE CASCADE")
    ))
    private List<Card> hand;

    @ElementCollection(fetch = FetchType.EAGER)
    @CollectionTable(name = "player_won_cards", joinColumns = @JoinColumn(name = "player_id"
            , foreignKey = @ForeignKey(name = "fk_player_won_cards",
            foreignKeyDefinition = "FOREIGN KEY (player_id) REFERENCES players(id) ON DELETE CASCADE")
    ))
    private List<Card> wonCards;

    private boolean active;

    private int score;

    @ManyToOne
    @JoinColumn(name = "game_id"
            , foreignKey = @ForeignKey(name = "fk_player_game",
            foreignKeyDefinition = "FOREIGN KEY (game_id) REFERENCES card_games(id) ON DELETE CASCADE"))
    @JsonBackReference
    private CardGame game;

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

    public void addCardToHand(Card card) {
        if (hand == null) {
            hand = new ArrayList<>();
        }
        hand.add(card);
    }

    public void removeCardFromHand(Card card) {
        if (hand != null) {
            hand.removeIf(c -> c.getSuit() == card.getSuit() && c.getRank() == card.getRank());
        }
    }
}

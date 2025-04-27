package hu.benkototh.cardgame.backend.game.model;

import java.util.List;

public class ZsirozasGame {
    private String id;
    private GameStatus status;
    private List<Player> players;
    private List<Card> deck;
    private List<Card> currentTrick;
    private Card challengeCard;
    private int currentPlayerIndex;
    private int startingPlayerIndex;
    private int lastTrickWinnerIndex;
    private boolean isPartnerGame;

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public GameStatus getStatus() {
        return status;
    }

    public void setStatus(GameStatus status) {
        this.status = status;
    }

    public List<Player> getPlayers() {
        return players;
    }

    public void setPlayers(List<Player> players) {
        this.players = players;
    }

    public List<Card> getDeck() {
        return deck;
    }

    public void setDeck(List<Card> deck) {
        this.deck = deck;
    }

    public List<Card> getCurrentTrick() {
        return currentTrick;
    }

    public void setCurrentTrick(List<Card> currentTrick) {
        this.currentTrick = currentTrick;
    }

    public Card getChallengeCard() {
        return challengeCard;
    }

    public void setChallengeCard(Card challengeCard) {
        this.challengeCard = challengeCard;
    }

    public int getCurrentPlayerIndex() {
        return currentPlayerIndex;
    }

    public void setCurrentPlayerIndex(int currentPlayerIndex) {
        this.currentPlayerIndex = currentPlayerIndex;
    }

    public int getStartingPlayerIndex() {
        return startingPlayerIndex;
    }

    public void setStartingPlayerIndex(int startingPlayerIndex) {
        this.startingPlayerIndex = startingPlayerIndex;
    }

    public int getLastTrickWinnerIndex() {
        return lastTrickWinnerIndex;
    }

    public void setLastTrickWinnerIndex(int lastTrickWinnerIndex) {
        this.lastTrickWinnerIndex = lastTrickWinnerIndex;
    }

    public boolean isPartnerGame() {
        return isPartnerGame;
    }

    public void setPartnerGame(boolean partnerGame) {
        isPartnerGame = partnerGame;
    }
}

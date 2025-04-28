package hu.benkototh.cardgame.backend.game.model.game;

import hu.benkototh.cardgame.backend.game.model.*;

import java.util.*;
import java.util.stream.Collectors;

public class ZsirGame extends CardGame {

    private static final int CARDS_PER_PLAYER = 4;
    private static final String CURRENT_TRICK = "currentTrick";
    private static final String DECK = "deck";
    private static final String LAST_PLAYER_TO_TAKE = "lastPlayerToTake";
    private static final String CURRENT_LEAD_CARD = "currentLeadCard";

    public ZsirGame() {
        super();
    }

    @Override
    public void initializeGame() {
        Deck deck = new Deck();

        if (getPlayers().size() == 3) {
            deck.initializeHungarianDeckForThreePlayers();
        } else {
            deck.initializeHungarianDeck();
        }

        deck.shuffle();

        for (Player player : getPlayers()) {
            List<Card> hand = deck.drawCards(CARDS_PER_PLAYER);
            player.setHand(hand);
            player.setWonCards(new ArrayList<>());
            player.setActive(true);
            player.setScore(0);
        }

        setCurrentPlayer(getPlayers().get(0));

        setGameState(DECK, deck);

        setGameState(CURRENT_TRICK, new ArrayList<Card>());
        setGameState(CURRENT_LEAD_CARD, "none");
        setGameState(LAST_PLAYER_TO_TAKE, "none");
    }

    @Override
    public boolean isValidMove(String playerId, GameAction action) {
        if (!getCurrentPlayer().getId().equals(playerId)) {
            return false;
        }

        if (!"playCard".equals(action.getActionType())) {
            return false;
        }

        Card card = action.getCardParameter("card");
        if (card == null) {
            return false;
        }


        Player player = getPlayerById(playerId);
        if (player == null || !playerHasCard(player, card)) {
            return false;
        }

        Card leadCard = (Card) getGameState(CURRENT_LEAD_CARD);
        if (leadCard != null) {
            if (card.getRank() == Rank.SEVEN) {
                return true;
            }

            return card.getRank() == leadCard.getRank();
        }

        return true;
    }

    @Override
    public void executeMove(String playerId, GameAction action) {
        Player player = getPlayerById(playerId);
        Card card = action.getCardParameter("card");

        removeCardFromHand(player, card);

        List<Card> currentTrick = (List<Card>) getGameState(CURRENT_TRICK);
        currentTrick.add(card);
        setGameState(CURRENT_TRICK, currentTrick);

        if (getGameState(CURRENT_LEAD_CARD) == null) {
            setGameState(CURRENT_LEAD_CARD, card);
        }

        Card leadCard = (Card) getGameState(CURRENT_LEAD_CARD);
        boolean isMatchingCard = card.getRank() == leadCard.getRank() || card.getRank() == Rank.SEVEN;

        if (isMatchingCard) {
            for (Card c : currentTrick) {
                player.getWonCards().add(c);
            }

            setGameState(CURRENT_TRICK, new ArrayList<Card>());
            setGameState(CURRENT_LEAD_CARD, null);
            setGameState(LAST_PLAYER_TO_TAKE, player);

            setCurrentPlayer(player);
        } else {
            setCurrentPlayer(getNextPlayer(player));
        }

        if (player.getHand().isEmpty()) {
            Deck deck = (Deck) getGameState(DECK);
            if (!deck.isEmpty()) {
                List<Card> newCards = deck.drawCards(CARDS_PER_PLAYER);
                player.setHand(newCards);
            }
        }
    }

    @Override
    public boolean isGameOver() {
        Object deckObj = getGameState(DECK);
        if (deckObj == null) {
            return false;
        }
        Deck deck = (Deck) getGameState(DECK);

        if (!deck.isEmpty()) {
            return false;
        }

        for (Player player : getPlayers()) {
            if (!player.getHand().isEmpty()) {
                return false;
            }
        }

        return true;
    }

    @Override
    public Map<String, Integer> calculateScores() {
        Map<String, Integer> scores = new HashMap<>();

        for (Player player : getPlayers()) {
            int score = 0;
            for (Card card : player.getWonCards()) {
                if (card.getRank() == Rank.ACE || card.getRank() == Rank.TEN) {
                    score += card.getValue();
                }
            }
            player.setScore(score);
            scores.put(player.getId(), score);
        }

        return scores;
    }

    @Override
    public int getMinPlayers() {
        return 2;
    }

    @Override
    public int getMaxPlayers() {
        return 4;
    }

    private Player getPlayerById(String playerId) {
        for (Player player : getPlayers()) {
            if (player.getId().equals(playerId)) {
                return player;
            }
        }
        return null;
    }

    private boolean playerHasCard(Player player, Card card) {
        for (Card c : player.getHand()) {
            if (c.getSuit() == card.getSuit() && c.getRank() == card.getRank()) {
                return true;
            }
        }
        return false;
    }

    private void removeCardFromHand(Player player, Card card) {
        Iterator<Card> iterator = player.getHand().iterator();
        while (iterator.hasNext()) {
            Card c = iterator.next();
            if (c.getSuit() == card.getSuit() && c.getRank() == card.getRank()) {
                iterator.remove();
                break;
            }
        }
    }

    private Player getNextPlayer(Player currentPlayer) {
        List<Player> players = getPlayers();
        int currentIndex = players.indexOf(currentPlayer);
        int nextIndex = (currentIndex + 1) % players.size();
        return players.get(nextIndex);
    }
}

package hu.benkototh.cardgame.backend.game.model.game;

import hu.benkototh.cardgame.backend.game.model.*;

import jakarta.persistence.DiscriminatorValue;
import jakarta.persistence.Entity;
import java.util.*;
import java.util.stream.Collectors;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Entity
@DiscriminatorValue("ZSIR")
public class ZsirGame extends CardGame {
    private static final Logger logger = LoggerFactory.getLogger(ZsirGame.class);
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
        logger.debug("Initializing ZsirGame");
        Deck deck = new Deck();

        if (getPlayers().size() == 3) {
            deck.initializeHungarianDeckForThreePlayers();
            logger.debug("Initialized Hungarian deck for three players");
        } else {
            deck.initializeHungarianDeck();
            logger.debug("Initialized Hungarian deck");
        }

        deck.shuffle();
        logger.debug("Deck shuffled, size: {}", deck.size());

        for (Player player : getPlayers()) {
            List<Card> hand = deck.drawCards(CARDS_PER_PLAYER);
            player.setHand(hand);
            player.setWonCards(new ArrayList<>());
            player.setActive(true);
            player.setScore(0);
            logger.debug("Player {} initialized with {} cards", player.getId(), hand.size());
        }

        setCurrentPlayer(getPlayers().get(0));
        logger.debug("Current player set to: {}", getPlayers().get(0).getId());

        setGameState(DECK, deck);
        setGameState(CURRENT_TRICK, new ArrayList<Card>());

        setGameState(CURRENT_LEAD_CARD, null);
        setGameState(LAST_PLAYER_TO_TAKE, null);

        logger.debug("ZsirGame initialization complete");
    }

    @Override
    public boolean isValidMove(String playerId, GameAction action) {
        if (!getCurrentPlayer().getId().equals(playerId)) {
            logger.debug("Invalid move: Not player's turn. Expected: {}, Actual: {}",
                    getCurrentPlayer().getId(), playerId);
            return false;
        }

        if (!"playCard".equals(action.getActionType())) {
            logger.debug("Invalid move: Unsupported action type: {}", action.getActionType());
            return false;
        }

        Card card = action.getCardParameter("card");
        if (card == null) {
            logger.debug("Invalid move: No card parameter provided");
            return false;
        }

        Player player = getPlayerById(playerId);
        if (player == null || !playerHasCard(player, card)) {
            logger.debug("Invalid move: Player does not have the card");
            return false;
        }

        Card leadCard = (Card) getGameState(CURRENT_LEAD_CARD);
        if (leadCard != null) {
            if (card.getRank() == Rank.SEVEN) {
                logger.debug("Valid move: Playing a seven");
                return true;
            }

            boolean isValidRank = card.getRank() == leadCard.getRank();
            logger.debug("Checking if card rank matches lead card: {} == {}: {}",
                    card.getRank(), leadCard.getRank(), isValidRank);
            return isValidRank;
        }

        logger.debug("Valid move: First card in trick");
        return true;
    }

    @Override
    public void executeMove(String playerId, GameAction action) {
        logger.debug("Executing move for player {}: {}", playerId, action.getActionType());
        Player player = getPlayerById(playerId);
        Card card = action.getCardParameter("card");

        removeCardFromHand(player, card);
        logger.debug("Removed card {} from player {}'s hand", card.getRank(), player.getId());

        @SuppressWarnings("unchecked")
        List<Card> currentTrick = (List<Card>) getGameState(CURRENT_TRICK);
        if (currentTrick == null) {
            currentTrick = new ArrayList<>();
        }
        currentTrick.add(card);
        setGameState(CURRENT_TRICK, currentTrick);
        logger.debug("Added card to current trick, size now: {}", currentTrick.size());

        Card leadCard = (Card) getGameState(CURRENT_LEAD_CARD);
        if (leadCard == null) {
            setGameState(CURRENT_LEAD_CARD, card);
            leadCard = card;
            logger.debug("Set lead card to: {}", card.getRank());
        }

        boolean isMatchingCard = card.getRank() == leadCard.getRank() || card.getRank() == Rank.SEVEN;
        logger.debug("Card matches lead card or is seven: {}", isMatchingCard);

        if (isMatchingCard) {
            for (Card c : currentTrick) {
                player.getWonCards().add(c);
            }
            logger.debug("Player {} won the trick, added {} cards to won pile",
                    player.getId(), currentTrick.size());

            setGameState(CURRENT_TRICK, new ArrayList<Card>());
            setGameState(CURRENT_LEAD_CARD, null);
            setGameState(LAST_PLAYER_TO_TAKE, player);

            setCurrentPlayer(player);
            logger.debug("Current player set to: {}", player.getId());
        } else {
            Player nextPlayer = getNextPlayer(player);
            setCurrentPlayer(nextPlayer);
            logger.debug("Current player set to next player: {}", nextPlayer.getId());
        }

        if (player.getHand().isEmpty()) {
            Deck deck = (Deck) getGameState(DECK);
            if (deck != null && !deck.isEmpty()) {
                List<Card> newCards = deck.drawCards(CARDS_PER_PLAYER);
                player.setHand(newCards);
                logger.debug("Player {} drew {} new cards", player.getId(), newCards.size());
            } else {
                logger.debug("Player {} has no cards left and deck is empty", player.getId());
            }
        }
    }

    @Override
    public boolean isGameOver() {
        Deck deck = (Deck) getGameState(DECK);
        if (deck == null) {
            logger.debug("Game can't be over if deck is null");
            return false;
        }

        if (!deck.isEmpty()) {
            logger.debug("Game not over: Deck still has cards");
            return false;
        }

        for (Player player : getPlayers()) {
            if (!player.getHand().isEmpty()) {
                logger.debug("Game not over: Player {} still has cards", player.getId());
                return false;
            }
        }

        logger.debug("Game is over: Deck empty and all players have no cards");
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
            logger.debug("Player {} final score: {}", player.getId(), score);
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
        logger.warn("Player not found with ID: {}", playerId);
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

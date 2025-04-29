package hu.benkototh.cardgame.backend.game.model.game;

import hu.benkototh.cardgame.backend.game.model.*;

import jakarta.persistence.DiscriminatorValue;
import jakarta.persistence.Entity;
import java.util.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Entity
@DiscriminatorValue("ZSIR")
public class ZsirGame extends CardGame {
    private static final Logger logger = LoggerFactory.getLogger(ZsirGame.class);
    private static final int CARDS_PER_PLAYER = 4;
    private static final String CURRENT_TRICK = "currentTrick";
    private static final String TRICK_CARDS = "trickCards";
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
        setGameState(CURRENT_TRICK, new ArrayList<Object>());
        setGameState(TRICK_CARDS, new HashMap<String, Object>());

        getGameState().remove(CURRENT_LEAD_CARD);
        getGameState().remove(LAST_PLAYER_TO_TAKE);

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

        if (!hasGameState(CURRENT_LEAD_CARD) || getGameState(CURRENT_LEAD_CARD) == null) {
            logger.debug("Valid move: First card in trick");
            return true;
        }

        Card leadCard = getLeadCard();
        if (leadCard == null) {
            logger.debug("Valid move: Lead card could not be determined");
            return true;
        }

        if (card.getRank() == Rank.SEVEN) {
            logger.debug("Valid move: Playing a seven (can match any card)");
            return true;
        }

        logger.debug("Valid move: Players can play any card in Zsir");
        return true;
    }

    private Card getLeadCard() {
        Object leadCardObj = getGameState(CURRENT_LEAD_CARD);
        if (leadCardObj instanceof Card) {
            return (Card) leadCardObj;
        } else if (leadCardObj instanceof Map) {
            try {
                @SuppressWarnings("unchecked")
                Map<String, Object> cardMap = (Map<String, Object>) leadCardObj;
                return Card.fromMap(cardMap);
            } catch (Exception e) {
                logger.error("Error converting lead card map to Card: {}", e.getMessage());
                return null;
            }
        }
        return null;
    }

    @Override
    public void executeMove(String playerId, GameAction action) {
        logger.debug("Executing move for player {}: {}", playerId, action.getActionType());
        Player player = getPlayerById(playerId);
        Card card = action.getCardParameter("card");

        removeCardFromHand(player, card);
        logger.debug("Removed card {} from player {}'s hand", card.getRank(), player.getId());

        @SuppressWarnings("unchecked")
        List<Object> currentTrick = (List<Object>) getGameState(CURRENT_TRICK);
        if (currentTrick == null) {
            currentTrick = new ArrayList<>();
        }
        currentTrick.add(card);
        setGameState(CURRENT_TRICK, currentTrick);

        @SuppressWarnings("unchecked")
        Map<String, Object> trickCards = (Map<String, Object>) getGameState(TRICK_CARDS);
        if (trickCards == null) {
            trickCards = new HashMap<>();
        }
        trickCards.put(playerId, card);
        setGameState(TRICK_CARDS, trickCards);

        logger.debug("Added card to current trick, size now: {}", currentTrick.size());

        if (!getGameState().containsKey(CURRENT_LEAD_CARD) || getGameState(CURRENT_LEAD_CARD) == null) {
            setGameState(CURRENT_LEAD_CARD, card);
            logger.debug("Set lead card to: {}", card.getRank());
        }

        boolean trickComplete = trickCards.size() == getPlayers().size();
        logger.debug("Trick complete? {}", trickComplete);

        if (trickComplete) {
            Player winner = determineTrickWinner(trickCards);
            logger.debug("Trick winner: {}", winner.getId());

            for (Object cardObj : currentTrick) {
                Card c = convertToCard(cardObj);
                if (c != null) {
                    winner.getWonCards().add(c);
                }
            }
            logger.debug("Player {} won the trick, added {} cards to won pile",
                    winner.getId(), currentTrick.size());

            getGameState().remove(CURRENT_TRICK);
            getGameState().remove(TRICK_CARDS);
            getGameState().remove(CURRENT_LEAD_CARD);

            setGameState(CURRENT_TRICK, new ArrayList<Object>());
            setGameState(TRICK_CARDS, new HashMap<String, Object>());
            setGameState(LAST_PLAYER_TO_TAKE, winner);

            logger.debug("Trick cleared. Current trick size: {}, Trick cards size: {}",
                    ((List<?>)getGameState(CURRENT_TRICK)).size(),
                    ((Map<?,?>)getGameState(TRICK_CARDS)).size());

            checkAndDrawCards();

            setCurrentPlayer(winner);
            logger.debug("Current player set to trick winner: {}", winner.getId());
        } else {
            Player nextPlayer = getNextPlayer(player);
            setCurrentPlayer(nextPlayer);
            logger.debug("Current player set to next player: {}", nextPlayer.getId());
        }
    }

    private Card convertToCard(Object cardObj) {
        if (cardObj instanceof Card) {
            return (Card) cardObj;
        } else if (cardObj instanceof Map) {
            try {
                @SuppressWarnings("unchecked")
                Map<String, Object> cardMap = (Map<String, Object>) cardObj;
                return Card.fromMap(cardMap);
            } catch (Exception e) {
                logger.error("Error converting card map to Card: {}", e.getMessage());
                return null;
            }
        } else {
            logger.error("Cannot convert to Card: {}",
                    cardObj != null ? cardObj.getClass().getName() : "null");
            return null;
        }
    }

    private Player determineTrickWinner(Map<String, Object> trickCards) {
        Object leadCardObj = getGameState(CURRENT_LEAD_CARD);
        Card leadCard = convertToCard(leadCardObj);

        if (leadCard == null) {
            logger.error("Lead card is null when determining trick winner!");
            @SuppressWarnings("unchecked")
            List<Object> currentTrick = (List<Object>) getGameState(CURRENT_TRICK);
            if (currentTrick != null && !currentTrick.isEmpty()) {
                leadCard = convertToCard(currentTrick.get(0));
            }

            if (leadCard == null) {
                logger.error("Could not determine lead card, using default");
                leadCard = new Card(Suit.HEARTS, Rank.SEVEN);
            }
        }

        String winnerId = null;
        Card winningCard = null;

        for (Map.Entry<String, Object> entry : trickCards.entrySet()) {
            String playerId = entry.getKey();
            Card card = convertToCard(entry.getValue());

            if (card == null) {
                logger.error("Failed to get card for player {}", playerId);
                continue;
            }

            if (card.getRank() == Rank.SEVEN) {
                winnerId = playerId;
                winningCard = card;
                break;
            }

            if (winningCard == null ||
                    (card.getRank() == leadCard.getRank() &&
                            card.getRank().ordinal() > winningCard.getRank().ordinal())) {
                winnerId = playerId;
                winningCard = card;
            }
        }

        if (winnerId == null) {
            logger.error("Could not determine trick winner!");
            return getPlayers().get(0);
        }

        return getPlayerById(winnerId);
    }

    private void checkAndDrawCards() {
        logger.debug("Checking if players need to draw cards");
        Deck deck = (Deck) getGameState(DECK);

        if (deck == null || deck.isEmpty()) {
            logger.debug("Deck is empty or null, no cards to draw");
            return;
        }

        for (Player player : getPlayers()) {
            int cardsNeeded = CARDS_PER_PLAYER - player.getHand().size();

            if (cardsNeeded > 0) {
                List<Card> newCards = deck.drawCards(cardsNeeded);
                if (!newCards.isEmpty()) {
                    player.getHand().addAll(newCards);
                    logger.debug("Player {} drew {} new cards, now has {} cards",
                            player.getId(), newCards.size(), player.getHand().size());

                    setGameState(DECK, deck);
                }
            } else {
                logger.debug("Player {} already has {} cards, no need to draw",
                        player.getId(), player.getHand().size());
            }

            if (deck.isEmpty()) {
                logger.debug("Deck is now empty, stopping card draw");
                break;
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

        @SuppressWarnings("unchecked")
        Map<String, Object> trickCards = (Map<String, Object>) getGameState(TRICK_CARDS);
        if (trickCards != null && !trickCards.isEmpty()) {
            logger.debug("Game not over: There's an ongoing trick");
            return false;
        }

        logger.debug("Game is over: Deck empty, all players have no cards, and no ongoing trick");
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

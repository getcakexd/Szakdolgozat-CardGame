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
    private static final String TRICK_CARDS = "trickCards";
    private static final String DECK = "deck";
    private static final String LAST_PLAYER_TO_TAKE = "lastPlayerToTake";
    private static final String CURRENT_LEAD_CARD = "currentLeadCard";
    private static final String CURRENT_LEAD_PLAYER = "currentLeadPlayer";
    private static final String TRICK_WINNER = "trickWinner";
    private static final String TRICK_SEQUENCE = "trickSequence";

    public ZsirGame() {
        super();
    }

    @Override
    public void initializeGame() {
        logger.debug("Initializing ZsirGame");
        Deck deck = new Deck();

        if (getPlayers().size() == 3) {
            deck.initializeHungarianDeck();
            List<Card> cardsToRemove = new ArrayList<>();
            int viiiCount = 0;

            for (Card card : deck.getCards()) {
                if (card.getRank() == Rank.EIGHT && viiiCount < 2) {
                    cardsToRemove.add(card);
                    viiiCount++;
                }
            }

            for (Card card : cardsToRemove) {
                deck.getCards().remove(card);
            }

            logger.debug("Initialized Hungarian deck for three players (removed 2 VIIIs)");
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
        setGameState(TRICK_SEQUENCE, new ArrayList<String>());

        getGameState().remove(CURRENT_LEAD_CARD);
        getGameState().remove(CURRENT_LEAD_PLAYER);
        getGameState().remove(TRICK_WINNER);
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

        boolean hasMatchingRank = false;
        for (Card c : player.getHand()) {
            if (c.getRank() == leadCard.getRank() || c.getRank() == Rank.SEVEN) {
                hasMatchingRank = true;
                break;
            }
        }

        if (hasMatchingRank) {
            boolean isValidRank = card.getRank() == leadCard.getRank();
            logger.debug("Player has matching rank card. Checking if played card matches lead card rank: {} == {}: {}",
                    card.getRank(), leadCard.getRank(), isValidRank);
            return isValidRank;
        } else {

            logger.debug("Player doesn't have matching rank card. Any card is valid.");
            return true;
        }
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

        @SuppressWarnings("unchecked")
        List<String> trickSequence = (List<String>) getGameState(TRICK_SEQUENCE);
        if (trickSequence == null) {
            trickSequence = new ArrayList<>();
        }
        trickSequence.add(playerId);
        setGameState(TRICK_SEQUENCE, trickSequence);

        logger.debug("Added card to current trick, size now: {}", currentTrick.size());

        if (!hasGameState(CURRENT_LEAD_CARD) || getGameState(CURRENT_LEAD_CARD) == null) {
            setGameState(CURRENT_LEAD_CARD, card);
            setGameState(CURRENT_LEAD_PLAYER, playerId);
            logger.debug("Set lead card to: {} by player {}", card.getRank(), playerId);
        }

        Card leadCard = getLeadCard();
        boolean isMatchingCard = (leadCard != null && card.getRank() == leadCard.getRank()) ||
                card.getRank() == Rank.SEVEN ||
                (leadCard != null && leadCard.getRank() == Rank.EIGHT &&
                        card.getRank() == Rank.KING &&
                        card.getSuit() == leadCard.getSuit());

        if (isMatchingCard) {
            setGameState(TRICK_WINNER, playerId);
            logger.debug("Player {} is now the potential trick winner with card {}", playerId, card.getRank());
        }

        boolean trickComplete = trickCards.size() == getPlayers().size();
        logger.debug("Trick complete? {}", trickComplete);

        if (trickComplete) {
            String winnerId = (String) getGameState(TRICK_WINNER);
            Player winner = winnerId != null ? getPlayerById(winnerId) : getPlayers().get(0);

            if (winnerId == null) {
                String leadPlayerId = (String) getGameState(CURRENT_LEAD_PLAYER);
                winner = leadPlayerId != null ? getPlayerById(leadPlayerId) : getPlayers().get(0);
                logger.warn("No trick winner determined, defaulting to lead player: {}", winner.getId());
            }

            logger.debug("Trick winner: {}", winner.getId());

            for (Object cardObj : currentTrick) {
                Card c = convertToCard(cardObj);
                if (c != null) {
                    winner.getWonCards().add(c);
                }
            }
            logger.debug("Player {} won the trick, added {} cards to won pile",
                    winner.getId(), currentTrick.size());

            setGameState(CURRENT_TRICK, new ArrayList<Object>());
            setGameState(TRICK_CARDS, new HashMap<String, Object>());
            setGameState(TRICK_SEQUENCE, new ArrayList<String>());
            getGameState().remove(CURRENT_LEAD_CARD);
            getGameState().remove(CURRENT_LEAD_PLAYER);
            getGameState().remove(TRICK_WINNER);
            setGameState(LAST_PLAYER_TO_TAKE, winner);

            checkAndDrawCards();

            setCurrentPlayer(winner);
            logger.debug("Current player set to trick winner: {}", winner.getId());
        } else {
            Player nextPlayer = getNextPlayer(player);
            setCurrentPlayer(nextPlayer);
            logger.debug("Current player set to next player: {}", nextPlayer.getId());
        }
    }

    private Card getLeadCard() {
        Object leadCardObj = getGameState(CURRENT_LEAD_CARD);
        return convertToCard(leadCardObj);
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
                List<Card> newCards = deck.drawCards(Math.min(cardsNeeded, deck.size()));
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
                score += card.getValue();
            }
            player.setScore(score);
            scores.put(player.getId(), score);
            logger.debug("Player {} final score: {}", player.getId(), score);
        }

        if (hasGameState(LAST_PLAYER_TO_TAKE)) {
            Player lastPlayerToTake = (Player) getGameState(LAST_PLAYER_TO_TAKE);
            if (lastPlayerToTake != null) {
                logger.debug("Last player to take a trick: {}", lastPlayerToTake.getId());
            }
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

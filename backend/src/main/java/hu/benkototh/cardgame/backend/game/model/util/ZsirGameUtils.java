package hu.benkototh.cardgame.backend.game.model.util;

import hu.benkototh.cardgame.backend.game.model.Card;
import hu.benkototh.cardgame.backend.game.model.Player;
import hu.benkototh.cardgame.backend.game.model.Rank;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ZsirGameUtils {
    private static final Logger logger = LoggerFactory.getLogger(ZsirGameUtils.class);

    public static Card convertToCard(Object cardObj) {
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
            logger.debug("Cannot convert to Card: {}",
                    cardObj != null ? cardObj.getClass().getName() : "null");
            return null;
        }
    }

    public static List<Card> convertToCardList(Object currentTrickObj) {
        List<Card> currentTrick = new ArrayList<>();

        if (currentTrickObj instanceof List) {
            @SuppressWarnings("unchecked")
            List<?> tempList = (List<?>) currentTrickObj;

            for (Object obj : tempList) {
                Card convertedCard = convertToCard(obj);
                if (convertedCard != null) {
                    currentTrick.add(convertedCard);
                }
            }
        }

        return currentTrick;
    }

    public static boolean playerHasCard(Player player, Card card) {
        for (Card c : player.getHand()) {
            if (c.getSuit() == card.getSuit() && c.getRank() == card.getRank()) {
                return true;
            }
        }
        return false;
    }

    public static void removeCardFromHand(Player player, Card card) {
        Iterator<Card> iterator = player.getHand().iterator();
        while (iterator.hasNext()) {
            Card c = iterator.next();
            if (c.getSuit() == card.getSuit() && c.getRank() == card.getRank()) {
                iterator.remove();
                break;
            }
        }
    }

    public static boolean checkIfPlayerCanHit(Player player, Card cardToMatch) {
        for (Card card : player.getHand()) {
            if (card.getRank() == Rank.SEVEN || card.getRank() == cardToMatch.getRank()) {
                return true;
            }
        }
        return false;
    }

    public static Player getOtherPlayer(List<Player> players, Player currentPlayer) {
        if (players.size() != 2) {
            return currentPlayer;
        }

        return players.get(0).equals(currentPlayer) ? players.get(1) : players.get(0);
    }
}

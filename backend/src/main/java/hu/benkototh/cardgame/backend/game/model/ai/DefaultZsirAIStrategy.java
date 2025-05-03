package hu.benkototh.cardgame.backend.game.model.ai;

import hu.benkototh.cardgame.backend.game.model.Card;
import hu.benkototh.cardgame.backend.game.model.Player;
import hu.benkototh.cardgame.backend.game.model.Rank;

import java.util.*;

public class DefaultZsirAIStrategy implements ZsirAIStrategy {

    @Override
    public Card selectBestCard(Player aiPlayer, List<Card> currentTrick) {
        List<Card> hand = aiPlayer.getHand();

        if (currentTrick.isEmpty()) {
            Map<Rank, Integer> rankCounts = new HashMap<>();
            for (Card card : hand) {
                rankCounts.put(card.getRank(), rankCounts.getOrDefault(card.getRank(), 0) + 1);
            }

            List<Card> duplicateCards = new ArrayList<>();
            for (Card card : hand) {
                if (rankCounts.get(card.getRank()) > 1) {
                    duplicateCards.add(card);
                }
            }

            if (!duplicateCards.isEmpty()) {
                return duplicateCards.get(new Random().nextInt(duplicateCards.size()));
            }

            List<Card> nonFatCards = new ArrayList<>();
            for (Card card : hand) {
                if (card.getRank() != Rank.ACE && card.getRank() != Rank.TEN) {
                    nonFatCards.add(card);
                }
            }

            if (!nonFatCards.isEmpty()) {
                return nonFatCards.get(new Random().nextInt(nonFatCards.size()));
            }

            return hand.get(new Random().nextInt(hand.size()));
        }

        Card leadCard = currentTrick.get(0);

        List<Card> matchingCards = new ArrayList<>();
        for (Card card : hand) {
            if (card.getRank() == leadCard.getRank()) {
                matchingCards.add(card);
            }
        }

        List<Card> sevens = new ArrayList<>();
        for (Card card : hand) {
            if (card.getRank() == Rank.SEVEN) {
                sevens.add(card);
            }
        }

        if (!matchingCards.isEmpty()) {
            return matchingCards.get(new Random().nextInt(matchingCards.size()));
        }

        if (!sevens.isEmpty()) {
            return sevens.get(new Random().nextInt(sevens.size()));
        }

        List<Card> nonFatCards = new ArrayList<>();
        for (Card card : hand) {
            if (card.getRank() != Rank.ACE && card.getRank() != Rank.TEN) {
                nonFatCards.add(card);
            }
        }

        if (!nonFatCards.isEmpty()) {
            return nonFatCards.get(new Random().nextInt(nonFatCards.size()));
        }

        return hand.get(new Random().nextInt(hand.size()));
    }

    @Override
    public boolean shouldPass(Player aiPlayer, List<Card> currentTrick) {
        return Math.random() < 0.3;
    }
}

package hu.benkototh.cardgame.backend.game.model.ai;

import hu.benkototh.cardgame.backend.game.model.*;
import hu.benkototh.cardgame.backend.game.model.game.ZsirGame;

import java.util.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ZsirGameAI {
    private static final Logger logger = LoggerFactory.getLogger(ZsirGameAI.class);

    private final ZsirGame game;
    private final Player aiPlayer;
    private final Player humanPlayer;

    public ZsirGameAI(ZsirGame game, Player aiPlayer, Player humanPlayer) {
        this.game = game;
        this.aiPlayer = aiPlayer;
        this.humanPlayer = humanPlayer;
    }

    public GameAction decideMove() {
        List<Card> currentTrick = game.fetchCurrentTrickCards();

        Boolean canHit = (Boolean) game.getGameState("canHit");
        if (Boolean.TRUE.equals(canHit)) {
            return decideHitOrPass(currentTrick);
        }

        if (currentTrick.isEmpty() || aiPlayer.getId().equals(game.getGameState("trickStarter"))) {
            return decideLeadCard();
        } else {
            return decideResponseCard(currentTrick);
        }
    }

    private GameAction decideHitOrPass(List<Card> currentTrick) {
        if (currentTrick.isEmpty()) {
            return createPassAction();
        }

        Card lastCard = currentTrick.get(currentTrick.size() - 1);
        List<Card> matchingCards = new ArrayList<>();

        for (Card card : aiPlayer.getHand()) {
            if (card.getRank() == Rank.SEVEN || card.getRank() == lastCard.getRank()) {
                matchingCards.add(card);
            }
        }

        if (matchingCards.isEmpty()) {
            return createPassAction();
        }

        boolean shouldHit = evaluateHitBenefit(matchingCards, currentTrick);

        if (shouldHit) {
            Card bestCard = chooseBestHitCard(matchingCards);
            return createPlayCardAction(bestCard);
        } else {
            return createPassAction();
        }
    }

    private boolean evaluateHitBenefit(List<Card> matchingCards, List<Card> currentTrick) {
        int trickValue = 0;
        for (Card card : currentTrick) {
            if (card.getRank() == Rank.ACE || card.getRank() == Rank.TEN) {
                trickValue += card.getValue();
            }
        }

        if (trickValue >= 10) {
            return true;
        }

        boolean hasSeven = matchingCards.stream().anyMatch(card -> card.getRank() == Rank.SEVEN);
        if (hasSeven && trickValue >= 5) {
            return true;
        }

        if (aiPlayer.getHand().size() <= 2 && trickValue > 0) {
            return true;
        }

        return trickValue > 0 && matchingCards.stream().noneMatch(card ->
                card.getRank() == Rank.ACE || card.getRank() == Rank.TEN);
    }

    private Card chooseBestHitCard(List<Card> matchingCards) {
        List<Card> nonValuableCards = new ArrayList<>();
        for (Card card : matchingCards) {
            if (card.getRank() != Rank.ACE && card.getRank() != Rank.TEN) {
                nonValuableCards.add(card);
            }
        }

        if (!nonValuableCards.isEmpty()) {
            for (Card card : nonValuableCards) {
                if (card.getRank() == Rank.SEVEN) {
                    return card;
                }
            }
            return nonValuableCards.get(0);
        }

        return matchingCards.stream()
                .min(Comparator.comparingInt(Card::getValue))
                .orElse(matchingCards.get(0));
    }

    private GameAction decideLeadCard() {
        List<Card> hand = aiPlayer.getHand();
        if (hand.isEmpty()) {
            logger.error("AI hand is empty when trying to lead a card");
            return createPassAction();
        }

        List<Card> nonValuableCards = new ArrayList<>();
        List<Card> valuableCards = new ArrayList<>();

        for (Card card : hand) {
            if (card.getRank() == Rank.ACE || card.getRank() == Rank.TEN) {
                valuableCards.add(card);
            } else {
                nonValuableCards.add(card);
            }
        }

        if (!nonValuableCards.isEmpty()) {
            List<Card> nonSevenCards = nonValuableCards.stream()
                    .filter(card -> card.getRank() != Rank.SEVEN)
                    .toList();

            if (!nonSevenCards.isEmpty()) {
                Card bestLead = findBestLeadCard(nonSevenCards);
                return createPlayCardAction(bestLead);
            }

            return createPlayCardAction(nonValuableCards.get(0));
        }

        Card lowestValueCard = valuableCards.stream()
                .min(Comparator.comparingInt(Card::getValue))
                .orElse(valuableCards.get(0));

        return createPlayCardAction(lowestValueCard);
    }

    private Card findBestLeadCard(List<Card> candidates) {
        Random random = new Random();
        return candidates.get(random.nextInt(candidates.size()));
    }

    private GameAction decideResponseCard(List<Card> currentTrick) {
        if (currentTrick.isEmpty()) {
            return decideLeadCard();
        }

        Card leadCard = currentTrick.get(0);
        List<Card> hand = aiPlayer.getHand();

        List<Card> matchingCards = new ArrayList<>();
        for (Card card : hand) {
            if (card.getRank() == leadCard.getRank() || card.getRank() == Rank.SEVEN) {
                matchingCards.add(card);
            }
        }

        if (matchingCards.isEmpty()) {
            return createPlayCardAction(findLeastValuableCard(hand));
        }

        boolean shouldMatch = evaluateMatchBenefit(matchingCards, currentTrick);

        if (shouldMatch) {
            Card bestCard = chooseBestMatchCard(matchingCards);
            return createPlayCardAction(bestCard);
        } else {
            List<Card> nonMatchingCards = new ArrayList<>(hand);
            nonMatchingCards.removeAll(matchingCards);

            if (nonMatchingCards.isEmpty()) {
                return createPlayCardAction(findLeastValuableCard(matchingCards));
            }

            return createPlayCardAction(findLeastValuableCard(nonMatchingCards));
        }
    }

    private boolean evaluateMatchBenefit(List<Card> matchingCards, List<Card> currentTrick) {
        boolean hasFatCards = currentTrick.stream().anyMatch(card ->
                card.getRank() == Rank.ACE || card.getRank() == Rank.TEN);

        if (hasFatCards) {
            return true;
        }

        Deck deck = (Deck) game.getGameState("deck");
        boolean endgame = deck == null || deck.isEmpty() || deck.getCards().size() < 4;

        if (endgame) {
            return true;
        }

        boolean hasSeven = matchingCards.stream().anyMatch(card -> card.getRank() == Rank.SEVEN);
        if (hasSeven && aiPlayer.getHand().size() <= 3) {
            return true;
        }

        return matchingCards.stream().anyMatch(card ->
                card.getRank() != Rank.ACE && card.getRank() != Rank.TEN);
    }

    private Card chooseBestMatchCard(List<Card> matchingCards) {
        List<Card> nonValuableCards = matchingCards.stream()
                .filter(card -> card.getRank() != Rank.ACE && card.getRank() != Rank.TEN)
                .toList();

        if (!nonValuableCards.isEmpty()) {
            for (Card card : nonValuableCards) {
                if (card.getRank() == Rank.SEVEN) {
                    return card;
                }
            }
            return nonValuableCards.get(0);
        }

        return matchingCards.stream()
                .min(Comparator.comparingInt(Card::getValue))
                .orElse(matchingCards.get(0));
    }

    private Card findLeastValuableCard(List<Card> cards) {
        if (cards.isEmpty()) {
            throw new IllegalArgumentException("Cannot find least valuable card in empty list");
        }

        List<Card> nonValuableCards = cards.stream()
                .filter(card -> card.getRank() != Rank.ACE && card.getRank() != Rank.TEN)
                .toList();

        if (!nonValuableCards.isEmpty()) {
            List<Card> nonSevenCards = nonValuableCards.stream()
                    .filter(card -> card.getRank() != Rank.SEVEN)
                    .toList();

            if (!nonSevenCards.isEmpty()) {
                return nonSevenCards.get(0);
            }

            return nonValuableCards.get(0);
        }

        return cards.stream()
                .min(Comparator.comparingInt(Card::getValue))
                .orElse(cards.get(0));
    }

    private GameAction createPlayCardAction(Card card) {
        GameAction action = new GameAction();
        action.setActionType("playCard");
        action.addParameter("card", card);
        return action;
    }

    private GameAction createPassAction() {
        GameAction action = new GameAction();
        action.setActionType("pass");
        return action;
    }
}

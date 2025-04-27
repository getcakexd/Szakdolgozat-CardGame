package hu.benkototh.cardgame.backend.game.service;

import hu.benkototh.cardgame.backend.game.model.*;
import hu.benkototh.cardgame.backend.game.exception.GameException;
import org.springframework.stereotype.Service;

import java.util.*;

@Service
public class ZsirozasGameService {


    public ZsirozasGame initializeGame(List<String> playerIds, List<String> usernames, boolean isPartnerGame) {
        if (playerIds.size() < 2 || playerIds.size() > 4) {
            throw new GameException("Zsírozás requires 2-4 players");
        }

        if (playerIds.size() != usernames.size()) {
            throw new GameException("Player IDs and usernames must match");
        }

        ZsirozasGame game = new ZsirozasGame();
        game.setId(UUID.randomUUID().toString());
        game.setStatus(GameStatus.WAITING);
        game.setPartnerGame(isPartnerGame && playerIds.size() == 4);

        List<Player> players = new ArrayList<>();
        for (int i = 0; i < playerIds.size(); i++) {
            Player player = new Player();
            player.setId(playerIds.get(i));
            player.setUsername(usernames.get(i));
            player.setHand(new ArrayList<>());
            player.setWonCards(new ArrayList<>());
            player.setActive(true);
            player.setScore(0);
            players.add(player);
        }
        game.setPlayers(players);

        List<Card> deck = createDeck(playerIds.size() == 3);
        Collections.shuffle(deck);
        game.setDeck(deck);

        game.setCurrentTrick(new ArrayList<>());
        game.setChallengeCard(null);
        game.setCurrentPlayerIndex(0);
        game.setStartingPlayerIndex(0);
        game.setLastTrickWinnerIndex(-1);

        dealCards(game);

        game.setStatus(GameStatus.ACTIVE);
        return game;
    }

    private List<Card> createDeck(boolean isThreePlayerGame) {
        List<Card> deck = new ArrayList<>();

        for (Suit suit : Suit.values()) {
            for (Rank rank : Rank.values()) {
                if (isThreePlayerGame && rank == Rank.EIGHT &&
                        (suit == Suit.HEARTS || suit == Suit.BELLS)) {
                    continue;
                }

                Card card = new Card(suit, rank);

                if (isThreePlayerGame && rank == Rank.EIGHT) {
                    card.setSeven(true);
                }

                deck.add(card);
            }
        }

        return deck;
    }

    private void dealCards(ZsirozasGame game) {
        for (int i = 0; i < 4; i++) {
            for (Player player : game.getPlayers()) {
                if (!game.getDeck().isEmpty()) {
                    Card card = game.getDeck().remove(0);
                    player.getHand().add(card);
                }
            }
        }
    }

    public ZsirozasGame playCard(String gameId, String playerId, Card playedCard) {
        ZsirozasGame game = getGameById(gameId);

        if (game.getStatus() != GameStatus.ACTIVE) {
            throw new GameException("Game is not active");
        }

        Player currentPlayer = game.getPlayers().get(game.getCurrentPlayerIndex());
        if (!currentPlayer.getId().equals(playerId)) {
            throw new GameException("Not your turn");
        }

        Card cardToPlay = findCardInPlayerHand(currentPlayer, playedCard);
        if (cardToPlay == null) {
            throw new GameException("Card not in player's hand");
        }

        currentPlayer.getHand().remove(cardToPlay);

        processPlayedCard(game, cardToPlay);

        if (isGameOver(game)) {
            endGame(game);
        }

        return game;
    }

    private Card findCardInPlayerHand(Player player, Card cardToFind) {
        return player.getHand().stream()
                .filter(card -> card.getSuit() == cardToFind.getSuit() && card.getRank() == cardToFind.getRank())
                .findFirst()
                .orElse(null);
    }

    private void processPlayedCard(ZsirozasGame game, Card playedCard) {
        game.getCurrentTrick().add(playedCard);

        if (game.getChallengeCard() == null) {
            game.setChallengeCard(playedCard);
        }

        moveToNextPlayer(game);

        if (isTrickComplete(game)) {
            resolveTrick(game);

            if (!game.getDeck().isEmpty() && game.getPlayers().stream().anyMatch(p -> p.getHand().isEmpty() && p.isActive())) {
                dealCardsAfterTrick(game);
            }
        }
    }

    private void moveToNextPlayer(ZsirozasGame game) {
        int nextPlayerIndex = (game.getCurrentPlayerIndex() + 1) % game.getPlayers().size();

        while (!game.getPlayers().get(nextPlayerIndex).isActive() && nextPlayerIndex != game.getCurrentPlayerIndex()) {
            nextPlayerIndex = (nextPlayerIndex + 1) % game.getPlayers().size();
        }

        game.setCurrentPlayerIndex(nextPlayerIndex);
    }

    private boolean isTrickComplete(ZsirozasGame game) {
        long activePlayerCount = game.getPlayers().stream()
                .filter(Player::isActive)
                .count();

        return game.getCurrentTrick().size() >= activePlayerCount;
    }

    private void resolveTrick(ZsirozasGame game) {
        Card challengeCard = game.getChallengeCard();
        List<Card> matchingCards = new ArrayList<>();

        for (Card card : game.getCurrentTrick()) {
            if (card.getRank() == challengeCard.getRank() || card.isSeven()) {
                matchingCards.add(card);
            }
        }

        if (matchingCards.isEmpty()) {
            matchingCards.add(challengeCard);
        }

        Card winningCard = matchingCards.get(matchingCards.size() - 1);

        int winnerIndex = -1;
        for (int i = 0; i < game.getPlayers().size(); i++) {
            Player player = game.getPlayers().get(i);
            if (player.isActive() && game.getCurrentTrick().contains(winningCard)) {
                winnerIndex = i;
                break;
            }
        }

        if (winnerIndex == -1) {
            throw new GameException("Could not determine trick winner");
        }

        Player winner = game.getPlayers().get(winnerIndex);
        winner.getWonCards().addAll(game.getCurrentTrick());

        game.setLastTrickWinnerIndex(winnerIndex);
        game.setCurrentPlayerIndex(winnerIndex);
        game.setCurrentTrick(new ArrayList<>());
        game.setChallengeCard(null);

        if (game.getPlayers().size() == 3) {
            updatePlayerActivity(game);
        }
    }

    private void updatePlayerActivity(ZsirozasGame game) {
        for (Player player : game.getPlayers()) {
            player.setActive(!player.getHand().isEmpty());
        }
    }

    private void dealCardsAfterTrick(ZsirozasGame game) {
        int playerIndex = game.getLastTrickWinnerIndex();

        for (int i = 0; i < game.getPlayers().size(); i++) {
            Player player = game.getPlayers().get((playerIndex + i) % game.getPlayers().size());

            if (player.getHand().isEmpty() && !game.getDeck().isEmpty()) {
                Card card = game.getDeck().remove(0);
                player.getHand().add(card);
                player.setActive(true);
            }
        }
    }

    private boolean isGameOver(ZsirozasGame game) {
        return game.getDeck().isEmpty() &&
                game.getPlayers().stream().allMatch(p -> p.getHand().isEmpty());
    }

    private void endGame(ZsirozasGame game) {
        calculateScores(game);

        game.setStatus(GameStatus.FINISHED);
    }

    private void calculateScores(ZsirozasGame game) {
        for (Player player : game.getPlayers()) {
            int score = 0;
            for (Card card : player.getWonCards()) {
                if (card.getRank() == Rank.ACE) {
                    score += 11;
                } else if (card.getRank() == Rank.TEN) {
                    score += 10;
                }
            }
            player.setScore(score);
        }

        if (game.isPartnerGame() && game.getPlayers().size() == 4) {
            int team1Score = game.getPlayers().get(0).getScore() + game.getPlayers().get(2).getScore();
            int team2Score = game.getPlayers().get(1).getScore() + game.getPlayers().get(3).getScore();

            game.getPlayers().get(0).setScore(team1Score);
            game.getPlayers().get(2).setScore(team1Score);
            game.getPlayers().get(1).setScore(team2Score);
            game.getPlayers().get(3).setScore(team2Score);
        }
    }


    private ZsirozasGame getGameById(String gameId) {
        throw new UnsupportedOperationException("Method not implemented");
    }

    public ZsirozasGame sendPartnerMessage(String gameId, String playerId, PartnerMessage message) {
        ZsirozasGame game = getGameById(gameId);

        if (!game.isPartnerGame()) {
            throw new GameException("Partner messages only allowed in partner games");
        }

        if (game.getStatus() != GameStatus.ACTIVE) {
            throw new GameException("Game is not active");
        }

        int playerIndex = -1;
        for (int i = 0; i < game.getPlayers().size(); i++) {
            if (game.getPlayers().get(i).getId().equals(playerId)) {
                playerIndex = i;
                break;
            }
        }

        if (playerIndex == -1) {
            throw new GameException("Player not in game");
        }

        int partnerIndex = (playerIndex + 2) % 4;
        if (game.getCurrentPlayerIndex() != playerIndex && game.getCurrentPlayerIndex() != partnerIndex) {
            throw new GameException("Can only send messages during your or your partner's turn");
        }

        return game;
    }
}
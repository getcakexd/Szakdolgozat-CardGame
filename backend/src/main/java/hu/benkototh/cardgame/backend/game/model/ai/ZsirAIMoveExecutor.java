package hu.benkototh.cardgame.backend.game.model.ai;

import hu.benkototh.cardgame.backend.game.model.*;
import hu.benkototh.cardgame.backend.game.model.util.ZsirGameUtils;

import java.util.ArrayList;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ZsirAIMoveExecutor {
    private static final Logger logger = LoggerFactory.getLogger(ZsirAIMoveExecutor.class);

    private final ZsirAIStrategy strategy;

    public ZsirAIMoveExecutor(ZsirAIStrategy strategy) {
        this.strategy = strategy;
    }

    public void executeAIMove(Player aiPlayer, List<Card> currentTrick, String aiPlayerId,
                              String trickStarter, boolean canHit,
                              GameStateCallback gameStateCallback) {
        if (aiPlayer == null || aiPlayer.getHand().isEmpty()) {
            return;
        }

        try {
            Thread.sleep(500);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }

        if (canHit && strategy.shouldPass(aiPlayer, currentTrick)) {
            GameAction passAction = new GameAction();
            passAction.setActionType("pass");
            gameStateCallback.handlePass(aiPlayerId);
            return;
        }

        Card cardToPlay = strategy.selectBestCard(aiPlayer, currentTrick);

        GameAction playCardAction = new GameAction();
        playCardAction.setActionType("playCard");
        playCardAction.addParameter("card", cardToPlay);

        ZsirGameUtils.removeCardFromHand(aiPlayer, cardToPlay);

        currentTrick.add(cardToPlay);
        gameStateCallback.setCurrentTrick(currentTrick);

        gameStateCallback.setLastPlayedCard(cardToPlay);
        gameStateCallback.setLastPlayer(aiPlayerId);

        if (currentTrick.size() == 1) {
            gameStateCallback.setTrickStarter(aiPlayerId);
        }

        boolean isStarterTurn = aiPlayerId.equals(trickStarter);

        boolean playerHit = false;
        if (currentTrick.size() > 1) {
            Card previousCard = currentTrick.get(currentTrick.size() - 2);
            playerHit = cardToPlay.getRank() == Rank.SEVEN || cardToPlay.getRank() == previousCard.getRank();
        }

        gameStateCallback.setCanHit(false);

        boolean trickComplete = false;

        if (currentTrick.size() >= 2) {
            if (!isStarterTurn && !playerHit) {
                trickComplete = true;
            } else if (!isStarterTurn && playerHit) {
                Player starterPlayer = gameStateCallback.getPlayerById(trickStarter);
                boolean firstPlayerCanHit = ZsirGameUtils.checkIfPlayerCanHit(starterPlayer, cardToPlay);
                gameStateCallback.setCanHit(firstPlayerCanHit);

                if (!firstPlayerCanHit) {
                    trickComplete = true;
                }
            }
        }

        if (currentTrick.size() >= 8) {
            trickComplete = true;
        }

        if (trickComplete) {
            Player winner = gameStateCallback.determineTrickWinner(currentTrick, trickStarter);
            winner.getWonCards().addAll(currentTrick);

            gameStateCallback.setCurrentTrick(new ArrayList<>());
            gameStateCallback.removeTrickStarter();
            gameStateCallback.setCanHit(false);

            gameStateCallback.checkAndDrawCards();
            gameStateCallback.setCurrentPlayer(winner);

            if (winner.getId().equals(aiPlayerId)) {
                executeAIMove(aiPlayer, new ArrayList<>(), aiPlayerId, null, false, gameStateCallback);
            }
        } else {
            Player nextPlayer = ZsirGameUtils.getOtherPlayer(gameStateCallback.getPlayers(), aiPlayer);
            gameStateCallback.setCurrentPlayer(nextPlayer);
        }

        logger.info("AI played card: {}", cardToPlay);
    }

    public interface GameStateCallback {
        void handlePass(String playerId);
        void setCurrentTrick(List<Card> currentTrick);
        void setLastPlayedCard(Card card);
        void setLastPlayer(String playerId);
        void setTrickStarter(String playerId);
        void setCanHit(boolean canHit);
        void removeTrickStarter();
        Player getPlayerById(String playerId);
        Player determineTrickWinner(List<Card> currentTrick, String trickStarter);
        void checkAndDrawCards();
        void setCurrentPlayer(Player player);
        List<Player> getPlayers();
    }
}

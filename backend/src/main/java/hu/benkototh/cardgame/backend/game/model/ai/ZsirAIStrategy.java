package hu.benkototh.cardgame.backend.game.model.ai;

import hu.benkototh.cardgame.backend.game.model.Card;
import hu.benkototh.cardgame.backend.game.model.Player;

import java.util.List;

public interface ZsirAIStrategy {
    Card selectBestCard(Player aiPlayer, List<Card> currentTrick);
    boolean shouldPass(Player aiPlayer, List<Card> currentTrick);
}

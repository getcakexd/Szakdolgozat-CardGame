package hu.benkototh.cardgame.backend.game.service;

import hu.benkototh.cardgame.backend.game.model.CardGame;
import hu.benkototh.cardgame.backend.game.exception.GameException;
import hu.benkototh.cardgame.backend.game.model.game.ZsirGame;

public class GameFactory {

    public static CardGame createGame(String factorySign, int factoryId) throws GameException {
        switch (factorySign) {
            case "ZSIR":
                return new ZsirGame(factoryId);
            default:
                throw new GameException("Unknown game type: " + factorySign);
        }
    }
}
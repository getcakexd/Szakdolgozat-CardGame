package hu.benkototh.cardgame.backend.game.service;

import hu.benkototh.cardgame.backend.game.model.CardGame;
import hu.benkototh.cardgame.backend.game.exception.GameException;
import hu.benkototh.cardgame.backend.game.model.game.ZsirGame;

public class GameFactory {

    public static CardGame createGame(String gameType) {
        switch (gameType.toLowerCase()) {
            case "zsir":
            case "fat":
            case "zsírozás":
            case "zsírozas":
            case "zsirozas":
            case "zsirozás":
                return new ZsirGame();
            default:
                throw new GameException("Unknown game type: " + gameType);
        }
    }
}
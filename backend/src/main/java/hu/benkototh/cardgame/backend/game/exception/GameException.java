package hu.benkototh.cardgame.backend.game.exception;

public class GameException extends RuntimeException {
    public GameException(String message) {
        super(message);
    }
}
package hu.benkototh.cardgame.backend.game.service;

import hu.benkototh.cardgame.backend.game.exception.GameException;
import hu.benkototh.cardgame.backend.game.model.*;
import hu.benkototh.cardgame.backend.game.repository.ICardGameRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Map;
import java.util.Optional;

@Service
public class CardGameService {

    @Autowired
    private ICardGameRepository cardGameRepository;

    @Transactional
    public CardGame createGame(String gameType, String name, boolean trackStatistics) {
        CardGame game = GameFactory.createGame(gameType);
        game.setName(name);
        game.setTrackStatistics(trackStatistics);
        return cardGameRepository.save(game);
    }

    @Transactional
    public CardGame addPlayer(String gameId, Player player) {
        CardGame game = findGameById(gameId);
        game.addPlayer(player);
        player.setGame(game);
        return cardGameRepository.save(game);
    }

    @Transactional
    public CardGame removePlayer(String gameId, String playerId) {
        CardGame game = findGameById(gameId);
        game.removePlayer(playerId);
        return cardGameRepository.save(game);
    }

    @Transactional
    public CardGame startGame(String gameId) {
        CardGame game = findGameById(gameId);
        game.startGame();
        return cardGameRepository.save(game);
    }

    @Transactional
    public CardGame executeMove(String gameId, String playerId, GameAction action) {
        CardGame game = findGameById(gameId);

        if (game.getStatus() != GameStatus.ACTIVE) {
            throw new GameException("Game is not active");
        }

        if (!game.getCurrentPlayer().getId().equals(playerId)) {
            throw new GameException("It's not your turn");
        }

        if (!game.isValidMove(playerId, action)) {
            throw new GameException("Invalid move");
        }

        game.executeMove(playerId, action);

        if (game.isGameOver()) {
            game.endGame();
        }

        return cardGameRepository.save(game);
    }

    @Transactional
    public CardGame abandonGame(String gameId, String playerId) {
        CardGame game = findGameById(gameId);

        if (game.getStatus() != GameStatus.FINISHED) {
            game.endGame();
        }

        return cardGameRepository.save(game);
    }

    @Transactional(readOnly = true)
    public CardGame findGameById(String gameId) {
        return cardGameRepository.findById(gameId)
                .orElseThrow(() -> new GameException("Game not found with ID: " + gameId));
    }

    @Transactional(readOnly = true)
    public List<CardGame> findGamesByStatus(GameStatus status) {
        return cardGameRepository.findByStatus(status);
    }

    @Transactional(readOnly = true)
    public List<CardGame> findGamesByPlayer(String playerId) {
        return cardGameRepository.findByPlayerId(playerId);
    }

    @Transactional
    public void deleteGame(String gameId) {
        cardGameRepository.deleteById(gameId);
    }
}

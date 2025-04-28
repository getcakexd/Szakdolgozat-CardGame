package hu.benkototh.cardgame.backend.game.repository;

import hu.benkototh.cardgame.backend.game.model.CardGame;
import hu.benkototh.cardgame.backend.game.model.GameStatus;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

@Repository
public class ICardGameRepository {

    private final Map<String, CardGame> games = new ConcurrentHashMap<>();

    public CardGame save(CardGame game) {
        games.put(game.getId(), game);
        return game;
    }

    public Optional<CardGame> findById(String id) {
        return Optional.ofNullable(games.get(id));
    }

    public List<CardGame> findAll() {
        return List.copyOf(games.values());
    }

    public void deleteById(String id) {
        games.remove(id);
    }

    public List<CardGame> findByStatus(GameStatus status) {
        return games.values().stream()
                .filter(game -> game.getStatus() == status)
                .collect(Collectors.toList());
    }

    public List<CardGame> findByGameDefinitionId(long gameDefinitionId) {
        return games.values().stream()
                .filter(game -> game.getGameDefinitionId() == gameDefinitionId)
                .collect(Collectors.toList());
    }
}
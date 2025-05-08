package hu.benkototh.cardgame.backend.game.controller;

import hu.benkototh.cardgame.backend.game.model.*;
import hu.benkototh.cardgame.backend.game.repository.IUserStatsRepository;
import hu.benkototh.cardgame.backend.game.repository.IUserGameStatsRepository;
import hu.benkototh.cardgame.backend.game.repository.IGameStatisticsRepository;
import hu.benkototh.cardgame.backend.rest.Data.User;
import hu.benkototh.cardgame.backend.rest.Data.Game;
import hu.benkototh.cardgame.backend.rest.Data.Lobby;
import hu.benkototh.cardgame.backend.rest.controller.GameController;
import hu.benkototh.cardgame.backend.rest.controller.UserController;
import hu.benkototh.cardgame.backend.rest.controller.LobbyController;
import hu.benkototh.cardgame.backend.rest.repository.IGameRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Controller;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;
import java.util.stream.Collectors;

@Controller
public class StatsController {
    private static final Logger logger = LoggerFactory.getLogger(StatsController.class);
    private static final int ABANDON_PENALTY_POINTS = 80;

    @Autowired
    private IUserStatsRepository userStatsRepository;

    @Autowired
    private IUserGameStatsRepository userGameStatsRepository;

    @Autowired
    private IGameStatisticsRepository gameStatisticsRepository;

    @Autowired
    private IGameRepository gameRepository;

    @Lazy
    @Autowired
    private UserController userController;

    @Lazy
    @Autowired
    private LobbyController lobbyController;

    @Lazy
    @Autowired
    private ClubStatsController clubStatsController;

    @Lazy
    @Autowired
    private StatsController statsController;

    @Lazy
    @Autowired
    private CardGameController cardGameController;

    @Transactional
    public void recordGameResult(CardGame cardGame, Map<String, Integer> scores, boolean abandoned, String abandonedBy) {
        recordGameResult(cardGame, scores, abandoned, abandonedBy, false, Collections.emptyList());
    }

    @Transactional
    public void recordGameResult(CardGame cardGame, Map<String, Integer> scores, boolean abandoned,
                                 String abandonedBy, boolean isDraw, List<String> drawPlayerIds) {
        logger.info("Recording game result for game {}, abandoned: {}, draw: {}",
                cardGame.getId(), abandoned, isDraw);

        Optional<Game> gameDefinitionOpt = gameRepository.findById(cardGame.getGameDefinitionId());
        if (gameDefinitionOpt.isEmpty()) {
            logger.error("Game definition not found for ID: {}", cardGame.getGameDefinitionId());
            return;
        }
        Game gameDefinition = gameDefinitionOpt.get();
        boolean isFriendly = !cardGame.isTrackStatistics();

        for (Player player : cardGame.getPlayers()) {
            String playerId = player.getId();

            if (player.isAI()) continue;
            User user = userController.getUser(Long.parseLong(playerId));

            if (user == null) {
                logger.error("User not found for ID: {}", playerId);
                continue;
            }

            boolean isWinner = false;
            boolean isDrawn = false;
            boolean isAbandoner = abandoned && playerId.equals(abandonedBy);
            int playerScore = scores.getOrDefault(playerId, 0);

            if (abandoned) {
                isWinner = !isAbandoner;
                isDrawn = false;
            } else if (isDraw) {
                isDrawn = drawPlayerIds.contains(playerId);
            } else {
                int highestScore = scores.values().stream()
                        .mapToInt(Integer::intValue)
                        .max()
                        .orElse(0);
                isWinner = playerScore == highestScore;
            }

            int fatCards = 0;
            for (Card card : player.getWonCards()) {
                if (card.getRank() == Rank.ACE || card.getRank() == Rank.TEN) {
                    fatCards++;
                }
            }

            GameStatistics gameStats = new GameStatistics();
            gameStats.setUserId(playerId);
            gameStats.setGameId(cardGame.getId());
            gameStats.setGameDefinitionId(cardGame.getGameDefinitionId());
            gameStats.setGameType(cardGame.getClass().getSimpleName());
            gameStats.setScore(playerScore);
            gameStats.setWon(isWinner);
            gameStats.setDrawn(isDrawn);
            gameStats.setFatCardsCollected(fatCards);
            gameStats.setTricksTaken(player.getWonCards().size() / 2);
            gameStats.setPlayedAt(new Date());
            gameStats.setFriendly(isFriendly);
            gameStatisticsRepository.save(gameStats);

            if(!isFriendly) {
                updateUserStats(user, isWinner, isDrawn, isAbandoner, playerScore, fatCards);

                updateUserGameStats(user, gameDefinition, isWinner, isDrawn, isAbandoner, playerScore, fatCards);
            }

        }

        Lobby lobby = findLobbyByCardGameId(cardGame.getId());
        if (lobby != null && lobby.getClub() != null) {
            clubStatsController.recordClubGameResult(cardGame, lobby, scores, abandoned, abandonedBy);
        }

        logger.info("Game result recorded successfully for game {}", cardGame.getId());
    }

    @Transactional
    public void recordAbandonedGame(CardGame game, String abandonedBy) {
        logger.info("Recording abandoned game {}", game.getId());

        Map<String, Integer> scores = game.calculateScores();

        for (Player player : game.getPlayers()) {
            if (!player.getId().equals(abandonedBy)) {
                Integer currentScore = scores.getOrDefault(player.getId(), 0);
                scores.put(player.getId(), currentScore + ABANDON_PENALTY_POINTS);
                player.setScore(currentScore + ABANDON_PENALTY_POINTS);
            }
        }

        List<String> winnerIds = game.getPlayers().stream()
                .filter(p -> !p.getId().equals(abandonedBy))
                .map(Player::getId)
                .collect(Collectors.toList());

        statsController.recordGameResult(game, scores, true, abandonedBy, false, winnerIds);
        cardGameController.saveGame(game);
        logger.info("Abandoned game {} recorded", game.getId());
    }

    private Lobby findLobbyByCardGameId(String cardGameId) {
        if (cardGameId == null) {
            return null;
        }

        return lobbyController.findLobbyByCardGameId(cardGameId);
    }

    @Transactional
    protected void updateUserStats(User user, boolean won, boolean drawn, boolean abandoned, int score, int fatCards) {
        if (user == null) {
            logger.warn("Cannot update stats for null user");
            return;
        }

        UserStats userStats = userStatsRepository.findByUser(user)
                .orElse(new UserStats());

        if (userStats.getUser() == null) {
            userStats.setUser(user);
        }

        userStats.incrementGamesPlayed();

        if (abandoned) {
            userStats.incrementGamesAbandoned();
        } else if (won) {
            userStats.incrementGamesWon();
        } else if (drawn) {
            userStats.incrementGamesDrawn();
        } else {
            userStats.incrementGamesLost();
        }

        userStats.addPoints(score);
        userStats.addFatsCollected(fatCards);

        userStatsRepository.save(userStats);
    }

    @Transactional
    protected void updateUserGameStats(User user, Game gameDefinition, boolean won, boolean drawn,
                                       boolean abandoned, int score, int fatCards) {
        if (user == null || gameDefinition == null) {
            logger.warn("Cannot update game stats for null user or game definition");
            return;
        }

        UserGameStats userGameStats = userGameStatsRepository.findByUserAndGameDefinition(user, gameDefinition)
                .orElse(new UserGameStats());

        if (userGameStats.getUser() == null) {
            userGameStats.setUser(user);
            userGameStats.setGameDefinition(gameDefinition);
        }

        userGameStats.incrementGamesPlayed();

        if (abandoned) {
            userGameStats.incrementGamesAbandoned();
        } else if (won) {
            userGameStats.incrementGamesWon();
        } else if (drawn) {
            userGameStats.incrementGamesDrawn();
        } else {
            userGameStats.incrementGamesLost();
        }

        userGameStats.addPoints(score);
        userGameStats.addFatsCollected(fatCards);

        userGameStatsRepository.save(userGameStats);
    }

    public UserStats getUserStats(Long userId) {
        User user = userController.getUser(userId);
        if (user == null) {
            return null;
        }

        return userStatsRepository.findByUser(user)
                .orElse(new UserStats());
    }

    public List<UserGameStats> getUserGameStats(Long userId) {
        User user = userController.getUser(userId);
        if (user == null) {
            return Collections.emptyList();
        }

        return userGameStatsRepository.findByUser(user);
    }

    public UserGameStats getUserGameStatsByGame(Long userId, Long gameDefinitionId) {
        User user = userController.getUser(userId);
        if (user == null) {
            return null;
        }

        Optional<Game> gameDefinitionOpt = gameRepository.findById(gameDefinitionId);
        if (gameDefinitionOpt.isEmpty()) {
            return null;
        }

        return userGameStatsRepository.findByUserAndGameDefinition(user, gameDefinitionOpt.get())
                .orElse(new UserGameStats());
    }

    public List<UserGameStats> getTopPlayersByPoints(int limit) {
        return userGameStatsRepository.findTopPlayersByPoints().stream()
                .limit(limit)
                .toList();
    }

    public List<UserGameStats> getTopPlayersByGameAndPoints(Long gameDefinitionId, int limit) {
        return userGameStatsRepository.findTopPlayersByGameAndPoints(gameDefinitionId).stream()
                .limit(limit)
                .toList();
    }

    public List<GameStatistics> getRecentGames(Long userId, int limit) {
        return gameStatisticsRepository.findAll().stream()
                .filter(gs -> gs.getUserId().equals(userId.toString()))
                .sorted(Comparator.comparing(GameStatistics::getPlayedAt).reversed())
                .limit(limit)
                .toList();
    }
}

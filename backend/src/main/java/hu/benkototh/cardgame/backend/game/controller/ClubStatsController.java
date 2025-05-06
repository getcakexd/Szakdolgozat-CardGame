package hu.benkototh.cardgame.backend.game.controller;

import hu.benkototh.cardgame.backend.game.model.*;
import hu.benkototh.cardgame.backend.game.repository.IClubStatsRepository;
import hu.benkototh.cardgame.backend.game.repository.IClubGameStatsRepository;
import hu.benkototh.cardgame.backend.game.repository.IClubMemberStatsRepository;
import hu.benkototh.cardgame.backend.rest.Data.Club;
import hu.benkototh.cardgame.backend.rest.Data.Game;
import hu.benkototh.cardgame.backend.rest.Data.User;
import hu.benkototh.cardgame.backend.rest.Data.Lobby;
import hu.benkototh.cardgame.backend.rest.controller.ClubController;
import hu.benkototh.cardgame.backend.rest.controller.UserController;
import hu.benkototh.cardgame.backend.rest.repository.IGameRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Controller;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;

@Controller
public class ClubStatsController {
    private static final Logger logger = LoggerFactory.getLogger(ClubStatsController.class);

    @Autowired
    private IClubStatsRepository clubStatsRepository;

    @Autowired
    private IClubGameStatsRepository clubGameStatsRepository;

    @Autowired
    private IClubMemberStatsRepository clubMemberStatsRepository;

    @Autowired
    private IGameRepository gameRepository;

    @Lazy
    @Autowired
    private UserController userController;

    @Lazy
    @Autowired
    private ClubController clubController;

    @Transactional
    public void recordClubGameResult(CardGame cardGame, Lobby lobby, Map<String, Integer> scores, boolean abandoned, String abandonedBy) {
        if (lobby == null || lobby.getClub() == null) {
            logger.info("Not a club game, skipping club stats recording");
            return;
        }

        logger.info("Recording club game result for game {}, club {}", cardGame.getId(), lobby.getClub().getId());

        Club club = lobby.getClub();
        Optional<Game> gameDefinitionOpt = gameRepository.findById(cardGame.getGameDefinitionId());
        if (gameDefinitionOpt.isEmpty()) {
            logger.error("Game definition not found for ID: {}", cardGame.getGameDefinitionId());
            return;
        }
        Game gameDefinition = gameDefinitionOpt.get();

        String winnerId = null;
        int highestScore = -1;

        for (Map.Entry<String, Integer> entry : scores.entrySet()) {
            if (entry.getValue() > highestScore) {
                highestScore = entry.getValue();
                winnerId = entry.getKey();
            }
        }

        ClubStats clubStats = clubStatsRepository.findByClub(club)
                .orElse(new ClubStats());

        if (clubStats.getClub() == null) {
            clubStats.setClub(club);
        }

        clubStats.incrementGamesPlayed();

        ClubGameStats clubGameStats = clubGameStatsRepository.findByClubAndGameDefinition(club, gameDefinition)
                .orElse(new ClubGameStats());

        if (clubGameStats.getClub() == null) {
            clubGameStats.setClub(club);
            clubGameStats.setGameDefinition(gameDefinition);
        }

        clubGameStats.incrementGamesPlayed();

        Set<String> countedPlayers = new HashSet<>();

        for (Player player : cardGame.getPlayers()) {
            String playerId = player.getId();
            User user = userController.getUser(Long.parseLong(playerId));

            if (user == null) {
                logger.error("User not found for ID: {}", playerId);
                continue;
            }

            boolean isWinner = playerId.equals(winnerId);
            int playerScore = scores.getOrDefault(playerId, 0);

            int fatCards = 0;
            for (Card card : player.getWonCards()) {
                if (card.getRank() == Rank.ACE || card.getRank() == Rank.TEN) {
                    fatCards++;
                }
            }

            ClubMemberStats memberStats = clubMemberStatsRepository.findByClubAndUser(club, user)
                    .orElse(new ClubMemberStats());

            if (memberStats.getClub() == null) {
                memberStats.setClub(club);
                memberStats.setUser(user);
            }

            memberStats.incrementGamesPlayed();
            if (isWinner) {
                memberStats.incrementGamesWon();
            }
            memberStats.addPoints(playerScore);
            memberStats.addFatsCollected(fatCards);
            clubMemberStatsRepository.save(memberStats);

            clubStats.addPoints(playerScore);
            clubStats.addFatsCollected(fatCards);

            clubGameStats.addPoints(playerScore);
            clubGameStats.addFatsCollected(fatCards);

            if (!countedPlayers.contains(playerId)) {
                countedPlayers.add(playerId);
            }
        }

        clubStatsRepository.save(clubStats);

        clubGameStatsRepository.save(clubGameStats);

        logger.info("Club game result recorded successfully for game {}, club {}", cardGame.getId(), club.getId());
    }

    public ClubStats getClubStats(Long clubId) {
        Club club = clubController.getClubById(clubId).orElse(null);
        if (club == null) {
            return null;
        }

        return clubStatsRepository.findByClub(club)
                .orElse(new ClubStats());
    }

    public List<ClubGameStats> getClubGameStats(Long clubId) {
        Club club = clubController.getClubById(clubId).orElse(null);
        if (club == null) {
            return Collections.emptyList();
        }

        return clubGameStatsRepository.findByClub(club);
    }

    public ClubGameStats getClubGameStatsByGame(Long clubId, Long gameDefinitionId) {
        Club club = clubController.getClubById(clubId).orElse(null);
        if (club == null) {
            return null;
        }

        Optional<Game> gameDefinitionOpt = gameRepository.findById(gameDefinitionId);
        if (gameDefinitionOpt.isEmpty()) {
            return null;
        }

        return clubGameStatsRepository.findByClubAndGameDefinition(club, gameDefinitionOpt.get())
                .orElse(new ClubGameStats());
    }

    public List<ClubMemberStats> getClubMemberStats(Long clubId) {
        Club club = clubController.getClubById(clubId).orElse(null);
        if (club == null) {
            return Collections.emptyList();
        }

        return clubMemberStatsRepository.findByClub(club);
    }

    public ClubMemberStats getClubMemberStatsByUser(Long clubId, Long userId) {
        Club club = clubController.getClubById(clubId).orElse(null);
        if (club == null) {
            return null;
        }

        User user = userController.getUser(userId);
        if (user == null) {
            return null;
        }

        return clubMemberStatsRepository.findByClubAndUser(club, user)
                .orElse(new ClubMemberStats());
    }

    public List<ClubGameStats> getTopClubsByPoints(int limit) {
        return clubGameStatsRepository.findTopClubsByPoints().stream()
                .limit(limit)
                .toList();
    }

    public List<ClubGameStats> getTopClubsByGameAndPoints(Long gameDefinitionId, int limit) {
        return clubGameStatsRepository.findTopClubsByGameAndPoints(gameDefinitionId).stream()
                .limit(limit)
                .toList();
    }

    public List<ClubMemberStats> getTopMembersByClubAndPoints(Long clubId, int limit) {
        return clubMemberStatsRepository.findTopMembersByClubAndPoints(clubId).stream()
                .limit(limit)
                .toList();
    }

    public List<ClubMemberStats> getTopMembersByClubAndGameAndPoints(Long clubId, Long gameDefinitionId, int limit) {
        return clubMemberStatsRepository.findTopMembersByClubAndGameAndPoints(clubId, gameDefinitionId).stream()
                .limit(limit)
                .toList();
    }
}

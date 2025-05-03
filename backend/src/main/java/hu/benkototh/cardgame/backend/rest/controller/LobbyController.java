package hu.benkototh.cardgame.backend.rest.controller;

import hu.benkototh.cardgame.backend.game.controller.CardGameController;
import hu.benkototh.cardgame.backend.game.model.CardGame;
import hu.benkototh.cardgame.backend.game.model.Player;
import hu.benkototh.cardgame.backend.rest.Data.Club;
import hu.benkototh.cardgame.backend.rest.Data.Game;
import hu.benkototh.cardgame.backend.rest.Data.Lobby;
import hu.benkototh.cardgame.backend.rest.Data.User;
import hu.benkototh.cardgame.backend.rest.repository.IGameRepository;
import hu.benkototh.cardgame.backend.rest.repository.ILobbyRepository;
import hu.benkototh.cardgame.backend.rest.repository.IUserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
public class LobbyController {

    @Autowired
    private ILobbyRepository lobbyRepository;

    @Lazy
    @Autowired
    private IUserRepository userRepository;

    @Autowired
    private IGameRepository gameRepository;

    @Lazy
    @Autowired
    private CardGameController cardGameController;

    public static final String STATUS_WAITING = "WAITING";
    public static final String STATUS_IN_GAME = "IN_GAME";
    public static final String STATUS_FINISHED = "FINISHED";
    @Autowired
    private ClubController clubController;

    public Lobby createLobby(long leaderId, long gameId, boolean playWithPoints, boolean isPublic) {
        Optional<User> leaderOpt = userRepository.findById(leaderId);
        Optional<Game> gameOpt = gameRepository.findById(gameId);

        if (leaderOpt.isPresent() && gameOpt.isPresent()) {
            User leader = leaderOpt.get();
            Game game = gameOpt.get();

            List<Lobby> existingLobbies = findByPlayersContaining(leader);
            if (!existingLobbies.isEmpty()) {
                return null;
            }

            Lobby lobby = new Lobby();
            lobby.setLeader(leader);
            lobby.setGame(game);
            lobby.setPlayWithPoints(playWithPoints);
            lobby.setMinPlayers(game.getMinPlayers());
            lobby.addPlayer(leader);
            lobby.setPublic(isPublic);
            lobby.setStatus(STATUS_WAITING);

            return lobbyRepository.save(lobby);
        }
        return null;
    }

    public Lobby createClubLobby(long leaderId, long gameId, boolean playWithPoints, long clubId, boolean isPublic) {
        Optional<User> leaderOpt = userRepository.findById(leaderId);
        Optional<Game> gameOpt = gameRepository.findById(gameId);
        Optional<Club> clubOpt = clubController.getClubById(clubId);

        if (leaderOpt.isPresent() && gameOpt.isPresent() && clubOpt.isPresent()) {
            User leader = leaderOpt.get();
            Game game = gameOpt.get();
            Club club = clubOpt.get();

            List<Lobby> existingLobbies = findByPlayersContaining(leader);
            if (!existingLobbies.isEmpty()) {
                return null;
            }

            Lobby lobby = new Lobby();
            lobby.setLeader(leader);
            lobby.setGame(game);
            lobby.setClub(club);
            lobby.setPlayWithPoints(playWithPoints);
            lobby.setMinPlayers(game.getMinPlayers());
            lobby.addPlayer(leader);
            lobby.setPublic(false);
            lobby.setStatus(STATUS_WAITING);

            return lobbyRepository.save(lobby);
        }
        return null;
    }


    public Lobby joinLobby(String code, long userId) {
        Optional<Lobby> lobbyOpt = findByCode(code);
        Optional<User> userOpt = userRepository.findById(userId);

        if (lobbyOpt.isPresent() && userOpt.isPresent()) {
            Lobby lobby = lobbyOpt.get();
            User user = userOpt.get();

            if (!STATUS_WAITING.equals(lobby.getStatus())) {
                return null;
            }

            List<Lobby> existingLobbies = findByPlayersContaining(user);
            if (!existingLobbies.isEmpty()) {
                return null;
            }

            lobby.addPlayer(user);
            return lobbyRepository.save(lobby);
        }
        return null;
    }

    public Lobby kickPlayer(long lobbyId, long leaderId, long playerId) {
        Optional<Lobby> lobbyOpt = lobbyRepository.findById(lobbyId);
        Optional<User> playerOpt = userRepository.findById(playerId);

        if (lobbyOpt.isPresent() && playerOpt.isPresent()) {
            Lobby lobby = lobbyOpt.get();
            User player = playerOpt.get();

            if (lobby.getLeader().getId() != leaderId) {
                return null;
            }

            if (!lobby.getPlayers().contains(player)) {
                return null;
            }

            if (player.getId() == leaderId) {
                return null;
            }

            lobby.removePlayer(player);
            return lobbyRepository.save(lobby);
        }
        return null;
    }

    public Lobby leaveLobby(long lobbyId, long userId) {
        Optional<Lobby> lobbyOpt = lobbyRepository.findById(lobbyId);
        Optional<User> userOpt = userRepository.findById(userId);

        if (lobbyOpt.isPresent() && userOpt.isPresent()) {
            Lobby lobby = lobbyOpt.get();
            User user = userOpt.get();

            if (!lobby.getPlayers().contains(user)) {
                return null;
            }

            if (user.getId() == lobby.getLeader().getId()) {
                if (lobby.getPlayers().size() <= 1) {
                    lobbyRepository.delete(lobby);
                    return null;
                } else {
                    User newLeader = lobby.getPlayers().stream()
                            .filter(p -> p.getId() != user.getId())
                            .findFirst()
                            .orElse(null);

                    if (newLeader != null) {
                        lobby.setLeader(newLeader);
                    }
                }
            }

            lobby.removePlayer(user);
            return lobbyRepository.save(lobby);
        }
        return null;
    }

    public Lobby updateLobbySettings(long lobbyId, long leaderId, long gameId, boolean playWithPoints) {
        Optional<Lobby> lobbyOpt = lobbyRepository.findById(lobbyId);
        Optional<Game> gameOpt = gameRepository.findById(gameId);

        if (lobbyOpt.isPresent() && gameOpt.isPresent()) {
            Lobby lobby = lobbyOpt.get();
            Game game = gameOpt.get();

            if (lobby.getLeader().getId() != leaderId) {
                return null;
            }

            lobby.setGame(game);
            lobby.setPlayWithPoints(playWithPoints);
            lobby.setMinPlayers(game.getMinPlayers());

            return lobbyRepository.save(lobby);
        }
        return null;
    }

    public Lobby startGame(long lobbyId, long leaderId) {
        Optional<Lobby> lobbyOpt = lobbyRepository.findById(lobbyId);

        if (lobbyOpt.isPresent()) {
            Lobby lobby = lobbyOpt.get();

            if (lobby.getLeader().getId() != leaderId) {
                return null;
            }

            if (!lobby.canStart()) {
                return null;
            }

            try {
                String gameName = "Game from Lobby " + lobbyId;
                String creatorId = String.valueOf(lobby.getLeader().getId());
                long gameDefinitionId = lobby.getGame().getId();
                boolean trackStatistics = lobby.isPlayWithPoints();

                CardGame cardGame = cardGameController.createCardGame(
                        gameDefinitionId,
                        creatorId,
                        gameName,
                        trackStatistics
                );

                if (cardGame != null) {
                    lobby.setCardGameId(cardGame.getId());

                    for (User user : lobby.getPlayers()) {
                        if (user.getId() == lobby.getLeader().getId()) {
                            continue;
                        }

                        Player player = new Player();
                        player.setId(String.valueOf(user.getId()));
                        player.setUsername(user.getUsername());
                        player.setHand(new ArrayList<>());
                        player.setWonCards(new ArrayList<>());
                        player.setGame(cardGame);

                        cardGame.addPlayer(player);
                    }

                    cardGameController.save(cardGame);

                    cardGameController.debugRepositoryState();

                    cardGame.startGame();

                    cardGameController.save(cardGame);

                    cardGameController.debugRepositoryState();

                    lobby.setStatus(STATUS_IN_GAME);
                    return lobbyRepository.save(lobby);
                } else {
                    return null;
                }
            } catch (Exception e) {
                e.printStackTrace();
                return null;
            }
        }
        return null;
    }

    public Lobby getLobbyById(long lobbyId) {
        return lobbyRepository.findById(lobbyId).orElse(null);
    }

    private Optional<Lobby> findByCode(String code) {
        return lobbyRepository.findAll()
                .stream()
                .filter(lobby -> lobby.getCode().equals(code))
                .findFirst();
    }

    public List<Lobby> getLobbiesByPlayer(long userId) {
        Optional<User> userOpt = userRepository.findById(userId);
        if (userOpt.isPresent()) {
            return findByPlayersContaining(userOpt.get());
        }
        return List.of();
    }

    public List<Game> getAllGames() {
        return gameRepository.findAll();
    }

    public Lobby getLobbyByCode(String code) {
        return findByCode(code).orElse(null);
    }

    private List<Lobby> findByPlayersContaining(User user) {
        return  lobbyRepository.findAll()
                .stream()
                .filter(lobby -> lobby.getPlayers().contains(user))
                .toList();
    }

    public void endGame(String id) {
        Lobby lobby = lobbyRepository.findAll().stream()
                .filter(l -> l.getCardGameId() != null && l.getCardGameId().equals(id))
                .findFirst().orElse(null);

        lobby.setCardGameId(null);
        lobby.setStatus(STATUS_WAITING);
        lobbyRepository.save(lobby);
    }

    public Lobby getLobbyByPlayer(long userId) {
        return lobbyRepository.findAll()
                .stream()
                .filter(lobby -> lobby.getPlayers().stream().anyMatch(player -> player.getId() == userId))
                .findFirst()
                .orElse(null);
    }

    public List<Lobby> getPublicLobbies() {
        return lobbyRepository.findAll()
                .stream()
                .filter(lobby -> lobby.isPublic() && "WAITING".equals(lobby.getStatus()))
                .collect(Collectors.toList());
    }
}

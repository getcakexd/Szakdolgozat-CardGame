package hu.benkototh.cardgame.backend.rest.controller;

import hu.benkototh.cardgame.backend.rest.Data.Game;
import hu.benkototh.cardgame.backend.rest.Data.Lobby;
import hu.benkototh.cardgame.backend.rest.Data.User;
import hu.benkototh.cardgame.backend.rest.repository.IGameRepository;
import hu.benkototh.cardgame.backend.rest.repository.ILobbyRepository;
import hu.benkototh.cardgame.backend.rest.repository.IUserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class LobbyController {

    @Autowired
    private ILobbyRepository lobbyRepository;

    @Autowired
    private IUserRepository userRepository;

    @Autowired
    private IGameRepository gameRepository;

    public static final String STATUS_WAITING = "WAITING";
    public static final String STATUS_IN_GAME = "IN_GAME";
    public static final String STATUS_FINISHED = "FINISHED";

    public Lobby createLobby(long leaderId, long gameId, boolean playWithPoints) {
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

            lobby.setStatus(STATUS_IN_GAME);
            return lobbyRepository.save(lobby);
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

}
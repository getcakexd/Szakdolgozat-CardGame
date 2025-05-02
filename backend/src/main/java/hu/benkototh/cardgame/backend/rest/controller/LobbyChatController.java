package hu.benkototh.cardgame.backend.rest.controller;

import hu.benkototh.cardgame.backend.rest.Data.LobbyMessage;
import hu.benkototh.cardgame.backend.rest.Data.User;
import hu.benkototh.cardgame.backend.rest.repository.ILobbyMessageRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Controller;

import java.util.Date;
import java.util.List;
import java.util.Optional;

@Controller
public class LobbyChatController {

    @Autowired
    private ILobbyMessageRepository lobbyMessageRepository;

    @Lazy
    @Autowired
    private UserController userController;

    @Autowired
    private AuditLogController auditLogController;

    public List<LobbyMessage> getLobbyMessages(long lobbyId) {
        return lobbyMessageRepository.findTop100ByLobbyIdOrderByTimestampDesc(lobbyId);
    }

    public List<LobbyMessage> getGameMessages(String gameId) {
        return lobbyMessageRepository.findTop100ByGameIdOrderByTimestampDesc(gameId);
    }

    public LobbyMessage sendLobbyMessage(long senderId, long lobbyId, String content) {
        User user = userController.getUser(senderId);

        if (user == null) {
            return null;
        }

        LobbyMessage message = new LobbyMessage();
        message.setUser(user);
        message.setContent(content);
        message.setLobbyId(lobbyId);
        message.setLobbyMessage(true);
        message.setTimestamp(new Date());
        message.setStatus("active");

        LobbyMessage savedMessage = lobbyMessageRepository.save(message);

        auditLogController.logAction("LOBBY_MESSAGE_SENT", senderId,
                "Message sent to lobby " + lobbyId + ": " + (content.length() > 50 ? content.substring(0, 47) + "..." : content));

        return savedMessage;
    }

    public LobbyMessage sendGameMessage(long senderId, String gameId, String content) {
        User user = userController.getUser(senderId);

        if (user == null) {
            return null;
        }

        LobbyMessage message = new LobbyMessage();
        message.setUser(user);
        message.setContent(content);
        message.setLobbyMessage(false);
        message.setGameId(gameId);
        message.setTimestamp(new Date());
        message.setStatus("active");

        LobbyMessage savedMessage = lobbyMessageRepository.save(message);

        auditLogController.logAction("GAME_MESSAGE_SENT", senderId,
                "Message sent to game " + gameId + ": " + (content.length() > 50 ? content.substring(0, 47) + "..." : content));

        return savedMessage;
    }

    public LobbyMessage unsendLobbyMessage(long messageId) {
        Optional<LobbyMessage> message = lobbyMessageRepository.findById(messageId);

        if (message.isEmpty()) {
            return null;
        }

        LobbyMessage unsendMessage = new LobbyMessage();
        unsendMessage.setId(message.get().getId());
        unsendMessage.setUser(message.get().getUser());
        unsendMessage.setContent("This message has been unsent.");
        unsendMessage.setLobbyMessage(message.get().isLobbyMessage());
        unsendMessage.setGameId(message.get().getGameId());
        unsendMessage.setLobbyId(message.get().getLobbyId());
        unsendMessage.setTimestamp(message.get().getTimestamp());
        unsendMessage.setStatus("unsent");

        LobbyMessage updatedMessage = lobbyMessageRepository.save(unsendMessage);

        auditLogController.logAction("LOBBY_MESSAGE_UNSENT", message.get().getUser().getId(),
                "Message unsent: " + messageId);

        return updatedMessage;
    }

    public LobbyMessage removeMessage(long messageId) {
        Optional<LobbyMessage> message = lobbyMessageRepository.findById(messageId);

        if (message.isEmpty()) {
            return null;
        }

        LobbyMessage removedMessage = new LobbyMessage();
        removedMessage.setId(message.get().getId());
        removedMessage.setUser(message.get().getUser());
        removedMessage.setContent("This message has been removed by a moderator.");
        removedMessage.setLobbyMessage(message.get().isLobbyMessage());
        removedMessage.setGameId(message.get().getGameId());
        removedMessage.setLobbyId(message.get().getLobbyId());
        removedMessage.setTimestamp(message.get().getTimestamp());
        removedMessage.setStatus("removed");

        LobbyMessage updatedMessage = lobbyMessageRepository.save(removedMessage);

        auditLogController.logAction("LOBBY_MESSAGE_REMOVED", 0L,
                "Message removed by moderator: " + messageId + " from user: " + message.get().getUser().getId());

        return updatedMessage;
    }
}

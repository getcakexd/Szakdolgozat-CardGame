package hu.benkototh.cardgame.backend.websocket.handler;

import hu.benkototh.cardgame.backend.rest.model.LobbyMessage;
import hu.benkototh.cardgame.backend.rest.controller.LobbyChatController;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Controller;
import hu.benkototh.cardgame.backend.websocket.dto.lobby.LobbyChatMessageDTO;
import hu.benkototh.cardgame.backend.websocket.dto.lobby.GetLobbyMessagesDTO;
import hu.benkototh.cardgame.backend.websocket.dto.lobby.RemoveLobbyMessageDTO;
import hu.benkototh.cardgame.backend.websocket.dto.lobby.UnsendLobbyMessageDTO;

import java.util.List;

@Controller
public class LobbyChatWebSocketHandler {

    @Autowired
    private SimpMessagingTemplate messagingTemplate;

    @Autowired
    private LobbyChatController lobbyChatController;

    @MessageMapping("/lobby.sendMessage")
    public void sendLobbyMessage(@Payload LobbyChatMessageDTO chatMessageDTO) {
        if (chatMessageDTO.getLobbyId() > 0) {
            LobbyMessage message = lobbyChatController.sendLobbyMessage(
                    chatMessageDTO.getSenderId(),
                    chatMessageDTO.getLobbyId(),
                    chatMessageDTO.getContent()
            );

            if (message != null) {
                messagingTemplate.convertAndSend(
                        "/topic/lobby/" + chatMessageDTO.getLobbyId(),
                        message
                );
            }
        } else if (chatMessageDTO.getGameId() != null && !chatMessageDTO.getGameId().isEmpty()) {
            LobbyMessage message = lobbyChatController.sendGameMessage(
                    chatMessageDTO.getSenderId(),
                    chatMessageDTO.getGameId(),
                    chatMessageDTO.getContent()
            );

            if (message != null) {
                messagingTemplate.convertAndSend(
                        "/topic/game/" + chatMessageDTO.getGameId(),
                        message
                );
            }
        }
    }

    @MessageMapping("/lobby.unsendMessage")
    public void unsendMessage(@Payload UnsendLobbyMessageDTO unsendMessageDTO) {
        LobbyMessage message = lobbyChatController.unsendLobbyMessage(unsendMessageDTO.getMessageId());

        if (message != null) {
            if (message.isLobbyMessage()) {
                messagingTemplate.convertAndSend(
                        "/topic/lobby/" + message.getLobbyId(),
                        message
                );
            } else {
                messagingTemplate.convertAndSend(
                        "/topic/game/" + message.getGameId(),
                        message
                );
            }
        }
    }

    @MessageMapping("/lobby.removeMessage")
    public void removeMessage(@Payload RemoveLobbyMessageDTO removeMessageDTO) {
        LobbyMessage message = lobbyChatController.removeMessage(removeMessageDTO.getMessageId());

        if (message != null) {
            if (message.isLobbyMessage()) {
                messagingTemplate.convertAndSend(
                        "/topic/lobby/" + message.getLobbyId(),
                        message
                );
            } else {
                messagingTemplate.convertAndSend(
                        "/topic/game/" + message.getGameId(),
                        message
                );
            }
        }
    }

    @MessageMapping("/lobby.getMessages")
    public void getMessages(@Payload GetLobbyMessagesDTO getMessagesDTO) {
        List<LobbyMessage> messages;

        if (getMessagesDTO.getLobbyId() > 0) {
            messages = lobbyChatController.getLobbyMessages(getMessagesDTO.getLobbyId());
            messagingTemplate.convertAndSend(
                    "/topic/lobby/" + getMessagesDTO.getLobbyId(),
                    messages
            );
        } else if (getMessagesDTO.getGameId() != null && !getMessagesDTO.getGameId().isEmpty()) {
            messages = lobbyChatController.getGameMessages(getMessagesDTO.getGameId());
            messagingTemplate.convertAndSend(
                    "/topic/game/" + getMessagesDTO.getGameId(),
                    messages
            );
        }
    }
}

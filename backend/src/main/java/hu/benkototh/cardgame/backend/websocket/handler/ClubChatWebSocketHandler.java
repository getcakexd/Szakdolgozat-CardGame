package hu.benkototh.cardgame.backend.websocket.handler;

import hu.benkototh.cardgame.backend.rest.model.ClubMessage;
import hu.benkototh.cardgame.backend.rest.controller.ClubChatController;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Controller;
import hu.benkototh.cardgame.backend.websocket.dto.club.ClubChatMessageDTO;
import hu.benkototh.cardgame.backend.websocket.dto.club.GetClubMessagesDTO;
import hu.benkototh.cardgame.backend.websocket.dto.club.RemoveClubMessageDTO;
import hu.benkototh.cardgame.backend.websocket.dto.club.UnsendClubMessageDTO;

import java.util.List;

@Controller
public class ClubChatWebSocketHandler {

    @Autowired
    private SimpMessagingTemplate messagingTemplate;

    @Autowired
    private ClubChatController clubChatController;

    @MessageMapping("/club.sendMessage")
    public void sendMessage(@Payload ClubChatMessageDTO chatMessageDTO) {
        ClubMessage message = clubChatController.sendClubMessage(
                chatMessageDTO.getClubId(),
                chatMessageDTO.getSenderId(),
                chatMessageDTO.getContent()
        );
        
        if (message != null) {
            messagingTemplate.convertAndSend(
                    "/topic/club/" + chatMessageDTO.getClubId(),
                    message
            );
        }
    }
    
    @MessageMapping("/club.unsendMessage")
    public void unsendMessage(@Payload UnsendClubMessageDTO unsendMessageDTO) {
        ClubMessage message = clubChatController.unsendClubMessage(unsendMessageDTO.getMessageId());
        
        if (message != null) {
            messagingTemplate.convertAndSend(
                    "/topic/club/" + message.getClub().getId(),
                    message
            );
        }
    }
    
    @MessageMapping("/club.removeMessage")
    public void removeMessage(@Payload RemoveClubMessageDTO removeMessageDTO) {
        ClubMessage message = clubChatController.removeClubMessage(removeMessageDTO.getMessageId());
        
        if (message != null) {
            messagingTemplate.convertAndSend(
                    "/topic/club/" + message.getClub().getId(),
                    message
            );
        }
    }
    
    @MessageMapping("/club.getMessages")
    public void getMessages(@Payload GetClubMessagesDTO getMessagesDTO) {
        List<ClubMessage> messages = clubChatController.getClubChatHistory(getMessagesDTO.getClubId());

        messagingTemplate.convertAndSend(
                "/topic/club/" + getMessagesDTO.getClubId(),
                messages
        );
    }
}

package hu.benkototh.cardgame.backend.websocket.handler;

import hu.benkototh.cardgame.backend.rest.Data.Message;
import hu.benkototh.cardgame.backend.rest.controller.ChatController;
import hu.benkototh.cardgame.backend.websocket.dto.friends.ChatMessageDTO;
import hu.benkototh.cardgame.backend.websocket.dto.friends.GetMessagesDTO;
import hu.benkototh.cardgame.backend.websocket.dto.friends.UnsendMessageDTO;
import hu.benkototh.cardgame.backend.websocket.dto.UserIdDTO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Controller;

import java.util.Map;

@Controller
public class ChatWebSocketHandler {

    @Autowired
    private SimpMessagingTemplate messagingTemplate;

    @Autowired
    private ChatController chatController;

    @MessageMapping("/chat.sendMessage")
    public void sendMessage(@Payload ChatMessageDTO chatMessageDTO) {
        Message message = chatController.sendMessage(
                chatMessageDTO.getSenderId(),
                chatMessageDTO.getReceiverId(),
                chatMessageDTO.getContent()
        );

        messagingTemplate.convertAndSend(
                "/topic/user/" + chatMessageDTO.getSenderId(),
                message
        );

        messagingTemplate.convertAndSend(
                "/topic/user/" + chatMessageDTO.getReceiverId(),
                message
        );

        updateUnreadCounts(chatMessageDTO.getReceiverId());
    }

    @MessageMapping("/chat.unsendMessage")
    public void unsendMessage(@Payload UnsendMessageDTO unsendMessageDTO) {
        Message message = chatController.unsendMessage(unsendMessageDTO.getMessageId());

        if (message != null) {
            messagingTemplate.convertAndSend(
                    "/topic/user/" + message.getSender().getId(),
                    message
            );

            messagingTemplate.convertAndSend(
                    "/topic/user/" + message.getReceiver().getId(),
                    message
            );
        }
    }

    @MessageMapping("/chat.getMessages")
    public void getMessages(@Payload GetMessagesDTO getMessagesDTO) {
        var messages = chatController.getMessages(
                getMessagesDTO.getUserId(),
                getMessagesDTO.getFriendId()
        );

        messagingTemplate.convertAndSend(
                "/topic/chat/" + getMessagesDTO.getUserId() + "/" + getMessagesDTO.getFriendId(),
                messages
        );

        updateUnreadCounts(getMessagesDTO.getUserId());
    }

    @MessageMapping("/chat.getUnreadCounts")
    public void getUnreadCounts(@Payload UserIdDTO userIdDTO) {
        updateUnreadCounts(userIdDTO.getUserId());
    }

    private void updateUnreadCounts(long userId) {
        Map<Long, Integer> unreadCounts = chatController.getUnreadMessageCounts(userId);
        messagingTemplate.convertAndSend("/topic/unread/" + userId, unreadCounts);
    }
}

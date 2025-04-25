package hu.benkototh.cardgame.backend.rest.controller;

import hu.benkototh.cardgame.backend.rest.Data.Message;
import hu.benkototh.cardgame.backend.rest.Data.User;
import hu.benkototh.cardgame.backend.rest.repository.IMessageRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Controller;
import org.springframework.web.server.ResponseStatusException;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@Controller
public class ChatController {

    @Autowired
    private IMessageRepository messageRepository;

    @Lazy
    @Autowired
    private UserController userController;
    
    @Autowired
    private AuditLogController auditLogController;

    public List<Message> getMessages(long userId, long friendId) {
        List<Message> messages = findBySenderAndReceiver(userId, friendId);

        messages.stream()
                .filter(msg -> msg.getReceiver().getId() == (userId) && msg.getStatus().equals("unread"))
                .forEach(msg -> {
                    msg.setStatus("read");
                    messageRepository.save(msg);
                });
        
        auditLogController.logAction("MESSAGES_VIEWED", userId,
                "Chat messages viewed with user: " + friendId);
                
        return messages;
    }

    public Message sendMessage(long userId, long friendId, String content) {
        User sender = userController.getUser(userId);
        User receiver = userController.getUser(friendId);

        if (sender == null || receiver == null) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Sender or receiver not found");
        }

        Message message = new Message(sender, receiver, content);
        Message savedMessage = messageRepository.save(message);
        
        auditLogController.logAction("MESSAGE_SENT", userId,
                "Message sent to user " + friendId + ": " + (content.length() > 50 ? content.substring(0, 47) + "..." : content));
        
        return savedMessage;
    }

    public Message unsendMessage(long messageId) {
        Optional<Message> messageOpt = messageRepository.findById(messageId);
        
        if (messageOpt.isEmpty()) {
            return null;
        }
        
        Message message = messageOpt.get();
        message.setStatus("unsent");
        message.setContent("This message has been unsent");
        
        Message updatedMessage = messageRepository.save(message);
        
        auditLogController.logAction("MESSAGE_UNSENT", message.getSender().getId(),
                "Message unsent: " + messageId);
        
        return updatedMessage;
    }

    public Map<Long, Integer> getUnreadMessageCounts(long userId) {
        List<Message> unreadMessages = messageRepository.findAll().stream()
                .filter(msg -> msg.getReceiver().getId() == userId && msg.getStatus().equals("unread"))
                .toList();

        Map<Long, Integer> unreadCounts = new HashMap<>();
        unreadMessages.forEach(msg -> unreadCounts.merge(msg.getSender().getId(), 1, Integer::sum));
        return unreadCounts;
    }

    public List<Message> findBySenderAndReceiver(Long userId, Long friendId) {
       return messageRepository.findAll().stream().filter(message ->
                message.getSender().getId() == userId && message.getReceiver().getId() == friendId ||
                message.getSender().getId() == friendId && message.getReceiver().getId() == userId
        ).toList();
    }
}

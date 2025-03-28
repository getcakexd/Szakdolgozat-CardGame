package hu.benkototh.cardgame.backend.rest.service;

import hu.benkototh.cardgame.backend.rest.Data.Message;
import hu.benkototh.cardgame.backend.rest.Data.User;
import hu.benkototh.cardgame.backend.rest.repository.IMessageRepository;
import hu.benkototh.cardgame.backend.rest.repository.IUserRepository;
import org.aspectj.bridge.IMessage;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.web.servlet.function.EntityResponse;

import javax.swing.text.html.parser.Entity;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/messages")
public class ChatRestService {

    @Autowired
    private IMessageRepository messageRepository;

    @Autowired
    private IUserRepository userRepository;

    @GetMapping("/list")
    public List<Message> getMessages(@RequestParam String userId, @RequestParam String friendId) {
        Long userIdLong = Long.parseLong(userId);
        Long friendIdLong = Long.parseLong(friendId);

        List<Message> messages = findBySenderAndReceiver(userIdLong, friendIdLong);

        messages.stream()
                .filter(msg -> msg.getReceiver().getId() == (userIdLong) && msg.getStatus().equals("unread"))
                .forEach(msg -> {
                    msg.setStatus("read");
                    messageRepository.save(msg);
                });
        return messages;
    }

    @PostMapping("/send")
    public ResponseEntity<Message> sendMessage(@RequestParam String userId, @RequestParam String friendId, @RequestParam String content) {
        User sender = userRepository.findById(Long.parseLong(userId))
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.BAD_REQUEST, "Sender not found"));

        User receiver = userRepository.findById(Long.parseLong(friendId))
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.BAD_REQUEST, "Receiver not found"));

        Message message = new Message(sender, receiver, content);
        messageRepository.save(message);


        return ResponseEntity.ok(message);
    }

    @PutMapping("/unsend")
    public ResponseEntity<Map<String, String>> deleteMessage(@RequestParam String messageId) {
        Long id = Long.parseLong(messageId);
        Message message = messageRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Message not found"));

        message.setStatus("unsent");
        message.setContent("This message has been unsent");
        messageRepository.save(message);

        Map<String, String> response = new HashMap<>();
        response.put("message", "Message marked as unsent");
        return ResponseEntity.ok(response);
    }

    @GetMapping("/unread")
    public ResponseEntity<Map<Long, Integer>> getUnreadMessageCounts(@RequestParam String userId) {
        Long userIdLong = Long.parseLong(userId);
        List<Message> unreadMessages = messageRepository.findAll().stream()
                .filter(msg -> msg.getReceiver().getId() == (userIdLong) && msg.getStatus().equals("unread"))
                .toList();

        Map<Long, Integer> unreadCounts = new HashMap<>();
        unreadMessages.forEach(msg -> unreadCounts.merge(msg.getSender().getId(), 1, Integer::sum));

        return ResponseEntity.ok(unreadCounts);
    }

    private List<Message> findBySenderAndReceiver(Long userId, Long friendId) {
       return messageRepository.findAll().stream().filter(message ->
                message.getSender().getId() == userId && message.getReceiver().getId() == friendId ||
                message.getSender().getId() == friendId && message.getReceiver().getId() == userId
        ).toList();
    }
}

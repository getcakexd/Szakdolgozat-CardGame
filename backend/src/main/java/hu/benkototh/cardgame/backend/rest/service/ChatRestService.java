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
    public ResponseEntity<List<Message>> getMessages(@RequestParam long userId, @RequestParam long friendId) {

        List<Message> messages = findBySenderAndReceiver(userId, friendId);

        messages.stream()
                .filter(msg -> msg.getReceiver().getId() == (userId) && msg.getStatus().equals("unread"))
                .forEach(msg -> {
                    msg.setStatus("read");
                    messageRepository.save(msg);
                });
        return ResponseEntity.ok(messages);
    }

    @PostMapping("/send")
    public ResponseEntity<Message> sendMessage(@RequestParam long userId, @RequestParam long friendId, @RequestParam String content) {
        User sender = userRepository.findById(userId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.BAD_REQUEST, "Sender not found"));

        User receiver = userRepository.findById(friendId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.BAD_REQUEST, "Receiver not found"));

        Message message = new Message(sender, receiver, content);
        messageRepository.save(message);


        return ResponseEntity.ok(message);
    }

    @PutMapping("/unsend")
    public ResponseEntity<String> deleteMessage(@RequestParam long messageId) {
        Message message = messageRepository.findById(messageId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Message not found"));

        message.setStatus("unsent");
        message.setContent("This message has been unsent");
        messageRepository.save(message);
        ;
        return ResponseEntity.ok("Message unsent");
    }

    @GetMapping("/unread")
    public ResponseEntity<Map<Long, Integer>> getUnreadMessageCounts(@RequestParam long userId) {
        List<Message> unreadMessages = messageRepository.findAll().stream()
                .filter(msg -> msg.getReceiver().getId() == userId && msg.getStatus().equals("unread"))
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

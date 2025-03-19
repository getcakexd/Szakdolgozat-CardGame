package hu.benkototh.cardgame.backend.rest.service;

import hu.benkototh.cardgame.backend.rest.Data.Message;
import hu.benkototh.cardgame.backend.rest.Data.User;
import hu.benkototh.cardgame.backend.rest.repository.IMessageRepository;
import hu.benkototh.cardgame.backend.rest.repository.IUserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

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

        return findBySenderAndReceiver(userIdLong, friendIdLong);
    }

    @PostMapping("/send")
    public Map<String, Object> sendMessage(@RequestParam String userId, @RequestParam String friendId, @RequestParam String content) {
        Map<String, Object> response = new HashMap<>();
        User sender = userRepository.findById(Long.parseLong(userId))
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.BAD_REQUEST, "Sender not found"));

        User receiver = userRepository.findById(Long.parseLong(friendId))
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.BAD_REQUEST, "Receiver not found"));

        Message message = new Message(sender, receiver, content);
        messageRepository.save(message);


        response.put("status", "ok");
        return response;
    }

    private List<Message> findBySenderAndReceiver(Long userId, Long friendId) {
       return messageRepository.findAll().stream().filter(message ->
                message.getSender().getId() == userId && message.getReceiver().getId() == friendId ||
                message.getSender().getId() == friendId && message.getReceiver().getId() == userId
        ).toList();
    }
}

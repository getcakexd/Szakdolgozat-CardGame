package hu.benkototh.cardgame.backend.rest.service;

import hu.benkototh.cardgame.backend.rest.Data.Message;
import hu.benkototh.cardgame.backend.rest.Data.MessageDTO;
import hu.benkototh.cardgame.backend.rest.Data.User;
import hu.benkototh.cardgame.backend.rest.repository.IMessageRepository;
import hu.benkototh.cardgame.backend.rest.repository.IUserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;

@RestController
@RequestMapping("/api/messages")
public class ChatRestService {

    @Autowired
    private IMessageRepository messageRepository;

    @Autowired
    private IUserRepository userRepository;

    @GetMapping("/{userId}")
    public List<Message> getMessages(@PathVariable Long userId) {
        return messageRepository.findAll().stream().filter(message ->
                message.getSender().getId() == userId || message.getReceiver().getId() == userId
        ).toList();
    }

    @PostMapping("/send")
    public ResponseEntity<Message> sendMessage(@RequestBody MessageDTO messageDTO) {
        User sender = userRepository.findById(messageDTO.getSenderId())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.BAD_REQUEST, "Sender not found"));

        User receiver = userRepository.findById(messageDTO.getReceiverId())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.BAD_REQUEST, "Receiver not found"));

        Message message = new Message();
        message.setSender(sender);
        message.setReceiver(receiver);
        message.setContent(messageDTO.getContent());
        message.setTimestamp(messageDTO.getTimestamp());

        messageRepository.save(message);
        return ResponseEntity.ok(message);
    }

    @GetMapping("/{userId}/{friendId}")
    public List<Message> getMessages(@PathVariable Long userId, @PathVariable Long friendId) {
        return findBySenderAndReceiver(userId, friendId);
    }

    private List<Message> findBySenderAndReceiver(Long userId, Long friendId) {
       return messageRepository.findAll().stream().filter(message ->
                message.getSender().getId() == userId && message.getReceiver().getId() == friendId ||
                message.getSender().getId() == friendId && message.getReceiver().getId() == userId
        ).toList();
    }
}

package hu.benkototh.cardgame.backend.rest.service;

import hu.benkototh.cardgame.backend.rest.controller.ChatController;
import hu.benkototh.cardgame.backend.rest.Data.Message;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/messages")
public class ChatRestService {

    @Autowired
    private ChatController chatController;

    @GetMapping("/list")
    public ResponseEntity<List<Message>> getMessages(@RequestParam long userId, @RequestParam long friendId) {
        List<Message> messages = chatController.getMessages(userId, friendId);
        return ResponseEntity.ok(messages);
    }

    @PostMapping("/send")
    public ResponseEntity<Message> sendMessage(@RequestParam long userId, @RequestParam long friendId, @RequestParam String content) {
        Message message = chatController.sendMessage(userId, friendId, content);
        return ResponseEntity.ok(message);
    }

    @PutMapping("/unsend")
    public ResponseEntity<Map<String, String>> deleteMessage(@RequestParam long messageId) {
        Map<String, String> response = new HashMap<>();
        
        Message message = chatController.unsendMessage(messageId);
        
        if (message == null) {
            response.put("message", "Message not found");
            return ResponseEntity.status(404).body(response);
        }
        
        response.put("message", "Message unsent");
        return ResponseEntity.ok(response);
    }

    @GetMapping("/unread")
    public ResponseEntity<Map<Long, Integer>> getUnreadMessageCounts(@RequestParam long userId) {
        Map<Long, Integer> unreadCounts = chatController.getUnreadMessageCounts(userId);
        return ResponseEntity.ok(unreadCounts);
    }
}

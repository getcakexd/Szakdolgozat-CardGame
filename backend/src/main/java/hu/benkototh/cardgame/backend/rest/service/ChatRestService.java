package hu.benkototh.cardgame.backend.rest.service;

import hu.benkototh.cardgame.backend.rest.controller.ChatController;
import hu.benkototh.cardgame.backend.rest.Data.Message;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/messages")
@Tag(name = "Chat", description = "Operations for direct messaging between users")
public class ChatRestService {

    @Autowired
    private ChatController chatController;

    @Operation(summary = "Get messages", description = "Retrieves messages between two users")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Successfully retrieved messages",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = Message.class)))
    })
    @GetMapping("/list")
    public ResponseEntity<List<Message>> getMessages(
            @Parameter(description = "ID of the requesting user", required = true) @RequestParam long userId,
            @Parameter(description = "ID of the friend", required = true) @RequestParam long friendId) {
        List<Message> messages = chatController.getMessages(userId, friendId);
        return ResponseEntity.ok(messages);
    }

    @Operation(summary = "Send message", description = "Sends a message from one user to another")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Message sent successfully",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = Message.class)))
    })
    @PostMapping("/send")
    public ResponseEntity<Message> sendMessage(
            @Parameter(description = "ID of the sender", required = true) @RequestParam long userId,
            @Parameter(description = "ID of the recipient", required = true) @RequestParam long friendId,
            @Parameter(description = "Message content", required = true) @RequestParam String content) {
        Message message = chatController.sendMessage(userId, friendId, content);
        return ResponseEntity.ok(message);
    }

    @Operation(summary = "Unsend message", description = "Marks a message as unsent")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Message unsent successfully"),
            @ApiResponse(responseCode = "404", description = "Message not found")
    })
    @PutMapping("/unsend")
    public ResponseEntity<Map<String, String>> deleteMessage(
            @Parameter(description = "ID of the message to unsend", required = true) @RequestParam long messageId) {
        Map<String, String> response = new HashMap<>();

        Message message = chatController.unsendMessage(messageId);

        if (message == null) {
            response.put("message", "Message not found");
            return ResponseEntity.status(404).body(response);
        }

        response.put("message", "Message unsent");
        return ResponseEntity.ok(response);
    }

    @Operation(summary = "Get unread message counts", description = "Retrieves the count of unread messages from each friend")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Successfully retrieved unread message counts",
                    content = @Content(mediaType = "application/json"))
    })
    @GetMapping("/unread")
    public ResponseEntity<Map<Long, Integer>> getUnreadMessageCounts(
            @Parameter(description = "ID of the user", required = true) @RequestParam long userId) {
        Map<Long, Integer> unreadCounts = chatController.getUnreadMessageCounts(userId);
        return ResponseEntity.ok(unreadCounts);
    }
}
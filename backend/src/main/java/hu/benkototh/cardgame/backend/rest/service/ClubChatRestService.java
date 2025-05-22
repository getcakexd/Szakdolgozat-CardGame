package hu.benkototh.cardgame.backend.rest.service;

import hu.benkototh.cardgame.backend.rest.controller.ClubChatController;
import hu.benkototh.cardgame.backend.rest.model.ClubMessage;
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

import java.util.List;

@RestController
@RequestMapping("/api/clubchat")
@Tag(name = "Club Chat", description = "Operations for club chat messaging")
public class ClubChatRestService {

    @Autowired
    private ClubChatController clubChatController;

    @Operation(summary = "Get club chat history", description = "Retrieves the chat history for a specific club")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Successfully retrieved chat history",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = ClubMessage.class)))
    })
    @GetMapping("/history")
    public ResponseEntity<List<ClubMessage>> getClubChatHistory(
            @Parameter(description = "Club ID", required = true) @RequestParam long clubId) {
        List<ClubMessage> history = clubChatController.getClubChatHistory(clubId);
        return ResponseEntity.ok(history);
    }

    @Operation(summary = "Send club message", description = "Sends a message to a club chat")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Message sent successfully",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = ClubMessage.class))),
            @ApiResponse(responseCode = "404", description = "Club or sender not found")
    })
    @PostMapping("/send")
    public ResponseEntity<ClubMessage> sendClubMessage(
            @Parameter(description = "Club ID", required = true) @RequestParam long clubId,
            @Parameter(description = "Sender user ID", required = true) @RequestParam long senderId,
            @Parameter(description = "Message content", required = true) @RequestParam String content) {
        ClubMessage message = clubChatController.sendClubMessage(clubId, senderId, content);

        if (message == null) {
            return ResponseEntity.status(404).build();
        }

        return ResponseEntity.ok(message);
    }

    @Operation(summary = "Unsend club message", description = "Marks a club message as unsent")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Message unsent successfully",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = ClubMessage.class))),
            @ApiResponse(responseCode = "404", description = "Message not found")
    })
    @PutMapping("/unsend")
    public ResponseEntity<ClubMessage> unsendClubMessage(
            @Parameter(description = "Message ID", required = true) @RequestParam long messageId) {
        ClubMessage message = clubChatController.unsendClubMessage(messageId);

        if (message == null) {
            return ResponseEntity.status(404).build();
        }

        return ResponseEntity.ok(message);
    }

    @Operation(summary = "Remove club message", description = "Removes a club message (admin/moderator action)")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Message removed successfully",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = ClubMessage.class))),
            @ApiResponse(responseCode = "404", description = "Message not found")
    })
    @PutMapping("/remove")
    public ResponseEntity<ClubMessage> removeClubMessage(
            @Parameter(description = "Message ID", required = true) @RequestParam long messageId) {
        ClubMessage message = clubChatController.removeClubMessage(messageId);

        if (message == null) {
            return ResponseEntity.status(404).build();
        }

        return ResponseEntity.ok(message);
    }
}
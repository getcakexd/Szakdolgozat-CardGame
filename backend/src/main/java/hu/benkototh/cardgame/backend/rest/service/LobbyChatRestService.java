package hu.benkototh.cardgame.backend.rest.service;

import hu.benkototh.cardgame.backend.rest.controller.LobbyChatController;
import hu.benkototh.cardgame.backend.rest.Data.LobbyMessage;
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
@RequestMapping("/api/lobbychat")
@Tag(name = "Lobby Chat", description = "Operations for managing lobby and game chat messages")
public class LobbyChatRestService {

    @Autowired
    private LobbyChatController lobbyChatController;

    @Operation(summary = "Get lobby messages", description = "Retrieves all chat messages for a specific lobby")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Successfully retrieved lobby messages",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = LobbyMessage.class)))
    })
    @GetMapping("/lobby/{lobbyId}")
    public ResponseEntity<List<LobbyMessage>> getLobbyMessages(
            @Parameter(description = "Lobby ID", required = true) @PathVariable long lobbyId) {
        List<LobbyMessage> messages = lobbyChatController.getLobbyMessages(lobbyId);
        return ResponseEntity.ok(messages);
    }

    @Operation(summary = "Get game messages", description = "Retrieves all chat messages for a specific game")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Successfully retrieved game messages",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = LobbyMessage.class)))
    })
    @GetMapping("/game/{gameId}")
    public ResponseEntity<List<LobbyMessage>> getGameMessages(
            @Parameter(description = "Game ID", required = true) @PathVariable String gameId) {
        List<LobbyMessage> messages = lobbyChatController.getGameMessages(gameId);
        return ResponseEntity.ok(messages);
    }

    @Operation(summary = "Send lobby message", description = "Sends a message to a lobby chat")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Message sent successfully",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = LobbyMessage.class))),
            @ApiResponse(responseCode = "404", description = "Lobby or sender not found")
    })
    @PostMapping("/send")
    public ResponseEntity<LobbyMessage> sendMessage(
            @Parameter(description = "Lobby ID", required = true) @RequestParam long lobbyId,
            @Parameter(description = "Sender user ID", required = true) @RequestParam long senderId,
            @Parameter(description = "Message content", required = true) @RequestParam String content) {

        LobbyMessage message = lobbyChatController.sendLobbyMessage(senderId, lobbyId, content);

        if (message == null) {
            return ResponseEntity.status(404).build();
        }

        return ResponseEntity.ok(message);
    }

    @Operation(summary = "Send game message", description = "Sends a message to a game chat")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Message sent successfully",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = LobbyMessage.class))),
            @ApiResponse(responseCode = "404", description = "Game or sender not found")
    })
    @PostMapping("/game/{gameId}/send")
    public ResponseEntity<LobbyMessage> sendGameMessage(
            @Parameter(description = "Game ID", required = true) @PathVariable String gameId,
            @Parameter(description = "Sender user ID", required = true) @RequestParam long senderId,
            @Parameter(description = "Message content", required = true) @RequestParam String content) {

        LobbyMessage message = lobbyChatController.sendGameMessage(senderId, gameId, content);

        if (message == null) {
            return ResponseEntity.status(404).build();
        }

        return ResponseEntity.ok(message);
    }

    @Operation(summary = "Unsend message", description = "Marks a message as unsent")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Message unsent successfully",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = LobbyMessage.class))),
            @ApiResponse(responseCode = "404", description = "Message not found")
    })
    @PutMapping("/unsend")
    public ResponseEntity<LobbyMessage> unsendMessage(
            @Parameter(description = "Message ID", required = true) @RequestParam long messageId) {
        LobbyMessage message = lobbyChatController.unsendLobbyMessage(messageId);

        if (message == null) {
            return ResponseEntity.status(404).build();
        }

        return ResponseEntity.ok(message);
    }

    @Operation(summary = "Remove message", description = "Removes a message (admin/moderator action)")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Message removed successfully",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = LobbyMessage.class))),
            @ApiResponse(responseCode = "404", description = "Message not found")
    })
    @PutMapping("/remove")
    public ResponseEntity<LobbyMessage> removeMessage(
            @Parameter(description = "Message ID", required = true) @RequestParam long messageId) {
        LobbyMessage message = lobbyChatController.removeMessage(messageId);

        if (message == null) {
            return ResponseEntity.status(404).build();
        }

        return ResponseEntity.ok(message);
    }
}
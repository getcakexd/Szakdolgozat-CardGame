package hu.benkototh.cardgame.backend.rest.service;

import hu.benkototh.cardgame.backend.rest.Data.ClubInvite;
import hu.benkototh.cardgame.backend.rest.controller.ClubInviteController;
import hu.benkototh.cardgame.backend.rest.controller.UserController;
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
@RequestMapping("/api/clubinvites")
@Tag(name = "Club Invites", description = "Operations for managing club invitations")
public class ClubInviteRestService {

    @Autowired
    private ClubInviteController clubInviteController;
    @Autowired
    private UserController userController;

    @Operation(summary = "Get user's club invites", description = "Retrieves all club invites for a specific user")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Successfully retrieved invites",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = ClubInvite.class)))
    })
    @GetMapping("/list")
    public ResponseEntity<List<ClubInvite>> getClubInvites(
            @Parameter(description = "User ID", required = true) @RequestParam long userId) {
        List<ClubInvite> clubInvites = clubInviteController.getClubInvites(userId);
        return ResponseEntity.ok(clubInvites);
    }

    @Operation(summary = "Get pending club invites", description = "Retrieves all pending invites for a specific club")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Successfully retrieved pending invites",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = ClubInvite.class)))
    })
    @GetMapping("/pending")
    public ResponseEntity<List<ClubInvite>> getClubInvite(
            @Parameter(description = "Club ID", required = true) @RequestParam long clubId) {
        List<ClubInvite> clubInvites = clubInviteController.getPendingInvites(clubId);
        return ResponseEntity.ok(clubInvites);
    }

    @Operation(summary = "Get club invite history", description = "Retrieves the history of all invites for a specific club")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Successfully retrieved invite history",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = ClubInvite.class)))
    })
    @GetMapping("/history")
    public ResponseEntity<List<ClubInvite>> getInviteHistory(
            @Parameter(description = "Club ID", required = true) @RequestParam long clubId) {
        List<ClubInvite> history = clubInviteController.getInviteHistory(clubId);
        return ResponseEntity.ok(history);
    }

    @Operation(summary = "Send club invite", description = "Sends an invitation to join a club to a user")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Invite sent successfully",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = ClubInvite.class))),
            @ApiResponse(responseCode = "400", description = "User not found or already a member/invited")
    })
    @PostMapping("/add")
    public ResponseEntity<?> addClubInvite(
            @Parameter(description = "Club ID", required = true) @RequestParam long clubId,
            @Parameter(description = "Username to invite", required = true) @RequestParam String username) {
        Map<String, String> response = new HashMap<>();

        ClubInvite clubInvite = clubInviteController.addClubInvite(clubId, username);

        if (clubInvite == null) {
            if (userController.findByUsername(username) == null) {
                response.put("message", "User not found.");
                return ResponseEntity.status(400).body(response);
            }

            response.put("message", "User is already a member of this club or invite already sent.");
            return ResponseEntity.status(400).body(response);
        }

        return ResponseEntity.ok(clubInvite);
    }

    @Operation(summary = "Accept club invite", description = "Accepts a club invitation")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Invite accepted successfully",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = ClubInvite.class))),
            @ApiResponse(responseCode = "404", description = "Invite not found")
    })
    @PutMapping("/accept")
    public ResponseEntity<ClubInvite> acceptClubInvite(
            @Parameter(description = "Invite ID", required = true) @RequestParam long id) {
        ClubInvite clubInvite = clubInviteController.acceptClubInvite(id);

        if (clubInvite == null) {
            return ResponseEntity.status(404).body(null);
        }

        return ResponseEntity.ok(clubInvite);
    }

    @Operation(summary = "Decline club invite", description = "Declines a club invitation")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Invite declined successfully",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = ClubInvite.class))),
            @ApiResponse(responseCode = "404", description = "Invite not found")
    })
    @PutMapping("/decline")
    public ResponseEntity<ClubInvite> declineClubInvite(
            @Parameter(description = "Invite ID", required = true) @RequestParam long id) {
        ClubInvite clubInvite = clubInviteController.declineClubInvite(id);

        if (clubInvite == null) {
            return ResponseEntity.status(404).body(null);
        }

        return ResponseEntity.ok(clubInvite);
    }

    @Operation(summary = "Delete club invite", description = "Deletes a club invitation")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Invite deleted successfully"),
            @ApiResponse(responseCode = "404", description = "Invite not found")
    })
    @DeleteMapping("/delete")
    public ResponseEntity<?> deleteClubInvite(
            @Parameter(description = "Invite ID", required = true) @RequestParam long id) {
        boolean deleted = clubInviteController.deleteClubInvite(id);

        if (!deleted) {
            return ResponseEntity.status(404).body(null);
        }

        return ResponseEntity.ok(null);
    }
}
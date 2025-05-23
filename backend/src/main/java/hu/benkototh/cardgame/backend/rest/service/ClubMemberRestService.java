package hu.benkototh.cardgame.backend.rest.service;

import hu.benkototh.cardgame.backend.rest.controller.ClubMemberController;
import hu.benkototh.cardgame.backend.rest.model.ClubMember;
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
@RequestMapping("/api/clubmembers")
@Tag(name = "Club Members", description = "Operations for managing club members")
public class ClubMemberRestService {

    @Autowired
    private ClubMemberController clubMemberController;

    @Operation(summary = "Get club members", description = "Retrieves all members of a specific club")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Successfully retrieved club members",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = ClubMember.class))),
            @ApiResponse(responseCode = "404", description = "Club not found")
    })
    @GetMapping("/list")
    public ResponseEntity<List<ClubMember>> getClubMembers(
            @Parameter(description = "Club ID", required = true) @RequestParam long clubId) {
        List<ClubMember> clubMembers = clubMemberController.getClubMembers(clubId);

        if (clubMembers == null) {
            return ResponseEntity.status(404).body(null);
        }

        return ResponseEntity.ok(clubMembers);
    }

    @Operation(summary = "Get specific club member", description = "Retrieves information about a specific member in a club")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Successfully retrieved club member",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = ClubMember.class))),
            @ApiResponse(responseCode = "404", description = "Club member not found")
    })
    @GetMapping("/get")
    public ResponseEntity<ClubMember> getClubMember(
            @Parameter(description = "Club ID", required = true) @RequestParam long clubId,
            @Parameter(description = "User ID", required = true) @RequestParam long userId) {
        ClubMember clubMember = clubMemberController.getClubMember(clubId, userId);

        if (clubMember == null) {
            return ResponseEntity.status(404).body(null);
        }

        return ResponseEntity.ok(clubMember);
    }

    @Operation(summary = "Add club member", description = "Adds a user as a member to a club")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "201", description = "Member added successfully",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = ClubMember.class))),
            @ApiResponse(responseCode = "404", description = "Club or user not found")
    })
    @PostMapping("/add")
    public ResponseEntity<ClubMember> addClubMember(
            @Parameter(description = "Club ID", required = true) @RequestParam long clubId,
            @Parameter(description = "User ID", required = true) @RequestParam long userId) {
        ClubMember clubMember = clubMemberController.addClubMember(clubId, userId);

        if (clubMember == null) {
            return ResponseEntity.status(404).body(null);
        }

        return ResponseEntity.status(201).body(clubMember);
    }

    @Operation(summary = "Get member role", description = "Retrieves the role of a specific member in a club")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Successfully retrieved member role")
    })
    @GetMapping("/role")
    public ResponseEntity<Map<String, String>> getClubMemberRole(
            @Parameter(description = "Club ID", required = true) @RequestParam long clubId,
            @Parameter(description = "User ID", required = true) @RequestParam long userId) {
        Map<String, String> response = new HashMap<>();
        String role = clubMemberController.getClubMemberRole(clubId, userId);

        if (role == null) {
            response.put("message", "User is not a member of the club.");
        }
        response.put("role", role);
        return ResponseEntity.ok(response);
    }

    @Operation(summary = "Modify member role", description = "Changes the role of a club member")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Member role modified successfully",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = ClubMember.class))),
            @ApiResponse(responseCode = "400", description = "Invalid role or insufficient permissions")
    })
    @PutMapping("/modify")
    public ResponseEntity<ClubMember> promoteClubMember(
            @Parameter(description = "Club ID", required = true) @RequestParam long clubId,
            @Parameter(description = "User ID", required = true) @RequestParam long userId,
            @Parameter(description = "New role", required = true) @RequestParam String role
    ) {
        ClubMember clubMember = clubMemberController.modifyClubMember(clubId, userId, role);

        if (clubMember == null) {
            return ResponseEntity.status(400).body(null);
        }

        return ResponseEntity.ok(clubMember);
    }

    @Operation(summary = "Kick member", description = "Removes a member from a club (admin/moderator action)")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Member removed successfully"),
            @ApiResponse(responseCode = "404", description = "Member not found in club")
    })
    @DeleteMapping("/kick")
    public ResponseEntity<Map<String, String>> removeClubMember(
            @Parameter(description = "Club ID", required = true) @RequestParam long clubId,
            @Parameter(description = "User ID", required = true) @RequestParam long userId) {
        Map<String, String> response = new HashMap<>();

        boolean removed = clubMemberController.removeClubMember(clubId, userId);

        if (!removed) {
            response.put("message", "User is not a member of the club.");
            return ResponseEntity.status(404).body(response);
        }

        response.put("message", "User removed from the club.");
        return ResponseEntity.ok(response);
    }

    @Operation(summary = "Leave club", description = "Allows a user to leave a club")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Successfully left the club"),
            @ApiResponse(responseCode = "400", description = "Admin cannot leave the club")
    })
    @DeleteMapping("/leave")
    public ResponseEntity<Map<String, String>> leaveClub(
            @Parameter(description = "Club ID", required = true) @RequestParam long clubId,
            @Parameter(description = "User ID", required = true) @RequestParam long userId) {
        Map<String, String> response = new HashMap<>();

        boolean left = clubMemberController.leaveClub(clubId, userId);

        if (!left) {
            response.put("message", "Admin cannot leave the club.");
            return ResponseEntity.status(400).body(response);
        }

        response.put("message", "User left the club.");
        return ResponseEntity.ok(response);
    }
}
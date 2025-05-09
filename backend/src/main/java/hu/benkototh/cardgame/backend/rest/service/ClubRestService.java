package hu.benkototh.cardgame.backend.rest.service;

import hu.benkototh.cardgame.backend.rest.controller.ClubController;
import hu.benkototh.cardgame.backend.rest.Data.Club;
import hu.benkototh.cardgame.backend.rest.Data.ClubMember;
import hu.benkototh.cardgame.backend.rest.controller.ClubMemberController;
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
@RequestMapping("/api/clubs")
@Tag(name = "Clubs", description = "Operations for managing clubs")
public class ClubRestService {

    @Autowired
    private ClubController clubController;
    @Autowired
    private ClubMemberController clubMemberController;

    @Operation(summary = "Get all clubs", description = "Retrieves a list of all clubs")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Successfully retrieved clubs",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = Club.class)))
    })
    @GetMapping("/list")
    public ResponseEntity<List<Club>> getClubs() {
        return ResponseEntity.ok(clubController.getAllClubs());
    }

    @Operation(summary = "Get public clubs", description = "Retrieves a list of all public clubs")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Successfully retrieved public clubs",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = Club.class)))
    })
    @GetMapping("/public")
    public ResponseEntity<List<Club>> getPublicClubs() {
        return ResponseEntity.ok(clubController.getPublicClubs());
    }

    @Operation(summary = "Get joinable clubs", description = "Retrieves a list of clubs that a user can join")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Successfully retrieved joinable clubs",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = Club.class))),
            @ApiResponse(responseCode = "404", description = "User not found")
    })
    @GetMapping("/joinable")
    public ResponseEntity<List<Club>> getPublicClubs(
            @Parameter(description = "User ID", required = true) @RequestParam long userId) {
        List<Club> clubs = clubController.getJoinableClubs(userId);

        if (clubs == null) {
            return ResponseEntity.status(404).body(null);
        }

        return ResponseEntity.ok(clubs);
    }

    @Operation(summary = "Get club by ID", description = "Retrieves a specific club by its ID")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Successfully retrieved club",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = Club.class))),
            @ApiResponse(responseCode = "404", description = "Club not found")
    })
    @GetMapping("/get")
    public ResponseEntity<Club> getClub(
            @Parameter(description = "Club ID", required = true) @RequestParam long clubId) {
        Club club = clubController.getClub(clubId);

        if (club == null) {
            return ResponseEntity.notFound().build();
        }

        return ResponseEntity.ok(club);
    }

    @Operation(summary = "Get user's clubs", description = "Retrieves all clubs that a user is a member of")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Successfully retrieved user's clubs",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = Club.class))),
            @ApiResponse(responseCode = "404", description = "User not found")
    })
    @GetMapping("/user")
    public ResponseEntity<List<Club>> getClubsByUser(
            @Parameter(description = "User ID", required = true) @RequestParam long userId) {
        List<Club> clubs = clubController.getClubsByUser(userId);

        if (clubs == null) {
            return ResponseEntity.status(404).body(null);
        }

        return ResponseEntity.ok(clubs);
    }

    @Operation(summary = "Get club members", description = "Retrieves all members of a specific club")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Successfully retrieved club members",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = ClubMember.class))),
            @ApiResponse(responseCode = "404", description = "Club not found")
    })
    @GetMapping("/members")
    public ResponseEntity<?> getClubMembers(
            @Parameter(description = "Club ID", required = true) @RequestParam long clubId) {
        List<ClubMember> clubMembers = clubMemberController.getClubMembers(clubId);

        if (clubMembers == null) {
            return ResponseEntity.status(404).body("Club not found");
        }

        return ResponseEntity.ok(clubMembers);
    }

    @Operation(summary = "Create club", description = "Creates a new club with the specified user as admin")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "201", description = "Club created successfully",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = Club.class))),
            @ApiResponse(responseCode = "404", description = "User not found")
    })
    @PostMapping("/create")
    public ResponseEntity<?> createClub(
            @Parameter(description = "Club name", required = true) @RequestParam String name,
            @Parameter(description = "Club description", required = true) @RequestParam String description,
            @Parameter(description = "Whether the club is public", required = true) @RequestParam boolean isPublic,
            @Parameter(description = "Admin user ID", required = true) @RequestParam long userId) {

        Club club = clubController.createClub(name, description, isPublic, userId);

        if (club == null) {
            return ResponseEntity.status(404).body("User not found");
        }

        return ResponseEntity.status(201).body(club);
    }

    @Operation(summary = "Update club", description = "Updates an existing club's information")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Club updated successfully",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = Club.class))),
            @ApiResponse(responseCode = "404", description = "Club not found")
    })
    @PutMapping("/update")
    public ResponseEntity<?> updateClub(
            @Parameter(description = "Club ID", required = true) @RequestParam long clubId,
            @Parameter(description = "New club name", required = true) @RequestParam String name,
            @Parameter(description = "New club description", required = true) @RequestParam String description,
            @Parameter(description = "Whether the club is public", required = true) @RequestParam boolean isPublic) {

        Club club = clubController.updateClub(clubId, name, description, isPublic);

        if (club == null) {
            return ResponseEntity.status(404).body("Club not found");
        }

        return ResponseEntity.ok(club);
    }

    @Operation(summary = "Delete club", description = "Deletes a club and all associated data")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Club deleted successfully"),
            @ApiResponse(responseCode = "404", description = "Club not found")
    })
    @DeleteMapping("/delete")
    public ResponseEntity<Map<String, String>> deleteClub(
            @Parameter(description = "Club ID", required = true) @RequestParam long clubId) {
        Map<String, String> response = new HashMap<>();

        boolean deleted = clubController.deleteClub(clubId);

        if (!deleted) {
            response.put("message", "Club not found");
            return ResponseEntity.status(404).body(response);
        }

        response.put("message", "Club deleted");
        return ResponseEntity.ok(response);
    }

    @Operation(summary = "Get club member count", description = "Retrieves the number of members in a club")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Successfully retrieved member count"),
            @ApiResponse(responseCode = "404", description = "Club not found")
    })
    @GetMapping("/member/count")
    public ResponseEntity<Map<String, Integer>> getClubMemberCount(
            @Parameter(description = "Club ID", required = true) @RequestParam long clubId) {
        Map<String, Integer> response = new HashMap<>();

        int memberCount = clubMemberController.getClubMemberCount(clubId);

        if (memberCount == -1) {
            response.put("message", 0);
            return ResponseEntity.status(404).body(response);
        }

        response.put("memberCount", memberCount);
        return ResponseEntity.ok(response);
    }
}
package hu.benkototh.cardgame.backend.rest.service;

import hu.benkototh.cardgame.backend.rest.Data.Data;
import hu.benkototh.cardgame.backend.rest.repository.IDataRepository;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@Tag(name = "Health", description = "Health check endpoints")
public class HeartBeatRestService {

    @Autowired
    private IDataRepository dataRepository;

    @Operation(summary = "Heartbeat check", description = "Checks if the API is running and can connect to the database")
    @ApiResponse(responseCode = "200", description = "API is running and database is accessible",
            content = @Content(mediaType = "application/json", schema = @Schema(implementation = Data.class)))
    @GetMapping("/api/heartbeat")
    public List<Data> all() {
        return dataRepository.findAll();
    }
}
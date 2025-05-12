package hu.benkototh.cardgame.backend.rest.Data;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.persistence.*;

@Entity
@Table(name = "clubs")
@Schema(description = "Represents a club where players can gather and play games together")
public class Club {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Schema(description = "Unique identifier for the club", example = "1")
    private long id;

    @Column(nullable = false)
    @Schema(description = "Name of the club", example = "Card Masters")
    private String name;

    @Column(nullable = false)
    @Schema(description = "Description of the club", example = "A club for card game enthusiasts")
    private String description;

    @Column(nullable = false)
    @Schema(description = "Whether the club is publicly visible and joinable", example = "true")
    private boolean isPublic;

    public Club() {}

    public Club(String name, String description, boolean isPublic) {
        this.name = name;
        this.description = description;
        this.isPublic = isPublic;
    }

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public boolean isPublic() {
        return isPublic;
    }

    public void setPublic(boolean aPublic) {
        isPublic = aPublic;
    }
}
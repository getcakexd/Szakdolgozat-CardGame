package hu.benkototh.cardgame.backend.rest.model;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "Request object for Google authentication")
public class GoogleAuthRequest {

    @Schema(description = "User's email address from Google", example = "user@gmail.com")
    private String email;

    @Schema(description = "User's full name from Google", example = "John Doe")
    private String name;

    @Schema(description = "Google authentication token", example = "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...")
    private String token;

    @Schema(description = "URL to user's Google profile photo", example = "https://lh3.googleusercontent.com/a/ACg8ocLt...")
    private String photoUrl;

    public GoogleAuthRequest() {
    }

    public GoogleAuthRequest(String email, String name, String token, String photoUrl) {
        this.email = email;
        this.name = name;
        this.token = token;
        this.photoUrl = photoUrl;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getToken() {
        return token;
    }

    public void setToken(String token) {
        this.token = token;
    }

    public String getPhotoUrl() {
        return photoUrl;
    }

    public void setPhotoUrl(String photoUrl) {
        this.photoUrl = photoUrl;
    }
}
package hu.benkototh.cardgame.backend.rest.Data;

public class GoogleAuthRequest {
    private String email;
    private String name;
    private String token;
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

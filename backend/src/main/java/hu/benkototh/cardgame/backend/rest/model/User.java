package hu.benkototh.cardgame.backend.rest.model;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.persistence.*;

import java.util.Date;

@Entity
@Table(name = "users")
@Schema(description = "Represents a user account")
public class User {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Schema(description = "Unique identifier for the user", example = "1")
    private long id;

    @Schema(description = "Username for login", example = "johndoe")
    private String username;

    @Schema(description = "Hashed password", example = "$2a$10$dXJ3SW6G7P50lGmMkkmwe.20cQQubK3.HZWzG3YB1tlRy.fqvM/BG")
    private String password;

    @Schema(description = "Email address", example = "john.doe@example.com")
    private String email;

    @Schema(description = "User role for authorization", example = "USER", allowableValues = {"USER", "ADMIN", "AGENT", "MODERATOR"})
    private String role;

    @Schema(description = "Whether the account is locked due to security concerns", example = "false")
    private boolean locked;

    @Schema(description = "Number of consecutive failed login attempts", example = "0")
    private int failedLoginAttempts;

    @Schema(description = "Whether the email address has been verified", example = "true")
    private boolean verified = false;

    @Schema(description = "Token for email verification", example = "7c9e6679-7425-40de-944b-e07fc1f90ae7")
    private String verificationToken;

    @Schema(description = "Expiry date for the verification token", example = "2023-05-21T14:30:00Z")
    private Date verificationTokenExpiry;

    public User() {
    }

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getRole() {
        return role;
    }

    public void setRole(String role) {
        this.role = role;
    }

    public boolean isLocked() {
        return locked;
    }

    public void setLocked(boolean locked) {
        this.locked = locked;
    }

    public int getFailedLoginAttempts() {
        return failedLoginAttempts;
    }

    public void setFailedLoginAttempts(int failedLoginAttempts) {
        this.failedLoginAttempts = failedLoginAttempts;
    }

    public boolean isVerified() {
        return verified;
    }

    public void setVerified(boolean verified) {
        this.verified = verified;
    }

    public String getVerificationToken() {
        return verificationToken;
    }

    public void setVerificationToken(String verificationToken) {
        this.verificationToken = verificationToken;
    }

    public Date getVerificationTokenExpiry() {
        return this.verificationTokenExpiry;
    }

    public void setVerificationTokenExpiry(Date verificationTokenExpiry) {
        this.verificationTokenExpiry = verificationTokenExpiry;
    }
}
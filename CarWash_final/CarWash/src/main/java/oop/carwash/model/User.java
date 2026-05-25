
package oop.carwash.model;


/**
 * Represents a user (staff member) who can log in and create receipts.
 *
 * Encapsulation: all fields private, accessed via getters/setters.
 */
public class User {

    private int userId;
    private String username;
    private String password;  // Plain text as per specification
    private int isActive;     // 1 = active, 0 = disabled
    private String createdAt;

    // ── Constructors ──────────────────────────────────────────────────────

    public User() {
    }

    public User(String username, String password) {
        this.username = username;
        this.password = password;
        this.isActive = 1;
    }

    public User(int userId, String username, String password, int isActive, String createdAt) {
        this.userId = userId;
        this.username = username;
        this.password = password;
        this.isActive = isActive;
        this.createdAt = createdAt;
    }

    // ── Getters / Setters ─────────────────────────────────────────────────

    public int getUserId() {
        return userId;
    }

    public void setUserId(int userId) {
        this.userId = userId;
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

    public int getIsActive() {
        return isActive;
    }

    public void setIsActive(int isActive) {
        this.isActive = isActive;
    }

    public String getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(String createdAt) {
        this.createdAt = createdAt;
    }

    @Override
    public String toString() {
        return "User{" +
                "userId=" + userId +
                ", username='" + username + '\'' +
                ", isActive=" + isActive +
                ", createdAt='" + createdAt + '\'' +
                '}';
    }
}
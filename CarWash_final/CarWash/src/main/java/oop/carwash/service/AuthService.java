
package oop.carwash.service;


import oop.carwash.dao.UserDAO;
import oop.carwash.model.User;

import java.util.Optional;

/**
 * Handles user authentication (login).
 * Plain text password comparison as per specification.
 *
 * Usage:
 *   boolean success = AuthService.login("admin", "admin");
 *   if (success) {
 *       User user = SessionContext.getInstance().getLoggedInUser();
 *   }
 */
public class AuthService {

    private static final UserDAO userDAO = new UserDAO();

    private AuthService() {
        // Utility class
    }

    // ── Login ──────────────────────────────────────────────────────────────

    /**
     * Attempts to log in a user with the given username and password.
     * If successful, stores the user in SessionContext.
     * Returns true on success, false otherwise.
     */
    public static boolean login(String username, String password) {
        if (username == null || username.trim().isEmpty() ||
            password == null || password.trim().isEmpty()) {
            System.out.println("[Auth] Login failed: empty username or password");
            return false;
        }

        Optional<User> userOpt = userDAO.findByUsername(username);
        if (userOpt.isEmpty()) {
            System.out.println("[Auth] Login failed: user not found - " + username);
            return false;
        }

        User user = userOpt.get();

        // Check if user is active
        if (user.getIsActive() == 0) {
            System.out.println("[Auth] Login failed: user is disabled - " + username);
            return false;
        }

        // Plain text password comparison (as per specification)
        if (!user.getPassword().equals(password)) {
            System.out.println("[Auth] Login failed: incorrect password - " + username);
            return false;
        }

        // Success: store in session
        SessionContext.getInstance().setLoggedInUser(user);
        System.out.println("[Auth] Login successful: " + username);
        return true;
    }

    /**
     * Logs out the current user.
     */
    public static void logout() {
        SessionContext.getInstance().clearSession();
        System.out.println("[Auth] User logged out");
    }

    /**
     * Returns the currently logged-in user, or null if not logged in.
     */
    public static User getCurrentUser() {
        return SessionContext.getInstance().getLoggedInUser();
    }
}

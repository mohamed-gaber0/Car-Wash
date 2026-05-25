
package oop.carwash.service;

import oop.carwash.model.User;

/**
 * Singleton that holds the currently logged-in user.
 *
 * After AuthService succeeds, call SessionContext.getInstance().setLoggedInUser(user).
 * Use getLoggedInUser() throughout the app to check who is logged in.
 * On logout, call clearSession().
 */
public class SessionContext {

    private static SessionContext instance;
    private User loggedInUser;

    // ── Singleton ──────────────────────────────────────────────────────────

    private SessionContext() {
    }

    public static synchronized SessionContext getInstance() {
        if (instance == null) {
            instance = new SessionContext();
        }
        return instance;
    }

    // ── Session management ─────────────────────────────────────────────────

    /**
     * Called after successful login.
     */
    public void setLoggedInUser(User user) {
        this.loggedInUser = user;
        System.out.println("[Session] User logged in: " + user.getUsername());
    }

    /**
     * Returns the logged-in user, or null if no one is logged in.
     */
    public User getLoggedInUser() {
        return loggedInUser;
    }

    /**
     * Returns true if someone is logged in.
     */
    public boolean isLoggedIn() {
        return loggedInUser != null;
    }

    /**
     * Called on logout.
     */
    public void clearSession() {
        if (loggedInUser != null) {
            System.out.println("[Session] User logged out: " + loggedInUser.getUsername());
        }
        loggedInUser = null;
    }
}

package oop.carwash.dao;


import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.Statement;

/**
 * Singleton that owns the single JDBC connection to carwash.db.
 *
 * Usage from any DAO:
 *   Connection conn = DatabaseConnection.getInstance().getConnection();
 *
 * Thread-safety note: SQLite in WAL mode allows one writer + multiple
 * concurrent readers. All write operations in DAOs must be synchronised
 * or wrapped in transactions to avoid SQLITE_BUSY errors when the
 * background socket server thread reads simultaneously.
 */
public class DatabaseConnection {

    // The .db file is created next to the JAR in the working directory.
    private static final String DB_URL = "jdbc:sqlite:carwash.db";

    // The one and only instance (created on first call, never replaced).
    private static DatabaseConnection instance;

    // The live JDBC connection held for the lifetime of the application.
    private Connection connection;

    // ── Private constructor ───────────────────────────────────────────────

    private DatabaseConnection() {
        try {
            // Explicitly load the SQLite JDBC driver.
            // Required on some JVM configurations (e.g. fat JARs).
            Class.forName("org.sqlite.JDBC");

            connection = DriverManager.getConnection(DB_URL);
            applyPragmas(connection);

            System.out.println("[DB] Connected to " + DB_URL);

        } catch (ClassNotFoundException e) {
            throw new RuntimeException("SQLite JDBC driver not found on classpath.", e);
        } catch (SQLException e) {
            throw new RuntimeException("Failed to open database connection: " + e.getMessage(), e);
        }
    }

    // ── Singleton access ──────────────────────────────────────────────────

    /**
     * Returns the singleton instance, creating it on the first call.
     * Synchronized to be safe if two threads happen to call this
     * simultaneously during startup.
     */
    public static synchronized DatabaseConnection getInstance() {
        if (instance == null) {
            instance = new DatabaseConnection();
        }
        return instance;
    }

    // ── Public API ────────────────────────────────────────────────────────

    /**
     * Returns the live Connection. If the connection was closed or became
     * stale (e.g. the file was deleted at runtime), it is automatically
     * re-opened.
     */
    public Connection getConnection() {
        try {
            if (connection == null || connection.isClosed()) {
                System.out.println("[DB] Connection was closed — reopening.");
                connection = DriverManager.getConnection(DB_URL);
                applyPragmas(connection);
            }
        } catch (SQLException e) {
            throw new RuntimeException("Failed to recover database connection: " + e.getMessage(), e);
        }
        return connection;
    }

    /**
     * Cleanly closes the connection. Call this in your Application.stop()
     * override to avoid file-lock warnings on Windows.
     */
    public void close() {
        try {
            if (connection != null && !connection.isClosed()) {
                connection.close();
                System.out.println("[DB] Connection closed.");
            }
        } catch (SQLException e) {
            System.err.println("[DB] Warning: error while closing connection: " + e.getMessage());
        }
    }

    // ── Private helpers ───────────────────────────────────────────────────

    /**
     * Fires essential PRAGMAs on a freshly opened connection.
     * SQLite resets these per-connection, so they must be set every time.
     */
    private void applyPragmas(Connection conn) throws SQLException {
        try (Statement stmt = conn.createStatement()) {
            // Enforce foreign key constraints (OFF by default in SQLite).
            stmt.execute("PRAGMA foreign_keys = ON");

            // WAL (Write-Ahead Logging) mode: much faster for mixed
            // read/write workloads and safer during background thread access.
            stmt.execute("PRAGMA journal_mode = WAL");

            // Sensible busy timeout: if a write lock is held by another
            // thread, wait up to 3 seconds before throwing SQLITE_BUSY.
            stmt.execute("PRAGMA busy_timeout = 3000");
        }
    }
}
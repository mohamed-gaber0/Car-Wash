package oop.carwash.dao;


import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

/**
 * Initialises the database schema on first run.
 *
 * Call DatabaseManager.initialise() once from MainApp before the UI starts.
 * Every statement uses CREATE TABLE IF NOT EXISTS, so re-running on an
 * existing database is completely safe - existing data is never touched.
 *
 * Table creation order matters because of foreign key constraints:
 *   users, customers, services, products  ->  receipts
 *   receipts  ->  receipt_services, receipt_products, payments
 */
public class DatabaseManager {

    private DatabaseManager() {
        // Utility class - no instances needed.
    }

    /**
     * Creates all tables and indexes if they do not already exist.
     * Throws a RuntimeException (wrapping the SQLException) so that
     * MainApp can display an error dialog and exit cleanly.
     */
    public static void initialise() {
        Connection conn = DatabaseConnection.getInstance().getConnection();
        try (Statement stmt = conn.createStatement()) {
            createTables(stmt);
            createIndexes(stmt);
            seedDefaultData(conn);
            System.out.println("[DB] Schema ready.");
        } catch (SQLException e) {
            throw new RuntimeException("Failed to initialise database schema: " + e.getMessage(), e);
        }
    }

    // ── Table definitions ─────────────────────────────────────────────────

    private static void createTables(Statement stmt) throws SQLException {

        // ── Users ─────────────────────────────────────────────────────
        stmt.execute(
            "CREATE TABLE IF NOT EXISTS users (" +
            "    user_id    INTEGER PRIMARY KEY AUTOINCREMENT," +
            "    username   TEXT    NOT NULL UNIQUE," +
            "    password   TEXT    NOT NULL," +
            "    is_active  INTEGER NOT NULL DEFAULT 1," +
            "    created_at TEXT    NOT NULL DEFAULT CURRENT_TIMESTAMP" +
            ")"
        );

        // ── Customers ─────────────────────────────────────────────────
        stmt.execute(
            "CREATE TABLE IF NOT EXISTS customers (" +
            "    customer_id      INTEGER PRIMARY KEY AUTOINCREMENT," +
            "    name             TEXT    NOT NULL," +
            "    phone            TEXT," +
            "    car_plate_number TEXT," +
            "    created_at       TEXT    NOT NULL DEFAULT CURRENT_TIMESTAMP" +
            ")"
        );

        // ── Services ──────────────────────────────────────────────────
        stmt.execute(
            "CREATE TABLE IF NOT EXISTS services (" +
            "    service_id   INTEGER PRIMARY KEY AUTOINCREMENT," +
            "    service_name TEXT    NOT NULL," +
            "    price        REAL    NOT NULL" +
            ")"
        );

        // ── Products ──────────────────────────────────────────────────
        // product_type: 'GALLON' or 'CARTON'
        // For GALLON: unit = liters (can be float), units_per_carton ignored
        // For CARTON: units_per_carton = number of pieces per carton
        // stock_quantity: total available (float for gallons in liters, int-like for cartons)
        // sold_quantity: total sold same unit
        stmt.execute(
            "CREATE TABLE IF NOT EXISTS products (" +
            "    product_id        INTEGER PRIMARY KEY AUTOINCREMENT," +
            "    product_name      TEXT    NOT NULL," +
            "    unit_price        REAL    NOT NULL," +
            "    product_type      TEXT    NOT NULL DEFAULT 'GALLON'," +
            "    units_per_carton  INTEGER NOT NULL DEFAULT 1," +
            "    stock_quantity    REAL    NOT NULL DEFAULT 0," +
            "    sold_quantity     REAL    NOT NULL DEFAULT 0" +
            ")"
        );
        // Migration: add new columns to existing databases safely
        migrateProductsTable(stmt);

        // ── Receipts ──────────────────────────────────────────────────
        stmt.execute(
            "CREATE TABLE IF NOT EXISTS receipts (" +
            "    receipt_id     INTEGER PRIMARY KEY AUTOINCREMENT," +
            "    customer_id    INTEGER NOT NULL," +
            "    created_by     INTEGER," +
            "    receipt_date   TEXT    NOT NULL DEFAULT CURRENT_TIMESTAMP," +
            "    services_total REAL    NOT NULL DEFAULT 0," +
            "    products_total REAL    NOT NULL DEFAULT 0," +
            "    grand_total    REAL    NOT NULL DEFAULT 0," +
            "    status         TEXT    NOT NULL DEFAULT 'UNPAID'," +
            "    notes          TEXT," +
            "    FOREIGN KEY (customer_id) REFERENCES customers(customer_id)," +
            "    FOREIGN KEY (created_by)  REFERENCES users(user_id)" +
            ")"
        );

        // ── Receipt services (line items) ─────────────────────────────
        stmt.execute(
            "CREATE TABLE IF NOT EXISTS receipt_services (" +
            "    id            INTEGER PRIMARY KEY AUTOINCREMENT," +
            "    receipt_id    INTEGER NOT NULL," +
            "    service_id    INTEGER NOT NULL," +
            "    price_at_time REAL    NOT NULL," +
            "    FOREIGN KEY (receipt_id) REFERENCES receipts(receipt_id) ON DELETE CASCADE," +
            "    FOREIGN KEY (service_id) REFERENCES services(service_id)" +
            ")"
        );

        // ── Receipt products (line items) ─────────────────────────────
        stmt.execute(
            "CREATE TABLE IF NOT EXISTS receipt_products (" +
            "    id                 INTEGER PRIMARY KEY AUTOINCREMENT," +
            "    receipt_id         INTEGER NOT NULL," +
            "    product_id         INTEGER NOT NULL," +
            "    quantity           REAL    NOT NULL DEFAULT 1," +
            "    unit_price_at_time REAL    NOT NULL," +
            "    total_price        REAL    NOT NULL," +
            "    FOREIGN KEY (receipt_id) REFERENCES receipts(receipt_id) ON DELETE CASCADE," +
            "    FOREIGN KEY (product_id) REFERENCES products(product_id)" +
            ")"
        );

        // ── Payments ──────────────────────────────────────────────────
        stmt.execute(
            "CREATE TABLE IF NOT EXISTS payments (" +
            "    payment_id   INTEGER PRIMARY KEY AUTOINCREMENT," +
            "    receipt_id   INTEGER NOT NULL," +
            "    payment_date TEXT    NOT NULL DEFAULT CURRENT_TIMESTAMP," +
            "    amount       REAL    NOT NULL," +
            "    FOREIGN KEY (receipt_id) REFERENCES receipts(receipt_id) ON DELETE CASCADE" +
            ")"
        );
    }

    // ── Products table migration ───────────────────────────────────────────

    /**
     * Safely adds new columns to the products table if they don't exist yet.
     * This ensures backward compatibility with existing databases.
     */
    private static void migrateProductsTable(Statement stmt) throws SQLException {
        String[] migrations = {
            "ALTER TABLE products ADD COLUMN product_type TEXT NOT NULL DEFAULT 'GALLON'",
            "ALTER TABLE products ADD COLUMN units_per_carton INTEGER NOT NULL DEFAULT 1",
            "ALTER TABLE products ADD COLUMN stock_quantity REAL NOT NULL DEFAULT 0",
            "ALTER TABLE products ADD COLUMN sold_quantity REAL NOT NULL DEFAULT 0"
        };
        for (String sql : migrations) {
            try {
                stmt.execute(sql);
                System.out.println("[DB] Migration applied: " + sql.substring(0, 50));
            } catch (java.sql.SQLException ignored) {
                // Column already exists – safe to ignore
            }
        }
    }

    // ── Index definitions ─────────────────────────────────────────────────

    private static void createIndexes(Statement stmt) throws SQLException {
        stmt.execute("CREATE INDEX IF NOT EXISTS idx_users_username    ON users(username)");
        stmt.execute("CREATE INDEX IF NOT EXISTS idx_receipts_customer ON receipts(customer_id)");
        stmt.execute("CREATE INDEX IF NOT EXISTS idx_receipts_user     ON receipts(created_by)");
        stmt.execute("CREATE INDEX IF NOT EXISTS idx_receipt_services  ON receipt_services(receipt_id)");
        stmt.execute("CREATE INDEX IF NOT EXISTS idx_receipt_products  ON receipt_products(receipt_id)");
        stmt.execute("CREATE INDEX IF NOT EXISTS idx_payments_receipt  ON payments(receipt_id)");
    }

    // ── Default seed data ─────────────────────────────────────────────────

    /**
     * Seeds default data on first run:
     * - Admin user (admin/admin)
     * - Guest customer for unknown customers
     */
    private static void seedDefaultData(Connection conn) throws SQLException {
        // Create default admin user
        ResultSet rs = conn.createStatement().executeQuery("SELECT COUNT(*) FROM users");
        if (rs.next() && rs.getInt(1) == 0) {
            conn.createStatement().execute(
                "INSERT INTO users (username, password, is_active) VALUES ('admin', 'admin', 1)"
            );
            System.out.println("[DB] Default admin user created (username: admin, password: admin)");
        }

        // Create default "Guest" customer for unknown/new customers
        rs = conn.createStatement().executeQuery("SELECT COUNT(*) FROM customers");
        if (rs.next() && rs.getInt(1) == 0) {
            conn.createStatement().execute(
                "INSERT INTO customers (name, phone, car_plate_number) VALUES ('Guest / Unknown', '', '')"
            );
            System.out.println("[DB] Default guest customer created (ID=1)");
        }
    }
}

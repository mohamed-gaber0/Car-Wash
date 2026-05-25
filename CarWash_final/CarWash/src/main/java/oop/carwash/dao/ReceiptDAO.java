
package oop.carwash.dao;


import oop.carwash.model.Receipt;
import oop.carwash.model.ReceiptItem;
import oop.carwash.model.ReceiptService;
import oop.carwash.model.ReceiptProduct;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/**
 * DAO for the receipts table and associated line items.
 *
 * Complex operations:
 *   - save(receipt) saves the receipt + all its ReceiptService/ReceiptProduct items
 *   - findById loads the receipt and joins to fetch all line items
 *     (polymorphically reconstructed as ReceiptService or ReceiptProduct objects)
 */
public class ReceiptDAO extends GenericDAO<Receipt> {

    @Override
    protected Receipt mapRow(ResultSet rs) throws SQLException {
        return new Receipt(
            rs.getInt("receipt_id"),
            rs.getInt("customer_id"),
            rs.getInt("created_by"),
            rs.getString("receipt_date"),
            rs.getDouble("services_total"),
            rs.getDouble("products_total"),
            rs.getDouble("grand_total"),
            rs.getString("status"),
            rs.getString("notes")
        );
    }

    @Override
    public List<Receipt> findAll() {
        List<Receipt> receipts = new ArrayList<>();
        String sql = "SELECT * FROM receipts ORDER BY receipt_id DESC";
        try (Connection conn = DatabaseConnection.getInstance().getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {

            while (rs.next()) {
                Receipt receipt = mapRow(rs);
                loadReceiptItems(conn, receipt);
                receipts.add(receipt);
            }
        } catch (SQLException e) {
            System.err.println("[ReceiptDAO] Error in findAll: " + e.getMessage());
        }
        return receipts;
    }

    @Override
    public Optional<Receipt> findById(int id) {
        String sql = "SELECT * FROM receipts WHERE receipt_id = ?";
        try (Connection conn = DatabaseConnection.getInstance().getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, id);
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    Receipt receipt = mapRow(rs);
                    loadReceiptItems(conn, receipt);
                    return Optional.of(receipt);
                }
            }
        } catch (SQLException e) {
            System.err.println("[ReceiptDAO] Error in findById: " + e.getMessage());
        }
        return Optional.empty();
    }

    /**
     * Saves a receipt and all its line items in a single transaction.
     * All items (ReceiptService or ReceiptProduct) are polymorphically saved.
     */
    @Override
    public void save(Receipt receipt) {
        String sql = "INSERT INTO receipts (customer_id, created_by, services_total, products_total, grand_total, status, notes) " +
                     "VALUES (?, ?, ?, ?, ?, ?, ?)";

        try (Connection conn = DatabaseConnection.getInstance().getConnection()) {
            conn.setAutoCommit(false);  // Start transaction

            try (PreparedStatement stmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
                stmt.setInt(1, receipt.getCustomerId());
                stmt.setInt(2, receipt.getCreatedBy());
                stmt.setDouble(3, receipt.getServicesTotal());
                stmt.setDouble(4, receipt.getProductsTotal());
                stmt.setDouble(5, receipt.getGrandTotal());
                stmt.setString(6, receipt.getStatus());
                stmt.setString(7, receipt.getNotes());
                stmt.executeUpdate();

                try (ResultSet keys = stmt.getGeneratedKeys()) {
                    if (keys.next()) {
                        receipt.setReceiptId(keys.getInt(1));
                    }
                }
            }

            // Save all line items (polymorphically handle ReceiptService vs ReceiptProduct)
            for (ReceiptItem item : receipt.getItems()) {
                item.setReceiptId(receipt.getReceiptId());
                saveReceiptItem(conn, item);
            }

            conn.commit();
            conn.setAutoCommit(true);

        } catch (SQLException e) {
            System.err.println("[ReceiptDAO] Error in save: " + e.getMessage());
        }
    }

    /**
     * Polymorphic save: check instanceof and insert into the appropriate line item table.
     */
    private void saveReceiptItem(Connection conn, ReceiptItem item) throws SQLException {
        if (item instanceof ReceiptService) {
            ReceiptService rs = (ReceiptService) item;
            String sql = "INSERT INTO receipt_services (receipt_id, service_id, price_at_time) VALUES (?, ?, ?)";
            try (PreparedStatement stmt = conn.prepareStatement(sql)) {
                stmt.setInt(1, rs.getReceiptId());
                stmt.setInt(2, rs.getServiceId());
                stmt.setDouble(3, rs.getPriceAtTime());
                stmt.executeUpdate();
            }
        } else if (item instanceof ReceiptProduct) {
            ReceiptProduct rp = (ReceiptProduct) item;
            String sql = "INSERT INTO receipt_products (receipt_id, product_id, quantity, unit_price_at_time, total_price) " +
                         "VALUES (?, ?, ?, ?, ?)";
            try (PreparedStatement stmt = conn.prepareStatement(sql)) {
                stmt.setInt(1, rp.getReceiptId());
                stmt.setInt(2, rp.getProductId());
                stmt.setDouble(3, rp.getQuantity());
                stmt.setDouble(4, rp.getUnitPriceAtTime());
                stmt.setDouble(5, rp.getTotalPrice());
                stmt.executeUpdate();
            }
        }
    }

    @Override
    public void update(Receipt receipt) {
        String sql = "UPDATE receipts SET customer_id = ?, created_by = ?, services_total = ?, " +
                     "products_total = ?, grand_total = ?, status = ?, notes = ? WHERE receipt_id = ?";
        try (Connection conn = DatabaseConnection.getInstance().getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, receipt.getCustomerId());
            stmt.setInt(2, receipt.getCreatedBy());
            stmt.setDouble(3, receipt.getServicesTotal());
            stmt.setDouble(4, receipt.getProductsTotal());
            stmt.setDouble(5, receipt.getGrandTotal());
            stmt.setString(6, receipt.getStatus());
            stmt.setString(7, receipt.getNotes());
            stmt.setInt(8, receipt.getReceiptId());
            stmt.executeUpdate();

        } catch (SQLException e) {
            System.err.println("[ReceiptDAO] Error in update: " + e.getMessage());
        }
    }

    @Override
    public void delete(int id) {
        String sql = "DELETE FROM receipts WHERE receipt_id = ?";
        try (Connection conn = DatabaseConnection.getInstance().getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, id);
            stmt.executeUpdate();
            // ON DELETE CASCADE handles line items automatically

        } catch (SQLException e) {
            System.err.println("[ReceiptDAO] Error in delete: " + e.getMessage());
        }
    }

    // ── Helper: load all line items for a receipt ────────────────────────

    /**
     * Loads all ReceiptService and ReceiptProduct items for a receipt.
     * Reconstructs them polymorphically as ReceiptItem objects.
     */
    private void loadReceiptItems(Connection conn, Receipt receipt) throws SQLException {
        loadReceiptServices(conn, receipt);
        loadReceiptProducts(conn, receipt);
    }

    private void loadReceiptServices(Connection conn, Receipt receipt) throws SQLException {
        String sql = "SELECT rs.id, rs.receipt_id, rs.service_id, rs.price_at_time, s.service_name " +
                     "FROM receipt_services rs " +
                     "LEFT JOIN services s ON rs.service_id = s.service_id " +
                     "WHERE rs.receipt_id = ?";
        try (PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, receipt.getReceiptId());
            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    String serviceName = rs.getString("service_name");
                    if (serviceName == null) serviceName = "خدمة غير معروفة";
                    ReceiptService item = new ReceiptService(
                        rs.getInt("id"),
                        rs.getInt("receipt_id"),
                        rs.getInt("service_id"),
                        serviceName,
                        rs.getDouble("price_at_time")
                    );
                    receipt.getItems().add(item);
                }
            }
        }
    }

    private void loadReceiptProducts(Connection conn, Receipt receipt) throws SQLException {
        String sql = "SELECT rp.id, rp.receipt_id, rp.product_id, rp.quantity, rp.unit_price_at_time, rp.total_price, p.product_name " +
                     "FROM receipt_products rp " +
                     "LEFT JOIN products p ON rp.product_id = p.product_id " +
                     "WHERE rp.receipt_id = ?";
        try (PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, receipt.getReceiptId());
            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    String productName = rs.getString("product_name");
                    if (productName == null) productName = "منتج غير معروف";
                    ReceiptProduct item = new ReceiptProduct(
                        rs.getInt("id"),
                        rs.getInt("receipt_id"),
                        rs.getInt("product_id"),
                        productName,
                        rs.getDouble("quantity"),
                        rs.getDouble("unit_price_at_time"),
                        rs.getDouble("total_price")
                    );
                    receipt.getItems().add(item);
                }
            }
        }
    }
}

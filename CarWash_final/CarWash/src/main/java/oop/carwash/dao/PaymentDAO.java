
package oop.carwash.dao;


import oop.carwash.model.Payment;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/**
 * DAO for the payments table.
 */
public class PaymentDAO extends GenericDAO<Payment> {

    @Override
    protected Payment mapRow(ResultSet rs) throws SQLException {
        return new Payment(
            rs.getInt("payment_id"),
            rs.getInt("receipt_id"),
            rs.getString("payment_date"),
            rs.getDouble("amount")
        );
    }

    @Override
    public List<Payment> findAll() {
        List<Payment> payments = new ArrayList<>();
        String sql = "SELECT * FROM payments ORDER BY payment_id DESC";
        try (Connection conn = DatabaseConnection.getInstance().getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {

            while (rs.next()) {
                payments.add(mapRow(rs));
            }
        } catch (SQLException e) {
            System.err.println("[PaymentDAO] Error in findAll: " + e.getMessage());
        }
        return payments;
    }

    @Override
    public Optional<Payment> findById(int id) {
        String sql = "SELECT * FROM payments WHERE payment_id = ?";
        try (Connection conn = DatabaseConnection.getInstance().getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, id);
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return Optional.of(mapRow(rs));
                }
            }
        } catch (SQLException e) {
            System.err.println("[PaymentDAO] Error in findById: " + e.getMessage());
        }
        return Optional.empty();
    }

    /**
     * Find all payments for a specific receipt.
     */
    public List<Payment> findByReceiptId(int receiptId) {
        List<Payment> payments = new ArrayList<>();
        String sql = "SELECT * FROM payments WHERE receipt_id = ? ORDER BY payment_date";
        try (Connection conn = DatabaseConnection.getInstance().getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, receiptId);
            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    payments.add(mapRow(rs));
                }
            }
        } catch (SQLException e) {
            System.err.println("[PaymentDAO] Error in findByReceiptId: " + e.getMessage());
        }
        return payments;
    }

    /**
     * Calculate total amount paid for a receipt.
     */
    public double getTotalPaidForReceipt(int receiptId) {
        String sql = "SELECT COALESCE(SUM(amount), 0) as total FROM payments WHERE receipt_id = ?";
        try (Connection conn = DatabaseConnection.getInstance().getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, receiptId);
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return rs.getDouble("total");
                }
            }
        } catch (SQLException e) {
            System.err.println("[PaymentDAO] Error in getTotalPaidForReceipt: " + e.getMessage());
        }
        return 0.0;
    }

    @Override
    public void save(Payment payment) {
        String sql = "INSERT INTO payments (receipt_id, amount) VALUES (?, ?)";
        try (Connection conn = DatabaseConnection.getInstance().getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {

            stmt.setInt(1, payment.getReceiptId());
            stmt.setDouble(2, payment.getAmount());
            stmt.executeUpdate();

            try (ResultSet keys = stmt.getGeneratedKeys()) {
                if (keys.next()) {
                    payment.setPaymentId(keys.getInt(1));
                }
            }
        } catch (SQLException e) {
            System.err.println("[PaymentDAO] Error in save: " + e.getMessage());
        }
    }

    @Override
    public void update(Payment payment) {
        String sql = "UPDATE payments SET receipt_id = ?, amount = ? WHERE payment_id = ?";
        try (Connection conn = DatabaseConnection.getInstance().getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, payment.getReceiptId());
            stmt.setDouble(2, payment.getAmount());
            stmt.setInt(3, payment.getPaymentId());
            stmt.executeUpdate();

        } catch (SQLException e) {
            System.err.println("[PaymentDAO] Error in update: " + e.getMessage());
        }
    }

    @Override
    public void delete(int id) {
        String sql = "DELETE FROM payments WHERE payment_id = ?";
        try (Connection conn = DatabaseConnection.getInstance().getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, id);
            stmt.executeUpdate();

        } catch (SQLException e) {
            System.err.println("[PaymentDAO] Error in delete: " + e.getMessage());
        }
    }
}

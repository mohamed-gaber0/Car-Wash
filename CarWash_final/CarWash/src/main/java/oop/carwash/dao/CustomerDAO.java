
package oop.carwash.dao;


import oop.carwash.model.Customer;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/**
 * DAO for the customers table.
 */
public class CustomerDAO extends GenericDAO<Customer> {

    @Override
    protected Customer mapRow(ResultSet rs) throws SQLException {
        return new Customer(
            rs.getInt("customer_id"),
            rs.getString("name"),
            rs.getString("phone"),
            rs.getString("car_plate_number"),
            rs.getString("created_at")
        );
    }

    @Override
    public List<Customer> findAll() {
        List<Customer> customers = new ArrayList<>();
        String sql = "SELECT * FROM customers ORDER BY customer_id";
        try (Connection conn = DatabaseConnection.getInstance().getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {

            while (rs.next()) {
                customers.add(mapRow(rs));
            }
        } catch (SQLException e) {
            System.err.println("[CustomerDAO] Error in findAll: " + e.getMessage());
        }
        return customers;
    }

    @Override
    public Optional<Customer> findById(int id) {
        String sql = "SELECT * FROM customers WHERE customer_id = ?";
        try (Connection conn = DatabaseConnection.getInstance().getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, id);
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return Optional.of(mapRow(rs));
                }
            }
        } catch (SQLException e) {
            System.err.println("[CustomerDAO] Error in findById: " + e.getMessage());
        }
        return Optional.empty();
    }

    @Override
    public void save(Customer customer) {
        String sql = "INSERT INTO customers (name, phone, car_plate_number) VALUES (?, ?, ?)";
        try (Connection conn = DatabaseConnection.getInstance().getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {

            stmt.setString(1, customer.getName());
            stmt.setString(2, customer.getPhone());
            stmt.setString(3, customer.getCarPlateNumber());
            stmt.executeUpdate();

            try (ResultSet keys = stmt.getGeneratedKeys()) {
                if (keys.next()) {
                    customer.setCustomerId(keys.getInt(1));
                }
            }
        } catch (SQLException e) {
            System.err.println("[CustomerDAO] Error in save: " + e.getMessage());
        }
    }

    @Override
    public void update(Customer customer) {
        String sql = "UPDATE customers SET name = ?, phone = ?, car_plate_number = ? WHERE customer_id = ?";
        try (Connection conn = DatabaseConnection.getInstance().getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, customer.getName());
            stmt.setString(2, customer.getPhone());
            stmt.setString(3, customer.getCarPlateNumber());
            stmt.setInt(4, customer.getCustomerId());
            stmt.executeUpdate();

        } catch (SQLException e) {
            System.err.println("[CustomerDAO] Error in update: " + e.getMessage());
        }
    }

    @Override
    public void delete(int id) {
        String sql = "DELETE FROM customers WHERE customer_id = ?";
        try (Connection conn = DatabaseConnection.getInstance().getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, id);
            stmt.executeUpdate();

        } catch (SQLException e) {
            System.err.println("[CustomerDAO] Error in delete: " + e.getMessage());
        }
    }

    /**
     * Returns the total outstanding debt for a customer:
     * sum of grand_total of UNPAID receipts minus any partial payments made.
     */
    public double getTotalDebt(int customerId) {
        // Sum of grand_total of unpaid receipts minus payments already made
        String sql = "SELECT COALESCE(SUM(r.grand_total), 0) - COALESCE((" +
                     "  SELECT SUM(p.amount) FROM payments p " +
                     "  JOIN receipts r2 ON p.receipt_id = r2.receipt_id " +
                     "  WHERE r2.customer_id = ? AND r2.status = 'UNPAID'" +
                     "), 0) AS debt " +
                     "FROM receipts r WHERE r.customer_id = ? AND r.status = 'UNPAID'";
        try (Connection conn = DatabaseConnection.getInstance().getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, customerId);
            stmt.setInt(2, customerId);
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return Math.max(0, rs.getDouble("debt"));
                }
            }
        } catch (SQLException e) {
            System.err.println("[CustomerDAO] Error in getTotalDebt: " + e.getMessage());
        }
        return 0.0;
    }
}
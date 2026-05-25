
package oop.carwash.dao;


import oop.carwash.model.Service;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/**
 * DAO for the services table.
 */
public class ServiceDAO extends GenericDAO<Service> {

    @Override
    protected Service mapRow(ResultSet rs) throws SQLException {
        return new Service(
            rs.getInt("service_id"),
            rs.getString("service_name"),
            rs.getDouble("price")
        );
    }

    @Override
    public List<Service> findAll() {
        List<Service> services = new ArrayList<>();
        String sql = "SELECT * FROM services ORDER BY service_id";
        try (Connection conn = DatabaseConnection.getInstance().getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {

            while (rs.next()) {
                services.add(mapRow(rs));
            }
        } catch (SQLException e) {
            System.err.println("[ServiceDAO] Error in findAll: " + e.getMessage());
        }
        return services;
    }

    @Override
    public Optional<Service> findById(int id) {
        String sql = "SELECT * FROM services WHERE service_id = ?";
        try (Connection conn = DatabaseConnection.getInstance().getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, id);
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return Optional.of(mapRow(rs));
                }
            }
        } catch (SQLException e) {
            System.err.println("[ServiceDAO] Error in findById: " + e.getMessage());
        }
        return Optional.empty();
    }

    @Override
    public void save(Service service) {
        String sql = "INSERT INTO services (service_name, price) VALUES (?, ?)";
        try (Connection conn = DatabaseConnection.getInstance().getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {

            stmt.setString(1, service.getServiceName());
            stmt.setDouble(2, service.getPrice());
            stmt.executeUpdate();

            try (ResultSet keys = stmt.getGeneratedKeys()) {
                if (keys.next()) {
                    service.setServiceId(keys.getInt(1));
                }
            }
        } catch (SQLException e) {
            System.err.println("[ServiceDAO] Error in save: " + e.getMessage());
        }
    }

    @Override
    public void update(Service service) {
        String sql = "UPDATE services SET service_name = ?, price = ? WHERE service_id = ?";
        try (Connection conn = DatabaseConnection.getInstance().getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, service.getServiceName());
            stmt.setDouble(2, service.getPrice());
            stmt.setInt(3, service.getServiceId());
            stmt.executeUpdate();

        } catch (SQLException e) {
            System.err.println("[ServiceDAO] Error in update: " + e.getMessage());
        }
    }

    @Override
    public void delete(int id) {
        String sql = "DELETE FROM services WHERE service_id = ?";
        try (Connection conn = DatabaseConnection.getInstance().getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, id);
            stmt.executeUpdate();

        } catch (SQLException e) {
            System.err.println("[ServiceDAO] Error in delete: " + e.getMessage());
        }
    }
}

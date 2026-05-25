
package oop.carwash.dao;


import oop.carwash.model.Product;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/**
 * DAO for the products table.
 * Supports GALLON (sold by liters, float qty) and CARTON (sold by pieces, int qty).
 */
public class ProductDAO extends GenericDAO<Product> {

    @Override
    protected Product mapRow(ResultSet rs) throws SQLException {
        return new Product(
            rs.getInt("product_id"),
            rs.getString("product_name"),
            rs.getDouble("unit_price"),
            rs.getString("product_type"),
            rs.getInt("units_per_carton"),
            rs.getDouble("stock_quantity"),
            rs.getDouble("sold_quantity")
        );
    }

    @Override
    public List<Product> findAll() {
        List<Product> products = new ArrayList<>();
        String sql = "SELECT * FROM products ORDER BY product_id";
        try (Connection conn = DatabaseConnection.getInstance().getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {

            while (rs.next()) {
                products.add(mapRow(rs));
            }
        } catch (SQLException e) {
            System.err.println("[ProductDAO] Error in findAll: " + e.getMessage());
        }
        return products;
    }

    @Override
    public Optional<Product> findById(int id) {
        String sql = "SELECT * FROM products WHERE product_id = ?";
        try (Connection conn = DatabaseConnection.getInstance().getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, id);
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return Optional.of(mapRow(rs));
                }
            }
        } catch (SQLException e) {
            System.err.println("[ProductDAO] Error in findById: " + e.getMessage());
        }
        return Optional.empty();
    }

    @Override
    public void save(Product product) {
        String sql = "INSERT INTO products (product_name, unit_price, product_type, units_per_carton, stock_quantity, sold_quantity) VALUES (?, ?, ?, ?, ?, ?)";
        try (Connection conn = DatabaseConnection.getInstance().getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {

            stmt.setString(1, product.getProductName());
            stmt.setDouble(2, product.getUnitPrice());
            stmt.setString(3, product.getProductType());
            stmt.setInt(4, product.getUnitsPerCarton());
            stmt.setDouble(5, product.getStockQuantity());
            stmt.setDouble(6, product.getSoldQuantity());
            stmt.executeUpdate();

            try (ResultSet keys = stmt.getGeneratedKeys()) {
                if (keys.next()) {
                    product.setProductId(keys.getInt(1));
                }
            }
        } catch (SQLException e) {
            System.err.println("[ProductDAO] Error in save: " + e.getMessage());
        }
    }

    @Override
    public void update(Product product) {
        String sql = "UPDATE products SET product_name = ?, unit_price = ?, product_type = ?, units_per_carton = ?, stock_quantity = ?, sold_quantity = ? WHERE product_id = ?";
        try (Connection conn = DatabaseConnection.getInstance().getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, product.getProductName());
            stmt.setDouble(2, product.getUnitPrice());
            stmt.setString(3, product.getProductType());
            stmt.setInt(4, product.getUnitsPerCarton());
            stmt.setDouble(5, product.getStockQuantity());
            stmt.setDouble(6, product.getSoldQuantity());
            stmt.setInt(7, product.getProductId());
            stmt.executeUpdate();

        } catch (SQLException e) {
            System.err.println("[ProductDAO] Error in update: " + e.getMessage());
        }
    }

    /**
     * Deducts sold quantity from stock and adds to sold_quantity.
     * Called when a receipt is saved containing this product.
     */
    public void recordSale(int productId, double qtySold) {
        String sql = "UPDATE products SET sold_quantity = sold_quantity + ?, stock_quantity = stock_quantity - ? WHERE product_id = ?";
        try (Connection conn = DatabaseConnection.getInstance().getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setDouble(1, qtySold);
            stmt.setDouble(2, qtySold);
            stmt.setInt(3, productId);
            stmt.executeUpdate();
        } catch (SQLException e) {
            System.err.println("[ProductDAO] Error in recordSale: " + e.getMessage());
        }
    }

    @Override
    public void delete(int id) {
        String sql = "DELETE FROM products WHERE product_id = ?";
        try (Connection conn = DatabaseConnection.getInstance().getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, id);
            stmt.executeUpdate();

        } catch (SQLException e) {
            System.err.println("[ProductDAO] Error in delete: " + e.getMessage());
            throw new RuntimeException("Cannot delete product. It may be linked to existing receipts.", e);
        }
    }
}

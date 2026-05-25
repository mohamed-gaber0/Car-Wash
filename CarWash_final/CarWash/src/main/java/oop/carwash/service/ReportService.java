
package oop.carwash.service;

import oop.carwash.dao.DatabaseConnection;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * Aggregates data from the database for reporting and dashboard display.
 *
 * Methods return simple DTOs (maps or lists) suitable for feeding into
 * charts and dashboard cards. All queries are read-only.
 *
 * This is where the FXGL chart will pull its data from.
 */
public class ReportService {

    private ReportService() {
        // Utility class
    }

    // ── Dashboard cards ────────────────────────────────────────────────────

    /**
     * Returns today's total revenue (sum of grand_total for receipts created today).
     */
    public static double getTodayRevenue() {
        String sql = "SELECT COALESCE(SUM(grand_total), 0) as total FROM receipts " +
                     "WHERE DATE(receipt_date) = DATE('now')";
        return getDoubleResult(sql);
    }

    /**
     * Returns the count of unpaid receipts.
     */
    public static int getUnpaidReceiptCount() {
        String sql = "SELECT COUNT(*) as count FROM receipts WHERE status = 'UNPAID'";
        return getIntResult(sql);
    }

    /**
     * Returns the count of receipts created today.
     */
    public static int getTodayReceiptCount() {
        String sql = "SELECT COUNT(*) as count FROM receipts WHERE DATE(receipt_date) = DATE('now')";
        return getIntResult(sql);
    }

    /**
     * Returns all-time total revenue.
     */
    public static double getTotalRevenue() {
        String sql = "SELECT COALESCE(SUM(grand_total), 0) as total FROM receipts";
        return getDoubleResult(sql);
    }

    // ── Chart data (FXGL) ──────────────────────────────────────────────────

    /**
     * Returns revenue grouped by date in the given range.
     * Used for line/bar chart: dates on X-axis, revenue on Y-axis.
     *
     * @param startDate format: YYYY-MM-DD
     * @param endDate   format: YYYY-MM-DD
     * @return LinkedHashMap<date, revenue> ordered by date
     */
    public static Map<String, Double> getRevenueByDate(String startDate, String endDate) {
        Map<String, Double> result = new LinkedHashMap<>();
        String sql = "SELECT DATE(receipt_date) as date, SUM(grand_total) as revenue " +
                     "FROM receipts " +
                     "WHERE DATE(receipt_date) >= ? AND DATE(receipt_date) <= ? " +
                     "GROUP BY DATE(receipt_date) " +
                     "ORDER BY date";

        try (Connection conn = DatabaseConnection.getInstance().getConnection();
             var stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, startDate);
            stmt.setString(2, endDate);

            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    String date = rs.getString("date");
                    double revenue = rs.getDouble("revenue");
                    result.put(date, revenue);
                }
            }
        } catch (SQLException e) {
            System.err.println("[Report] Error in getRevenueByDate: " + e.getMessage());
        }

        return result;
    }

    /**
     * Returns the top N services by total revenue.
     * Used for pie or bar chart.
     *
     * @param limit e.g., 5 for top 5
     * @return LinkedHashMap<serviceName, totalRevenue> ordered by revenue desc
     */
    public static Map<String, Double> getTopServices(int limit) {
        Map<String, Double> result = new LinkedHashMap<>();
        String sql = "SELECT s.service_name, SUM(rs.price_at_time) as total " +
                     "FROM receipt_services rs " +
                     "JOIN services s ON rs.service_id = s.service_id " +
                     "GROUP BY s.service_name " +
                     "ORDER BY total DESC " +
                     "LIMIT ?";

        try (Connection conn = DatabaseConnection.getInstance().getConnection();
             var stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, limit);

            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    String serviceName = rs.getString("service_name");
                    double total = rs.getDouble("total");
                    result.put(serviceName, total);
                }
            }
        } catch (SQLException e) {
            System.err.println("[Report] Error in getTopServices: " + e.getMessage());
        }

        return result;
    }

    /**
     * Returns transaction count per day (useful for another chart).
     */
    public static Map<String, Integer> getTransactionCountByDate(String startDate, String endDate) {
        Map<String, Integer> result = new LinkedHashMap<>();
        String sql = "SELECT DATE(receipt_date) as date, COUNT(*) as count " +
                     "FROM receipts " +
                     "WHERE DATE(receipt_date) >= ? AND DATE(receipt_date) <= ? " +
                     "GROUP BY DATE(receipt_date) " +
                     "ORDER BY date";

        try (Connection conn = DatabaseConnection.getInstance().getConnection();
             var stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, startDate);
            stmt.setString(2, endDate);

            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    String date = rs.getString("date");
                    int count = rs.getInt("count");
                    result.put(date, count);
                }
            }
        } catch (SQLException e) {
            System.err.println("[Report] Error in getTransactionCountByDate: " + e.getMessage());
        }

        return result;
    }

    /**
     * Returns a summary object with key metrics.
     * Used by socket server to send a snapshot to clients.
     */
    public static Map<String, Object> getSummary() {
        Map<String, Object> summary = new LinkedHashMap<>();
        summary.put("timestamp", System.currentTimeMillis());
        summary.put("todayRevenue", getTodayRevenue());
        summary.put("todayReceiptCount", getTodayReceiptCount());
        summary.put("unpaidCount", getUnpaidReceiptCount());
        summary.put("totalRevenue", getTotalRevenue());
        return summary;
    }

    // ── Sales report: products sold per period ─────────────────────────────

    /**
     * DTO for a product sales summary row.
     */
    public static class ProductSalesSummary {
        public final int productId;
        public final String productName;
        public final String productType;   // "GALLON" or "CARTON"
        public final double totalQtySold;  // Liters or pieces
        public final double totalRevenue;
        public final String unitLabel;

        public ProductSalesSummary(int productId, String productName, String productType,
                                   double totalQtySold, double totalRevenue) {
            this.productId = productId;
            this.productName = productName;
            this.productType = productType;
            this.totalQtySold = totalQtySold;
            this.totalRevenue = totalRevenue;
            this.unitLabel = "GALLON".equals(productType) ? "لتر" : "قطعة";
        }
    }

    /**
     * Returns per-product sales summary for a date range.
     * Includes how many units (liters/pieces) were sold and total revenue.
     *
     * @param startDate format: YYYY-MM-DD
     * @param endDate   format: YYYY-MM-DD
     * @return list ordered by total revenue desc
     */
    public static java.util.List<ProductSalesSummary> getProductSalesReport(String startDate, String endDate) {
        java.util.List<ProductSalesSummary> result = new java.util.ArrayList<>();
        String sql = "SELECT p.product_id, p.product_name, p.product_type, " +
                     "       COALESCE(SUM(rp.quantity), 0) AS total_qty, " +
                     "       COALESCE(SUM(rp.total_price), 0) AS total_revenue " +
                     "FROM products p " +
                     "INNER JOIN receipt_products rp ON p.product_id = rp.product_id " +
                     "INNER JOIN receipts r ON rp.receipt_id = r.receipt_id " +
                     "WHERE DATE(r.receipt_date) >= ? AND DATE(r.receipt_date) <= ? " +
                     "GROUP BY p.product_id, p.product_name, p.product_type " +
                     "ORDER BY total_revenue DESC";

        try (Connection conn = DatabaseConnection.getInstance().getConnection();
             var stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, startDate);
            stmt.setString(2, endDate);
            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    result.add(new ProductSalesSummary(
                        rs.getInt("product_id"),
                        rs.getString("product_name"),
                        rs.getString("product_type"),
                        rs.getDouble("total_qty"),
                        rs.getDouble("total_revenue")
                    ));
                }
            }
        } catch (SQLException e) {
            System.err.println("[Report] Error in getProductSalesReport: " + e.getMessage());
        }
        return result;
    }

    /**
     * Returns the grand total revenue for all products in the given date range.
     */
    public static double getProductsGrandTotal(String startDate, String endDate) {
        String sql = "SELECT COALESCE(SUM(rp.total_price), 0) AS total " +
                     "FROM receipt_products rp " +
                     "JOIN receipts r ON rp.receipt_id = r.receipt_id " +
                     "WHERE DATE(r.receipt_date) >= ? AND DATE(r.receipt_date) <= ?";
        try (Connection conn = DatabaseConnection.getInstance().getConnection();
             var stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, startDate);
            stmt.setString(2, endDate);
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) return rs.getDouble("total");
            }
        } catch (SQLException e) {
            System.err.println("[Report] Error in getProductsGrandTotal: " + e.getMessage());
        }
        return 0.0;
    }

    // ── Sales report: services sold per period ─────────────────────────────

    /**
     * DTO for a service sales summary row.
     */
    public static class ServiceSalesSummary {
        public final int serviceId;
        public final String serviceName;
        public final int totalCount;       // How many times this service was sold
        public final double totalRevenue;

        public ServiceSalesSummary(int serviceId, String serviceName,
                                   int totalCount, double totalRevenue) {
            this.serviceId = serviceId;
            this.serviceName = serviceName;
            this.totalCount = totalCount;
            this.totalRevenue = totalRevenue;
        }
    }

    /**
     * Returns per-service sales summary for a date range.
     * Only includes services that were actually sold in the period.
     *
     * @param startDate format: YYYY-MM-DD
     * @param endDate   format: YYYY-MM-DD
     * @return list ordered by total revenue desc
     */
    public static java.util.List<ServiceSalesSummary> getServiceSalesReport(String startDate, String endDate) {
        java.util.List<ServiceSalesSummary> result = new java.util.ArrayList<>();
        String sql = "SELECT s.service_id, s.service_name, " +
                     "       COUNT(rs.id) AS total_count, " +
                     "       COALESCE(SUM(rs.price_at_time), 0) AS total_revenue " +
                     "FROM services s " +
                     "INNER JOIN receipt_services rs ON s.service_id = rs.service_id " +
                     "INNER JOIN receipts r ON rs.receipt_id = r.receipt_id " +
                     "WHERE DATE(r.receipt_date) >= ? AND DATE(r.receipt_date) <= ? " +
                     "GROUP BY s.service_id, s.service_name " +
                     "ORDER BY total_revenue DESC";

        try (Connection conn = DatabaseConnection.getInstance().getConnection();
             var stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, startDate);
            stmt.setString(2, endDate);
            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    result.add(new ServiceSalesSummary(
                        rs.getInt("service_id"),
                        rs.getString("service_name"),
                        rs.getInt("total_count"),
                        rs.getDouble("total_revenue")
                    ));
                }
            }
        } catch (SQLException e) {
            System.err.println("[Report] Error in getServiceSalesReport: " + e.getMessage());
        }
        return result;
    }

    /**
     * Returns total revenue for all services in the given date range.
     */
    public static double getServicesGrandTotal(String startDate, String endDate) {
        String sql = "SELECT COALESCE(SUM(rs.price_at_time), 0) AS total " +
                     "FROM receipt_services rs " +
                     "JOIN receipts r ON rs.receipt_id = r.receipt_id " +
                     "WHERE DATE(r.receipt_date) >= ? AND DATE(r.receipt_date) <= ?";
        try (Connection conn = DatabaseConnection.getInstance().getConnection();
             var stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, startDate);
            stmt.setString(2, endDate);
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) return rs.getDouble("total");
            }
        } catch (SQLException e) {
            System.err.println("[Report] Error in getServicesGrandTotal: " + e.getMessage());
        }
        return 0.0;
    }



    private static double getDoubleResult(String sql) {
        try (Connection conn = DatabaseConnection.getInstance().getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            if (rs.next()) {
                return rs.getDouble(1);
            }
        } catch (SQLException e) {
            System.err.println("[Report] SQL error: " + e.getMessage());
        }
        return 0.0;
    }

    private static int getIntResult(String sql) {
        try (Connection conn = DatabaseConnection.getInstance().getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            if (rs.next()) {
                return rs.getInt(1);
            }
        } catch (SQLException e) {
            System.err.println("[Report] SQL error: " + e.getMessage());
        }
        return 0;
    }
}
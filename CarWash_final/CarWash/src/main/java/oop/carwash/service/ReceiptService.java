
package oop.carwash.service;

import oop.carwash.dao.PaymentDAO;
import oop.carwash.dao.ReceiptDAO;
import oop.carwash.dao.ServiceDAO;
import oop.carwash.dao.ProductDAO;
import oop.carwash.model.Receipt;
import oop.carwash.model.Service;
import oop.carwash.model.Product;
import oop.carwash.model.Payment;

import java.util.Optional;

/**
 * Business logic for creating, updating, and managing receipts.
 *
 * Usage:
 *   Receipt receipt = new Receipt(customerId, userId);
 *   receipt.addService(serviceId, serviceName, price);
 *   receipt.addProduct(productId, productName, qty, price);
 *   receipt.recalculateTotals();
 *   ReceiptService.saveReceipt(receipt);
 */
public class ReceiptService {

    private static final ReceiptDAO receiptDAO = new ReceiptDAO();
    private static final ServiceDAO serviceDAO = new ServiceDAO();
    private static final ProductDAO productDAO = new ProductDAO();
    private static final PaymentDAO paymentDAO = new PaymentDAO();

    private ReceiptService() {
        // Utility class
    }

    // ── Saving ─────────────────────────────────────────────────────────────

    /**
     * Saves a new receipt with all its line items to the database.
     * Assumes the receipt object is properly constructed with items and totals.
     */
    public static void saveReceipt(Receipt receipt) {
        if (receipt.getCustomerId() == 0) {
            throw new IllegalArgumentException("Receipt must have a customer_id");
        }
        if (receipt.getCreatedBy() == 0) {
            throw new IllegalArgumentException("Receipt must have a created_by user_id");
        }

        receipt.recalculateTotals();
        receiptDAO.save(receipt);
        // Update product stock for each product line item
        for (oop.carwash.model.ReceiptItem item : receipt.getItems()) {
            if (item instanceof oop.carwash.model.ReceiptProduct) {
                oop.carwash.model.ReceiptProduct rp = (oop.carwash.model.ReceiptProduct) item;
                productDAO.recordSale(rp.getProductId(), rp.getQuantity());
            }
        }
        System.out.println("[Receipt] Saved receipt #" + receipt.getReceiptId() +
                " for customer " + receipt.getCustomerId() +
                " - Total: " + receipt.getGrandTotal());
    }

    /**
     * Updates an existing receipt.
     */
    public static void updateReceipt(Receipt receipt) {
        receipt.recalculateTotals();
        receiptDAO.update(receipt);
    }

    /**
     * Retrieves a receipt by ID (with all line items).
     */
    public static Optional<Receipt> getReceipt(int receiptId) {
        return receiptDAO.findById(receiptId);
    }

    /**
     * Retrieves all receipts.
     */
    public static java.util.List<Receipt> getAllReceipts() {
        return receiptDAO.findAll();
    }

    /**
     * Deletes a receipt (cascades to line items).
     */
    public static void deleteReceipt(int receiptId) {
        receiptDAO.delete(receiptId);
    }

    // ── Payments ───────────────────────────────────────────────────────────

    /**
     * Records a payment for a receipt.
     * Handles UNPAID / PARTIAL / PAID automatically.
     */
    public static void recordPayment(int receiptId, double amount) {

        Optional<Receipt> receiptOpt = receiptDAO.findById(receiptId);
        if (receiptOpt.isEmpty()) {
            System.err.println("[Receipt] Payment failed: receipt not found - " + receiptId);
            return;
        }

        if (amount <= 0) {
            System.err.println("[Receipt] Payment amount must be greater than 0");
            return;
        }

        Receipt receipt = receiptOpt.get();

        double totalPaidBefore = paymentDAO.getTotalPaidForReceipt(receiptId);
        double newTotalPaid = totalPaidBefore + amount;

        if (newTotalPaid > receipt.getGrandTotal()) {
            System.err.println("[Receipt] Payment exceeds remaining balance");
            return;
        }

        // Save payment
        Payment payment = new Payment(receiptId, amount);
        paymentDAO.save(payment);

        // Determine new status
        String newStatus;

        if (newTotalPaid == 0) {
            newStatus = "UNPAID";
        } else if (newTotalPaid < receipt.getGrandTotal()) {
            newStatus = "PARTIAL";
        } else {
            newStatus = "PAID";
        }

        receipt.setStatus(newStatus);
        receiptDAO.update(receipt);

        System.out.println("[Receipt] Payment recorded: receipt #" + receiptId +
                " - Amount: " + amount +
                " - Status: " + newStatus);
    }

    /**
     * Returns total amount paid for a receipt.
     */
    public static double getTotalPaid(int receiptId) {
        return paymentDAO.getTotalPaidForReceipt(receiptId);
    }

    /**
     * Returns all payments for a receipt.
     */
    public static java.util.List<Payment> getPayments(int receiptId) {
        return paymentDAO.findByReceiptId(receiptId);
    }

    // ── Lookup ─────────────────────────────────────────────────────────────

    /**
     * Look up a service by ID to get its current price.
     */
    public static Optional<Service> getService(int serviceId) {
        return serviceDAO.findById(serviceId);
    }

    /**
     * Look up all services.
     */
    public static java.util.List<Service> getAllServices() {
        return serviceDAO.findAll();
    }

    /**
     * Look up a product by ID.
     */
    public static Optional<Product> getProduct(int productId) {
        return productDAO.findById(productId);
    }

    /**
     * Look up all products.
     */
    public static java.util.List<Product> getAllProducts() {
        return productDAO.findAll();
    }
}
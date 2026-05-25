
package oop.carwash.model;


import java.util.ArrayList;
import java.util.List;

/**
 * Represents a receipt/invoice for a customer.
 * Holds a list of ReceiptItems (services and products).
 *
 * POLYMORPHISM in action: items is List<ReceiptItem>, and we call
 * item.getTotal() polymorphically. The Receipt doesn't care if an item
 * is a ReceiptService or ReceiptProduct.
 */
public class Receipt {

    private int receiptId;
    private int customerId;
    private int createdBy;  // user_id
    private String receiptDate;
    private List<ReceiptItem> items;  // Services and products
    private double servicesTotal;
    private double productsTotal;
    private double grandTotal;
    private String status;  // "UNPAID", "PAID"
    private String notes;

    // ── Constructors ──────────────────────────────────────────────────────

    public Receipt() {
        this.items = new ArrayList<>();
        this.status = "UNPAID";
        this.servicesTotal = 0;
        this.productsTotal = 0;
        this.grandTotal = 0;
    }

    public Receipt(int customerId, int createdBy) {
        this();
        this.customerId = customerId;
        this.createdBy = createdBy;
    }

    public Receipt(int receiptId, int customerId, int createdBy, String receiptDate,
                   double servicesTotal, double productsTotal, double grandTotal,
                   String status, String notes) {
        this();
        this.receiptId = receiptId;
        this.customerId = customerId;
        this.createdBy = createdBy;
        this.receiptDate = receiptDate;
        this.servicesTotal = servicesTotal;
        this.productsTotal = productsTotal;
        this.grandTotal = grandTotal;
        this.status = status;
        this.notes = notes;
    }

    // ── Business logic ────────────────────────────────────────────────────

    /**
     * Recalculates totals from the line items.
     * Called before saving to the database.
     */
    public void recalculateTotals() {
        servicesTotal = 0;
        productsTotal = 0;

        for (ReceiptItem item : items) {
            if (item instanceof ReceiptService) {
                servicesTotal += item.getTotal();
            } else if (item instanceof ReceiptProduct) {
                productsTotal += item.getTotal();
            }
        }

        grandTotal = servicesTotal + productsTotal;
    }

    /**
     * Adds a service line to the receipt.
     */
    public void addService(int serviceId, String serviceName, double price) {
        items.add(new ReceiptService(receiptId, serviceId, serviceName, price));
    }

    /**
     * Adds a product line to the receipt.
     */
    public void addProduct(int productId, String productName, int quantity, double unitPrice) {
        items.add(new ReceiptProduct(receiptId, productId, productName, (double) quantity, unitPrice));
    }

    /** Overload for fractional quantities (e.g. liters for GALLON products) */
    public void addProduct(int productId, String productName, double quantity, double unitPrice) {
        items.add(new ReceiptProduct(receiptId, productId, productName, quantity, unitPrice));
    }

    // ── Getters / Setters ─────────────────────────────────────────────────

    public int getReceiptId() {
        return receiptId;
    }

    public void setReceiptId(int receiptId) {
        this.receiptId = receiptId;
    }

    public int getCustomerId() {
        return customerId;
    }

    public void setCustomerId(int customerId) {
        this.customerId = customerId;
    }

    public int getCreatedBy() {
        return createdBy;
    }

    public void setCreatedBy(int createdBy) {
        this.createdBy = createdBy;
    }

    public String getReceiptDate() {
        return receiptDate;
    }

    public void setReceiptDate(String receiptDate) {
        this.receiptDate = receiptDate;
    }

    public List<ReceiptItem> getItems() {
        return items;
    }

    public void setItems(List<ReceiptItem> items) {
        this.items = items;
    }

    public double getServicesTotal() {
        return servicesTotal;
    }

    public void setServicesTotal(double servicesTotal) {
        this.servicesTotal = servicesTotal;
    }

    public double getProductsTotal() {
        return productsTotal;
    }

    public void setProductsTotal(double productsTotal) {
        this.productsTotal = productsTotal;
    }

    public double getGrandTotal() {
        return grandTotal;
    }

    public void setGrandTotal(double grandTotal) {
        this.grandTotal = grandTotal;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public String getNotes() {
        return notes;
    }

    public void setNotes(String notes) {
        this.notes = notes;
    }

    @Override
    public String toString() {
        return "Receipt{" +
                "receiptId=" + receiptId +
                ", customerId=" + customerId +
                ", status='" + status + '\'' +
                ", grandTotal=" + grandTotal +
                ", itemCount=" + items.size() +
                '}';
    }
}
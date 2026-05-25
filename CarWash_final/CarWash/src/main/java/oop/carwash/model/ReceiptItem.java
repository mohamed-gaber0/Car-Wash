
package oop.carwash.model;


/**
 * Abstract base class for receipt line items (services or products).
 *
 * INHERITANCE REQUIREMENT: ReceiptService and ReceiptProduct extend this.
 * POLYMORPHISM REQUIREMENT: Receipt holds List<ReceiptItem> and calls
 * item.getTotal() polymorphically on each item.
 *
 * This design ensures the Receipt class doesn't know or care whether
 * an item is a service or product - it treats them uniformly.
 */
public abstract class ReceiptItem {

    protected int id;
    protected int receiptId;

    // ── Constructors ──────────────────────────────────────────────────────

    public ReceiptItem() {
    }

    public ReceiptItem(int receiptId) {
        this.receiptId = receiptId;
    }

    // ── Abstract methods (polymorphic) ────────────────────────────────────

    /**
     * Returns the total price of this line item.
     * Subclasses must implement this.
     */
    public abstract double getTotal();

    /**
     * Returns a human-readable description of this item.
     */
    public abstract String getDescription();

    // ── Common getters / setters ──────────────────────────────────────────

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public int getReceiptId() {
        return receiptId;
    }

    public void setReceiptId(int receiptId) {
        this.receiptId = receiptId;
    }
}

package oop.carwash.model;

/**
 * A product line item on a receipt.
 * Stores the product ID, quantity (double to support liters for GALLONs),
 * and unit price AT THE TIME the receipt was created.
 * Total = quantity * unitPriceAtTime.
 */
public class ReceiptProduct extends ReceiptItem {

    private int productId;
    private String productName;  // Cached for display
    private double quantity;     // Double: supports fractional liters (GALLON) and whole pieces (CARTON)
    private double unitPriceAtTime;
    private double totalPrice;

    // Constructors

    public ReceiptProduct() {
    }

    public ReceiptProduct(int receiptId, int productId, String productName,
                          double quantity, double unitPriceAtTime) {
        super(receiptId);
        this.productId = productId;
        this.productName = productName;
        this.quantity = quantity;
        this.unitPriceAtTime = unitPriceAtTime;
        this.totalPrice = quantity * unitPriceAtTime;
    }

    public ReceiptProduct(int id, int receiptId, int productId, String productName,
                          double quantity, double unitPriceAtTime, double totalPrice) {
        super(receiptId);
        this.id = id;
        this.productId = productId;
        this.productName = productName;
        this.quantity = quantity;
        this.unitPriceAtTime = unitPriceAtTime;
        this.totalPrice = totalPrice;
    }

    // Polymorphic implementations

    @Override
    public double getTotal() {
        return totalPrice;
    }

    @Override
    public String getDescription() {
        String qtyStr = (quantity == Math.floor(quantity))
                ? String.valueOf((int) quantity)
                : String.format("%.2f", quantity);
        return productName + " x" + qtyStr + " @ " + unitPriceAtTime + " = " + totalPrice;
    }

    // Getters / Setters

    public int getProductId() { return productId; }
    public void setProductId(int productId) { this.productId = productId; }

    public String getProductName() { return productName; }
    public void setProductName(String productName) { this.productName = productName; }

    public double getQuantity() { return quantity; }
    public void setQuantity(double quantity) {
        this.quantity = quantity;
        recalculateTotal();
    }

    /** Convenience getter - rounds to int for CARTON display */
    public int getQuantityInt() { return (int) Math.round(quantity); }

    public double getUnitPriceAtTime() { return unitPriceAtTime; }
    public void setUnitPriceAtTime(double unitPriceAtTime) {
        this.unitPriceAtTime = unitPriceAtTime;
        recalculateTotal();
    }

    public double getTotalPrice() { return totalPrice; }
    public void setTotalPrice(double totalPrice) { this.totalPrice = totalPrice; }

    private void recalculateTotal() {
        this.totalPrice = quantity * unitPriceAtTime;
    }

    @Override
    public String toString() {
        return "ReceiptProduct{" +
                "id=" + id +
                ", receiptId=" + receiptId +
                ", productId=" + productId +
                ", productName='" + productName + '\'' +
                ", quantity=" + quantity +
                ", unitPriceAtTime=" + unitPriceAtTime +
                ", totalPrice=" + totalPrice +
                '}';
    }
}


package oop.carwash.model;


/**
 * A service line item on a receipt.
 * Stores the service ID and its price AT THE TIME the receipt was created
 * (so if the service price changes later, old receipts are unaffected).
 */
public class ReceiptService extends ReceiptItem {

    private int serviceId;
    private String serviceName;  // Cached for display
    private double priceAtTime;

    // ── Constructors ──────────────────────────────────────────────────────

    public ReceiptService() {
    }

    public ReceiptService(int receiptId, int serviceId, String serviceName, double priceAtTime) {
        super(receiptId);
        this.serviceId = serviceId;
        this.serviceName = serviceName;
        this.priceAtTime = priceAtTime;
    }

    public ReceiptService(int id, int receiptId, int serviceId, String serviceName, double priceAtTime) {
        super(receiptId);
        this.id = id;
        this.serviceId = serviceId;
        this.serviceName = serviceName;
        this.priceAtTime = priceAtTime;
    }

    // ── Polymorphic implementations ───────────────────────────────────────

    @Override
    public double getTotal() {
        return priceAtTime;
    }

    @Override
    public String getDescription() {
        return serviceName + " - " + priceAtTime;
    }

    // ── Getters / Setters ─────────────────────────────────────────────────

    public int getServiceId() {
        return serviceId;
    }

    public void setServiceId(int serviceId) {
        this.serviceId = serviceId;
    }

    public String getServiceName() {
        return serviceName;
    }

    public void setServiceName(String serviceName) {
        this.serviceName = serviceName;
    }

    public double getPriceAtTime() {
        return priceAtTime;
    }

    public void setPriceAtTime(double priceAtTime) {
        this.priceAtTime = priceAtTime;
    }

    @Override
    public String toString() {
        return "ReceiptService{" +
                "id=" + id +
                ", receiptId=" + receiptId +
                ", serviceId=" + serviceId +
                ", serviceName='" + serviceName + '\'' +
                ", priceAtTime=" + priceAtTime +
                '}';
    }
}

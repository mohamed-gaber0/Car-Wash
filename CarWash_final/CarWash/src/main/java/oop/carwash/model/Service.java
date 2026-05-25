
package oop.carwash.model;


/**
 * Represents a car wash service (e.g. "Basic Wash", "Wax Coating", etc.)
 * with a fixed price.
 */
public class Service {

    private int serviceId;
    private String serviceName;
    private double price;

    // ── Constructors ──────────────────────────────────────────────────────

    public Service() {
    }

    public Service(String serviceName, double price) {
        this.serviceName = serviceName;
        this.price = price;
    }

    public Service(int serviceId, String serviceName, double price) {
        this.serviceId = serviceId;
        this.serviceName = serviceName;
        this.price = price;
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

    public double getPrice() {
        return price;
    }

    public void setPrice(double price) {
        this.price = price;
    }

    @Override
    public String toString() {
        return "Service{" +
                "serviceId=" + serviceId +
                ", serviceName='" + serviceName + '\'' +
                ", price=" + price +
                '}';
    }
}

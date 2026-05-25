
package oop.carwash.model;


/**
 * Represents a customer (vehicle owner) who can have receipts.
 */
public class Customer {

    private int customerId;
    private String name;
    private String phone;
    private String carPlateNumber;
    private String createdAt;

    // ── Constructors ──────────────────────────────────────────────────────

    public Customer() {
    }

    public Customer(String name, String phone, String carPlateNumber) {
        this.name = name;
        this.phone = phone;
        this.carPlateNumber = carPlateNumber;
    }

    public Customer(int customerId, String name, String phone, String carPlateNumber, String createdAt) {
        this.customerId = customerId;
        this.name = name;
        this.phone = phone;
        this.carPlateNumber = carPlateNumber;
        this.createdAt = createdAt;
    }

    // ── Getters / Setters ─────────────────────────────────────────────────

    public int getCustomerId() {
        return customerId;
    }

    public void setCustomerId(int customerId) {
        this.customerId = customerId;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getPhone() {
        return phone;
    }

    public void setPhone(String phone) {
        this.phone = phone;
    }

    public String getCarPlateNumber() {
        return carPlateNumber;
    }

    public void setCarPlateNumber(String carPlateNumber) {
        this.carPlateNumber = carPlateNumber;
    }

    public String getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(String createdAt) {
        this.createdAt = createdAt;
    }

    @Override
    public String toString() {
        return "Customer{" +
                "customerId=" + customerId +
                ", name='" + name + '\'' +
                ", phone='" + phone + '\'' +
                ", carPlateNumber='" + carPlateNumber + '\'' +
                ", createdAt='" + createdAt + '\'' +
                '}';
    }
}

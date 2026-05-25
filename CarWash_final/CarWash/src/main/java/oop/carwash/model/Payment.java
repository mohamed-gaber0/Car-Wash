
package oop.carwash.model;


/**
 * Represents a payment made against a receipt.
 * A receipt can have multiple payments (e.g. paid in installments).
 */
public class Payment {

    private int paymentId;
    private int receiptId;
    private String paymentDate;
    private double amount;

    // ── Constructors ──────────────────────────────────────────────────────

    public Payment() {
    }

    public Payment(int receiptId, double amount) {
        this.receiptId = receiptId;
        this.amount = amount;
    }

    public Payment(int paymentId, int receiptId, String paymentDate, double amount) {
        this.paymentId = paymentId;
        this.receiptId = receiptId;
        this.paymentDate = paymentDate;
        this.amount = amount;
    }

    // ── Getters / Setters ─────────────────────────────────────────────────

    public int getPaymentId() {
        return paymentId;
    }

    public void setPaymentId(int paymentId) {
        this.paymentId = paymentId;
    }

    public int getReceiptId() {
        return receiptId;
    }

    public void setReceiptId(int receiptId) {
        this.receiptId = receiptId;
    }

    public String getPaymentDate() {
        return paymentDate;
    }

    public void setPaymentDate(String paymentDate) {
        this.paymentDate = paymentDate;
    }

    public double getAmount() {
        return amount;
    }

    public void setAmount(double amount) {
        this.amount = amount;
    }

    @Override
    public String toString() {
        return "Payment{" +
                "paymentId=" + paymentId +
                ", receiptId=" + receiptId +
                ", amount=" + amount +
                ", paymentDate='" + paymentDate + '\'' +
                '}';
    }
}
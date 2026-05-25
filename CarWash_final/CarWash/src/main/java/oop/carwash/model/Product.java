package oop.carwash.model;

/**
 * Represents a product sold with car washes (e.g. "Air Freshener", "Wax", etc.)
 *
 * product_type: "GALLON" (sold by liters, float qty) or "CARTON" (sold by pieces, int qty).
 * For CARTON, units_per_carton indicates how many pieces per carton.
 * stock_quantity: remaining stock (liters for GALLON, pieces for CARTON).
 * sold_quantity: total sold to date.
 */
public class Product {

    public static final String TYPE_GALLON = "GALLON";
    public static final String TYPE_CARTON = "CARTON";

    private int productId;
    private String productName;
    private double unitPrice;
    private String productType;      // "GALLON" or "CARTON"
    private int unitsPerCarton;      // For CARTON: pieces per carton; For GALLON: 1
    private double stockQuantity;    // Remaining (liters or pieces)
    private double soldQuantity;     // Total sold (liters or pieces)

    // Constructors

    public Product() {
        this.productType = TYPE_GALLON;
        this.unitsPerCarton = 1;
    }

    /** Backward-compatible constructor (name + price only) */
    public Product(String productName, double unitPrice) {
        this.productName = productName;
        this.unitPrice = unitPrice;
        this.productType = TYPE_GALLON;
        this.unitsPerCarton = 1;
    }

    /** Backward-compatible constructor (id + name + price) */
    public Product(int productId, String productName, double unitPrice) {
        this.productId = productId;
        this.productName = productName;
        this.unitPrice = unitPrice;
        this.productType = TYPE_GALLON;
        this.unitsPerCarton = 1;
    }

    /** Full constructor with inventory fields */
    public Product(int productId, String productName, double unitPrice,
                   String productType, int unitsPerCarton,
                   double stockQuantity, double soldQuantity) {
        this.productId = productId;
        this.productName = productName;
        this.unitPrice = unitPrice;
        this.productType = (productType != null) ? productType : TYPE_GALLON;
        this.unitsPerCarton = (unitsPerCarton > 0) ? unitsPerCarton : 1;
        this.stockQuantity = stockQuantity;
        this.soldQuantity = soldQuantity;
    }

    // Getters / Setters

    public int getProductId() { return productId; }
    public void setProductId(int productId) { this.productId = productId; }

    public String getProductName() { return productName; }
    public void setProductName(String productName) { this.productName = productName; }

    public double getUnitPrice() { return unitPrice; }
    public void setUnitPrice(double unitPrice) { this.unitPrice = unitPrice; }

    public String getProductType() { return productType; }
    public void setProductType(String productType) { this.productType = productType; }

    public int getUnitsPerCarton() { return unitsPerCarton; }
    public void setUnitsPerCarton(int unitsPerCarton) { this.unitsPerCarton = unitsPerCarton; }

    public double getStockQuantity() { return stockQuantity; }
    public void setStockQuantity(double stockQuantity) { this.stockQuantity = stockQuantity; }

    public double getSoldQuantity() { return soldQuantity; }
    public void setSoldQuantity(double soldQuantity) { this.soldQuantity = soldQuantity; }

    /** Whether this product is sold as a gallon (by liters) */
    public boolean isGallon() { return TYPE_GALLON.equals(productType); }

    /** Whether this product is sold as a carton (by pieces) */
    public boolean isCarton() { return TYPE_CARTON.equals(productType); }

    /** Human-readable unit label in Arabic */
    public String getUnitLabel() {
        return isGallon() ? "لتر" : "قطعة";
    }

    /** Human-readable type label in Arabic */
    public String getTypeLabel() {
        return isGallon() ? "جالون" : "كرتونة";
    }

    /** Is stock depleted? */
    public boolean isOutOfStock() {
        return stockQuantity <= 0;
    }

    @Override
    public String toString() {
        return "Product{" +
                "productId=" + productId +
                ", productName='" + productName + '\'' +
                ", unitPrice=" + unitPrice +
                ", productType='" + productType + '\'' +
                ", unitsPerCarton=" + unitsPerCarton +
                ", stockQuantity=" + stockQuantity +
                ", soldQuantity=" + soldQuantity +
                '}';
    }
}

module oop.carwash {

    // ── JavaFX Modules ──────────────────────────────────────────────────────
    requires javafx.controls;
    requires javafx.graphics;
    requires javafx.base;
    requires javafx.swing;

    // ── Java Standard Modules ───────────────────────────────────────────────
    requires java.sql;
    requires java.desktop;              // ✅ NEW: For BufferedImage, Swing integration

    // ── Barcode/QR Code (ZXing) ─────────────────────────────────────────────
    requires com.google.zxing;
    requires com.google.zxing.javase;

    // ── Webcam Capture (automatic module) ───────────────────────────────────
    requires webcam.capture;

    // ── Exports ─────────────────────────────────────────────────────────────
    exports oop.carwash;

    // ── Opens for Reflection ────────────────────────────────────────────────
    opens oop.carwash to javafx.graphics;
    opens oop.carwash.ui to javafx.graphics, javafx.controls;
    opens oop.carwash.model to javafx.base;
    opens oop.carwash.dao to oop.carwash;
}
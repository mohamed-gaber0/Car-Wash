
package oop.carwash.util;


import com.google.zxing.BarcodeFormat;
import com.google.zxing.MultiFormatWriter;
import com.google.zxing.WriterException;
import com.google.zxing.client.j2se.MatrixToImageWriter;
import com.google.zxing.common.BitMatrix;
import javafx.scene.image.Image;

import java.io.ByteArrayOutputStream;
import java.io.IOException;

/**
 * Generates QR codes for customers to access their bills/receipts.
 *
 * The QR code encodes a URL like:
 *   http://localhost:8080/bills?customer=123
 *
 * Or can encode any string data.
 */
public class QRCodeGenerator {

    private static final int QR_CODE_SIZE = 300;

    private QRCodeGenerator() {
        // Utility class
    }

    /**
     * Generates a QR code from data and returns it as a JavaFX Image.
     *
     * @param data the data to encode (usually a URL or customer ID)
     * @return JavaFX Image of the QR code
     */
    public static Image generateQRCode(String data) {
        try {
            MultiFormatWriter writer = new MultiFormatWriter();
            BitMatrix bitMatrix = writer.encode(data, BarcodeFormat.QR_CODE, QR_CODE_SIZE, QR_CODE_SIZE);

            ByteArrayOutputStream pngOutputStream = new ByteArrayOutputStream();
            MatrixToImageWriter.writeToStream(bitMatrix, "PNG", pngOutputStream);

            byte[] pngData = pngOutputStream.toByteArray();
            java.io.ByteArrayInputStream inputStream = new java.io.ByteArrayInputStream(pngData);

            return new Image(inputStream);

        } catch (WriterException | IOException e) {
            System.err.println("[QR] Error generating QR code: " + e.getMessage());
            return null;
        }
    }

    /**
     * Generates a QR code URL for a customer's bills.
     * Can be customized to point to your actual bill service.
     *
     * @param customerId the customer ID
     * @return QR code image
     */
    public static Image generateCustomerBillQR(int customerId) {
        String url = "http://carwash.local/bills?customer=" + customerId;
        return generateQRCode(url);
    }

    /**
     * Generates a QR code for a specific receipt.
     *
     * @param receiptId the receipt ID
     * @return QR code image
     */
    public static Image generateReceiptQR(int receiptId) {
        String url = "http://carwash.local/receipt/" + receiptId;
        return generateQRCode(url);
    }
}
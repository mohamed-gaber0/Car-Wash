package oop.carwash.ui;

import com.github.sarxos.webcam.Webcam;
import com.github.sarxos.webcam.WebcamPanel;
import com.github.sarxos.webcam.WebcamResolution;
import com.google.zxing.*;
import com.google.zxing.client.j2se.BufferedImageLuminanceSource;
import com.google.zxing.common.HybridBinarizer;
import javafx.application.Platform;
import javafx.embed.swing.SwingFXUtils;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.stage.Modality;
import javafx.stage.Stage;

import java.awt.image.BufferedImage;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * Dialog for scanning barcodes using the webcam.
 * Uses Webcam Capture and ZXing libraries for camera access and barcode decoding.
 */
public class BarcodeScannerDialog {

    private Stage stage;
    private Webcam webcam;
    private ExecutorService executor;
    private AtomicBoolean running = new AtomicBoolean(false);
    private ImageView cameraView;
    private TextField manualInputField;
    private BarcodeScannedHandler onBarcodeScanned;
    private Label statusLabel;

    public interface BarcodeScannedHandler {
        void onBarcodeScanned(String barcode);
    }

    public BarcodeScannerDialog() {
        buildUI();
    }

    private void buildUI() {
        stage = new Stage();
        stage.initModality(Modality.APPLICATION_MODAL);
        stage.setTitle("📷 قارئ الباركود");
        stage.setMinWidth(500);
        stage.setMinHeight(450);

        VBox root = new VBox(16);
        root.setPadding(new Insets(20));
        root.setStyle("-fx-background-color: white;");
        root.setAlignment(Pos.CENTER);

        // Title
        Label titleLabel = new Label("📷 امسح باركود العميل");
        titleLabel.setStyle("-fx-font-size: 18px; -fx-font-weight: bold; -fx-text-fill: #1E293B;");

        // Camera view
        cameraView = new ImageView();
        cameraView.setFitWidth(400);
        cameraView.setFitHeight(300);
        cameraView.setPreserveRatio(true);
        cameraView.setStyle("-fx-background-color: #F1F5F9; -fx-border-color: #CBD5E1; -fx-border-width: 2; -fx-border-radius: 8;");

        // Placeholder when camera is starting
        Label placeholderLabel = new Label("🎥 جاري تشغيل الكاميرا...");
        placeholderLabel.setStyle("-fx-font-size: 16px; -fx-text-fill: #64748B;");
        
        VBox cameraContainer = new VBox(8);
        cameraContainer.setAlignment(Pos.CENTER);
        cameraContainer.setPrefSize(420, 320);
        cameraContainer.setStyle("-fx-background-color: #F8FAFC; -fx-border-color: #E2E8F0; -fx-border-width: 1; -fx-background-radius: 8;");
        cameraContainer.getChildren().add(placeholderLabel);

        // Status label
        statusLabel = new Label("حط الباركود قدام الكاميرا");
        statusLabel.setStyle("-fx-font-size: 13px; -fx-text-fill: #64748B;");

        // Manual input section
        Label orLabel = new Label("─────────  أو  ─────────");
        orLabel.setStyle("-fx-font-size: 13px; -fx-text-fill: #94A3B8;");

        HBox manualBox = new HBox(10);
        manualBox.setAlignment(Pos.CENTER_LEFT);

        Label manualLabel = new Label("إدخال يدوي:");
        manualLabel.setStyle(Theme.labelStyle());

        manualInputField = new TextField();
        manualInputField.setPromptText("رقم العميل...");
        manualInputField.setMinWidth(200);
        manualInputField.setPrefWidth(250);
        manualInputField.setStyle(Theme.inputFieldStyle());

        Button submitBtn = Theme.createPrimaryButton("بحث");
        submitBtn.setMinWidth(80);
        submitBtn.setOnAction(e -> handleManualInput());

        manualInputField.setOnAction(e -> handleManualInput());

        manualBox.getChildren().addAll(manualLabel, manualInputField, submitBtn);

        // Buttons
        HBox buttonBox = new HBox(12);
        buttonBox.setAlignment(Pos.CENTER);

        Button cancelButton = Theme.createSecondaryButton("إلغاء");
        cancelButton.setMinWidth(100);
        cancelButton.setOnAction(e -> close());

        buttonBox.getChildren().addAll(cancelButton);

        root.getChildren().addAll(titleLabel, cameraContainer, statusLabel, orLabel, manualBox, buttonBox);

        Scene scene = new Scene(root);
        stage.setScene(scene);
        stage.setOnCloseRequest(e -> close());

        // Start camera after UI is built
        startCamera(cameraContainer);
    }

    private void startCamera(VBox cameraContainer) {
        executor = Executors.newSingleThreadExecutor();
        running.set(true);

        executor.submit(() -> {
            try {
                webcam = Webcam.getDefault();
                if (webcam != null) {
                    webcam.setViewSize(WebcamResolution.VGA.getSize());
                    webcam.open();

                    // Update UI on FX thread
                    Platform.runLater(() -> {
                        cameraContainer.getChildren().clear();
                        cameraContainer.getChildren().add(cameraView);
                        statusLabel.setText("الكاميرا جاهزة - حط الباركود قدام الكاميرا");
                    });

                    // Start capture loop
                    while (running.get() && webcam.isOpen()) {
                        BufferedImage image = webcam.getImage();
                        if (image != null) {
                            // Convert to JavaFX Image
                            Image fxImage = SwingFXUtils.toFXImage(image, null);
                            
                            // Try to decode barcode
                            String barcode = decodeBarcode(image);
                            if (barcode != null) {
                                Platform.runLater(() -> {
                                    statusLabel.setText("✅ تم قراءة الباركود: " + barcode);
                                    statusLabel.setStyle("-fx-font-size: 14px; -fx-text-fill: #10B981; -fx-font-weight: bold;");
                                });
                                
                                // Wait a moment then process
                                try {
                                    Thread.sleep(500);
                                } catch (InterruptedException e) {
                                    Thread.currentThread().interrupt();
                                }
                                
                                // Notify handler
                                if (onBarcodeScanned != null) {
                                    Platform.runLater(() -> {
                                        close();
                                        onBarcodeScanned.onBarcodeScanned(barcode);
                                    });
                                }
                                break;
                            }

                            // Update camera view
                            Platform.runLater(() -> cameraView.setImage(fxImage));
                        }

                        try {
                            Thread.sleep(50); // ~20 FPS
                        } catch (InterruptedException e) {
                            Thread.currentThread().interrupt();
                            break;
                        }
                    }
                } else {
                    Platform.runLater(() -> {
                        Label noCameraLabel = new Label("❌ مفيش كاميرا\n\nاستخدم الإدخال اليدوي تحت");
                        noCameraLabel.setStyle("-fx-font-size: 14px; -fx-text-fill: #EF4444; -fx-text-alignment: center;");
                        noCameraLabel.setWrapText(true);
                        cameraContainer.getChildren().clear();
                        cameraContainer.getChildren().add(noCameraLabel);
                        statusLabel.setText("مفيش كاميرا - استخدم الإدخال اليدوي");
                        statusLabel.setStyle("-fx-font-size: 13px; -fx-text-fill: #EF4444;");
                    });
                }
            } catch (Exception e) {
                Platform.runLater(() -> {
                    Label errorLabel = new Label("⚠️ خطأ في الكاميرا\n" + e.getMessage());
                    errorLabel.setStyle("-fx-font-size: 13px; -fx-text-fill: #EF4444; -fx-text-alignment: center;");
                    errorLabel.setWrapText(true);
                    cameraContainer.getChildren().clear();
                    cameraContainer.getChildren().add(errorLabel);
                    statusLabel.setText("الكاميرا مش متاحة - استخدم الإدخال اليدوي");
                });
            }
        });
    }

    private String decodeBarcode(BufferedImage image) {
        if (image == null) return null;

        try {
            LuminanceSource source = new BufferedImageLuminanceSource(image);
            BinaryBitmap bitmap = new BinaryBitmap(new HybridBinarizer(source));

            Result result = new MultiFormatReader().decode(bitmap);
            return result.getText();
        } catch (NotFoundException e) {
            // No barcode found in this frame
            return null;
        } catch (Exception e) {
            return null;
        }
    }

    private void handleManualInput() {
        String text = manualInputField.getText().trim();
        if (!text.isEmpty()) {
            if (onBarcodeScanned != null) {
                close();
                onBarcodeScanned.onBarcodeScanned(text);
            }
        }
    }

    public void setOnBarcodeScanned(BarcodeScannedHandler handler) {
        this.onBarcodeScanned = handler;
    }

    public void show() {
        if (stage != null) {
            stage.show();
        }
    }

    public void close() {
        running.set(false);
        
        if (webcam != null && webcam.isOpen()) {
            webcam.close();
        }
        
        if (executor != null && !executor.isShutdown()) {
            executor.shutdown();
        }
        
        if (stage != null) {
            stage.close();
        }
    }
}

package oop.carwash.util;


import oop.carwash.model.Customer;
import oop.carwash.service.ReportService;
import java.util.List;
import oop.carwash.model.Receipt;
import oop.carwash.model.ReceiptItem;
import com.google.zxing.BarcodeFormat;
import com.google.zxing.MultiFormatWriter;
import com.google.zxing.WriterException;
import com.google.zxing.client.j2se.MatrixToImageWriter;
import com.google.zxing.common.BitMatrix;
import javafx.geometry.Insets;
import javafx.geometry.NodeOrientation;
import javafx.geometry.Pos;
import javafx.print.PageLayout;
import javafx.print.PageOrientation;
import javafx.print.Paper;
import javafx.print.PrinterJob;
import javafx.scene.control.Label;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.VBox;
import javafx.scene.layout.HBox;
import java.io.ByteArrayOutputStream;
import java.io.IOException;

/**
 * Prints receipts and customer ID cards to small thermal printers.
 *
 * Creates formatted prints with:
 *   - Receipts: Header, items, totals, customer barcode
 *   - Customer cards: Customer info with barcode for quick ID scanning
 */
public class PrintService {

    private PrintService() {
        // Utility class
    }

    /**
     * Prints a receipt to a thermal printer.
     *
     * @param receipt the receipt to print
     */
    public static void printReceipt(Receipt receipt) {
        try {
            PrinterJob job = PrinterJob.createPrinterJob();

            if (job == null) {
                System.err.println("[Print] No printer available");
                return;
            }

            // Setup for thermal printer (58mm or 80mm paper width)
            PageLayout pageLayout = job.getPrinter().getDefaultPageLayout();
            Paper paper = Paper.A4;
            pageLayout = job.getPrinter().createPageLayout(
                paper,
                PageOrientation.PORTRAIT,
                job.getPrinter().getDefaultPageLayout().getLeftMargin(),
                job.getPrinter().getDefaultPageLayout().getRightMargin(),
                job.getPrinter().getDefaultPageLayout().getTopMargin(),
                job.getPrinter().getDefaultPageLayout().getBottomMargin()
            );

            VBox receiptNode = buildReceiptNode(receipt);

            job.printPage(pageLayout, receiptNode);

            if (job.endJob()) {
                System.out.println("[Print] Receipt printed successfully");
            } else {
                System.out.println("[Print] Print job failed");
            }

        } catch (Exception e) {
            System.err.println("[Print] Error printing receipt: " + e.getMessage());
            e.printStackTrace();
        }
    }

    /**
     * Prints a customer ID card to a thermal printer.
     * This creates a small card with customer info and a barcode for quick scanning.
     *
     * @param customer the customer whose ID card to print
     */
    public static void printCustomerCard(Customer customer) {
        try {
            PrinterJob job = PrinterJob.createPrinterJob();

            if (job == null) {
                System.err.println("[Print] No printer available");
                return;
            }

            PageLayout pageLayout = job.getPrinter().getDefaultPageLayout();
            Paper paper = Paper.A4;
            pageLayout = job.getPrinter().createPageLayout(
                paper,
                PageOrientation.PORTRAIT,
                job.getPrinter().getDefaultPageLayout().getLeftMargin(),
                job.getPrinter().getDefaultPageLayout().getRightMargin(),
                job.getPrinter().getDefaultPageLayout().getTopMargin(),
                job.getPrinter().getDefaultPageLayout().getBottomMargin()
            );

            VBox cardNode = buildCustomerCardNode(customer);

            job.printPage(pageLayout, cardNode);

            if (job.endJob()) {
                System.out.println("[Print] Customer ID card printed successfully");
            } else {
                System.out.println("[Print] Print job failed");
            }

        } catch (Exception e) {
            System.err.println("[Print] Error printing customer card: " + e.getMessage());
            e.printStackTrace();
        }
    }

    /**
     * Builds a VBox containing the formatted receipt for printing.
     */
    /**
     * Prints a product sales report for a given date range.
     * Lists each product: name, qty sold, and revenue. Same style as a receipt.
     */
    public static void printSalesReport(String startDate, String endDate,
                                        List<ReportService.ProductSalesSummary> rows,
                                        double grandTotal) {
        try {
            PrinterJob job = PrinterJob.createPrinterJob();
            if (job == null) { System.err.println("[Print] No printer available"); return; }
            PageLayout pageLayout = job.getPrinter().getDefaultPageLayout();
            pageLayout = job.getPrinter().createPageLayout(
                Paper.A4, PageOrientation.PORTRAIT,
                pageLayout.getLeftMargin(), pageLayout.getRightMargin(),
                pageLayout.getTopMargin(), pageLayout.getBottomMargin()
            );
            VBox reportNode = buildSalesReportNode(startDate, endDate, rows, grandTotal);
            job.printPage(pageLayout, reportNode);
            if (job.endJob()) System.out.println("[Print] Sales report printed.");
            else System.out.println("[Print] Print failed.");
        } catch (Exception e) {
            System.err.println("[Print] Error: " + e.getMessage());
        }
    }

    private static VBox buildSalesReportNode(String startDate, String endDate,
                                              List<ReportService.ProductSalesSummary> rows,
                                              double grandTotal) {
        VBox box = new VBox(6);
        box.setPadding(new Insets(12));
        box.setNodeOrientation(NodeOrientation.RIGHT_TO_LEFT);
        box.setAlignment(Pos.TOP_RIGHT);
        // AR_FONT is defined below, use literal here
        box.setStyle("-fx-font-family: 'Segoe UI','Tahoma',sans-serif; -fx-font-size: 11px;");

        Label header = new Label("\u2550\u2550\u2550\u2550 \u062a\u0642\u0631\u064a\u0631 \u0645\u0628\u064a\u0639\u0627\u062a \u0627\u0644\u0645\u0646\u062a\u062c\u0627\u062a \u2550\u2550\u2550\u2550");
        header.setStyle("-fx-font-weight: bold; -fx-font-size: 13px;");
        header.setMaxWidth(Double.MAX_VALUE); header.setAlignment(Pos.CENTER);
        box.getChildren().add(header);

        box.getChildren().add(new Label("\u0627\u0644\u0641\u062a\u0631\u0629: " + startDate + " \u2014 " + endDate));
        box.getChildren().add(new Label(repeatString("\u2500", 36)));

        for (ReportService.ProductSalesSummary row : rows) {
            String qtyStr = (row.totalQtySold == Math.floor(row.totalQtySold))
                    ? String.valueOf((int) row.totalQtySold)
                    : String.format("%.2f", row.totalQtySold);
            String line = row.productName + " | " + qtyStr + " " + row.unitLabel
                    + " | " + MoneyFormat.egp(row.totalRevenue);
            box.getChildren().add(new Label(line));
        }

        box.getChildren().add(new Label(repeatString("\u2500", 36)));
        Label totalLine = new Label("\u0627\u0644\u0625\u062c\u0645\u0627\u0644\u064a \u0627\u0644\u0643\u0644\u064a: " + MoneyFormat.egp(grandTotal));
        totalLine.setStyle("-fx-font-weight: bold; -fx-font-size: 12px;");
        box.getChildren().add(totalLine);

        Label footer = new Label("\u0645\u063a\u0633\u0644\u0629 \u0623\u0628\u0648 \u062c\u0645\u064a\u0644");
        footer.setMaxWidth(Double.MAX_VALUE); footer.setAlignment(Pos.CENTER);
        footer.setStyle("-fx-font-size: 10px;");
        box.getChildren().add(footer);
        return box;
    }

        private static final String AR_FONT =
            "'Segoe UI','Tahoma','Arial Unicode MS','Noto Sans Arabic',sans-serif";

    private static VBox buildReceiptNode(Receipt receipt) {
        VBox receiptBox = new VBox(6);
        receiptBox.setPadding(new Insets(12));
        receiptBox.setNodeOrientation(NodeOrientation.RIGHT_TO_LEFT);
        receiptBox.setAlignment(Pos.TOP_RIGHT);
        receiptBox.setStyle("-fx-font-family: " + AR_FONT + "; -fx-font-size: 11px;");

        Label header = new Label("════ فاتورة مغسلة ════");
        header.setStyle("-fx-font-weight: bold; -fx-font-size: 13px;");
        header.setMaxWidth(Double.MAX_VALUE);
        header.setAlignment(Pos.CENTER);
        receiptBox.getChildren().add(header);

        receiptBox.getChildren().add(new Label("رقم الفاتورة: " + receipt.getReceiptId()));
        receiptBox.getChildren().add(new Label("التاريخ: " + receipt.getReceiptDate()));
        receiptBox.getChildren().add(new Label(repeatString("─", 36)));

        Label itemsLabel = new Label("البنود:");
        itemsLabel.setStyle("-fx-font-weight: bold;");
        receiptBox.getChildren().add(itemsLabel);

        for (ReceiptItem item : receipt.getItems()) {
            receiptBox.getChildren().add(new Label("• " + item.getDescription()));
        }

        receiptBox.getChildren().add(new Label(repeatString("─", 36)));

        Label servicesLine = new Label("الخدمات: " + MoneyFormat.egp(receipt.getServicesTotal()));
        Label productsLine = new Label("المنتجات: " + MoneyFormat.egp(receipt.getProductsTotal()));
        Label totalLine = new Label("الإجمالي: " + MoneyFormat.egp(receipt.getGrandTotal()));
        totalLine.setStyle("-fx-font-weight: bold; -fx-font-size: 12px;");
        receiptBox.getChildren().addAll(servicesLine, productsLine, totalLine);

        receiptBox.getChildren().add(new Label(repeatString("─", 36)));
        receiptBox.getChildren().add(new Label("باركود رقم العميل:"));
        ImageView barcodeView = generateBarcodeImage(String.format("%06d", receipt.getCustomerId()));
        if (barcodeView != null) {
            barcodeView.setFitWidth(300);
            barcodeView.setFitHeight(80);
            HBox barcodeBox = new HBox(barcodeView);
            barcodeBox.setAlignment(Pos.CENTER);
            barcodeBox.setNodeOrientation(NodeOrientation.LEFT_TO_RIGHT);
            receiptBox.getChildren().add(barcodeBox);
        }

        receiptBox.getChildren().add(new Label("رقم العميل: " + receipt.getCustomerId()));
        receiptBox.getChildren().add(new Label("الحالة: " + ReceiptLabels.statusArabic(receipt.getStatus())));

        Label footer = new Label("شكراً لزيارتكم");
        footer.setMaxWidth(Double.MAX_VALUE);
        footer.setAlignment(Pos.CENTER);
        footer.setStyle("-fx-font-weight: bold; -fx-font-size: 12px;");
        receiptBox.getChildren().add(footer);

        return receiptBox;
    }

    /**
     * Builds a VBox containing the formatted customer ID card for printing.
     */
    private static VBox buildCustomerCardNode(Customer customer) {
        VBox cardBox = new VBox(8);
        cardBox.setPadding(new Insets(15));
        cardBox.setNodeOrientation(NodeOrientation.RIGHT_TO_LEFT);
        cardBox.setStyle("-fx-font-family: " + AR_FONT + "; -fx-font-size: 11px;");
        cardBox.setAlignment(Pos.CENTER);

        Label header = new Label("══════ مغسلة السيارات ══════");
        header.setStyle("-fx-font-weight: bold; -fx-font-size: 14px;");
        header.setAlignment(Pos.CENTER);
        cardBox.getChildren().add(header);

        Label subHeader = new Label("بطاقة تعريف العميل");
        subHeader.setStyle("-fx-font-weight: bold; -fx-font-size: 12px;");
        subHeader.setAlignment(Pos.CENTER);
        cardBox.getChildren().add(subHeader);

        cardBox.getChildren().add(new Label(repeatString("─", 36)));

        VBox infoBox = new VBox(4);
        infoBox.setStyle("-fx-font-family: " + AR_FONT + "; -fx-font-size: 11px;");
        infoBox.setAlignment(Pos.CENTER_RIGHT);

        Label idLabel = new Label("الرقم: " + String.format("%06d", customer.getCustomerId()));
        idLabel.setStyle("-fx-font-weight: bold; -fx-font-size: 14px;");
        idLabel.setAlignment(Pos.CENTER);

        Label nameLabel = new Label("الاسم: " + customer.getName());
        nameLabel.setStyle("-fx-font-size: 12px;");

        String na = "—";
        Label phoneLabel = new Label("الهاتف: " + (customer.getPhone() != null ? customer.getPhone() : na));
        phoneLabel.setStyle("-fx-font-size: 11px;");

        Label plateLabel = new Label("لوحة السيارة: " + (customer.getCarPlateNumber() != null ? customer.getCarPlateNumber() : na));
        plateLabel.setStyle("-fx-font-size: 11px;");

        infoBox.getChildren().addAll(idLabel, nameLabel, phoneLabel, plateLabel);
        cardBox.getChildren().add(infoBox);

        cardBox.getChildren().add(new Label(repeatString("─", 36)));

        Label barcodeLabel = new Label("امسح للدخول السريع:");
        barcodeLabel.setStyle("-fx-font-size: 10px;");
        barcodeLabel.setAlignment(Pos.CENTER);
        cardBox.getChildren().add(barcodeLabel);

        ImageView barcodeView = generateBarcodeImage(String.format("%06d", customer.getCustomerId()));
        if (barcodeView != null) {
            barcodeView.setFitWidth(280);
            barcodeView.setFitHeight(70);
            HBox barcodeBox = new HBox(barcodeView);
            barcodeBox.setAlignment(Pos.CENTER);
            barcodeBox.setNodeOrientation(NodeOrientation.LEFT_TO_RIGHT);
            cardBox.getChildren().add(barcodeBox);
        }

        cardBox.getChildren().add(new Label(repeatString("─", 36)));

        Label footer = new Label("يرجى إبراز هذه البطاقة عند الخدمة");
        footer.setAlignment(Pos.CENTER);
        footer.setStyle("-fx-font-size: 10px; -fx-font-style: italic;");
        cardBox.getChildren().add(footer);

        Label dateLabel = new Label("تاريخ الإصدار: " + (customer.getCreatedAt() != null ? customer.getCreatedAt() : java.time.LocalDate.now().toString()));
        dateLabel.setAlignment(Pos.CENTER);
        dateLabel.setStyle("-fx-font-size: 9px;");
        cardBox.getChildren().add(dateLabel);

        return cardBox;
    }

    /**
     * Generates a CODE128 barcode image from customer ID.
     */
    private static ImageView generateBarcodeImage(String data) {
        try {
            MultiFormatWriter writer = new MultiFormatWriter();
            BitMatrix bitMatrix = writer.encode(
                data,
                BarcodeFormat.CODE_128,
                400, 100
            );

            ByteArrayOutputStream pngOutputStream = new ByteArrayOutputStream();
            MatrixToImageWriter.writeToStream(bitMatrix, "PNG", pngOutputStream);

            byte[] pngData = pngOutputStream.toByteArray();
            Image barcode = new Image(new java.io.ByteArrayInputStream(pngData));
            return new ImageView(barcode);

        } catch (WriterException | IOException e) {
            System.err.println("[Barcode] Error: " + e.getMessage());
            return null;
        }
    }

    /**
     * Repeats a string n times.
     */
    private static String repeatString(String s, int count) {
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < count; i++) {
            sb.append(s);
        }
        return sb.toString();
    }
}
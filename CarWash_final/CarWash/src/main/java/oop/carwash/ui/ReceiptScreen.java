package oop.carwash.ui;

import oop.carwash.dao.CustomerDAO;
import oop.carwash.model.Customer;
import oop.carwash.model.Service;
import oop.carwash.model.Product;
import oop.carwash.model.Receipt;
import oop.carwash.model.ReceiptItem;
// ReceiptService model imported via fully qualified name when needed
import oop.carwash.model.ReceiptProduct;
import oop.carwash.service.AuthService;
import oop.carwash.service.ReceiptService;
import oop.carwash.util.PrintService;
import oop.carwash.util.MoneyFormat;

import javafx.application.Platform;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.*;
import javafx.scene.control.SpinnerValueFactory;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.Region;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.stage.Stage;

import java.util.List;
import java.util.stream.Collectors;

/**
 * Modern screen for creating receipts.
 * IMPROVED: Full consistency with CustomerScreen design patterns.
 * Features: Search with barcode scanner, TableView for items, remove items,
 * context menu, double-click support, and improved layout.
 */
public class ReceiptScreen implements AppScreen {

    private BorderPane root;
    private CustomerDAO customerDAO = new CustomerDAO();
    
    // Customer search components
    private TextField customerSearchField;
    private ComboBox<String> customerSearchTypeCombo;
    private ComboBox<Customer> customerCombo;
    private List<Customer> allCustomers;
    
    // Item selection components
    private ComboBox<Service> serviceCombo;

    // Carton products (integer qty, +1 step)
    private ComboBox<Product> cartonProductCombo;
    private Spinner<Integer> cartonQtySpinner;

    // Gallon products (float qty, +0.5 step)
    private ComboBox<Product> gallonProductCombo;
    private Spinner<Double> gallonQtySpinner;
    
    // Receipt items table
    private TableView<ReceiptItem> itemsTable;
    private ObservableList<ReceiptItem> itemsList;
    
    // Receipt state
    private Label totalLabel;
    private Label itemCountLabel;
    private Receipt currentReceipt;
    private boolean guestPaidFull = false;
    
    // Barcode scanner
    private BarcodeScannerDialog scannerDialog;

    public ReceiptScreen() {
        buildUI();
        loadCustomers();
        preSelectGuestCustomer();
    }

    private void buildUI() {
        root = new BorderPane();
        root.setStyle(Theme.pageBackgroundStyle());
        
        VBox sidebar = createSidebar();
        root.setLeft(sidebar);
        
        VBox mainContent = new VBox(20);
        mainContent.setPadding(new Insets(24));
        VBox.setVgrow(mainContent, Priority.ALWAYS);
        
        // Page header
        HBox headerBox = new HBox(16);
        headerBox.setAlignment(Pos.CENTER_LEFT);
        
        Button backBtn = Theme.createBackButton();
        backBtn.setOnAction(e -> ScreenNavigator.navigateTo(new DashboardScreen()));
        
        Label titleLabel = new Label("🧾 فاتورة جديدة");
        titleLabel.setStyle(Theme.pageTitleStyle());
        
        Region headerSpacer = new Region();
        HBox.setHgrow(headerSpacer, Priority.ALWAYS);
        
        // Quick action buttons in header
        Button clearAllBtn = Theme.createDangerButton("مسح الكل");
        clearAllBtn.setMinWidth(120);
        clearAllBtn.setOnAction(e -> clearReceipt());
        
        headerBox.getChildren().addAll(backBtn, titleLabel, headerSpacer, clearAllBtn);
        
        // Customer search bar
        HBox customerSearchBox = createCustomerSearchBar();
        
        // Main content split
        HBox contentRow = new HBox(20);
        contentRow.setAlignment(Pos.TOP_LEFT);
        
        VBox leftColumn = buildLeftColumn();
        leftColumn.setMinWidth(450);
        leftColumn.setPrefWidth(500);
        
        VBox rightColumn = buildRightColumn();
        HBox.setHgrow(rightColumn, Priority.ALWAYS);
        
        contentRow.getChildren().addAll(leftColumn, rightColumn);
        VBox.setVgrow(contentRow, Priority.ALWAYS);
        
        mainContent.getChildren().addAll(headerBox, customerSearchBox, contentRow);
        root.setCenter(mainContent);
    }
    
    private HBox createCustomerSearchBar() {
        HBox searchBox = new HBox(12);
        searchBox.setAlignment(Pos.CENTER_LEFT);
        searchBox.setPadding(new Insets(16, 20, 16, 20));
        searchBox.setStyle(Theme.cardStyle());
        
        Label searchLabel = new Label("👤 العميل:");
        searchLabel.setStyle(Theme.labelStyle());
        searchLabel.setMinWidth(100);
        
        // Search type combo
        customerSearchTypeCombo = new ComboBox<>();
        customerSearchTypeCombo.setItems(FXCollections.observableArrayList("الاسم", "رقم العميل"));
        customerSearchTypeCombo.setValue("الاسم");
        customerSearchTypeCombo.setMinWidth(90);
        customerSearchTypeCombo.setPrefWidth(100);
        customerSearchTypeCombo.setStyle("-fx-background-color: white; " +
                "-fx-border-color: " + Theme.GRAY_300 + "; " +
                "-fx-border-width: 1; " +
                "-fx-border-radius: 8; " +
                "-fx-background-radius: 8; " +
                "-fx-font-size: 14px; " +
                "-fx-text-fill: " + Theme.GRAY_800 + ";");
        customerSearchTypeCombo.setButtonCell(new ListCell<String>() {
            @Override
            protected void updateItem(String item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) setText(null);
                else setText(item);
                setStyle("-fx-text-fill: " + Theme.GRAY_800 + "; -fx-font-size: 14px; -fx-font-weight: 600;");
            }
        });
        
        // Search field
        customerSearchField = Theme.createTextField("ابحث عن العميل...");
        customerSearchField.setMinWidth(200);
        customerSearchField.setPrefWidth(280);
        HBox.setHgrow(customerSearchField, Priority.ALWAYS);
        customerSearchField.textProperty().addListener((obs, oldVal, newVal) -> filterCustomers());
        
        // Scan button with camera icon
        Button scanBtn = new Button("📷 سكان");
        scanBtn.setStyle("-fx-background-color: " + Theme.INFO_COLOR + "; " +
                        "-fx-text-fill: white; -fx-font-size: 15px; -fx-font-weight: bold; " +
                        "-fx-padding: 12px 24px; " + Theme.RADIUS_MD + " -fx-cursor: hand; " +
                        "-fx-min-height: 44px;");
        scanBtn.setMinWidth(110);
        scanBtn.setPrefWidth(120);
        scanBtn.setOnMouseEntered(e -> scanBtn.setStyle("-fx-background-color: derive(" + Theme.INFO_COLOR + ", -15%); " +
                        "-fx-text-fill: white; -fx-font-size: 15px; -fx-font-weight: bold; " +
                        "-fx-padding: 12px 24px; " + Theme.RADIUS_MD + " -fx-cursor: hand; " +
                        "-fx-min-height: 44px;"));
        scanBtn.setOnMouseExited(e -> scanBtn.setStyle("-fx-background-color: " + Theme.INFO_COLOR + "; " +
                        "-fx-text-fill: white; -fx-font-size: 15px; -fx-font-weight: bold; " +
                        "-fx-padding: 12px 24px; " + Theme.RADIUS_MD + " -fx-cursor: hand; " +
                        "-fx-min-height: 44px;"));
        scanBtn.setOnAction(e -> openBarcodeScanner());
        
        // Customer dropdown
        customerCombo = new ComboBox<>();
        customerCombo.setMinWidth(200);
        customerCombo.setPrefWidth(260);
        customerCombo.setStyle("-fx-background-color: white; -fx-border-color: " + Theme.GRAY_300 + "; " +
                "-fx-border-width: 1; -fx-border-radius: 8; -fx-background-radius: 8; -fx-font-size: 14px;");
        customerCombo.setPromptText("اختار العميل...");
        customerCombo.setButtonCell(new ListCell<Customer>() {
            @Override
            protected void updateItem(Customer item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) {
                    setText("اختار العميل...");
                } else {
                    setText(item.getName());
                }
                setStyle("-fx-text-fill: " + Theme.GRAY_800 + "; -fx-font-size: 14px; -fx-padding: 8px;");
            }
        });
        customerCombo.setCellFactory(listView -> new ListCell<Customer>() {
            @Override
            protected void updateItem(Customer item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) {
                    setText(null);
                } else {
                    setText(item.getName() + " (رقم: " + item.getCustomerId() + ")");
                }
                setStyle("-fx-text-fill: " + Theme.GRAY_800 + "; -fx-font-size: 14px; -fx-padding: 8px;");
            }
        });
        customerCombo.setOnAction(e -> {
            Customer selected = customerCombo.getValue();
            if (selected != null) startNewReceipt(selected);
        });
        
        Button clearSearchBtn = Theme.createSecondaryButton("مسح");
        clearSearchBtn.setMinWidth(90);
        clearSearchBtn.setPrefWidth(100);
        clearSearchBtn.setOnAction(e -> {
            customerSearchField.clear();
            loadCustomers();
        });
        
        searchBox.getChildren().addAll(searchLabel, customerSearchTypeCombo, customerSearchField, 
                scanBtn, customerCombo, clearSearchBtn);
        return searchBox;
    }
    
    private void openBarcodeScanner() {
        try {
            scannerDialog = new BarcodeScannerDialog();
            scannerDialog.setOnBarcodeScanned(barcode -> {
                Platform.runLater(() -> {
                    searchCustomerByBarcode(barcode);
                });
            });
            scannerDialog.show();
        } catch (Exception e) {
            showManualBarcodeDialog();
        }
    }
    
    private void showManualBarcodeDialog() {
        TextInputDialog dialog = new TextInputDialog();
        dialog.setTitle("إدخال الباركود");
        dialog.setHeaderText("📷 أدخل رقم العميل أو الباركود");
        dialog.setContentText("رقم العميل:");
        
        dialog.showAndWait().ifPresent(barcode -> {
            if (!barcode.trim().isEmpty()) {
                searchCustomerByBarcode(barcode.trim());
            }
        });
    }
    
    private void searchCustomerByBarcode(String barcode) {
        if (barcode == null || barcode.isEmpty()) {
            showError("لم يتم قراءة باركود");
            return;
        }
        
        try {
            String cleanId = barcode.replaceFirst("^0+", "");
            if (cleanId.isEmpty()) cleanId = "0";
            
            int customerId = Integer.parseInt(cleanId);
            Customer customer = allCustomers.stream()
                .filter(c -> c.getCustomerId() == customerId)
                .findFirst()
                .orElse(null);
            
            if (customer != null) {
                customerCombo.setValue(customer);
                startNewReceipt(customer);
                showInfo("تم العثور على العميل: " + customer.getName() + " (رقم: " + customer.getCustomerId() + ")");
            } else {
                showError("لا يوجد عميل بالرقم: " + customerId);
            }
        } catch (NumberFormatException e) {
            showError("صيغة الباركود غير صحيحة.");
        }
    }
    
    private VBox buildLeftColumn() {
        VBox column = new VBox(20);
        
        // Add Services card
        VBox servicesCard = new VBox(16);
        servicesCard.setStyle(Theme.cardStyle());
        
        Label servicesTitle = new Label("🧽 إضافة خدمات");
        servicesTitle.setStyle(Theme.sectionTitleStyle());
        
        HBox serviceRow = new HBox(12);
        serviceRow.setAlignment(Pos.CENTER_LEFT);
        
        serviceCombo = new ComboBox<>();
        serviceCombo.setPrefWidth(350);
        serviceCombo.setMinWidth(300);
        serviceCombo.setStyle("-fx-background-color: white; -fx-border-color: " + Theme.GRAY_300 + "; " +
                "-fx-border-width: 1; -fx-border-radius: 8; -fx-background-radius: 8; -fx-font-size: 14px;");
        serviceCombo.setItems(FXCollections.observableArrayList(ReceiptService.getAllServices()));
        serviceCombo.setPromptText("اختار الخدمة...");
        serviceCombo.setButtonCell(new ListCell<Service>() {
            @Override
            protected void updateItem(Service item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) setText("اختار الخدمة...");
                else setText(item.getServiceName() + " — " + MoneyFormat.egp(item.getPrice()));
                setStyle("-fx-text-fill: " + Theme.GRAY_800 + "; -fx-font-size: 14px; -fx-padding: 8px;");
            }
        });
        serviceCombo.setCellFactory(listView -> new ListCell<Service>() {
            @Override
            protected void updateItem(Service item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) setText(null);
                else setText(item.getServiceName() + " — " + MoneyFormat.egp(item.getPrice()));
                setStyle("-fx-text-fill: " + Theme.GRAY_800 + "; -fx-font-size: 14px; -fx-padding: 8px;");
            }
        });
        
        Button addServiceBtn = Theme.createSuccessButton("إضافة");
        addServiceBtn.setMinWidth(90);
        addServiceBtn.setOnAction(e -> addServiceToReceipt());
        
        serviceRow.getChildren().addAll(serviceCombo, addServiceBtn);
        servicesCard.getChildren().addAll(servicesTitle, serviceRow);
        
        // ── Carton Products Card ──────────────────────────────────────────────
        VBox cartonCard = new VBox(16);
        cartonCard.setStyle(Theme.cardStyle());

        Label cartonTitle = new Label("📦 منتجات بالكرتونة");
        cartonTitle.setStyle(Theme.sectionTitleStyle());

        HBox cartonRow = new HBox(10);
        cartonRow.setAlignment(Pos.CENTER_LEFT);

        cartonProductCombo = new ComboBox<>();
        HBox.setHgrow(cartonProductCombo, Priority.ALWAYS);
        cartonProductCombo.setMaxWidth(Double.MAX_VALUE);
        cartonProductCombo.setMinWidth(160);
        cartonProductCombo.setStyle("-fx-background-color: white; -fx-border-color: " + Theme.GRAY_300 + "; " +
                "-fx-border-width: 1; -fx-border-radius: 8; -fx-background-radius: 8; -fx-font-size: 14px;");
        java.util.List<Product> cartonProducts = ReceiptService.getAllProducts().stream()
                .filter(p -> p.isCarton()).collect(java.util.stream.Collectors.toList());
        cartonProductCombo.setItems(FXCollections.observableArrayList(cartonProducts));
        cartonProductCombo.setPromptText("اختار منتج كرتونة...");
        cartonProductCombo.setButtonCell(new ListCell<Product>() {
            @Override protected void updateItem(Product item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) setText("اختار منتج كرتونة...");
                else setText(item.getProductName() + " — " + MoneyFormat.egp(item.getUnitPrice())
                        + " (متبقي: " + (int)item.getStockQuantity() + " قطعة)");
                setStyle("-fx-text-fill: " + Theme.GRAY_800 + "; -fx-font-size: 14px; -fx-padding: 8px;");
            }
        });
        cartonProductCombo.setCellFactory(lv -> new ListCell<Product>() {
            @Override protected void updateItem(Product item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) setText(null);
                else {
                    setText(item.getProductName() + " — " + MoneyFormat.egp(item.getUnitPrice())
                            + " (متبقي: " + (int)item.getStockQuantity() + " قطعة)");
                    setStyle("-fx-text-fill: " + (item.isOutOfStock() ? Theme.DANGER_COLOR : Theme.GRAY_800)
                            + "; -fx-font-size: 14px; -fx-padding: 8px;");
                }
            }
        });

        cartonQtySpinner = new Spinner<>(1, 9999, 1, 1);
        cartonQtySpinner.setPrefWidth(80);
        cartonQtySpinner.setMinWidth(70);
        cartonQtySpinner.setStyle("-fx-background-color: white; -fx-border-color: " + Theme.GRAY_300 + "; " +
                "-fx-border-width: 1; -fx-border-radius: 8;");
        cartonQtySpinner.setEditable(true);

        Button addCartonBtn = Theme.createSuccessButton("إضافة");
        addCartonBtn.setMinWidth(90);
        addCartonBtn.setOnAction(e -> addCartonProductToReceipt());

        cartonRow.getChildren().addAll(cartonProductCombo, cartonQtySpinner, addCartonBtn);
        cartonCard.getChildren().addAll(cartonTitle, cartonRow);

        // ── Gallon Products Card ──────────────────────────────────────────────
        VBox gallonCard = new VBox(16);
        gallonCard.setStyle(Theme.cardStyle());

        Label gallonTitle = new Label("🧴 منتجات بالليتر (جالون)");
        gallonTitle.setStyle(Theme.sectionTitleStyle());

        HBox gallonRow = new HBox(10);
        gallonRow.setAlignment(Pos.CENTER_LEFT);

        gallonProductCombo = new ComboBox<>();
        HBox.setHgrow(gallonProductCombo, Priority.ALWAYS);
        gallonProductCombo.setMaxWidth(Double.MAX_VALUE);
        gallonProductCombo.setMinWidth(160);
        gallonProductCombo.setStyle("-fx-background-color: white; -fx-border-color: " + Theme.GRAY_300 + "; " +
                "-fx-border-width: 1; -fx-border-radius: 8; -fx-background-radius: 8; -fx-font-size: 14px;");
        java.util.List<Product> gallonProducts = ReceiptService.getAllProducts().stream()
                .filter(p -> p.isGallon()).collect(java.util.stream.Collectors.toList());
        gallonProductCombo.setItems(FXCollections.observableArrayList(gallonProducts));
        gallonProductCombo.setPromptText("اختار منتج جالون...");
        gallonProductCombo.setButtonCell(new ListCell<Product>() {
            @Override protected void updateItem(Product item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) setText("اختار منتج جالون...");
                else setText(item.getProductName() + " — " + MoneyFormat.egp(item.getUnitPrice())
                        + " (متبقي: " + String.format("%.1f", item.getStockQuantity()) + " لتر)");
                setStyle("-fx-text-fill: " + Theme.GRAY_800 + "; -fx-font-size: 14px; -fx-padding: 8px;");
            }
        });
        gallonProductCombo.setCellFactory(lv -> new ListCell<Product>() {
            @Override protected void updateItem(Product item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) setText(null);
                else {
                    setText(item.getProductName() + " — " + MoneyFormat.egp(item.getUnitPrice())
                            + " (متبقي: " + String.format("%.1f", item.getStockQuantity()) + " لتر)");
                    setStyle("-fx-text-fill: " + (item.isOutOfStock() ? Theme.DANGER_COLOR : Theme.GRAY_800)
                            + "; -fx-font-size: 14px; -fx-padding: 8px;");
                }
            }
        });

        // Gallon spinner: step = 0.5
        javafx.util.StringConverter<Double> doubleConverter = new javafx.util.StringConverter<Double>() {
            @Override public String toString(Double val) {
                if (val == null) return "0.5";
                return (val == Math.floor(val)) ? String.valueOf(val.intValue()) + ".0"
                        : String.format("%.1f", val);
            }
            @Override public Double fromString(String s) {
                try { return Double.parseDouble(s.trim()); } catch (Exception ex) { return 0.5; }
            }
        };
        SpinnerValueFactory.DoubleSpinnerValueFactory gallonVF =
                new SpinnerValueFactory.DoubleSpinnerValueFactory(0.5, 9999.0, 0.5, 0.5);
        gallonVF.setConverter(doubleConverter);
        gallonQtySpinner = new Spinner<>(gallonVF);
        gallonQtySpinner.setPrefWidth(90);
        gallonQtySpinner.setMinWidth(80);
        gallonQtySpinner.setEditable(true);
        gallonQtySpinner.setStyle("-fx-background-color: white; -fx-border-color: " + Theme.GRAY_300 + "; " +
                "-fx-border-width: 1; -fx-border-radius: 8;");

        Button addGallonBtn = Theme.createSuccessButton("إضافة");
        addGallonBtn.setMinWidth(90);
        addGallonBtn.setOnAction(e -> addGallonProductToReceipt());

        gallonRow.getChildren().addAll(gallonProductCombo, gallonQtySpinner, addGallonBtn);
        gallonCard.getChildren().addAll(gallonTitle, gallonRow);

        column.getChildren().addAll(servicesCard, cartonCard, gallonCard);
        return column;
    }
    
    private VBox buildRightColumn() {
        VBox column = new VBox(20);
        HBox.setHgrow(column, Priority.ALWAYS);
        
        VBox itemsCard = new VBox(16);
        itemsCard.setStyle(Theme.cardStyle());
        VBox.setVgrow(itemsCard, Priority.ALWAYS);
        
        // Table header with hint
        HBox tableHeader = new HBox(16);
        tableHeader.setAlignment(Pos.CENTER_LEFT);
        tableHeader.setPadding(new Insets(0, 0, 12, 0));
        
        Label tableTitle = new Label("عناصر الفاتورة");
        tableTitle.setStyle(Theme.sectionTitleStyle());
        
        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);
        
        itemCountLabel = new Label("0 عنصر");
        itemCountLabel.setStyle("-fx-font-size: 14px; -fx-text-fill: " + Theme.GRAY_500 + "; -fx-font-weight: 600;");
        
        Label hintLabel = new Label("💡 كليك مرتين للحذف • كليك يمين لخيارات اكتر");
        hintLabel.setStyle("-fx-font-size: 13px; -fx-text-fill: " + Theme.GRAY_500 + ";");
        
        tableHeader.getChildren().addAll(tableTitle, itemCountLabel, spacer, hintLabel);
        
        // Items table
        itemsList = FXCollections.observableArrayList();
        itemsTable = new TableView<>(itemsList);
        itemsTable.setStyle("-fx-background-color: white; -fx-background-radius: 8;");
        VBox.setVgrow(itemsTable, Priority.ALWAYS);
        itemsTable.setFixedCellSize(48);
        
        // Enable context menu (right-click)
        itemsTable.setRowFactory(tv -> {
            TableRow<ReceiptItem> row = new TableRow<>();
            
            ContextMenu contextMenu = new ContextMenu();
            
            MenuItem removeItem = new MenuItem("🗑️ حذف العنصر");
            removeItem.setStyle("-fx-font-size: 14px;");
            removeItem.setOnAction(e -> removeItem(row.getItem()));
            
            MenuItem editQtyItem = new MenuItem("✏️ تعديل الكمية");
            editQtyItem.setStyle("-fx-font-size: 14px;");
            editQtyItem.setOnAction(e -> editItemQuantity(row.getItem()));
            
            contextMenu.getItems().addAll(removeItem, editQtyItem);
            row.contextMenuProperty().set(contextMenu);
            
            // Double-click to remove
            row.setOnMouseClicked(event -> {
                if (event.getClickCount() == 2 && !row.isEmpty()) {
                    removeItem(row.getItem());
                }
            });
            
            return row;
        });
        
        // Name Column
        TableColumn<ReceiptItem, String> nameCol = new TableColumn<>("العنصر");
        nameCol.setMinWidth(150);
        nameCol.setPrefWidth(230);
        nameCol.setStyle("-fx-alignment: CENTER-LEFT;");
        nameCol.setCellFactory(col -> new TableCell<ReceiptItem, String>() {
            @Override
            protected void updateItem(String item, boolean empty) {
                super.updateItem(item, empty);
                if (empty) {
                    setText(null);
                    setGraphic(null);
                } else {
                    ReceiptItem ri = getTableView().getItems().get(getIndex());
                    String name = "";
                    if (ri instanceof oop.carwash.model.ReceiptService) {
                        name = ((oop.carwash.model.ReceiptService) ri).getServiceName();
                    } else if (ri instanceof ReceiptProduct) {
                        name = ((ReceiptProduct) ri).getProductName();
                    }
                    setText(name);
                    setStyle("-fx-text-fill: " + Theme.GRAY_800 + "; -fx-font-size: 14px; " +
                            "-fx-padding: 10px 14px; -fx-font-weight: 600;");
                }
            }
        });
        
        // Quantity Column
        TableColumn<ReceiptItem, String> qtyCol = new TableColumn<>("الكمية");
        qtyCol.setMinWidth(60);
        qtyCol.setPrefWidth(70);
        qtyCol.setStyle("-fx-alignment: CENTER;");
        qtyCol.setCellFactory(col -> new TableCell<ReceiptItem, String>() {
            @Override
            protected void updateItem(String item, boolean empty) {
                super.updateItem(item, empty);
                if (empty) {
                    setText(null);
                    setGraphic(null);
                } else {
                    ReceiptItem ri = getTableView().getItems().get(getIndex());
                    double qty = 1;
                    if (ri instanceof ReceiptProduct) {
                        qty = ((ReceiptProduct) ri).getQuantity();
                    }
                    String qtyStr = (qty == Math.floor(qty)) ? String.valueOf((int) qty) : String.format("%.2f", qty);
                    setText(qtyStr);
                    setStyle("-fx-text-fill: " + Theme.GRAY_600 + "; -fx-font-size: 14px; " +
                            "-fx-padding: 10px 14px;");
                }
            }
        });
        
        // Price Column
        TableColumn<ReceiptItem, String> priceCol = new TableColumn<>("سعر الوحدة");
        priceCol.setMinWidth(100);
        priceCol.setPrefWidth(110);
        priceCol.setStyle("-fx-alignment: CENTER-RIGHT;");
        priceCol.setCellFactory(col -> new TableCell<ReceiptItem, String>() {
            @Override
            protected void updateItem(String item, boolean empty) {
                super.updateItem(item, empty);
                if (empty) {
                    setText(null);
                    setGraphic(null);
                } else {
                    ReceiptItem ri = getTableView().getItems().get(getIndex());
                    double price = 0;
                    if (ri instanceof oop.carwash.model.ReceiptService) {
                        price = ((oop.carwash.model.ReceiptService) ri).getPriceAtTime();
                    } else if (ri instanceof ReceiptProduct) {
                        price = ((ReceiptProduct) ri).getUnitPriceAtTime();
                    }
                    setText(MoneyFormat.egp(price));
                    setStyle("-fx-text-fill: " + Theme.GRAY_600 + "; -fx-font-size: 14px; " +
                            "-fx-padding: 10px 14px;");
                }
            }
        });
        
        // Total Column
        TableColumn<ReceiptItem, String> totalCol = new TableColumn<>("الإجمالي");
        totalCol.setMinWidth(100);
        totalCol.setPrefWidth(110);
        totalCol.setStyle("-fx-alignment: CENTER-RIGHT;");
        totalCol.setCellFactory(col -> new TableCell<ReceiptItem, String>() {
            @Override
            protected void updateItem(String item, boolean empty) {
                super.updateItem(item, empty);
                if (empty) {
                    setText(null);
                    setGraphic(null);
                } else {
                    ReceiptItem ri = getTableView().getItems().get(getIndex());
                    setText(MoneyFormat.egp(ri.getTotal()));
                    setStyle("-fx-text-fill: " + Theme.SUCCESS_COLOR + "; -fx-font-size: 14px; " +
                            "-fx-padding: 10px 14px; -fx-font-weight: bold;");
                }
            }
        });
        
        // Actions Column
        TableColumn<ReceiptItem, Void> actionsCol = new TableColumn<>("الإجراءات");
        actionsCol.setMinWidth(90);
        actionsCol.setPrefWidth(100);
        actionsCol.setStyle("-fx-alignment: CENTER;");
        actionsCol.setCellFactory(col -> new TableCell<ReceiptItem, Void>() {
            private final Button removeBtn = new Button("✕");
            
            {
                removeBtn.setStyle("-fx-background-color: " + Theme.DANGER_COLOR + "; " +
                                "-fx-text-fill: white; -fx-font-size: 13px; " +
                                "-fx-padding: 6px 12px; -fx-background-radius: 6; -fx-cursor: hand;");
                removeBtn.setOnMouseEntered(e -> removeBtn.setStyle("-fx-background-color: " + Theme.DANGER_LIGHT + "; " +
                                "-fx-text-fill: white; -fx-font-size: 13px; " +
                                "-fx-padding: 6px 12px; -fx-background-radius: 6; -fx-cursor: hand;"));
                removeBtn.setOnMouseExited(e -> removeBtn.setStyle("-fx-background-color: " + Theme.DANGER_COLOR + "; " +
                                "-fx-text-fill: white; -fx-font-size: 13px; " +
                                "-fx-padding: 6px 12px; -fx-background-radius: 6; -fx-cursor: hand;"));
            }
            
            @Override
            protected void updateItem(Void item, boolean empty) {
                super.updateItem(item, empty);
                if (empty) {
                    setGraphic(null);
                } else {
                    ReceiptItem ri = getTableView().getItems().get(getIndex());
                    removeBtn.setOnAction(e -> removeItem(ri));
                    setGraphic(removeBtn);
                }
            }
        });
        
        itemsTable.getColumns().addAll(nameCol, qtyCol, priceCol, totalCol, actionsCol);
        itemsTable.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);
        
        // Total section
        HBox totalRow = new HBox(16);
        totalRow.setAlignment(Pos.CENTER_RIGHT);
        totalRow.setPadding(new Insets(16, 0, 0, 0));
        Label totalTextLabel = new Label("الإجمالي:");
        totalTextLabel.setStyle("-fx-font-size: 20px; -fx-font-weight: bold; -fx-text-fill: " + Theme.GRAY_700 + ";");
        totalLabel = new Label(MoneyFormat.egp(0));
        totalLabel.setStyle("-fx-font-size: 36px; -fx-font-weight: bold; -fx-text-fill: " + Theme.SUCCESS_COLOR + ";");
        totalRow.getChildren().addAll(totalTextLabel, totalLabel);
        
        // Action buttons
        VBox buttonsContainer = new VBox(12);
        buttonsContainer.setPadding(new Insets(16, 0, 0, 0));
        
        HBox buttonsRow1 = new HBox(12);
        buttonsRow1.setAlignment(Pos.CENTER_RIGHT);
        Button saveBtn = Theme.createPrimaryButton("💾 حفظ الفاتورة");
        saveBtn.setMinWidth(160);
        saveBtn.setOnAction(e -> saveReceipt());
        Button printBtn = Theme.createSecondaryButton("🖨️ طباعة");
        printBtn.setMinWidth(120);
        printBtn.setOnAction(e -> printReceipt());
        buttonsRow1.getChildren().addAll(saveBtn, printBtn);
        
        HBox buttonsRow2 = new HBox(12);
        buttonsRow2.setAlignment(Pos.CENTER_RIGHT);
        Button payNowBtn = Theme.createSuccessButton("💳 حفظ ودفع");
        payNowBtn.setMinWidth(160);
        payNowBtn.setOnAction(e -> saveAndPayNow());
        buttonsRow2.getChildren().addAll(payNowBtn);
        
        buttonsContainer.getChildren().addAll(buttonsRow1, buttonsRow2);
        
        itemsCard.getChildren().addAll(tableHeader, itemsTable, totalRow, buttonsContainer);
        VBox.setVgrow(itemsCard, Priority.ALWAYS);
        
        column.getChildren().add(itemsCard);
        return column;
    }
    
    private void filterCustomers() {
        if (allCustomers == null || allCustomers.isEmpty()) {
            loadCustomers();
            return;
        }
        
        String searchText = customerSearchField.getText().trim().toLowerCase();
        String searchType = customerSearchTypeCombo.getValue();
        
        if (searchText.isEmpty()) {
            customerCombo.setItems(FXCollections.observableArrayList(allCustomers));
            return;
        }
        
        List<Customer> filtered;
        if ("رقم العميل".equals(searchType)) {
            filtered = allCustomers.stream()
                .filter(c -> String.valueOf(c.getCustomerId()).contains(searchText))
                .collect(Collectors.toList());
        } else {
            filtered = allCustomers.stream()
                .filter(c -> c.getName().toLowerCase().contains(searchText))
                .collect(Collectors.toList());
        }
        
        customerCombo.setItems(FXCollections.observableArrayList(filtered));
        
        // Auto-select first match if available
        if (!filtered.isEmpty()) {
            customerCombo.show();
        }
    }
    
    private void loadCustomers() {
        allCustomers = customerDAO.findAll();
        customerCombo.setItems(FXCollections.observableArrayList(allCustomers));
    }
    
    private void preSelectGuestCustomer() {
        for (Customer c : allCustomers) {
            if (c.getName().equals("Guest / Unknown")) {
                customerCombo.setValue(c);
                startNewReceipt(c);
                break;
            }
        }
    }
    
    private boolean isGuestCustomer() {
        Customer selected = customerCombo.getValue();
        return selected != null && selected.getName().equals("Guest / Unknown");
    }

    private void startNewReceipt(Customer customer) {
        currentReceipt = new Receipt(customer.getCustomerId(), AuthService.getCurrentUser().getUserId());
        guestPaidFull = false;
        itemsList.clear();
        updateTotals();
    }
    
    private void addServiceToReceipt() {
        if (currentReceipt == null) { showError("اختر عميلاً أولاً"); return; }
        Service service = serviceCombo.getValue();
        if (service == null) { showError("اختر خدمة"); return; }
        
        currentReceipt.addService(service.getServiceId(), service.getServiceName(), service.getPrice());
        itemsList.setAll(currentReceipt.getItems());
        updateTotals();
        serviceCombo.setValue(null);
    }

    private void addCartonProductToReceipt() {
        if (currentReceipt == null) { showError("اختر عميلاً أولاً"); return; }
        Product product = cartonProductCombo.getValue();
        if (product == null) { showError("اختر منتج كرتونة"); return; }

        int qty = cartonQtySpinner.getValue();
        if (product.isOutOfStock()) {
            showError("⚠️ المنتج «" + product.getProductName() + "» خلص من المخزن!");
            return;
        }
        if (qty > product.getStockQuantity()) {
            showError("⚠️ الكمية المطلوبة (" + qty + " قطعة) أكبر من المتبقي في المخزن ("
                    + (int)product.getStockQuantity() + " قطعة)!");
            return;
        }

        currentReceipt.addProduct(product.getProductId(), product.getProductName(), qty, product.getUnitPrice());
        itemsList.setAll(currentReceipt.getItems());
        updateTotals();
        cartonProductCombo.setValue(null);
        cartonQtySpinner.getValueFactory().setValue(Integer.valueOf(1));
    }

    private void addGallonProductToReceipt() {
        if (currentReceipt == null) { showError("اختر عميلاً أولاً"); return; }
        Product product = gallonProductCombo.getValue();
        if (product == null) { showError("اختر منتج جالون"); return; }

        // Commit any typed value in the editable spinner
        try {
            gallonQtySpinner.commitValue();
        } catch (Exception ignored) {}
        double qty = gallonQtySpinner.getValue();
        if (qty <= 0) { showError("الكمية يجب أن تكون أكبر من صفر"); return; }

        if (product.isOutOfStock()) {
            showError("⚠️ المنتج «" + product.getProductName() + "» خلص من المخزن!");
            return;
        }
        if (qty > product.getStockQuantity()) {
            showError("⚠️ الكمية المطلوبة (" + String.format("%.1f", qty) + " لتر) أكبر من المتبقي في المخزن ("
                    + String.format("%.1f", product.getStockQuantity()) + " لتر)!");
            return;
        }

        currentReceipt.addProduct(product.getProductId(), product.getProductName(), qty, product.getUnitPrice());
        itemsList.setAll(currentReceipt.getItems());
        updateTotals();
        gallonProductCombo.setValue(null);
        gallonQtySpinner.getValueFactory().setValue(Double.valueOf(0.5));
    }
    
    private void removeItem(ReceiptItem item) {
        if (item == null || currentReceipt == null) return;
        
        currentReceipt.getItems().remove(item);
        itemsList.setAll(currentReceipt.getItems());
        updateTotals();
    }
    
    private void editItemQuantity(ReceiptItem item) {
        if (item == null || !(item instanceof ReceiptProduct)) {
            showInfo("يمكن تعديل كمية المنتجات فقط.");
            return;
        }
        
        ReceiptProduct product = (ReceiptProduct) item;
        
        TextInputDialog dialog = new TextInputDialog(String.valueOf(product.getQuantity()));
        dialog.setTitle("تعديل الكمية");
        dialog.setHeaderText("تعديل كمية: " + product.getProductName());
        dialog.setContentText("الكمية الجديدة:");
        
        dialog.showAndWait().ifPresent(newQty -> {
            try {
                // Support float quantity (liters for gallons, int for cartons)
                double qty = Double.parseDouble(newQty.trim());
                if (qty > 0) {
                    product.setQuantity(qty);
                    itemsList.setAll(currentReceipt.getItems());
                    updateTotals();
                } else {
                    showError("الكمية يجب أن تكون أكبر من صفر");
                }
            } catch (NumberFormatException e) {
                showError("كمية غير صحيحة");
            }
        });
    }
    
    private void updateTotals() {
        if (currentReceipt != null) {
            currentReceipt.recalculateTotals();
            totalLabel.setText(MoneyFormat.egp(currentReceipt.getGrandTotal()));
            itemCountLabel.setText(currentReceipt.getItems().size() + " عنصر");
        } else {
            totalLabel.setText(MoneyFormat.egp(0));
            itemCountLabel.setText("0 عنصر");
        }
    }
    
    private void clearReceipt() {
        currentReceipt = null;
        guestPaidFull = false;
        itemsList.clear();
        updateTotals();
        preSelectGuestCustomer();
    }

    private void saveReceipt() {
        if (currentReceipt == null || currentReceipt.getItems().isEmpty()) { 
            showError("أضف عناصر قبل الحفظ"); return; 
        }
        if (isGuestCustomer() && !guestPaidFull) { 
            showError("عملاء «زائر» يجب دفع المبلغ كاملاً.\nاستخدم زر «حفظ ودفع»."); return; 
        }
        ReceiptService.saveReceipt(currentReceipt);
        showInfo("تم حفظ الفاتورة بنجاح!");
        clearReceipt();
    }

    private void printReceipt() {
        if (currentReceipt == null || currentReceipt.getItems().isEmpty()) { 
            showError("لا توجد فاتورة للطباعة."); return; 
        }
        if (isGuestCustomer() && !guestPaidFull) { 
            showError("عملاء «زائر» يجب الدفع أولاً.\nاستخدم «حفظ ودفع»."); return; 
        }
        PrintService.printReceipt(currentReceipt);
        showInfo("تم إرسال الفاتورة للطباعة!");
    }

    private void saveAndPayNow() {
        if (currentReceipt == null || currentReceipt.getItems().isEmpty()) { 
            showError("أضف عناصر قبل الحفظ"); return; 
        }

        double grandTotal = currentReceipt.getGrandTotal();
        ReceiptService.saveReceipt(currentReceipt);
        ReceiptService.recordPayment(currentReceipt.getReceiptId(), grandTotal);
        
        guestPaidFull = true;
        showInfo("تم تسجيل دفعة " + MoneyFormat.egp(grandTotal) + "!");
        printReceipt();
        clearReceipt();
    }
    
    // ==================== SIDEBAR ====================
    
    private VBox createSidebar() {
        VBox sidebar = new VBox(0);
        sidebar.setPrefWidth(240);
        sidebar.setMinWidth(200);
        sidebar.setStyle("-fx-background-color: " + Theme.DARK_COLOR + ";");
        
        VBox logoSection = new VBox(8);
        logoSection.setPadding(new Insets(20, 16, 24, 16));
        logoSection.setAlignment(Pos.CENTER_LEFT);
        Label logoIcon = new Label("🚿");
        logoIcon.setStyle("-fx-font-size: 28px;");
        Label logoText = new Label("مغسلة أبو جميل");
        logoText.setStyle("-fx-font-size: 18px; -fx-font-weight: bold; -fx-text-fill: white;");
        logoSection.getChildren().addAll(logoIcon, logoText);
        
        VBox navSection = new VBox(4);
        navSection.setPadding(new Insets(12, 8, 12, 8));
        
        Button dashboardBtn = createNavButton("لوحة التحكم", "📊");
        dashboardBtn.setOnAction(e -> ScreenNavigator.navigateTo(new DashboardScreen()));
        Button customersBtn = createNavButton("العملاء", "👥");
        customersBtn.setOnAction(e -> ScreenNavigator.navigateTo(new CustomerScreen()));
        Button servicesBtn = createNavButton("الخدمات", "🧽");
        servicesBtn.setOnAction(e -> ScreenNavigator.navigateTo(new ServiceScreen()));
        Button productsBtn = createNavButton("المنتجات", "🛍️");
        productsBtn.setOnAction(e -> ScreenNavigator.navigateTo(new ProductScreen()));
        Button receiptBtn = createNavButton("فاتورة جديدة", "🧾");
        receiptBtn.setStyle(getActiveNavStyle());
        Button paymentsBtn = createNavButton("المدفوعات", "💳");
        paymentsBtn.setOnAction(e -> ScreenNavigator.navigateTo(new PaymentScreen()));
        Button reportsBtn = createNavButton("التقارير", "📈");
        reportsBtn.setOnAction(e -> ScreenNavigator.navigateTo(new ReportsScreen()));
        Button usersBtn = createNavButton("المستخدمين", "⚙️");
        usersBtn.setOnAction(e -> ScreenNavigator.navigateTo(new UserManagementScreen()));
        
        navSection.getChildren().addAll(dashboardBtn, customersBtn, servicesBtn, productsBtn, 
                receiptBtn, paymentsBtn, reportsBtn, usersBtn);
        
        Region spacer = new Region();
        VBox.setVgrow(spacer, Priority.ALWAYS);
        
        Button logoutBtn = createNavButton("تسجيل الخروج", "🚪");
        logoutBtn.setOnAction(e -> { AuthService.logout(); ScreenNavigator.navigateTo(new LoginScreen()); });
        
        sidebar.getChildren().addAll(logoSection, navSection, spacer, logoutBtn);
        return sidebar;
    }
    
    private Button createNavButton(String text, String icon) {
        Button btn = new Button(icon + "  " + text);
        btn.setPrefWidth(Double.MAX_VALUE);
        btn.setAlignment(Pos.CENTER_LEFT);
        btn.setStyle(getInactiveNavStyle());
        btn.setOnMouseEntered(e -> btn.setStyle(getHoverNavStyle()));
        btn.setOnMouseExited(e -> btn.setStyle(getInactiveNavStyle()));
        return btn;
    }
    
    private String getActiveNavStyle() {
        return "-fx-background-color: " + Theme.PRIMARY_COLOR + "; " +
               "-fx-text-fill: white; -fx-font-size: 13px; -fx-font-weight: bold; " +
               "-fx-padding: 12px 16px; " + Theme.RADIUS_MD + " -fx-cursor: hand; " +
               "-fx-border-width: 0; -fx-alignment: CENTER-LEFT;";
    }
    
    private String getInactiveNavStyle() {
        return "-fx-background-color: transparent; " +
               "-fx-text-fill: " + Theme.GRAY_400 + "; -fx-font-size: 13px; " +
               "-fx-padding: 12px 16px; " + Theme.RADIUS_MD + " -fx-cursor: hand; " +
               "-fx-border-width: 0; -fx-alignment: CENTER-LEFT;";
    }
    
    private String getHoverNavStyle() {
        return "-fx-background-color: " + Theme.GRAY_700 + "; " +
               "-fx-text-fill: white; -fx-font-size: 13px; " +
               "-fx-padding: 12px 16px; " + Theme.RADIUS_MD + " -fx-cursor: hand; " +
               "-fx-border-width: 0; -fx-alignment: CENTER-LEFT;";
    }

    private void showError(String message) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle("خطأ");
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }

    private void showInfo(String message) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle("نجاح");
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }

    @Override
    public javafx.scene.Parent getRoot() { return root; }
}

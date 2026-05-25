package oop.carwash.ui;

import oop.carwash.dao.ReceiptDAO;
import oop.carwash.dao.PaymentDAO;
import oop.carwash.dao.CustomerDAO;
import oop.carwash.model.Receipt;
import oop.carwash.model.Payment;
import oop.carwash.model.Customer;
import oop.carwash.service.ReceiptService;
import oop.carwash.util.PrintService;
import oop.carwash.util.MoneyFormat;
import oop.carwash.util.ReceiptLabels;

import javafx.application.Platform;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.*;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.Region;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;

import java.util.List;
import java.util.stream.Collectors;

/**
 * Modern screen for managing receipt payments.
 * IMPROVED: Full consistency with CustomerScreen design patterns.
 * Features: Barcode scanner with camera, double-click, context menu,
 * actions column, and improved styling.
 */
public class PaymentScreen implements AppScreen {

    private BorderPane root;
    private ReceiptDAO receiptDAO = new ReceiptDAO();
    private PaymentDAO paymentDAO = new PaymentDAO();
    private CustomerDAO customerDAO = new CustomerDAO();

    private TableView<Receipt> receiptTable;
    private Receipt selectedReceipt;
    
    // Search components
    private TextField searchField;
    private ComboBox<String> searchTypeCombo;
    private ComboBox<Customer> customerCombo;
    private ComboBox<String> filterCombo;
    
    private List<Receipt> allReceipts;
    private List<Customer> allCustomers;
    
    // Barcode scanner
    private BarcodeScannerDialog scannerDialog;

    // Payment details labels
    private Label receiptIdLabel;
    private Label customerIdLabel;
    private Label customerNameLabel;
    private Label totalLabel;
    private Label paidLabel;
    private Label balanceLabel;
    private Label statusLabel;
    private TextField paymentAmountField;
    private TableView<Payment> paymentHistoryTable;
    
    // Quick payment buttons
    private Button payFullBtn;
    private Button payHalfBtn;

    public PaymentScreen() {
        buildUI();
        loadCustomers();
        loadReceipts("ALL");
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
        
        Label titleLabel = new Label("💳 المدفوعات");
        titleLabel.setStyle(Theme.pageTitleStyle());
        
        Region headerSpacer = new Region();
        HBox.setHgrow(headerSpacer, Priority.ALWAYS);
        
        // Refresh button in header
        Button refreshBtn = Theme.createSecondaryButton("🔄 تحديث");
        refreshBtn.setMinWidth(100);
        refreshBtn.setOnAction(e -> {
            allReceipts = receiptDAO.findAll();
            loadCustomers();
            loadReceipts(selectedPaymentFilterKey());
            searchField.clear();
            showInfo("تم التحديث!");
        });
        
        headerBox.getChildren().addAll(backBtn, titleLabel, headerSpacer, refreshBtn);
        
        // Search bar
        HBox searchBox = createSearchBar();
        
        // Split view
        HBox contentRow = new HBox(20);
        contentRow.setAlignment(Pos.TOP_LEFT);
        
        VBox leftPanel = buildLeftPanel();
        leftPanel.setMinWidth(550);
        VBox rightPanel = buildRightPanel();
        rightPanel.setMinWidth(400);
        
        HBox.setHgrow(leftPanel, Priority.ALWAYS);
        HBox.setHgrow(rightPanel, Priority.ALWAYS);
        
        contentRow.getChildren().addAll(leftPanel, rightPanel);
        VBox.setVgrow(contentRow, Priority.ALWAYS);
        
        mainContent.getChildren().addAll(headerBox, searchBox, contentRow);
        root.setCenter(mainContent);
    }
    
    private HBox createSearchBar() {
        HBox searchBox = new HBox(12);
        searchBox.setAlignment(Pos.CENTER_LEFT);
        searchBox.setPadding(new Insets(16, 20, 16, 20));
        searchBox.setStyle(Theme.cardStyle());
        
        Label searchLabel = new Label("👤 العميل:");
        searchLabel.setStyle(Theme.labelStyle());
        searchLabel.setMinWidth(100);
        
        // Search type combo
        searchTypeCombo = new ComboBox<>();
        searchTypeCombo.setItems(FXCollections.observableArrayList("الاسم", "رقم العميل"));
        searchTypeCombo.setValue("الاسم");
        searchTypeCombo.setMinWidth(90);
        searchTypeCombo.setPrefWidth(100);
        searchTypeCombo.setStyle("-fx-background-color: white; " +
                "-fx-border-color: " + Theme.GRAY_300 + "; " +
                "-fx-border-width: 1; " +
                "-fx-border-radius: 8; " +
                "-fx-background-radius: 8; " +
                "-fx-font-size: 14px; " +
                "-fx-text-fill: " + Theme.GRAY_800 + ";");
        searchTypeCombo.setButtonCell(new ListCell<String>() {
            @Override
            protected void updateItem(String item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) setText(null);
                else setText(item);
                setStyle("-fx-text-fill: " + Theme.GRAY_800 + "; -fx-font-size: 14px; -fx-font-weight: 600;");
            }
        });
        
        // Search field
        searchField = Theme.createTextField("ابحث عن العميل...");
        searchField.setMinWidth(200);
        searchField.setPrefWidth(280);
        HBox.setHgrow(searchField, Priority.ALWAYS);
        searchField.textProperty().addListener((obs, oldVal, newVal) -> filterCustomerCombo());
        
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
            if (selected != null) filterReceiptsByCustomer(selected);
        });
        
        Button clearBtn = Theme.createSecondaryButton("مسح");
        clearBtn.setMinWidth(90);
        clearBtn.setPrefWidth(100);
        clearBtn.setOnAction(e -> {
            searchField.clear();
            loadCustomers();
            loadReceipts(selectedPaymentFilterKey());
        });
        
        searchBox.getChildren().addAll(searchLabel, searchTypeCombo, searchField, 
                scanBtn, customerCombo, clearBtn);
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
                filterReceiptsByCustomer(customer);
                showInfo("تم العثور على العميل: " + customer.getName() + " (رقم: " + customer.getCustomerId() + ")");
            } else {
                showError("لا يوجد عميل بالرقم: " + customerId);
            }
        } catch (NumberFormatException e) {
            showError("صيغة الباركود غير صحيحة.");
        }
    }
    
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
        receiptBtn.setOnAction(e -> ScreenNavigator.navigateTo(new ReceiptScreen()));
        
        Button paymentsBtn = createNavButton("المدفوعات", "💳");
        paymentsBtn.setStyle(getActiveNavStyle());
        
        Button reportsBtn = createNavButton("التقارير", "📈");
        reportsBtn.setOnAction(e -> ScreenNavigator.navigateTo(new ReportsScreen()));
        
        Button usersBtn = createNavButton("المستخدمين", "⚙️");
        usersBtn.setOnAction(e -> ScreenNavigator.navigateTo(new UserManagementScreen()));
        
        navSection.getChildren().addAll(dashboardBtn, customersBtn, servicesBtn, productsBtn,
            receiptBtn, paymentsBtn, reportsBtn, usersBtn);
        
        Region spacer = new Region();
        VBox.setVgrow(spacer, Priority.ALWAYS);
        
        Button logoutBtn = createNavButton("تسجيل الخروج", "🚪");
        logoutBtn.setOnAction(e -> {
            oop.carwash.service.AuthService.logout();
            ScreenNavigator.navigateTo(new LoginScreen());
        });
        
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

    private VBox buildLeftPanel() {
        VBox panel = new VBox(16);
        panel.setStyle(Theme.cardStyle());
        VBox.setVgrow(panel, Priority.ALWAYS);
        
        // Table header with hint
        HBox tableHeader = new HBox(16);
        tableHeader.setAlignment(Pos.CENTER_LEFT);
        tableHeader.setPadding(new Insets(0, 0, 12, 0));
        
        Label panelTitle = new Label("الفواتير");
        panelTitle.setStyle(Theme.sectionTitleStyle());
        
        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);
        
        Label hintLabel = new Label("💡 كليك مرتين للتفاصيل • كليك يمين لخيارات اكتر");
        hintLabel.setStyle("-fx-font-size: 13px; -fx-text-fill: " + Theme.GRAY_500 + ";");
        
        tableHeader.getChildren().addAll(panelTitle, spacer, hintLabel);
        
        // Filter row
        HBox filterRow = new HBox(12);
        filterRow.setAlignment(Pos.CENTER_LEFT);
        
        Label filterLabel = new Label("تصفية:");
        filterLabel.setStyle(Theme.labelStyle());
        filterLabel.setMinWidth(50);
        
        filterCombo = new ComboBox<>();
        filterCombo.setItems(FXCollections.observableArrayList(ReceiptLabels.paymentFilterOptionsArabic()));
        filterCombo.setValue("الكل");
        filterCombo.setMinWidth(100);
        filterCombo.setPrefWidth(110);
        filterCombo.setStyle("-fx-background-color: white; " +
                "-fx-border-color: " + Theme.GRAY_300 + "; " +
                "-fx-border-width: 1; " +
                "-fx-border-radius: 8; " +
                "-fx-background-radius: 8; " +
                "-fx-font-size: 14px; " +
                "-fx-text-fill: " + Theme.GRAY_800 + ";");
        filterCombo.setButtonCell(new ListCell<String>() {
            @Override
            protected void updateItem(String item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) setText(null);
                else setText(item);
                setStyle("-fx-text-fill: " + Theme.GRAY_800 + "; -fx-font-size: 14px; -fx-font-weight: 600;");
            }
        });
        filterCombo.setOnAction(e -> {
            Customer selectedCustomer = customerCombo.getValue();
            if (selectedCustomer != null) {
                filterReceiptsByCustomer(selectedCustomer);
            } else {
                loadReceipts(selectedPaymentFilterKey());
            }
        });
        
        filterRow.getChildren().addAll(filterLabel, filterCombo);
        
        // Table
        receiptTable = new TableView<>();
        receiptTable.setStyle("-fx-background-color: white; -fx-background-radius: 8;");
        VBox.setVgrow(receiptTable, Priority.ALWAYS);
        receiptTable.setFixedCellSize(48);
        
        // Enable context menu (right-click) and double-click
        receiptTable.setRowFactory(tv -> {
            TableRow<Receipt> row = new TableRow<>();
            
            ContextMenu contextMenu = new ContextMenu();
            
            MenuItem viewItem = new MenuItem("👁️ عرض التفاصيل");
            viewItem.setStyle("-fx-font-size: 14px;");
            viewItem.setOnAction(e -> {
                selectedReceipt = row.getItem();
                displayPaymentDetails();
            });
            
            MenuItem payItem = new MenuItem("💳 تسجيل دفعة");
            payItem.setStyle("-fx-font-size: 14px;");
            payItem.setOnAction(e -> {
                selectedReceipt = row.getItem();
                displayPaymentDetails();
                paymentAmountField.requestFocus();
            });
            
            MenuItem printItem = new MenuItem("🖨️ طباعة الفاتورة");
            printItem.setStyle("-fx-font-size: 14px;");
            printItem.setOnAction(e -> printSelectedReceipt(row.getItem()));
            
            contextMenu.getItems().addAll(viewItem, payItem, new SeparatorMenuItem(), printItem);
            row.contextMenuProperty().set(contextMenu);
            
            // Double-click to view details
            row.setOnMouseClicked(event -> {
                if (event.getClickCount() == 2 && !row.isEmpty()) {
                    selectedReceipt = row.getItem();
                    displayPaymentDetails();
                }
            });
            
            return row;
        });

        // Receipt # Column
        TableColumn<Receipt, String> idCol = new TableColumn<>("رقم الفاتورة");
        idCol.setMinWidth(90);
        idCol.setPrefWidth(100);
        idCol.setCellFactory(col -> new TableCell<Receipt, String>() {
            @Override
            protected void updateItem(String item, boolean empty) {
                super.updateItem(item, empty);
                if (empty) { setText(null); setGraphic(null); }
                else {
                    Receipt r = getTableView().getItems().get(getIndex());
                    setText("#" + r.getReceiptId());
                    setStyle("-fx-text-fill: " + Theme.GRAY_800 + "; -fx-font-size: 14px; " +
                            "-fx-padding: 10px 14px; -fx-font-weight: bold;");
                }
            }
        });

        // Customer Column
        TableColumn<Receipt, String> customerCol = new TableColumn<>("العميل");
        customerCol.setMinWidth(140);
        customerCol.setPrefWidth(160);
        customerCol.setCellFactory(col -> new TableCell<Receipt, String>() {
            @Override
            protected void updateItem(String item, boolean empty) {
                super.updateItem(item, empty);
                if (empty) { setText(null); setGraphic(null); }
                else {
                    Receipt r = getTableView().getItems().get(getIndex());
                    Customer c = customerDAO.findById(r.getCustomerId()).orElse(null);
                    setText(c != null ? c.getName() : "رقم: " + r.getCustomerId());
                    setStyle("-fx-text-fill: " + Theme.GRAY_800 + "; -fx-font-size: 14px; " +
                            "-fx-padding: 10px 14px;");
                }
            }
        });

        // Total Column
        TableColumn<Receipt, String> totalCol = new TableColumn<>("الإجمالي");
        totalCol.setMinWidth(90);
        totalCol.setPrefWidth(100);
        totalCol.setCellFactory(col -> new TableCell<Receipt, String>() {
            @Override
            protected void updateItem(String item, boolean empty) {
                super.updateItem(item, empty);
                if (empty) { setText(null); setGraphic(null); }
                else {
                    Receipt r = getTableView().getItems().get(getIndex());
                    setText(MoneyFormat.egp(r.getGrandTotal()));
                    setStyle("-fx-text-fill: " + Theme.SUCCESS_COLOR + "; -fx-font-size: 14px; " +
                            "-fx-padding: 10px 14px; -fx-font-weight: bold;");
                }
            }
        });

        // Status Column
        TableColumn<Receipt, String> statusCol = new TableColumn<>("الحالة");
        statusCol.setMinWidth(90);
        statusCol.setPrefWidth(100);
        statusCol.setCellFactory(col -> new TableCell<Receipt, String>() {
            @Override
            protected void updateItem(String item, boolean empty) {
                super.updateItem(item, empty);
                if (empty) { setText(null); setGraphic(null); }
                else {
                    Receipt r = getTableView().getItems().get(getIndex());
                    setText(ReceiptLabels.statusArabic(r.getStatus()));
                    String color = Theme.GRAY_600;
                    if ("UNPAID".equals(r.getStatus())) color = Theme.DANGER_COLOR;
                    else if ("PARTIAL".equals(r.getStatus())) color = Theme.WARNING_COLOR;
                    else if ("PAID".equals(r.getStatus())) color = Theme.SUCCESS_COLOR;
                    setStyle("-fx-text-fill: " + color + "; -fx-font-size: 14px; " +
                            "-fx-padding: 10px 14px; -fx-font-weight: bold;");
                }
            }
        });
        
        // Actions Column
        TableColumn<Receipt, Void> actionsCol = new TableColumn<>("الإجراءات");
        actionsCol.setMinWidth(100);
        actionsCol.setPrefWidth(110);
        actionsCol.setStyle("-fx-alignment: CENTER;");
        actionsCol.setCellFactory(col -> new TableCell<Receipt, Void>() {
            private final HBox buttons = new HBox(6);
            private final Button payBtn = new Button("دفع");
            private final Button printBtn = new Button("طباعة");
            
            {
                payBtn.setStyle("-fx-background-color: " + Theme.SUCCESS_COLOR + "; " +
                                "-fx-text-fill: white; -fx-font-size: 12px; " +
                                "-fx-padding: 5px 10px; -fx-background-radius: 6; -fx-cursor: hand;");
                payBtn.setOnMouseEntered(e -> payBtn.setStyle("-fx-background-color: " + Theme.SUCCESS_LIGHT + "; " +
                                "-fx-text-fill: white; -fx-font-size: 12px; " +
                                "-fx-padding: 5px 10px; -fx-background-radius: 6; -fx-cursor: hand;"));
                payBtn.setOnMouseExited(e -> payBtn.setStyle("-fx-background-color: " + Theme.SUCCESS_COLOR + "; " +
                                "-fx-text-fill: white; -fx-font-size: 12px; " +
                                "-fx-padding: 5px 10px; -fx-background-radius: 6; -fx-cursor: hand;"));
                
                printBtn.setStyle("-fx-background-color: " + Theme.GRAY_500 + "; " +
                                  "-fx-text-fill: white; -fx-font-size: 12px; " +
                                  "-fx-padding: 5px 10px; -fx-background-radius: 6; -fx-cursor: hand;");
                printBtn.setOnMouseEntered(e -> printBtn.setStyle("-fx-background-color: " + Theme.GRAY_600 + "; " +
                                  "-fx-text-fill: white; -fx-font-size: 12px; " +
                                  "-fx-padding: 5px 10px; -fx-background-radius: 6; -fx-cursor: hand;"));
                printBtn.setOnMouseExited(e -> printBtn.setStyle("-fx-background-color: " + Theme.GRAY_500 + "; " +
                                  "-fx-text-fill: white; -fx-font-size: 12px; " +
                                  "-fx-padding: 5px 10px; -fx-background-radius: 6; -fx-cursor: hand;"));
                
                buttons.setAlignment(Pos.CENTER);
                buttons.getChildren().addAll(payBtn, printBtn);
            }
            
            @Override
            protected void updateItem(Void item, boolean empty) {
                super.updateItem(item, empty);
                if (empty) {
                    setGraphic(null);
                } else {
                    Receipt receipt = getTableView().getItems().get(getIndex());
                    payBtn.setOnAction(e -> {
                        selectedReceipt = receipt;
                        displayPaymentDetails();
                        paymentAmountField.requestFocus();
                    });
                    printBtn.setOnAction(e -> printSelectedReceipt(receipt));
                    setGraphic(buttons);
                }
            }
        });

        receiptTable.getColumns().addAll(idCol, customerCol, totalCol, statusCol, actionsCol);
        receiptTable.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);

        receiptTable.setOnMouseClicked(e -> {
            Receipt selected = receiptTable.getSelectionModel().getSelectedItem();
            if (selected != null) {
                selectedReceipt = selected;
                displayPaymentDetails();
            }
        });

        panel.getChildren().addAll(tableHeader, filterRow, receiptTable);
        return panel;
    }

    private VBox buildRightPanel() {
        VBox panel = new VBox(18);
        panel.setStyle(Theme.cardStyle());
        VBox.setVgrow(panel, Priority.ALWAYS);
        
        Label panelTitle = new Label("تفاصيل الدفع");
        panelTitle.setStyle(Theme.sectionTitleStyle());
        
        // Details section with improved styling
        VBox detailsBox = new VBox(14);
        detailsBox.setPadding(new Insets(20));
        detailsBox.setStyle("-fx-background-color: " + Theme.GRAY_50 + "; " + Theme.RADIUS_LG + ";");
        
        receiptIdLabel = new Label("الفاتورة: -");
        receiptIdLabel.setStyle("-fx-font-size: 18px; -fx-font-weight: bold; -fx-text-fill: " + Theme.GRAY_800 + ";");
        
        customerIdLabel = new Label("رقم العميل: -");
        customerIdLabel.setStyle("-fx-font-size: 14px; -fx-text-fill: " + Theme.GRAY_600 + ";");
        
        customerNameLabel = new Label("العميل: -");
        customerNameLabel.setStyle("-fx-font-size: 14px; -fx-text-fill: " + Theme.GRAY_600 + ";");
        
        totalLabel = new Label("الإجمالي: 0.00");
        totalLabel.setStyle("-fx-font-size: 20px; -fx-font-weight: bold; -fx-text-fill: " + Theme.GRAY_800 + ";");
        
        paidLabel = new Label("المدفوع: 0.00");
        paidLabel.setStyle("-fx-font-size: 15px; -fx-text-fill: " + Theme.SUCCESS_COLOR + "; -fx-font-weight: 600;");
        
        balanceLabel = new Label("المتبقي: 0.00");
        balanceLabel.setStyle("-fx-font-size: 15px; -fx-text-fill: " + Theme.DANGER_COLOR + "; -fx-font-weight: 600;");
        
        statusLabel = new Label("الحالة: -");
        statusLabel.setStyle("-fx-font-size: 15px; -fx-font-weight: bold;");

        detailsBox.getChildren().addAll(receiptIdLabel, customerIdLabel, customerNameLabel, 
            totalLabel, paidLabel, balanceLabel, statusLabel);
        
        // Payment form with quick actions
        VBox formBox = new VBox(14);
        Label formTitle = new Label("💳 تسجيل دفعة");
        formTitle.setStyle(Theme.sectionTitleStyle());
        
        HBox paymentRow = new HBox(12);
        paymentRow.setAlignment(Pos.CENTER_LEFT);
        
        Label amountLabel = new Label("المبلغ:");
        amountLabel.setStyle(Theme.labelStyle());
        amountLabel.setMinWidth(70);
        
        paymentAmountField = Theme.createTextField("0.00");
        paymentAmountField.setMinWidth(100);
        paymentAmountField.setPrefWidth(130);

        Button recordBtn = Theme.createSuccessButton("تسجيل الدفعة");
        recordBtn.setMinWidth(160);
        recordBtn.setPrefWidth(180);
        recordBtn.setOnAction(e -> recordPayment());

        paymentRow.getChildren().addAll(amountLabel, paymentAmountField, recordBtn);
        
        // Quick payment buttons
        HBox quickPayRow = new HBox(10);
        quickPayRow.setAlignment(Pos.CENTER_LEFT);
        quickPayRow.setPadding(new Insets(8, 0, 0, 0));
        
        Label quickLabel = new Label("دفع سريع:");
        quickLabel.setStyle("-fx-font-size: 13px; -fx-text-fill: " + Theme.GRAY_500 + ";");
        
        payFullBtn = Theme.createPrimaryButton("المبلغ كامل");
        payFullBtn.setMinWidth(130);
        payFullBtn.setOnAction(e -> payFullAmount());
        
        payHalfBtn = Theme.createSecondaryButton("النص");
        payHalfBtn.setMinWidth(90);
        payHalfBtn.setOnAction(e -> payHalfAmount());
        
        Button customBtn = Theme.createSecondaryButton("مخصص");
        customBtn.setMinWidth(90);
        customBtn.setOnAction(e -> paymentAmountField.requestFocus());
        
        quickPayRow.getChildren().addAll(quickLabel, payFullBtn, payHalfBtn, customBtn);
        
        formBox.getChildren().addAll(formTitle, paymentRow, quickPayRow);
        
        // Payment history
        VBox historyBox = new VBox(12);
        VBox.setVgrow(historyBox, Priority.ALWAYS);
        
        Label historyTitle = new Label("📜 سجل الدفعات");
        historyTitle.setStyle(Theme.sectionTitleStyle());
        
        paymentHistoryTable = new TableView<>();
        paymentHistoryTable.setPrefHeight(180);
        paymentHistoryTable.setStyle("-fx-background-color: white; -fx-background-radius: 8;");
        paymentHistoryTable.setFixedCellSize(44);

        TableColumn<Payment, String> paymentIdCol = new TableColumn<>("رقم الحركة");
        paymentIdCol.setMinWidth(50);
        paymentIdCol.setPrefWidth(60);
        paymentIdCol.setCellFactory(col -> new TableCell<Payment, String>() {
            @Override
            protected void updateItem(String item, boolean empty) {
                super.updateItem(item, empty);
                if (empty) { setText(null); }
                else {
                    setText("#" + getTableView().getItems().get(getIndex()).getPaymentId());
                    setStyle("-fx-text-fill: " + Theme.GRAY_600 + "; -fx-font-size: 14px; -fx-padding: 10px 14px;");
                }
            }
        });

        TableColumn<Payment, String> amountCol = new TableColumn<>("المبلغ");
        amountCol.setMinWidth(90);
        amountCol.setPrefWidth(100);
        amountCol.setCellFactory(col -> new TableCell<Payment, String>() {
            @Override
            protected void updateItem(String item, boolean empty) {
                super.updateItem(item, empty);
                if (empty) { setText(null); }
                else {
                    setText(MoneyFormat.egp(getTableView().getItems().get(getIndex()).getAmount()));
                    setStyle("-fx-text-fill: " + Theme.SUCCESS_COLOR + "; -fx-font-size: 14px; " +
                            "-fx-padding: 10px 14px; -fx-font-weight: bold;");
                }
            }
        });

        TableColumn<Payment, String> dateCol = new TableColumn<>("التاريخ");
        dateCol.setMinWidth(140);
        dateCol.setPrefWidth(180);
        dateCol.setCellFactory(col -> new TableCell<Payment, String>() {
            @Override
            protected void updateItem(String item, boolean empty) {
                super.updateItem(item, empty);
                if (empty) { setText(null); }
                else {
                    setText(getTableView().getItems().get(getIndex()).getPaymentDate());
                    setStyle("-fx-text-fill: " + Theme.GRAY_600 + "; -fx-font-size: 14px; -fx-padding: 10px 14px;");
                }
            }
        });

        paymentHistoryTable.getColumns().addAll(paymentIdCol, amountCol, dateCol);
        paymentHistoryTable.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);

        VBox.setVgrow(paymentHistoryTable, Priority.ALWAYS);
        historyBox.getChildren().addAll(historyTitle, paymentHistoryTable);
        
        panel.getChildren().addAll(panelTitle, detailsBox, formBox, historyBox);
        return panel;
    }
    
    private void payFullAmount() {
        if (selectedReceipt == null) {
            showError("اختر فاتورة أولاً");
            return;
        }
        double totalPaid = paymentDAO.getTotalPaidForReceipt(selectedReceipt.getReceiptId());
        double balance = selectedReceipt.getGrandTotal() - totalPaid;
        paymentAmountField.setText(String.format("%.2f", balance));
    }
    
    private void payHalfAmount() {
        if (selectedReceipt == null) {
            showError("اختر فاتورة أولاً");
            return;
        }
        double totalPaid = paymentDAO.getTotalPaidForReceipt(selectedReceipt.getReceiptId());
        double balance = selectedReceipt.getGrandTotal() - totalPaid;
        paymentAmountField.setText(String.format("%.2f", balance / 2));
    }
    
    private void printSelectedReceipt(Receipt receipt) {
        if (receipt == null) {
            showError("لم يتم اختيار فاتورة");
            return;
        }
        PrintService.printReceipt(receipt);
        showInfo("تم إرسال الفاتورة رقم " + receipt.getReceiptId() + " للطباعة.");
    }

    private String selectedPaymentFilterKey() {
        return ReceiptLabels.filterKeyFromArabicLabel(filterCombo.getValue());
    }

    private void loadCustomers() {
        allCustomers = customerDAO.findAll();
        customerCombo.setItems(FXCollections.observableArrayList(allCustomers));
    }
    
    private void filterCustomerCombo() {
        if (allCustomers == null || allCustomers.isEmpty()) {
            loadCustomers();
            return;
        }
        
        String searchText = searchField.getText().trim().toLowerCase();
        String searchType = searchTypeCombo.getValue();
        
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
    
    private void filterReceiptsByCustomer(Customer customer) {
        if (customer == null || allReceipts == null) {
            loadReceipts(selectedPaymentFilterKey());
            return;
        }
        
        String statusFilter = selectedPaymentFilterKey();
        List<Receipt> filtered = allReceipts.stream()
            .filter(r -> r.getCustomerId() == customer.getCustomerId())
            .filter(r -> statusFilter.equals("ALL") || r.getStatus().equals(statusFilter))
            .collect(Collectors.toList());
        
        receiptTable.setItems(FXCollections.observableArrayList(filtered));
        
        if (filtered.isEmpty()) {
            showInfo("لا توجد فواتير للعميل: " + customer.getName());
        } else if (filtered.size() == 1) {
            receiptTable.getSelectionModel().select(0);
            selectedReceipt = filtered.get(0);
            displayPaymentDetails();
        }
    }
    
    private void loadReceipts(String status) {
        allReceipts = receiptDAO.findAll();
        List<Receipt> filtered = status.equals("ALL") ? allReceipts : 
                allReceipts.stream().filter(r -> r.getStatus().equals(status)).collect(Collectors.toList());
        receiptTable.setItems(FXCollections.observableArrayList(filtered));
    }

    private void displayPaymentDetails() {
        if (selectedReceipt == null) return;

        receiptIdLabel.setText("الفاتورة: #" + selectedReceipt.getReceiptId());
        customerIdLabel.setText("رقم العميل: " + selectedReceipt.getCustomerId());
        
        Customer customer = customerDAO.findById(selectedReceipt.getCustomerId()).orElse(null);
        customerNameLabel.setText("العميل: " + (customer != null ? customer.getName() : "غير معروف"));
        
        totalLabel.setText("الإجمالي: " + MoneyFormat.egp(selectedReceipt.getGrandTotal()));

        double totalPaid = paymentDAO.getTotalPaidForReceipt(selectedReceipt.getReceiptId());
        double balance = selectedReceipt.getGrandTotal() - totalPaid;

        paidLabel.setText("المدفوع: " + MoneyFormat.egp(totalPaid));
        balanceLabel.setText("المتبقي: " + MoneyFormat.egp(balance));
        statusLabel.setText("الحالة: " + ReceiptLabels.statusArabic(selectedReceipt.getStatus()));

        String statusStyle = "-fx-font-size: 15px; -fx-font-weight: bold;";
        if ("UNPAID".equals(selectedReceipt.getStatus())) statusStyle += "-fx-text-fill: " + Theme.DANGER_COLOR + ";";
        else if ("PARTIAL".equals(selectedReceipt.getStatus())) statusStyle += "-fx-text-fill: " + Theme.WARNING_COLOR + ";";
        else if ("PAID".equals(selectedReceipt.getStatus())) statusStyle += "-fx-text-fill: " + Theme.SUCCESS_COLOR + ";";
        statusLabel.setStyle(statusStyle);
        
        // Update balance label color based on amount
        if (balance > 0) {
            balanceLabel.setStyle("-fx-font-size: 15px; -fx-text-fill: " + Theme.DANGER_COLOR + "; -fx-font-weight: 600;");
        } else {
            balanceLabel.setStyle("-fx-font-size: 15px; -fx-text-fill: " + Theme.SUCCESS_COLOR + "; -fx-font-weight: 600;");
        }

        paymentHistoryTable.setItems(FXCollections.observableArrayList(
                paymentDAO.findByReceiptId(selectedReceipt.getReceiptId())));
        paymentAmountField.clear();
    }

    private void recordPayment() {
        if (selectedReceipt == null) { showError("اختر فاتورة أولاً"); return; }
        String amountStr = paymentAmountField.getText().trim();
        if (amountStr.isEmpty()) { showError("أدخل مبلغ الدفع"); return; }

        try {
            double amount = Double.parseDouble(amountStr);
            if (amount <= 0) { showError("المبلغ يجب أن يكون أكبر من صفر"); return; }

            // Check if overpaying
            double totalPaid = paymentDAO.getTotalPaidForReceipt(selectedReceipt.getReceiptId());
            double balance = selectedReceipt.getGrandTotal() - totalPaid;
            if (amount > balance + 0.01) {
                Alert confirm = new Alert(Alert.AlertType.CONFIRMATION);
                confirm.setTitle("تأكيد الدفع");
                confirm.setHeaderText("المبلغ أكبر من المتبقي");
                confirm.setContentText("المبلغ (" + MoneyFormat.egp(amount) + ") يتجاوز المتبقي (" + 
                        MoneyFormat.egp(balance) + "). هل تريد المتابعة؟");
                if (confirm.showAndWait().get() != ButtonType.OK) return;
            }

            ReceiptService.recordPayment(selectedReceipt.getReceiptId(), amount);
            allReceipts = receiptDAO.findAll();
            selectedReceipt = receiptDAO.findById(selectedReceipt.getReceiptId()).orElse(selectedReceipt);
            displayPaymentDetails();
            Customer selectedCustomer = customerCombo.getValue();
            if (selectedCustomer != null) {
                filterReceiptsByCustomer(selectedCustomer);
            } else {
                loadReceipts(selectedPaymentFilterKey());
            }
            showInfo("تم تسجيل دفعة " + MoneyFormat.egp(amount) + " بنجاح.");
        } catch (NumberFormatException e) {
            showError("صيغة المبلغ غير صحيحة");
        }
    }
    
    // ==================== AppScreen ====================
    
    @Override
    public BorderPane getRoot() {
        return root;
    }
    
    // ==================== Helpers ====================
    
    private void showError(String msg) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle("خطأ");
        alert.setHeaderText(null);
        alert.setContentText(msg);
        alert.showAndWait();
    }
    
    private void showInfo(String msg) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle("نجاح");
        alert.setHeaderText(null);
        alert.setContentText(msg);
        alert.showAndWait();
    }
}
package oop.carwash.ui;

import oop.carwash.dao.CustomerDAO;
import oop.carwash.model.Customer;
import oop.carwash.util.PrintService;
import javafx.application.Platform;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
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
 * Simplified customer management screen.
 * Features: Table with click-to-edit, search bar with barcode scanner, and add button.
 * Clean layout optimized for full window mode.
 */
public class CustomerScreen implements AppScreen {

    private BorderPane root;
    private CustomerDAO customerDAO = new CustomerDAO();
    private TableView<Customer> table;
    
    // Search components
    private TextField searchField;
    private ComboBox<String> searchTypeCombo;
    
    // Store all customers for filtering
    private List<Customer> allCustomers;
    
    // Barcode scanner reference
    private BarcodeScannerDialog scannerDialog;

    public CustomerScreen() {
        buildUI();
        loadTable();
    }

    private void buildUI() {
        root = new BorderPane();
        root.setStyle(Theme.pageBackgroundStyle());
        
        // Sidebar navigation
        VBox sidebar = createSidebar();
        root.setLeft(sidebar);
        
        // Main content
        VBox mainContent = new VBox(20);
        mainContent.setPadding(new Insets(24));
        VBox.setVgrow(mainContent, Priority.ALWAYS);
        
        // Page header
        HBox headerBox = new HBox(16);
        headerBox.setAlignment(Pos.CENTER_LEFT);
        
        Button backBtn = Theme.createBackButton();
        backBtn.setOnAction(e -> ScreenNavigator.navigateTo(new DashboardScreen()));
        
        Label titleLabel = new Label("👥 العملاء");
        titleLabel.setStyle(Theme.pageTitleStyle());
        
        Region headerSpacer = new Region();
        HBox.setHgrow(headerSpacer, Priority.ALWAYS);
        
        // Add Customer button in header
        Button addBtn = Theme.createSuccessButton("➕ إضافة عميل");
        addBtn.setMinWidth(140);
        addBtn.setPrefWidth(160);
        addBtn.setOnAction(e -> showAddDialog());
        
        headerBox.getChildren().addAll(backBtn, titleLabel, headerSpacer, addBtn);
        
        // Search bar
        HBox searchBox = createSearchBar();
        
        // Table
        VBox tableContainer = createTableSection();
        VBox.setVgrow(tableContainer, Priority.ALWAYS);
        
        mainContent.getChildren().addAll(headerBox, searchBox, tableContainer);
        root.setCenter(mainContent);
    }
    
    private HBox createSearchBar() {
        HBox searchBox = new HBox(12);
        searchBox.setAlignment(Pos.CENTER_LEFT);
        searchBox.setPadding(new Insets(16, 20, 16, 20));
        searchBox.setStyle(Theme.cardStyle());
        
        Label searchLabel = new Label("🔍 بحث:");
        searchLabel.setStyle(Theme.labelStyle());
        searchLabel.setMinWidth(90);
        
        // ComboBox with explicit styling for visibility
        searchTypeCombo = new ComboBox<>();
        searchTypeCombo.setItems(FXCollections.observableArrayList("الاسم", "الرقم"));
        searchTypeCombo.setValue("الاسم");
        searchTypeCombo.setMinWidth(110);
        searchTypeCombo.setPrefWidth(120);
        // Explicit styling to ensure text is visible
        searchTypeCombo.setStyle("-fx-background-color: white; " +
                "-fx-border-color: " + Theme.GRAY_300 + "; " +
                "-fx-border-width: 1; " +
                "-fx-border-radius: 8; " +
                "-fx-background-radius: 8; " +
                "-fx-font-size: 14px; " +
                "-fx-text-fill: " + Theme.GRAY_800 + ";");
        
        // Style the button cell to show selected value
        searchTypeCombo.setButtonCell(new ListCell<String>() {
            @Override
            protected void updateItem(String item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) {
                    setText(null);
                } else {
                    setText(item);
                    setStyle("-fx-text-fill: " + Theme.GRAY_800 + "; -fx-font-size: 14px; -fx-font-weight: 600;");
                }
            }
        });
        
        searchField = Theme.createTextField("اكتب للبحث...");
        searchField.setMinWidth(220);
        searchField.setPrefWidth(300);
        HBox.setHgrow(searchField, Priority.ALWAYS);
        searchField.textProperty().addListener((obs, oldVal, newVal) -> performSearch());
        
        // Scan button with camera icon
        Button scanBtn = new Button("📷 سكان");
        scanBtn.setStyle("-fx-background-color: " + Theme.INFO_COLOR + "; " +
                        "-fx-text-fill: white; -fx-font-size: 14px; -fx-font-weight: bold; " +
                        "-fx-padding: 10px 20px; " + Theme.RADIUS_MD + " -fx-cursor: hand; " +
                        "-fx-min-height: 40px;");
        scanBtn.setMinWidth(90);
        scanBtn.setPrefWidth(100);
        scanBtn.setOnMouseEntered(e -> scanBtn.setStyle("-fx-background-color: derive(" + Theme.INFO_COLOR + ", -15%); " +
                        "-fx-text-fill: white; -fx-font-size: 14px; -fx-font-weight: bold; " +
                        "-fx-padding: 10px 20px; " + Theme.RADIUS_MD + " -fx-cursor: hand; " +
                        "-fx-min-height: 40px;"));
        scanBtn.setOnMouseExited(e -> scanBtn.setStyle("-fx-background-color: " + Theme.INFO_COLOR + "; " +
                        "-fx-text-fill: white; -fx-font-size: 14px; -fx-font-weight: bold; " +
                        "-fx-padding: 10px 20px; " + Theme.RADIUS_MD + " -fx-cursor: hand; " +
                        "-fx-min-height: 40px;"));
        scanBtn.setOnAction(e -> openBarcodeScanner());
        
        Button clearBtn = Theme.createSecondaryButton("مسح");
        clearBtn.setMinWidth(80);
        clearBtn.setPrefWidth(90);
        clearBtn.setOnAction(e -> {
            searchField.clear();
            loadTable();
        });
        
        searchBox.getChildren().addAll(searchLabel, searchTypeCombo, searchField, scanBtn, clearBtn);
        return searchBox;
    }
    
    /**
     * Opens the barcode scanner dialog
     */
    private void openBarcodeScanner() {
        try {
            scannerDialog = new BarcodeScannerDialog();
            scannerDialog.setOnBarcodeScanned(barcode -> {
                Platform.runLater(() -> {
                    searchByBarcode(barcode);
                });
            });
            scannerDialog.show();
        } catch (Exception e) {
            // If camera fails, show manual input dialog
            showManualBarcodeDialog();
        }
    }
    
    /**
     * Shows a manual barcode input dialog as fallback
     */
    private void showManualBarcodeDialog() {
        TextInputDialog dialog = new TextInputDialog();
        dialog.setTitle("إدخال الباركود");
        dialog.setHeaderText("📷 دخل رقم العميل / الباركود");
        dialog.setContentText("رقم العميل:");
        
        dialog.showAndWait().ifPresent(barcode -> {
            if (!barcode.trim().isEmpty()) {
                searchByBarcode(barcode.trim());
            }
        });
    }
    
    /**
     * Search customer by barcode (customer ID)
     */
    private void searchByBarcode(String barcode) {
        if (barcode == null || barcode.isEmpty()) {
            showError("مفيش باركود");
            return;
        }
        
        try {
            // Remove leading zeros if present (barcode might be "000001" for ID 1)
            String cleanId = barcode.replaceFirst("^0+", "");
            if (cleanId.isEmpty()) cleanId = "0";
            
            int customerId = Integer.parseInt(cleanId);
            Customer customer = customerDAO.findById(customerId).orElse(null);
            
            if (customer != null) {
                // Select the customer in the table
                table.getSelectionModel().select(customer);
                
                // Scroll to the customer
                int index = table.getItems().indexOf(customer);
                if (index >= 0) {
                    table.scrollTo(index);
                }
                
                showInfo("تم العثور على العميل: " + customer.getName() + " (الرقم: " + customer.getCustomerId() + ")");
            } else {
                showError("مفيش عميل بالرقم ده: " + customerId);
            }
            
        } catch (NumberFormatException e) {
            showError("شكل الباركود غلط. برجاء إدخال باركود صحيح.");
        }
    }
    
    private VBox createTableSection() {
        VBox container = new VBox(0);
        container.setStyle(Theme.cardStyle());
        VBox.setVgrow(container, Priority.ALWAYS);
        
        // Table header with hint
        HBox tableHeader = new HBox(16);
        tableHeader.setAlignment(Pos.CENTER_LEFT);
        tableHeader.setPadding(new Insets(0, 0, 12, 0));
        tableHeader.setStyle("-fx-border-color: transparent; -fx-border-width: 0;");
        
        Label tableTitle = new Label("قائمة العملاء");
        tableTitle.setStyle(Theme.sectionTitleStyle());
        
        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);
        
        Label hintLabel = new Label("💡 اضغط مرتين للتعديل • كليك يمين لخيارات اكتر");
        hintLabel.setStyle("-fx-font-size: 13px; -fx-text-fill: " + Theme.GRAY_500 + ";");
        
        tableHeader.getChildren().addAll(tableTitle, spacer, hintLabel);
        
        // Table
        table = new TableView<>();
        table.setStyle("-fx-background-color: white; -fx-background-radius: 8;");
        VBox.setVgrow(table, Priority.ALWAYS);
        
        // Enable context menu (right-click)
        table.setRowFactory(tv -> {
            TableRow<Customer> row = new TableRow<>();
            
            // Context menu
            ContextMenu contextMenu = new ContextMenu();
            
            MenuItem editItem = new MenuItem("✏️ تعديل");
            editItem.setStyle("-fx-font-size: 14px;");
            editItem.setOnAction(e -> showEditDialog(row.getItem()));
            
            MenuItem deleteItem = new MenuItem("🗑️ حذف");
            deleteItem.setStyle("-fx-font-size: 14px;");
            deleteItem.setOnAction(e -> deleteCustomer(row.getItem()));
            
            MenuItem printItem = new MenuItem("🆔 طباعة الكارت");
            printItem.setStyle("-fx-font-size: 14px;");
            printItem.setOnAction(e -> printCustomerCard(row.getItem()));
            
            contextMenu.getItems().addAll(editItem, deleteItem, new SeparatorMenuItem(), printItem);
            
            // Set context menu on row
            row.contextMenuProperty().set(contextMenu);
            
            // Double-click to edit
            row.setOnMouseClicked(event -> {
                if (event.getClickCount() == 2 && !row.isEmpty()) {
                    showEditDialog(row.getItem());
                }
            });
            
            return row;
        });
        
        // ID Column - using custom cell factory
        TableColumn<Customer, String> idCol = new TableColumn<>("الرقم");
        idCol.setMinWidth(100);
        idCol.setPrefWidth(120);
        idCol.setStyle("-fx-alignment: CENTER-LEFT;");
        idCol.setCellFactory(col -> new TableCell<Customer, String>() {
            @Override
            protected void updateItem(String item, boolean empty) {
                super.updateItem(item, empty);
                if (empty) {
                    setText(null);
                    setGraphic(null);
                } else {
                    Customer customer = getTableView().getItems().get(getIndex());
                    if (customer != null) {
                        setText(String.format("%06d", customer.getCustomerId()));
                        setStyle("-fx-text-fill: " + Theme.GRAY_800 + "; -fx-font-size: 14px; -fx-padding: 10px 14px;");
                    }
                }
            }
        });
        
        // Name Column - using custom cell factory
        TableColumn<Customer, String> nameCol = new TableColumn<>("اسم العميل");
        nameCol.setMinWidth(180);
        nameCol.setPrefWidth(250);
        nameCol.setStyle("-fx-alignment: CENTER-LEFT;");
        nameCol.setCellFactory(col -> new TableCell<Customer, String>() {
            @Override
            protected void updateItem(String item, boolean empty) {
                super.updateItem(item, empty);
                if (empty) {
                    setText(null);
                    setGraphic(null);
                } else {
                    Customer customer = getTableView().getItems().get(getIndex());
                    if (customer != null) {
                        setText(customer.getName() != null ? customer.getName() : "");
                        setStyle("-fx-text-fill: " + Theme.GRAY_800 + "; -fx-font-size: 14px; -fx-padding: 10px 14px; -fx-font-weight: 600;");
                    }
                }
            }
        });
        
        // Phone Column - using custom cell factory
        TableColumn<Customer, String> phoneCol = new TableColumn<>("رقم التليفون");
        phoneCol.setMinWidth(140);
        phoneCol.setPrefWidth(180);
        phoneCol.setStyle("-fx-alignment: CENTER-LEFT;");
        phoneCol.setCellFactory(col -> new TableCell<Customer, String>() {
            @Override
            protected void updateItem(String item, boolean empty) {
                super.updateItem(item, empty);
                if (empty) {
                    setText(null);
                    setGraphic(null);
                } else {
                    Customer customer = getTableView().getItems().get(getIndex());
                    if (customer != null) {
                        setText(customer.getPhone() != null ? customer.getPhone() : "");
                        setStyle("-fx-text-fill: " + Theme.GRAY_600 + "; -fx-font-size: 14px; -fx-padding: 10px 14px;");
                    }
                }
            }
        });
        
        // Car Plate Column - using custom cell factory
        TableColumn<Customer, String> plateCol = new TableColumn<>("رقم العربية");
        plateCol.setMinWidth(140);
        plateCol.setPrefWidth(180);
        plateCol.setStyle("-fx-alignment: CENTER-LEFT;");
        plateCol.setCellFactory(col -> new TableCell<Customer, String>() {
            @Override
            protected void updateItem(String item, boolean empty) {
                super.updateItem(item, empty);
                if (empty) {
                    setText(null);
                    setGraphic(null);
                } else {
                    Customer customer = getTableView().getItems().get(getIndex());
                    if (customer != null) {
                        setText(customer.getCarPlateNumber() != null ? customer.getCarPlateNumber() : "");
                        setStyle("-fx-text-fill: " + Theme.GRAY_600 + "; -fx-font-size: 14px; -fx-padding: 10px 14px;");
                    }
                }
            }
        });
        
        // Actions Column
        TableColumn<Customer, Void> actionsCol = new TableColumn<>("الإجراءات");
        actionsCol.setMinWidth(160);
        actionsCol.setPrefWidth(180);
        actionsCol.setStyle("-fx-alignment: CENTER;");
        actionsCol.setCellFactory(col -> new TableCell<Customer, Void>() {
            private final HBox buttons = new HBox(8);
            private final Button editBtn = new Button("تعديل");
            private final Button deleteBtn = new Button("حذف");
            
            {
                editBtn.setStyle("-fx-background-color: " + Theme.PRIMARY_COLOR + "; " +
                                "-fx-text-fill: white; -fx-font-size: 13px; " +
                                "-fx-padding: 6px 14px; -fx-background-radius: 6; -fx-cursor: hand;");
                editBtn.setOnMouseEntered(e -> editBtn.setStyle("-fx-background-color: " + Theme.PRIMARY_DARK + "; " +
                                "-fx-text-fill: white; -fx-font-size: 13px; " +
                                "-fx-padding: 6px 14px; -fx-background-radius: 6; -fx-cursor: hand;"));
                editBtn.setOnMouseExited(e -> editBtn.setStyle("-fx-background-color: " + Theme.PRIMARY_COLOR + "; " +
                                "-fx-text-fill: white; -fx-font-size: 13px; " +
                                "-fx-padding: 6px 14px; -fx-background-radius: 6; -fx-cursor: hand;"));
                
                deleteBtn.setStyle("-fx-background-color: " + Theme.DANGER_COLOR + "; " +
                                  "-fx-text-fill: white; -fx-font-size: 13px; " +
                                  "-fx-padding: 6px 14px; -fx-background-radius: 6; -fx-cursor: hand;");
                deleteBtn.setOnMouseEntered(e -> deleteBtn.setStyle("-fx-background-color: " + Theme.DANGER_LIGHT + "; " +
                                  "-fx-text-fill: white; -fx-font-size: 13px; " +
                                  "-fx-padding: 6px 14px; -fx-background-radius: 6; -fx-cursor: hand;"));
                deleteBtn.setOnMouseExited(e -> deleteBtn.setStyle("-fx-background-color: " + Theme.DANGER_COLOR + "; " +
                                  "-fx-text-fill: white; -fx-font-size: 13px; " +
                                  "-fx-padding: 6px 14px; -fx-background-radius: 6; -fx-cursor: hand;"));
                
                buttons.setAlignment(Pos.CENTER);
                buttons.getChildren().addAll(editBtn, deleteBtn);
            }
            
            @Override
            protected void updateItem(Void item, boolean empty) {
                super.updateItem(item, empty);
                if (empty) {
                    setGraphic(null);
                } else {
                    Customer customer = getTableView().getItems().get(getIndex());
                    editBtn.setOnAction(e -> showEditDialog(customer));
                    deleteBtn.setOnAction(e -> deleteCustomer(customer));
                    setGraphic(buttons);
                }
            }
        });
        
        // Debt Column
        TableColumn<Customer, String> debtCol = new TableColumn<>("إجمالي الدين");
        debtCol.setMinWidth(130);
        debtCol.setPrefWidth(150);
        debtCol.setCellFactory(col -> new TableCell<Customer, String>() {
            @Override
            protected void updateItem(String item, boolean empty) {
                super.updateItem(item, empty);
                if (empty) { setText(null); setGraphic(null); }
                else {
                    Customer customer = getTableView().getItems().get(getIndex());
                    double debt = customerDAO.getTotalDebt(customer.getCustomerId());
                    if (debt > 0) {
                        setText(oop.carwash.util.MoneyFormat.egp(debt));
                        setStyle("-fx-text-fill: " + Theme.DANGER_COLOR + "; -fx-font-size: 14px; -fx-padding: 10px 14px; -fx-font-weight: bold;");
                    } else {
                        setText("لا يوجد");
                        setStyle("-fx-text-fill: " + Theme.SUCCESS_COLOR + "; -fx-font-size: 13px; -fx-padding: 10px 14px;");
                    }
                }
            }
        });

        table.getColumns().addAll(idCol, nameCol, phoneCol, plateCol, debtCol, actionsCol);
        table.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);
        
        // Apply row styling
        table.setFixedCellSize(48);
        
        container.getChildren().addAll(tableHeader, table);
        return container;
    }
    
    private void performSearch() {
        if (allCustomers == null) {
            allCustomers = customerDAO.findAll();
        }
        
        String searchText = searchField.getText().trim().toLowerCase();
        String searchType = searchTypeCombo.getValue();
        
        if (searchText.isEmpty()) {
            table.setItems(FXCollections.observableArrayList(allCustomers));
            return;
        }
        
        List<Customer> filtered;
        if ("الرقم".equals(searchType)) {
            filtered = allCustomers.stream()
                .filter(c -> String.valueOf(c.getCustomerId()).contains(searchText))
                .collect(Collectors.toList());
        } else {
            filtered = allCustomers.stream()
                .filter(c -> c.getName().toLowerCase().contains(searchText))
                .collect(Collectors.toList());
        }
        
        table.setItems(FXCollections.observableArrayList(filtered));
    }
    
    // ==================== DIALOG METHODS ====================
    
    private void showAddDialog() {
        showCustomerDialog(null);
    }
    
    private void showEditDialog(Customer customer) {
        if (customer == null) return;
        showCustomerDialog(customer);
    }
    
    private void showCustomerDialog(Customer existingCustomer) {
        boolean isEdit = existingCustomer != null;
        String title = isEdit ? "تعديل عميل" : "إضافة عميل جديد";
        
        Dialog<Customer> dialog = new Dialog<>();
        dialog.setTitle(title);
        dialog.setHeaderText(null);
        dialog.getDialogPane().setMinWidth(450);
        dialog.getDialogPane().setStyle("-fx-background-color: white;");
        
        // Form content
        VBox form = new VBox(16);
        form.setPadding(new Insets(20));
        
        Label formTitle = new Label(isEdit ? "✏️ تعديل بيانات العميل" : "➕ عميل جديد");
        formTitle.setStyle("-fx-font-size: 18px; -fx-font-weight: bold; -fx-text-fill: " + Theme.GRAY_800 + ";");
        
        // ID field (read-only for edit)
        if (isEdit) {
            HBox idBox = new HBox(10);
            idBox.setAlignment(Pos.CENTER_LEFT);
            Label idLabel = new Label("رقم العميل:");
            idLabel.setStyle(Theme.labelStyle());
            idLabel.setMinWidth(100);
            Label idValue = new Label(String.format("%06d", existingCustomer.getCustomerId()));
            idValue.setStyle("-fx-font-size: 15px; -fx-font-weight: bold; -fx-text-fill: " + Theme.PRIMARY_COLOR + ";");
            idBox.getChildren().addAll(idLabel, idValue);
            form.getChildren().add(idBox);
        }
        
        // Name field
        HBox nameBox = new HBox(10);
        nameBox.setAlignment(Pos.CENTER_LEFT);
        Label nameLabel = new Label("الاسم:");
        nameLabel.setStyle(Theme.labelStyle());
        nameLabel.setMinWidth(100);
        TextField nameField = Theme.createTextField("اكتب اسم العميل");
        nameField.setMinWidth(280);
        nameField.setPrefWidth(320);
        if (isEdit) nameField.setText(existingCustomer.getName());
        nameBox.getChildren().addAll(nameLabel, nameField);
        
        // Phone field
        HBox phoneBox = new HBox(10);
        phoneBox.setAlignment(Pos.CENTER_LEFT);
        Label phoneLabel = new Label("التليفون:");
        phoneLabel.setStyle(Theme.labelStyle());
        phoneLabel.setMinWidth(100);
        TextField phoneField = Theme.createTextField("اكتب رقم التليفون");
        phoneField.setMinWidth(280);
        phoneField.setPrefWidth(320);
        if (isEdit && existingCustomer.getPhone() != null) phoneField.setText(existingCustomer.getPhone());
        phoneBox.getChildren().addAll(phoneLabel, phoneField);
        
        // Plate field
        HBox plateBox = new HBox(10);
        plateBox.setAlignment(Pos.CENTER_LEFT);
        Label plateLabel = new Label("رقم العربية:");
        plateLabel.setStyle(Theme.labelStyle());
        plateLabel.setMinWidth(100);
        TextField plateField = Theme.createTextField("اكتب رقم العربية");
        plateField.setMinWidth(280);
        plateField.setPrefWidth(320);
        if (isEdit && existingCustomer.getCarPlateNumber() != null) plateField.setText(existingCustomer.getCarPlateNumber());
        plateBox.getChildren().addAll(plateLabel, plateField);
        
        form.getChildren().addAll(formTitle, nameBox, phoneBox, plateBox);
        
        dialog.getDialogPane().setContent(form);
        
        // Buttons
        ButtonType saveButtonType = new ButtonType("حفظ", ButtonBar.ButtonData.OK_DONE);
        ButtonType cancelButtonType = new ButtonType("إلغاء", ButtonBar.ButtonData.CANCEL_CLOSE);
        dialog.getDialogPane().getButtonTypes().addAll(saveButtonType, cancelButtonType);
        
        // Style the buttons
        Button saveButton = (Button) dialog.getDialogPane().lookupButton(saveButtonType);
        saveButton.setStyle("-fx-background-color: " + Theme.SUCCESS_COLOR + "; " +
                           "-fx-text-fill: white; -fx-font-size: 14px; -fx-font-weight: bold; " +
                           "-fx-padding: 10px 24px; -fx-background-radius: 8;");
        
        Button cancelButton = (Button) dialog.getDialogPane().lookupButton(cancelButtonType);
        cancelButton.setStyle("-fx-background-color: " + Theme.GRAY_200 + "; " +
                             "-fx-text-fill: " + Theme.GRAY_700 + "; -fx-font-size: 14px; " +
                             "-fx-padding: 10px 24px; -fx-background-radius: 8;");
        
        // Convert result
        dialog.setResultConverter(buttonType -> {
            if (buttonType == saveButtonType) {
                String name = nameField.getText().trim();
                if (name.isEmpty()) {
                    showError("الاسم مطلوب");
                    return null;
                }
                
                if (isEdit) {
                    existingCustomer.setName(name);
                    existingCustomer.setPhone(phoneField.getText().trim());
                    existingCustomer.setCarPlateNumber(plateField.getText().trim());
                    return existingCustomer;
                } else {
                    return new Customer(name, phoneField.getText().trim(), plateField.getText().trim());
                }
            }
            return null;
        });
        
        dialog.showAndWait().ifPresent(result -> {
            if (isEdit) {
                customerDAO.update(result);
                showInfo("تم تحديث العميل بنجاح!");
            } else {
                customerDAO.save(result);
                showInfo("تم إضافة العميل بنجاح!");
            }
            allCustomers = customerDAO.findAll();
            performSearch();
        });
    }
    
    private void deleteCustomer(Customer customer) {
        if (customer == null) return;
        
        Alert confirm = new Alert(Alert.AlertType.CONFIRMATION);
        confirm.setTitle("تأكيد الحذف");
        confirm.setHeaderText("حذف عميل");
        confirm.setContentText("متأكد إنك عايز تحذف \"" + customer.getName() + "\"؟");
        
        confirm.showAndWait().ifPresent(response -> {
            if (response == ButtonType.OK) {
                customerDAO.delete(customer.getCustomerId());
                allCustomers = customerDAO.findAll();
                performSearch();
                showInfo("تم حذف العميل بنجاح!");
            }
        });
    }
    
    private void printCustomerCard(Customer customer) {
        if (customer == null) return;
        PrintService.printCustomerCard(customer);
        showInfo("تم إرسال الكارت للطباعة!");
    }
    
    // ==================== SIDEBAR ====================
    
    private VBox createSidebar() {
        VBox sidebar = new VBox(0);
        sidebar.setPrefWidth(240);
        sidebar.setMinWidth(200);
        sidebar.setStyle("-fx-background-color: " + Theme.DARK_COLOR + ";");
        
        // Logo
        VBox logoSection = new VBox(8);
        logoSection.setPadding(new Insets(20, 16, 24, 16));
        logoSection.setAlignment(Pos.CENTER_LEFT);
        
        Label logoIcon = new Label("🚿");
        logoIcon.setStyle("-fx-font-size: 28px;");
        
        Label logoText = new Label("مغسلة أبو جميل");
        logoText.setStyle("-fx-font-size: 18px; -fx-font-weight: bold; -fx-text-fill: white;");
        
        logoSection.getChildren().addAll(logoIcon, logoText);
        
        // Navigation
        VBox navSection = new VBox(4);
        navSection.setPadding(new Insets(12, 8, 12, 8));
        
        Button dashboardBtn = createNavButton("لوحة التحكم", "📊");
        dashboardBtn.setOnAction(e -> ScreenNavigator.navigateTo(new DashboardScreen()));
        
        Button customersBtn = createNavButton("العملاء", "👥");
        customersBtn.setStyle(getActiveNavStyle());
        
        Button servicesBtn = createNavButton("الخدمات", "🧽");
        servicesBtn.setOnAction(e -> ScreenNavigator.navigateTo(new ServiceScreen()));
        
        Button productsBtn = createNavButton("المنتجات", "🛍️");
        productsBtn.setOnAction(e -> ScreenNavigator.navigateTo(new ProductScreen()));
        
        Button receiptBtn = createNavButton("فاتورة جديدة", "🧾");
        receiptBtn.setOnAction(e -> ScreenNavigator.navigateTo(new ReceiptScreen()));
        
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
    
    // ==================== UTILITIES ====================
    
    private void loadTable() {
        allCustomers = customerDAO.findAll();
        table.setItems(FXCollections.observableArrayList(allCustomers));
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
    public javafx.scene.Parent getRoot() {
        return root;
    }
}

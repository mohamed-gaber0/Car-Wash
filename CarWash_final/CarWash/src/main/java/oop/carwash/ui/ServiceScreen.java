package oop.carwash.ui;

import oop.carwash.dao.ServiceDAO;
import oop.carwash.model.Service;
import oop.carwash.util.MoneyFormat;

import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.*;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.Region;
import javafx.collections.FXCollections;

/**
 * Modern CRUD screen for services.
 * Updated with consistent styling matching CustomerScreen.
 */
public class ServiceScreen implements AppScreen {

    private BorderPane root;
    private ServiceDAO serviceDAO = new ServiceDAO();
    private TableView<Service> table;
    private TextField nameField;
    private TextField priceField;
    private Service selectedService;

    public ServiceScreen() {
        buildUI();
        loadTable();
    }

    private void buildUI() {
        root = new BorderPane();
        root.setStyle(Theme.pageBackgroundStyle());
        
        VBox sidebar = createSidebar();
        root.setLeft(sidebar);
        
        VBox mainContent = new VBox(20);
        mainContent.setPadding(new Insets(24));
        
        // Header with Add button
        HBox headerBox = new HBox(16);
        headerBox.setAlignment(Pos.CENTER_LEFT);
        
        Button backBtn = Theme.createBackButton();
        backBtn.setOnAction(e -> ScreenNavigator.navigateTo(new DashboardScreen()));
        
        Label titleLabel = new Label("🧽 الخدمات");
        titleLabel.setStyle(Theme.pageTitleStyle());
        
        Region headerSpacer = new Region();
        HBox.setHgrow(headerSpacer, Priority.ALWAYS);
        
        Button addBtn = Theme.createSuccessButton("➕ إضافة خدمة");
        addBtn.setMinWidth(130);
        addBtn.setPrefWidth(150);
        addBtn.setOnAction(e -> showAddDialog());
        
        headerBox.getChildren().addAll(backBtn, titleLabel, headerSpacer, addBtn);
        
        // Table
        VBox tableContainer = createTableSection();
        VBox.setVgrow(tableContainer, Priority.ALWAYS);
        
        mainContent.getChildren().addAll(headerBox, tableContainer);
        root.setCenter(mainContent);
    }
    
    private VBox createTableSection() {
        VBox container = new VBox(0);
        container.setStyle(Theme.cardStyle());
        VBox.setVgrow(container, Priority.ALWAYS);
        
        // Table header
        HBox tableHeader = new HBox(16);
        tableHeader.setAlignment(Pos.CENTER_LEFT);
        tableHeader.setPadding(new Insets(0, 0, 12, 0));
        
        Label tableTitle = new Label("قائمة الخدمات");
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
        table.setFixedCellSize(48);
        
        // Context menu
        table.setRowFactory(tv -> {
            TableRow<Service> row = new TableRow<>();
            ContextMenu contextMenu = new ContextMenu();
            
            MenuItem editItem = new MenuItem("✏️ تعديل");
            editItem.setOnAction(e -> showEditDialog(row.getItem()));
            
            MenuItem deleteItem = new MenuItem("🗑️ حذف");
            deleteItem.setOnAction(e -> deleteService(row.getItem()));
            
            contextMenu.getItems().addAll(editItem, deleteItem);
            row.contextMenuProperty().set(contextMenu);
            
            row.setOnMouseClicked(event -> {
                if (event.getClickCount() == 2 && !row.isEmpty()) {
                    showEditDialog(row.getItem());
                }
            });
            return row;
        });
        
        // ID Column
        TableColumn<Service, String> idCol = new TableColumn<>("الرقم");
        idCol.setMinWidth(80);
        idCol.setPrefWidth(100);
        idCol.setCellFactory(col -> new TableCell<Service, String>() {
            @Override
            protected void updateItem(String item, boolean empty) {
                super.updateItem(item, empty);
                if (empty) { setText(null); setGraphic(null); }
                else {
                    setText(String.valueOf(getTableView().getItems().get(getIndex()).getServiceId()));
                    setStyle("-fx-text-fill: " + Theme.GRAY_800 + "; -fx-font-size: 14px; -fx-padding: 10px 14px;");
                }
            }
        });
        
        // Name Column
        TableColumn<Service, String> nameCol = new TableColumn<>("اسم الخدمة");
        nameCol.setMinWidth(250);
        nameCol.setPrefWidth(350);
        nameCol.setCellFactory(col -> new TableCell<Service, String>() {
            @Override
            protected void updateItem(String item, boolean empty) {
                super.updateItem(item, empty);
                if (empty) { setText(null); setGraphic(null); }
                else {
                    setText(getTableView().getItems().get(getIndex()).getServiceName());
                    setStyle("-fx-text-fill: " + Theme.GRAY_800 + "; -fx-font-size: 14px; -fx-padding: 10px 14px; -fx-font-weight: 600;");
                }
            }
        });
        
        // Price Column
        TableColumn<Service, String> priceCol = new TableColumn<>("السعر");
        priceCol.setMinWidth(120);
        priceCol.setPrefWidth(150);
        priceCol.setCellFactory(col -> new TableCell<Service, String>() {
            @Override
            protected void updateItem(String item, boolean empty) {
                super.updateItem(item, empty);
                if (empty) { setText(null); setGraphic(null); }
                else {
                    setText(MoneyFormat.egp(getTableView().getItems().get(getIndex()).getPrice()));
                    setStyle("-fx-text-fill: " + Theme.SUCCESS_COLOR + "; -fx-font-size: 14px; -fx-padding: 10px 14px; -fx-font-weight: bold;");
                }
            }
        });
        
        // Actions Column
        TableColumn<Service, Void> actionsCol = new TableColumn<>("الإجراءات");
        actionsCol.setMinWidth(140);
        actionsCol.setPrefWidth(160);
        actionsCol.setCellFactory(col -> new TableCell<Service, Void>() {
            private final HBox buttons = new HBox(8);
            private final Button editBtn = new Button("تعديل");
            private final Button deleteBtn = new Button("حذف");
            
            {
                editBtn.setStyle("-fx-background-color: " + Theme.PRIMARY_COLOR + "; -fx-text-fill: white; -fx-font-size: 13px; -fx-padding: 6px 14px; -fx-background-radius: 6; -fx-cursor: hand;");
                deleteBtn.setStyle("-fx-background-color: " + Theme.DANGER_COLOR + "; -fx-text-fill: white; -fx-font-size: 13px; -fx-padding: 6px 14px; -fx-background-radius: 6; -fx-cursor: hand;");
                buttons.setAlignment(Pos.CENTER);
                buttons.getChildren().addAll(editBtn, deleteBtn);
            }
            
            @Override
            protected void updateItem(Void item, boolean empty) {
                super.updateItem(item, empty);
                if (empty) setGraphic(null);
                else {
                    Service s = getTableView().getItems().get(getIndex());
                    editBtn.setOnAction(e -> showEditDialog(s));
                    deleteBtn.setOnAction(e -> deleteService(s));
                    setGraphic(buttons);
                }
            }
        });
        
        table.getColumns().addAll(idCol, nameCol, priceCol, actionsCol);
        table.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);
        
        container.getChildren().addAll(tableHeader, table);
        return container;
    }
    
    private void showAddDialog() { showServiceDialog(null); }
    private void showEditDialog(Service service) { if (service != null) showServiceDialog(service); }
    
    private void showServiceDialog(Service existingService) {
        boolean isEdit = existingService != null;
        
        Dialog<Service> dialog = new Dialog<>();
        dialog.setTitle(isEdit ? "تعديل الخدمة" : "إضافة خدمة جديدة");
        dialog.getDialogPane().setMinWidth(400);
        dialog.getDialogPane().setStyle("-fx-background-color: white;");
        
        VBox form = new VBox(16);
        form.setPadding(new Insets(20));
        
        Label formTitle = new Label(isEdit ? "✏️ تعديل الخدمة" : "➕ خدمة جديدة");
        formTitle.setStyle("-fx-font-size: 18px; -fx-font-weight: bold; -fx-text-fill: " + Theme.GRAY_800 + ";");
        
        HBox nameBox = new HBox(10);
        nameBox.setAlignment(Pos.CENTER_LEFT);
        Label nameLabel = new Label("الاسم:");
        nameLabel.setStyle(Theme.labelStyle());
        nameLabel.setMinWidth(80);
        nameField = Theme.createTextField("اسم الخدمة");
        nameField.setPrefWidth(280);
        if (isEdit) nameField.setText(existingService.getServiceName());
        nameBox.getChildren().addAll(nameLabel, nameField);
        
        HBox priceBox = new HBox(10);
        priceBox.setAlignment(Pos.CENTER_LEFT);
        Label priceLabel = new Label("السعر:");
        priceLabel.setStyle(Theme.labelStyle());
        priceLabel.setMinWidth(80);
        priceField = Theme.createTextField("0.00");
        priceField.setPrefWidth(150);
        if (isEdit) priceField.setText(String.valueOf(existingService.getPrice()));
        priceBox.getChildren().addAll(priceLabel, priceField);
        
        form.getChildren().addAll(formTitle, nameBox, priceBox);
        dialog.getDialogPane().setContent(form);
        
        ButtonType saveButtonType = new ButtonType("حفظ", ButtonBar.ButtonData.OK_DONE);
        ButtonType cancelButtonType = new ButtonType("إلغاء", ButtonBar.ButtonData.CANCEL_CLOSE);
        dialog.getDialogPane().getButtonTypes().addAll(saveButtonType, cancelButtonType);
        
        dialog.setResultConverter(buttonType -> {
            if (buttonType == saveButtonType) {
                String name = nameField.getText().trim();
                if (name.isEmpty()) { showError("الاسم مطلوب"); return null; }
                try {
                    double price = Double.parseDouble(priceField.getText().trim());
                    if (isEdit) {
                        existingService.setServiceName(name);
                        existingService.setPrice(price);
                        return existingService;
                    }
                    return new Service(name, price);
                } catch (NumberFormatException e) { showError("السعر لازم يكون رقم"); return null; }
            }
            return null;
        });
        
        dialog.showAndWait().ifPresent(result -> {
            if (isEdit) { serviceDAO.update(result); showInfo("تم تحديث الخدمة!"); }
            else { serviceDAO.save(result); showInfo("تمت إضافة الخدمة!"); }
            loadTable();
        });
    }
    
    private void deleteService(Service service) {
        if (service == null) return;
        Alert confirm = new Alert(Alert.AlertType.CONFIRMATION);
        confirm.setTitle("تأكيد الحذف");
        confirm.setContentText("متأكد إنك عايز تحذف \"" + service.getServiceName() + "\"؟");
        confirm.showAndWait().ifPresent(response -> {
            if (response == ButtonType.OK) {
                serviceDAO.delete(service.getServiceId());
                loadTable();
                showInfo("تم حذف الخدمة بنجاح!");
            }
        });
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
        servicesBtn.setStyle(getActiveNavStyle());
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
        
        navSection.getChildren().addAll(dashboardBtn, customersBtn, servicesBtn, productsBtn, receiptBtn, paymentsBtn, reportsBtn, usersBtn);
        
        Region spacer = new Region();
        VBox.setVgrow(spacer, Priority.ALWAYS);
        
        Button logoutBtn = createNavButton("تسجيل الخروج", "🚪");
        logoutBtn.setOnAction(e -> { oop.carwash.service.AuthService.logout(); ScreenNavigator.navigateTo(new LoginScreen()); });
        
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
        return "-fx-background-color: " + Theme.PRIMARY_COLOR + "; -fx-text-fill: white; -fx-font-size: 13px; -fx-font-weight: bold; -fx-padding: 12px 16px; " + Theme.RADIUS_MD + " -fx-cursor: hand; -fx-border-width: 0; -fx-alignment: CENTER-LEFT;";
    }
    
    private String getInactiveNavStyle() {
        return "-fx-background-color: transparent; -fx-text-fill: " + Theme.GRAY_400 + "; -fx-font-size: 13px; -fx-padding: 12px 16px; " + Theme.RADIUS_MD + " -fx-cursor: hand; -fx-border-width: 0; -fx-alignment: CENTER-LEFT;";
    }
    
    private String getHoverNavStyle() {
        return "-fx-background-color: " + Theme.GRAY_700 + "; -fx-text-fill: white; -fx-font-size: 13px; -fx-padding: 12px 16px; " + Theme.RADIUS_MD + " -fx-cursor: hand; -fx-border-width: 0; -fx-alignment: CENTER-LEFT;";
    }
    
    private void loadTable() {
        table.setItems(FXCollections.observableArrayList(serviceDAO.findAll()));
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

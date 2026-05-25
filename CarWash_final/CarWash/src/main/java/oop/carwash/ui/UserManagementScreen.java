package oop.carwash.ui;

import oop.carwash.dao.UserDAO;
import oop.carwash.model.User;

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
 * Modern admin-only screen for managing users.
 * Updated with consistent styling matching CustomerScreen.
 */
public class UserManagementScreen implements AppScreen {

    private BorderPane root;
    private UserDAO userDAO = new UserDAO();
    private TableView<User> table;
    private TextField usernameField;
    private TextField passwordField;
    private CheckBox activeCheckBox;
    private User selectedUser;

    public UserManagementScreen() {
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
        
        Label titleLabel = new Label("⚙️ المستخدمين");
        titleLabel.setStyle(Theme.pageTitleStyle());
        
        Region headerSpacer = new Region();
        HBox.setHgrow(headerSpacer, Priority.ALWAYS);
        
        Button addBtn = Theme.createSuccessButton("➕ إضافة مستخدم");
        addBtn.setMinWidth(120);
        addBtn.setPrefWidth(140);
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
        
        Label tableTitle = new Label("قائمة المستخدمين");
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
            TableRow<User> row = new TableRow<>();
            ContextMenu contextMenu = new ContextMenu();
            
            MenuItem editItem = new MenuItem("✏️ تعديل");
            editItem.setOnAction(e -> showEditDialog(row.getItem()));
            
            MenuItem deleteItem = new MenuItem("🗑️ حذف");
            deleteItem.setOnAction(e -> deleteUser(row.getItem()));
            
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
        TableColumn<User, String> idCol = new TableColumn<>("الرقم");
        idCol.setMinWidth(60);
        idCol.setPrefWidth(80);
        idCol.setCellFactory(col -> new TableCell<User, String>() {
            @Override
            protected void updateItem(String item, boolean empty) {
                super.updateItem(item, empty);
                if (empty) { setText(null); setGraphic(null); }
                else {
                    setText(String.valueOf(getTableView().getItems().get(getIndex()).getUserId()));
                    setStyle("-fx-text-fill: " + Theme.GRAY_800 + "; -fx-font-size: 14px; -fx-padding: 10px 14px;");
                }
            }
        });
        
        // Username Column
        TableColumn<User, String> usernameCol = new TableColumn<>("اسم المستخدم");
        usernameCol.setMinWidth(180);
        usernameCol.setPrefWidth(220);
        usernameCol.setCellFactory(col -> new TableCell<User, String>() {
            @Override
            protected void updateItem(String item, boolean empty) {
                super.updateItem(item, empty);
                if (empty) { setText(null); setGraphic(null); }
                else {
                    setText(getTableView().getItems().get(getIndex()).getUsername());
                    setStyle("-fx-text-fill: " + Theme.GRAY_800 + "; -fx-font-size: 14px; -fx-padding: 10px 14px; -fx-font-weight: 600;");
                }
            }
        });
        
        // Status Column
        TableColumn<User, String> statusCol = new TableColumn<>("الحالة");
        statusCol.setMinWidth(100);
        statusCol.setPrefWidth(120);
        statusCol.setCellFactory(col -> new TableCell<User, String>() {
            @Override
            protected void updateItem(String item, boolean empty) {
                super.updateItem(item, empty);
                if (empty) { setText(null); setGraphic(null); }
                else {
                    User u = getTableView().getItems().get(getIndex());
                    setText(u.getIsActive() == 1 ? "✓ نشط" : "✗ غير نشط");
                    String color = u.getIsActive() == 1 ? Theme.SUCCESS_COLOR : Theme.DANGER_COLOR;
                    setStyle("-fx-text-fill: " + color + "; -fx-font-size: 14px; -fx-padding: 10px 14px; -fx-font-weight: bold;");
                }
            }
        });
        
        // Created Column
        TableColumn<User, String> createdCol = new TableColumn<>("تاريخ الإنشاء");
        createdCol.setMinWidth(140);
        createdCol.setPrefWidth(180);
        createdCol.setCellFactory(col -> new TableCell<User, String>() {
            @Override
            protected void updateItem(String item, boolean empty) {
                super.updateItem(item, empty);
                if (empty) { setText(null); setGraphic(null); }
                else {
                    User u = getTableView().getItems().get(getIndex());
                    setText(u.getCreatedAt() != null ? u.getCreatedAt() : "");
                    setStyle("-fx-text-fill: " + Theme.GRAY_500 + "; -fx-font-size: 14px; -fx-padding: 10px 14px;");
                }
            }
        });
        
        // Actions Column
        TableColumn<User, Void> actionsCol = new TableColumn<>("الإجراءات");
        actionsCol.setMinWidth(140);
        actionsCol.setPrefWidth(160);
        actionsCol.setCellFactory(col -> new TableCell<User, Void>() {
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
                    User u = getTableView().getItems().get(getIndex());
                    editBtn.setOnAction(e -> showEditDialog(u));
                    deleteBtn.setOnAction(e -> deleteUser(u));
                    setGraphic(buttons);
                }
            }
        });
        
        table.getColumns().addAll(idCol, usernameCol, statusCol, createdCol, actionsCol);
        table.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);
        
        container.getChildren().addAll(tableHeader, table);
        return container;
    }
    
    private void showAddDialog() { showUserDialog(null); }
    private void showEditDialog(User user) { if (user != null) showUserDialog(user); }
    
    private void showUserDialog(User existingUser) {
        boolean isEdit = existingUser != null;
        
        Dialog<User> dialog = new Dialog<>();
        dialog.setTitle(isEdit ? "تعديل المستخدم" : "إضافة مستخدم جديد");
        dialog.getDialogPane().setMinWidth(420);
        dialog.getDialogPane().setStyle("-fx-background-color: white;");
        
        VBox form = new VBox(16);
        form.setPadding(new Insets(20));
        
        Label formTitle = new Label(isEdit ? "✏️ تعديل المستخدم" : "➕ مستخدم جديد");
        formTitle.setStyle("-fx-font-size: 18px; -fx-font-weight: bold; -fx-text-fill: " + Theme.GRAY_800 + ";");
        
        HBox usernameBox = new HBox(10);
        usernameBox.setAlignment(Pos.CENTER_LEFT);
        Label usernameLabel = new Label("اسم المستخدم:");
        usernameLabel.setStyle(Theme.labelStyle());
        usernameLabel.setMinWidth(90);
        usernameField = Theme.createTextField("اسم المستخدم");
        usernameField.setPrefWidth(250);
        if (isEdit) usernameField.setText(existingUser.getUsername());
        usernameBox.getChildren().addAll(usernameLabel, usernameField);
        
        HBox passwordBox = new HBox(10);
        passwordBox.setAlignment(Pos.CENTER_LEFT);
        Label passwordLabel = new Label("كلمة السر:");
        passwordLabel.setStyle(Theme.labelStyle());
        passwordLabel.setMinWidth(90);
        passwordField = Theme.createTextField("كلمة السر");
        passwordField.setPrefWidth(250);
        if (isEdit) passwordField.setText(existingUser.getPassword());
        passwordBox.getChildren().addAll(passwordLabel, passwordField);
        
        HBox activeBox = new HBox(10);
        activeBox.setAlignment(Pos.CENTER_LEFT);
        Label activeLabel = new Label("الحالة:");
        activeLabel.setStyle(Theme.labelStyle());
        activeLabel.setMinWidth(90);
        activeCheckBox = new CheckBox("نشط");
        activeCheckBox.setStyle("-fx-font-size: 14px; -fx-text-fill: " + Theme.GRAY_700 + ";");
        activeCheckBox.setSelected(isEdit ? existingUser.getIsActive() == 1 : true);
        activeBox.getChildren().addAll(activeLabel, activeCheckBox);
        
        form.getChildren().addAll(formTitle, usernameBox, passwordBox, activeBox);
        dialog.getDialogPane().setContent(form);
        
        ButtonType saveButtonType = new ButtonType("حفظ", ButtonBar.ButtonData.OK_DONE);
        ButtonType cancelButtonType = new ButtonType("إلغاء", ButtonBar.ButtonData.CANCEL_CLOSE);
        dialog.getDialogPane().getButtonTypes().addAll(saveButtonType, cancelButtonType);
        
        dialog.setResultConverter(buttonType -> {
            if (buttonType == saveButtonType) {
                String username = usernameField.getText().trim();
                String password = passwordField.getText().trim();
                if (username.isEmpty() || password.isEmpty()) { showError("اسم المستخدم وكلمة السر مطلوبين"); return null; }
                if (isEdit) {
                    existingUser.setUsername(username);
                    existingUser.setPassword(password);
                    existingUser.setIsActive(activeCheckBox.isSelected() ? 1 : 0);
                    return existingUser;
                }
                User user = new User(username, password);
                user.setIsActive(activeCheckBox.isSelected() ? 1 : 0);
                return user;
            }
            return null;
        });
        
        dialog.showAndWait().ifPresent(result -> {
            if (isEdit) { userDAO.update(result); showInfo("تم تحديث المستخدم!"); }
            else { userDAO.save(result); showInfo("تم إضافة المستخدم!"); }
            loadTable();
        });
    }
    
    private void deleteUser(User user) {
        if (user == null) return;
        Alert confirm = new Alert(Alert.AlertType.CONFIRMATION);
        confirm.setTitle("تأكيد الحذف");
        confirm.setContentText("متأكد إنك عايز تحذف المستخدم \"" + user.getUsername() + "\"؟");
        confirm.showAndWait().ifPresent(response -> {
            if (response == ButtonType.OK) {
                userDAO.delete(user.getUserId());
                loadTable();
                showInfo("تم حذف المستخدم!");
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
        usersBtn.setStyle(getActiveNavStyle());
        
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
        table.setItems(FXCollections.observableArrayList(userDAO.findAll()));
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

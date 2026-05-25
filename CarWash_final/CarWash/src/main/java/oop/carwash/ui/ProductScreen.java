package oop.carwash.ui;

import oop.carwash.dao.ProductDAO;
import oop.carwash.model.Product;
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
import javafx.collections.ObservableList;

/**
 * CRUD screen for products.
 * Supports GALLON (sold by liters, float quantity) and CARTON (sold by pieces).
 */
public class ProductScreen implements AppScreen {

    private BorderPane root;
    private ProductDAO productDAO = new ProductDAO();
    private TableView<Product> table;

    public ProductScreen() {
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

        HBox headerBox = new HBox(16);
        headerBox.setAlignment(Pos.CENTER_LEFT);

        Button backBtn = Theme.createBackButton();
        backBtn.setOnAction(e -> ScreenNavigator.navigateTo(new DashboardScreen()));

        Label titleLabel = new Label("🛍️ المنتجات");
        titleLabel.setStyle(Theme.pageTitleStyle());

        Region headerSpacer = new Region();
        HBox.setHgrow(headerSpacer, Priority.ALWAYS);

        Button addBtn = Theme.createSuccessButton("➕ إضافة منتج");
        addBtn.setMinWidth(130);
        addBtn.setPrefWidth(150);
        addBtn.setOnAction(e -> showProductDialog(null));

        headerBox.getChildren().addAll(backBtn, titleLabel, headerSpacer, addBtn);

        VBox tableContainer = createTableSection();
        VBox.setVgrow(tableContainer, Priority.ALWAYS);

        mainContent.getChildren().addAll(headerBox, tableContainer);
        root.setCenter(mainContent);
    }

    private VBox createTableSection() {
        VBox container = new VBox(0);
        container.setStyle(Theme.cardStyle());
        VBox.setVgrow(container, Priority.ALWAYS);

        HBox tableHeader = new HBox(16);
        tableHeader.setAlignment(Pos.CENTER_LEFT);
        tableHeader.setPadding(new Insets(0, 0, 12, 0));

        Label tableTitle = new Label("قائمة المنتجات");
        tableTitle.setStyle(Theme.sectionTitleStyle());

        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);

        Label hintLabel = new Label("💡 اضغط مرتين للتعديل • كليك يمين لخيارات اكتر");
        hintLabel.setStyle("-fx-font-size: 13px; -fx-text-fill: " + Theme.GRAY_500 + ";");

        tableHeader.getChildren().addAll(tableTitle, spacer, hintLabel);

        table = new TableView<>();
        table.setStyle("-fx-background-color: white; -fx-background-radius: 8;");
        VBox.setVgrow(table, Priority.ALWAYS);
        table.setFixedCellSize(48);

        table.setRowFactory(tv -> {
            TableRow<Product> row = new TableRow<>();
            ContextMenu contextMenu = new ContextMenu();
            MenuItem editItem = new MenuItem("✏️ تعديل");
            editItem.setOnAction(e -> showProductDialog(row.getItem()));
            MenuItem deleteItem = new MenuItem("🗑️ حذف");
            deleteItem.setOnAction(e -> deleteProduct(row.getItem()));
            contextMenu.getItems().addAll(editItem, deleteItem);
            row.contextMenuProperty().set(contextMenu);
            row.setOnMouseClicked(event -> {
                if (event.getClickCount() == 2 && !row.isEmpty()) showProductDialog(row.getItem());
            });
            return row;
        });

        // ID Column
        TableColumn<Product, String> idCol = new TableColumn<>("الرقم");
        idCol.setMinWidth(60); idCol.setPrefWidth(70);
        idCol.setCellFactory(col -> new TableCell<Product, String>() {
            @Override protected void updateItem(String item, boolean empty) {
                super.updateItem(item, empty);
                if (empty) { setText(null); } else {
                    setText(String.valueOf(getTableView().getItems().get(getIndex()).getProductId()));
                    setStyle("-fx-font-size: 13px; -fx-padding: 10px 8px;");
                }
            }
        });

        // Name Column
        TableColumn<Product, String> nameCol = new TableColumn<>("اسم المنتج");
        nameCol.setMinWidth(180); nameCol.setPrefWidth(230);
        nameCol.setCellFactory(col -> new TableCell<Product, String>() {
            @Override protected void updateItem(String item, boolean empty) {
                super.updateItem(item, empty);
                if (empty) { setText(null); } else {
                    setText(getTableView().getItems().get(getIndex()).getProductName());
                    setStyle("-fx-font-size: 14px; -fx-padding: 10px 8px; -fx-font-weight: 600;");
                }
            }
        });

        // Type Column
        TableColumn<Product, String> typeCol = new TableColumn<>("النوع");
        typeCol.setMinWidth(80); typeCol.setPrefWidth(90);
        typeCol.setCellFactory(col -> new TableCell<Product, String>() {
            @Override protected void updateItem(String item, boolean empty) {
                super.updateItem(item, empty);
                if (empty) { setText(null); } else {
                    Product p = getTableView().getItems().get(getIndex());
                    setText(p.getTypeLabel());
                    setStyle("-fx-font-size: 13px; -fx-padding: 10px 8px; -fx-text-fill: " + Theme.PRIMARY_COLOR + ";");
                }
            }
        });

        // Price Column
        TableColumn<Product, String> priceCol = new TableColumn<>("السعر / وحدة");
        priceCol.setMinWidth(110); priceCol.setPrefWidth(130);
        priceCol.setCellFactory(col -> new TableCell<Product, String>() {
            @Override protected void updateItem(String item, boolean empty) {
                super.updateItem(item, empty);
                if (empty) { setText(null); } else {
                    Product p = getTableView().getItems().get(getIndex());
                    setText(MoneyFormat.egp(p.getUnitPrice()) + " / " + p.getUnitLabel());
                    setStyle("-fx-font-size: 13px; -fx-padding: 10px 8px; -fx-text-fill: " + Theme.SUCCESS_COLOR + ";");
                }
            }
        });

        // Stock Column
        TableColumn<Product, String> stockCol = new TableColumn<>("المخزون");
        stockCol.setMinWidth(100); stockCol.setPrefWidth(120);
        stockCol.setCellFactory(col -> new TableCell<Product, String>() {
            @Override protected void updateItem(String item, boolean empty) {
                super.updateItem(item, empty);
                if (empty) { setText(null); } else {
                    Product p = getTableView().getItems().get(getIndex());
                    double qty = p.getStockQuantity();
                    String display = (qty == Math.floor(qty)) ? String.valueOf((int) qty) : String.format("%.2f", qty);
                    display += " " + p.getUnitLabel();
                    if (p.isOutOfStock()) {
                        setText("خلص! " + display);
                        setStyle("-fx-font-size: 13px; -fx-padding: 10px 8px; -fx-text-fill: " + Theme.DANGER_COLOR + "; -fx-font-weight: bold;");
                    } else {
                        setText(display);
                        setStyle("-fx-font-size: 13px; -fx-padding: 10px 8px; -fx-text-fill: " + Theme.GRAY_700 + ";");
                    }
                }
            }
        });

        // Sold Column
        TableColumn<Product, String> soldCol = new TableColumn<>("المبيع");
        soldCol.setMinWidth(100); soldCol.setPrefWidth(120);
        soldCol.setCellFactory(col -> new TableCell<Product, String>() {
            @Override protected void updateItem(String item, boolean empty) {
                super.updateItem(item, empty);
                if (empty) { setText(null); } else {
                    Product p = getTableView().getItems().get(getIndex());
                    double qty = p.getSoldQuantity();
                    String display = (qty == Math.floor(qty)) ? String.valueOf((int) qty) : String.format("%.2f", qty);
                    setText(display + " " + p.getUnitLabel());
                    setStyle("-fx-font-size: 13px; -fx-padding: 10px 8px; -fx-text-fill: " + Theme.GRAY_600 + ";");
                }
            }
        });

        // Actions Column
        TableColumn<Product, Void> actionsCol = new TableColumn<>("الإجراءات");
        actionsCol.setMinWidth(140); actionsCol.setPrefWidth(160);
        actionsCol.setCellFactory(col -> new TableCell<Product, Void>() {
            private final HBox buttons = new HBox(8);
            private final Button editBtn = new Button("تعديل");
            private final Button deleteBtn = new Button("حذف");
            {
                editBtn.setStyle("-fx-background-color: " + Theme.PRIMARY_COLOR + "; -fx-text-fill: white; -fx-font-size: 13px; -fx-padding: 6px 14px; -fx-background-radius: 6; -fx-cursor: hand;");
                deleteBtn.setStyle("-fx-background-color: " + Theme.DANGER_COLOR + "; -fx-text-fill: white; -fx-font-size: 13px; -fx-padding: 6px 14px; -fx-background-radius: 6; -fx-cursor: hand;");
                buttons.setAlignment(Pos.CENTER);
                buttons.getChildren().addAll(editBtn, deleteBtn);
            }
            @Override protected void updateItem(Void item, boolean empty) {
                super.updateItem(item, empty);
                if (empty) setGraphic(null);
                else {
                    Product p = getTableView().getItems().get(getIndex());
                    editBtn.setOnAction(e -> showProductDialog(p));
                    deleteBtn.setOnAction(e -> deleteProduct(p));
                    setGraphic(buttons);
                }
            }
        });

        table.getColumns().addAll(idCol, nameCol, typeCol, priceCol, stockCol, soldCol, actionsCol);
        table.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);

        container.getChildren().addAll(tableHeader, table);
        return container;
    }

    private void showProductDialog(Product existingProduct) {
        boolean isEdit = existingProduct != null;

        Dialog<Product> dialog = new Dialog<>();
        dialog.setTitle(isEdit ? "تعديل المنتج" : "إضافة منتج جديد");
        dialog.getDialogPane().setMinWidth(440);
        dialog.getDialogPane().setStyle("-fx-background-color: white;");

        VBox form = new VBox(14);
        form.setPadding(new Insets(20));

        Label formTitle = new Label(isEdit ? "✏️ تعديل المنتج" : "➕ منتج جديد");
        formTitle.setStyle("-fx-font-size: 18px; -fx-font-weight: bold; -fx-text-fill: " + Theme.GRAY_800 + ";");

        // Name
        HBox nameBox = new HBox(10); nameBox.setAlignment(Pos.CENTER_LEFT);
        Label nameLabel = new Label("الاسم:"); nameLabel.setStyle(Theme.labelStyle()); nameLabel.setMinWidth(110);
        TextField nameField = Theme.createTextField("اسم المنتج"); nameField.setPrefWidth(280);
        if (isEdit) nameField.setText(existingProduct.getProductName());
        nameBox.getChildren().addAll(nameLabel, nameField);

        // Type
        HBox typeBox = new HBox(10); typeBox.setAlignment(Pos.CENTER_LEFT);
        Label typeLabel = new Label("النوع:"); typeLabel.setStyle(Theme.labelStyle()); typeLabel.setMinWidth(110);
        ComboBox<String> typeCombo = new ComboBox<>(FXCollections.observableArrayList("جالون (لترات)", "كرتونة (قطع)"));
        typeCombo.setPrefWidth(220);
        typeCombo.setValue(isEdit && Product.TYPE_CARTON.equals(existingProduct.getProductType())
                ? "كرتونة (قطع)" : "جالون (لترات)");
        typeBox.getChildren().addAll(typeLabel, typeCombo);

        // Units per carton (only visible for CARTON)
        HBox cartonBox = new HBox(10); cartonBox.setAlignment(Pos.CENTER_LEFT);
        Label cartonLabel = new Label("قطع / كرتونة:"); cartonLabel.setStyle(Theme.labelStyle()); cartonLabel.setMinWidth(110);
        TextField cartonField = Theme.createTextField("مثال: 12"); cartonField.setPrefWidth(120);
        if (isEdit) cartonField.setText(String.valueOf(existingProduct.getUnitsPerCarton()));
        else cartonField.setText("1");
        cartonBox.getChildren().addAll(cartonLabel, cartonField);
        cartonBox.setVisible(isEdit && existingProduct.isCarton());
        cartonBox.setManaged(isEdit && existingProduct.isCarton());

        typeCombo.valueProperty().addListener((obs, ov, nv) -> {
            boolean isCarton = "كرتونة (قطع)".equals(nv);
            cartonBox.setVisible(isCarton);
            cartonBox.setManaged(isCarton);
        });

        // Price
        HBox priceBox = new HBox(10); priceBox.setAlignment(Pos.CENTER_LEFT);
        Label priceLabel = new Label("السعر / وحدة:"); priceLabel.setStyle(Theme.labelStyle()); priceLabel.setMinWidth(110);
        TextField priceField = Theme.createTextField("0.00"); priceField.setPrefWidth(150);
        if (isEdit) priceField.setText(String.valueOf(existingProduct.getUnitPrice()));
        priceBox.getChildren().addAll(priceLabel, priceField);

        // Stock
        HBox stockBox = new HBox(10); stockBox.setAlignment(Pos.CENTER_LEFT);
        Label stockLabel = new Label("المخزون الحالي:"); stockLabel.setStyle(Theme.labelStyle()); stockLabel.setMinWidth(110);
        TextField stockField = Theme.createTextField("0"); stockField.setPrefWidth(150);
        if (isEdit) {
            double sq = existingProduct.getStockQuantity();
            stockField.setText(sq == Math.floor(sq) ? String.valueOf((int) sq) : String.format("%.2f", sq));
        } else stockField.setText("0");
        stockBox.getChildren().addAll(stockLabel, stockField);

        // Sold (read-only in edit, hidden in add)
        HBox soldBox = new HBox(10); soldBox.setAlignment(Pos.CENTER_LEFT);
        Label soldLabel = new Label("الكمية المباعة:"); soldLabel.setStyle(Theme.labelStyle()); soldLabel.setMinWidth(110);
        TextField soldField = Theme.createTextField("0"); soldField.setPrefWidth(150);
        soldField.setEditable(false);
        soldField.setStyle(soldField.getStyle() + " -fx-background-color: #f5f5f5;");
        if (isEdit) {
            double sold = existingProduct.getSoldQuantity();
            soldField.setText(sold == Math.floor(sold) ? String.valueOf((int) sold) : String.format("%.2f", sold));
        } else soldField.setText("0");
        soldBox.setVisible(isEdit); soldBox.setManaged(isEdit);
        soldBox.getChildren().addAll(soldLabel, soldField);

        form.getChildren().addAll(formTitle, nameBox, typeBox, cartonBox, priceBox, stockBox, soldBox);
        dialog.getDialogPane().setContent(form);

        ButtonType saveBtn = new ButtonType("حفظ", ButtonBar.ButtonData.OK_DONE);
        ButtonType cancelBtn = new ButtonType("إلغاء", ButtonBar.ButtonData.CANCEL_CLOSE);
        dialog.getDialogPane().getButtonTypes().addAll(saveBtn, cancelBtn);

        dialog.setResultConverter(bt -> {
            if (bt != saveBtn) return null;
            String name = nameField.getText().trim();
            if (name.isEmpty()) { showError("الاسم مطلوب"); return null; }
            try {
                double price = Double.parseDouble(priceField.getText().trim());
                double stock = Double.parseDouble(stockField.getText().trim());
                boolean isCarton = "كرتونة (قطع)".equals(typeCombo.getValue());
                String type = isCarton ? Product.TYPE_CARTON : Product.TYPE_GALLON;
                int unitsPerCarton = 1;
                if (isCarton) {
                    unitsPerCarton = Integer.parseInt(cartonField.getText().trim());
                    if (unitsPerCarton < 1) unitsPerCarton = 1;
                }
                double soldQty = isEdit ? existingProduct.getSoldQuantity() : 0;

                if (isEdit) {
                    existingProduct.setProductName(name);
                    existingProduct.setUnitPrice(price);
                    existingProduct.setProductType(type);
                    existingProduct.setUnitsPerCarton(unitsPerCarton);
                    existingProduct.setStockQuantity(stock);
                    existingProduct.setSoldQuantity(soldQty);
                    return existingProduct;
                }
                return new Product(0, name, price, type, unitsPerCarton, stock, 0);
            } catch (NumberFormatException e) {
                showError("تأكد من إدخال أرقام صحيحة في السعر والكمية");
                return null;
            }
        });

        dialog.showAndWait().ifPresent(result -> {
            if (isEdit) { productDAO.update(result); showInfo("تم تحديث المنتج!"); }
            else { productDAO.save(result); showInfo("تمت إضافة المنتج!"); }
            loadTable();
        });
    }

    private void deleteProduct(Product product) {
        if (product == null) return;
        Alert confirm = new Alert(Alert.AlertType.CONFIRMATION);
        confirm.setTitle("تأكيد الحذف");
        confirm.setContentText("متأكد انك عايز تحذف \"" + product.getProductName() + "\"؟");
        confirm.showAndWait().ifPresent(response -> {
            if (response == ButtonType.OK) {
                try {
                    productDAO.delete(product.getProductId());
                    loadTable();
                    showInfo("تم حذف المنتج!");
                } catch (RuntimeException ex) {
                    showError("مش هينفع تحذف المنتج ده لأنه مرتبط بفواتير موجودة.");
                }
            }
        });
    }

    private VBox createSidebar() {
        VBox sidebar = new VBox(0);
        sidebar.setPrefWidth(240); sidebar.setMinWidth(200);
        sidebar.setStyle("-fx-background-color: " + Theme.DARK_COLOR + ";");

        VBox logoSection = new VBox(8);
        logoSection.setPadding(new Insets(20, 16, 24, 16));
        logoSection.setAlignment(Pos.CENTER_LEFT);
        Label logoIcon = new Label("🚿"); logoIcon.setStyle("-fx-font-size: 28px;");
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
        productsBtn.setStyle(getActiveNavStyle());
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
        table.setItems(FXCollections.observableArrayList(productDAO.findAll()));
    }

    private void showError(String msg) {
        Alert a = new Alert(Alert.AlertType.ERROR); a.setTitle("خطأ"); a.setHeaderText(null); a.setContentText(msg); a.showAndWait();
    }
    private void showInfo(String msg) {
        Alert a = new Alert(Alert.AlertType.INFORMATION); a.setTitle("نجاح"); a.setHeaderText(null); a.setContentText(msg); a.showAndWait();
    }

    @Override
    public javafx.scene.Parent getRoot() { return root; }
}

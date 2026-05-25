package oop.carwash.ui;

import oop.carwash.util.MoneyFormat;
import oop.carwash.service.AuthService;
import oop.carwash.service.ReportService;
import oop.carwash.socket.CarWashClient;

import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.Region;

/**
 * Modern Dashboard screen with sidebar navigation and summary cards.
 * Updated with consistent styling matching CustomerScreen.
 */
public class DashboardScreen implements AppScreen {

    private BorderPane root;
    private Label todayRevLabel;
    private Label unpaidLabel;
    private Label totalRevLabel;
    private Label todayCountLabel;

    public DashboardScreen() {
        buildUI();
        refreshStats();
    }

    private void buildUI() {
        root = new BorderPane();
        root.setStyle(Theme.pageBackgroundStyle());
        
        VBox sidebar = createSidebar();
        root.setLeft(sidebar);
        
        VBox mainContent = createMainContent();
        root.setCenter(mainContent);
    }
    
    private VBox createSidebar() {
        VBox sidebar = new VBox(0);
        sidebar.setPrefWidth(240);
        sidebar.setMinWidth(200);
        sidebar.setStyle("-fx-background-color: " + Theme.DARK_COLOR + ";");
        
        // Logo section
        VBox logoSection = new VBox(8);
        logoSection.setPadding(new Insets(20, 16, 24, 16));
        logoSection.setAlignment(Pos.CENTER_LEFT);
        
        Label logoIcon = new Label("🚿");
        logoIcon.setStyle("-fx-font-size: 28px;");
        
        Label logoText = new Label("مغسلة أبو جميل");
        logoText.setStyle("-fx-font-size: 18px; -fx-font-weight: bold; -fx-text-fill: white;");
        
        logoSection.getChildren().addAll(logoIcon, logoText);
        
        // User info
        HBox userBox = new HBox(12);
        userBox.setPadding(new Insets(12, 16, 12, 16));
        userBox.setStyle("-fx-background-color: " + Theme.GRAY_800 + "; -fx-background-radius: 8; -fx-margin: 0 8 16 8;");
        userBox.setAlignment(Pos.CENTER_LEFT);
        
        Label userAvatar = new Label("👤");
        userAvatar.setStyle("-fx-font-size: 24px;");
        
        VBox userInfo = new VBox(2);
        Label userName = new Label(AuthService.getCurrentUser().getUsername());
        userName.setStyle("-fx-font-size: 14px; -fx-font-weight: bold; -fx-text-fill: white;");
        Label userRole = new Label("مدير النظام");
        userRole.setStyle("-fx-font-size: 11px; -fx-text-fill: " + Theme.GRAY_400 + ";");
        userInfo.getChildren().addAll(userName, userRole);
        
        userBox.getChildren().addAll(userAvatar, userInfo);
        
        // Navigation buttons
        VBox navSection = new VBox(4);
        navSection.setPadding(new Insets(12, 8, 12, 8));
        
        Button dashboardBtn = createNavButton("لوحة التحكم", "📊", true);
        dashboardBtn.setStyle(getActiveNavStyle());
        
        Button customersBtn = createNavButton("العملاء", "👥", false);
        customersBtn.setOnAction(e -> ScreenNavigator.navigateTo(new CustomerScreen()));
        
        Button servicesBtn = createNavButton("الخدمات", "🧽", false);
        servicesBtn.setOnAction(e -> ScreenNavigator.navigateTo(new ServiceScreen()));
        
        Button productsBtn = createNavButton("المنتجات", "🛍️", false);
        productsBtn.setOnAction(e -> ScreenNavigator.navigateTo(new ProductScreen()));
        
        Button receiptBtn = createNavButton("فاتورة جديدة", "🧾", false);
        receiptBtn.setOnAction(e -> ScreenNavigator.navigateTo(new ReceiptScreen()));
        
        Button paymentsBtn = createNavButton("المدفوعات", "💳", false);
        paymentsBtn.setOnAction(e -> ScreenNavigator.navigateTo(new PaymentScreen()));
        
        Button reportsBtn = createNavButton("التقارير", "📈", false);
        reportsBtn.setOnAction(e -> ScreenNavigator.navigateTo(new ReportsScreen()));
        
        Button usersBtn = createNavButton("المستخدمين", "⚙️", false);
        usersBtn.setOnAction(e -> ScreenNavigator.navigateTo(new UserManagementScreen()));
        
        navSection.getChildren().addAll(dashboardBtn, customersBtn, servicesBtn, productsBtn, receiptBtn, paymentsBtn, reportsBtn, usersBtn);
        
        Region spacer = new Region();
        VBox.setVgrow(spacer, Priority.ALWAYS);
        
        Button logoutBtn = createNavButton("تسجيل الخروج", "🚪", false);
        logoutBtn.setOnAction(e -> {
            AuthService.logout();
            ScreenNavigator.navigateTo(new LoginScreen());
        });
        
        sidebar.getChildren().addAll(logoSection, userBox, navSection, spacer, logoutBtn);
        return sidebar;
    }
    
    private Button createNavButton(String text, String icon, boolean isActive) {
        Button btn = new Button(icon + "  " + text);
        btn.setPrefWidth(Double.MAX_VALUE);
        btn.setAlignment(Pos.CENTER_LEFT);
        btn.setStyle(getInactiveNavStyle());
        btn.setOnMouseEntered(e -> { if (!isActive) btn.setStyle(getHoverNavStyle()); });
        btn.setOnMouseExited(e -> { if (!isActive) btn.setStyle(getInactiveNavStyle()); });
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
    
    private VBox createMainContent() {
        VBox content = new VBox(24);
        content.setPadding(new Insets(24));

        Label titleLabel = new Label("📊 لوحة التحكم");
        titleLabel.setStyle(Theme.pageTitleStyle());

        HBox statsRow = new HBox(20);
        statsRow.setAlignment(Pos.CENTER_LEFT);

        VBox todayRevCard = createStatCard("إيرادات اليوم", "💵", Theme.SUCCESS_COLOR, MoneyFormat.egp(0));
        todayRevLabel = getValueLabel(todayRevCard);

        VBox unpaidCard = createStatCard("فواتير غير مدفوعة", "📋", Theme.DANGER_COLOR, "0");
        unpaidLabel = getValueLabel(unpaidCard);

        VBox todayCountCard = createStatCard("معاملات اليوم", "🔢", Theme.PRIMARY_COLOR, "0");
        todayCountLabel = getValueLabel(todayCountCard);

        VBox totalCard = createStatCard("إجمالي الإيرادات", "💰", Theme.WARNING_COLOR, MoneyFormat.egp(0));
        totalRevLabel = getValueLabel(totalCard);

        statsRow.getChildren().addAll(todayRevCard, unpaidCard, todayCountCard, totalCard);

        VBox quickActionsCard = createQuickActionsCard();

        Button refreshBtn = Theme.createSecondaryButton("تحديث الإحصائيات");
        refreshBtn.setMinWidth(120);
        refreshBtn.setOnAction(e -> refreshStats());

        content.getChildren().addAll(titleLabel, statsRow, quickActionsCard, refreshBtn);
        return content;
    }

    private Label getValueLabel(VBox card) {
        VBox valueBox = (VBox) card.getChildren().get(1);
        return (Label) valueBox.getChildren().get(1);
    }
    
    private VBox createStatCard(String title, String icon, String accentColor, String initialValue) {
        VBox card = new VBox(12);
        card.setStyle("-fx-background-color: " + Theme.WHITE + "; " + Theme.RADIUS_LG + " " + Theme.SHADOW_CARD + " -fx-padding: 24px; -fx-border-color: " + Theme.GRAY_100 + "; -fx-border-width: 1; -fx-border-radius: 12;");
        card.setPrefWidth(260);
        card.setPrefHeight(140);

        HBox iconBox = new HBox();
        iconBox.setStyle("-fx-background-color: " + accentColor + "20; -fx-background-radius: 8; -fx-padding: 8px 12px;");
        Label iconLabel = new Label(icon);
        iconLabel.setStyle("-fx-font-size: 20px;");
        iconBox.getChildren().add(iconLabel);

        Label titleLabel = new Label(title);
        titleLabel.setStyle("-fx-font-size: 13px; -fx-text-fill: " + Theme.GRAY_500 + ";");

        Label valueLabel = new Label(initialValue);
        valueLabel.setStyle("-fx-font-size: 28px; -fx-font-weight: bold; -fx-text-fill: " + Theme.GRAY_900 + ";");

        VBox valueBox = new VBox(4, titleLabel, valueLabel);
        card.getChildren().addAll(iconBox, valueBox);
        return card;
    }

    private VBox createQuickActionsCard() {
        VBox card = new VBox(16);
        card.setStyle(Theme.cardStyle());
        
        Label cardTitle = new Label("إجراءات سريعة");
        cardTitle.setStyle(Theme.sectionTitleStyle());
        
        HBox row1 = new HBox(12);
        HBox row2 = new HBox(12);
        
        Button newReceiptBtn = createActionButton("فاتورة جديدة", "🧾", Theme.SUCCESS_COLOR);
        newReceiptBtn.setOnAction(e -> ScreenNavigator.navigateTo(new ReceiptScreen()));
        
        Button manageCustomersBtn = createActionButton("العملاء", "👥", Theme.PRIMARY_COLOR);
        manageCustomersBtn.setOnAction(e -> ScreenNavigator.navigateTo(new CustomerScreen()));
        
        Button manageServicesBtn = createActionButton("الخدمات", "🧽", Theme.SECONDARY_COLOR);
        manageServicesBtn.setOnAction(e -> ScreenNavigator.navigateTo(new ServiceScreen()));
        
        Button manageProductsBtn = createActionButton("المنتجات", "🛍️", Theme.ACCENT_COLOR);
        manageProductsBtn.setOnAction(e -> ScreenNavigator.navigateTo(new ProductScreen()));
        
        Button viewReportsBtn = createActionButton("عرض التقارير", "📈", Theme.WARNING_COLOR);
        viewReportsBtn.setOnAction(e -> ScreenNavigator.navigateTo(new ReportsScreen()));
        
        Button managePaymentsBtn = createActionButton("المدفوعات", "💳", Theme.DANGER_COLOR);
        managePaymentsBtn.setOnAction(e -> ScreenNavigator.navigateTo(new PaymentScreen()));
        
        row1.getChildren().addAll(newReceiptBtn, manageCustomersBtn, manageServicesBtn);
        row2.getChildren().addAll(manageProductsBtn, viewReportsBtn, managePaymentsBtn);
        
        card.getChildren().addAll(cardTitle, row1, row2);
        return card;
    }
    
    private Button createActionButton(String text, String icon, String color) {
        Button btn = new Button(icon + "  " + text);
        btn.setStyle("-fx-background-color: " + color + "; -fx-text-fill: white; -fx-font-size: 14px; -fx-font-weight: bold; -fx-padding: 14px 24px; " + Theme.RADIUS_MD + " -fx-cursor: hand;");
        btn.setPrefWidth(180);
        btn.setMinHeight(48);
        return btn;
    }
    
    private void refreshStats() {
        try {
            CarWashClient client = new CarWashClient("localhost", 5050);
            todayRevLabel.setText(MoneyFormat.egp(client.getTodayRevenue()));
            unpaidLabel.setText(String.valueOf(client.getUnpaidCount()));
            todayCountLabel.setText(String.valueOf(client.getTodayReceiptCount()));
            totalRevLabel.setText(MoneyFormat.egp(client.getTotalRevenue()));
        } catch (Exception e) {
            todayRevLabel.setText(MoneyFormat.egp(ReportService.getTodayRevenue()));
            unpaidLabel.setText(String.valueOf(ReportService.getUnpaidReceiptCount()));
            todayCountLabel.setText(String.valueOf(ReportService.getTodayReceiptCount()));
            totalRevLabel.setText(MoneyFormat.egp(ReportService.getTotalRevenue()));
        }
    }

    @Override
    public javafx.scene.Parent getRoot() { return root; }
}
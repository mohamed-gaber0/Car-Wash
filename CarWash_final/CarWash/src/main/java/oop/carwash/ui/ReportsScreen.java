package oop.carwash.ui;

import oop.carwash.service.ReportService;
import oop.carwash.util.MoneyFormat;
import oop.carwash.util.PrintService;

import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.chart.BarChart;
import javafx.scene.chart.CategoryAxis;
import javafx.scene.chart.NumberAxis;
import javafx.scene.chart.XYChart;
import javafx.scene.control.*;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.Region;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Map;

/**
 * Reports screen with two tabs:
 *   1. Revenue chart (existing)
 *   2. Product sales report (new) - printable
 */
public class ReportsScreen implements AppScreen {

    private BorderPane root;
    private BarChart<String, Number> chart;
    private DatePicker startDatePicker;
    private DatePicker endDatePicker;

    // Sales report tab
    private DatePicker salesStartPicker;
    private DatePicker salesEndPicker;
    private TableView<ReportService.ProductSalesSummary> salesTable;
    private TableView<ReportService.ServiceSalesSummary> serviceSalesTable;
    private Label grandTotalLabel;
    private Label serviceGrandTotalLabel;

    public ReportsScreen() {
        buildUI();
        refreshChart();
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

        Label titleLabel = new Label("📈 التقارير والإحصائيات");
        titleLabel.setStyle(Theme.pageTitleStyle());

        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);

        headerBox.getChildren().addAll(backBtn, titleLabel, spacer);

        // Tabs
        TabPane tabPane = new TabPane();
        tabPane.setTabClosingPolicy(TabPane.TabClosingPolicy.UNAVAILABLE);
        tabPane.setStyle("-fx-background-color: transparent;");

        Tab revenueTab = new Tab("📊 الإيرادات اليومية");
        revenueTab.setContent(buildRevenueTabContent());

        Tab salesTab = new Tab("🛍️ تقرير مبيعات المنتجات");
        salesTab.setContent(buildSalesReportTabContent());
        salesTab.selectedProperty().addListener((obs, ov, selected) -> {
            if (selected) refreshSalesReport();
        });

        tabPane.getTabs().addAll(revenueTab, salesTab);
        VBox.setVgrow(tabPane, Priority.ALWAYS);

        mainContent.getChildren().addAll(headerBox, tabPane);
        root.setCenter(mainContent);
    }

    // ── Revenue Tab ──────────────────────────────────────────────────────

    private VBox buildRevenueTabContent() {
        VBox content = new VBox(16);
        content.setPadding(new Insets(16, 0, 0, 0));

        content.getChildren().addAll(buildFilterCard(), buildChartCard());
        VBox.setVgrow(buildChartCard(), Priority.ALWAYS);
        return content;
    }

    private VBox buildFilterCard() {
        VBox card = new VBox(16);
        card.setStyle(Theme.cardStyle());

        Label filterTitle = new Label("تصفية بالتاريخ");
        filterTitle.setStyle(Theme.sectionTitleStyle());

        HBox filterRow = new HBox(16);
        filterRow.setAlignment(Pos.CENTER_LEFT);

        Label fromLabel = new Label("من:"); fromLabel.setStyle(Theme.labelStyle());

        LocalDate today = LocalDate.now();
        startDatePicker = new DatePicker(today.minusDays(7));
        startDatePicker.setStyle("-fx-background-color: white; -fx-border-color: " + Theme.GRAY_300 + "; -fx-border-width: 1; -fx-border-radius: 8; -fx-background-radius: 8; -fx-font-size: 14px;");
        startDatePicker.setPrefWidth(150);

        Label toLabel = new Label("إلى:"); toLabel.setStyle(Theme.labelStyle());

        endDatePicker = new DatePicker(today);
        endDatePicker.setStyle("-fx-background-color: white; -fx-border-color: " + Theme.GRAY_300 + "; -fx-border-width: 1; -fx-border-radius: 8; -fx-background-radius: 8; -fx-font-size: 14px;");
        endDatePicker.setPrefWidth(150);

        Button refreshBtn = Theme.createPrimaryButton("تطبيق التصفية");
        refreshBtn.setMinWidth(110);
        refreshBtn.setOnAction(e -> refreshChart());

        filterRow.getChildren().addAll(fromLabel, startDatePicker, toLabel, endDatePicker, refreshBtn);
        card.getChildren().addAll(filterTitle, filterRow);
        return card;
    }

    private VBox buildChartCard() {
        VBox card = new VBox(16);
        card.setStyle(Theme.cardStyle());
        VBox.setVgrow(card, Priority.ALWAYS);

        Label chartTitle = new Label("الإيرادات حسب التاريخ");
        chartTitle.setStyle(Theme.sectionTitleStyle());

        CategoryAxis xAxis = new CategoryAxis(); xAxis.setLabel("التاريخ");
        NumberAxis yAxis = new NumberAxis(); yAxis.setLabel("الإيرادات (جنيه)");

        chart = new BarChart<>(xAxis, yAxis);
        chart.setPrefHeight(450);
        chart.setStyle("-fx-background-color: transparent; -fx-font-size: 12px;");
        chart.setLegendVisible(false);

        card.getChildren().addAll(chartTitle, chart);
        VBox.setVgrow(chart, Priority.ALWAYS);
        return card;
    }

    private void refreshChart() {
        LocalDate startDate = startDatePicker.getValue();
        LocalDate endDate = endDatePicker.getValue();
        if (startDate == null || endDate == null) return;

        String startStr = startDate.format(DateTimeFormatter.ISO_DATE);
        String endStr = endDate.format(DateTimeFormatter.ISO_DATE);
        Map<String, Double> revenueByDate = ReportService.getRevenueByDate(startStr, endStr);

        chart.getData().clear();
        XYChart.Series<String, Number> series = new XYChart.Series<>();
        series.setName("الإيرادات اليومية");
        for (Map.Entry<String, Double> entry : revenueByDate.entrySet()) {
            series.getData().add(new XYChart.Data<>(entry.getKey(), entry.getValue()));
        }
        chart.getData().add(series);
        for (XYChart.Series<String, Number> s : chart.getData()) {
            for (XYChart.Data<String, Number> data : s.getData()) {
                if (data.getNode() != null)
                    data.getNode().setStyle("-fx-bar-fill: " + Theme.PRIMARY_COLOR + ";");
            }
        }
    }

    // ── Sales Report Tab ─────────────────────────────────────────────────

    private VBox buildSalesReportTabContent() {
        VBox content = new VBox(16);
        content.setPadding(new Insets(16, 0, 0, 0));

        // Filter card
        VBox filterCard = new VBox(12);
        filterCard.setStyle(Theme.cardStyle());

        Label filterTitle = new Label("اختر الفترة الزمنية للتقرير");
        filterTitle.setStyle(Theme.sectionTitleStyle());

        HBox filterRow = new HBox(16);
        filterRow.setAlignment(Pos.CENTER_LEFT);

        Label fromLabel = new Label("من:"); fromLabel.setStyle(Theme.labelStyle());
        salesStartPicker = new DatePicker(LocalDate.now().withDayOfMonth(1));
        salesStartPicker.setStyle("-fx-background-color: white; -fx-border-color: " + Theme.GRAY_300 + "; -fx-border-width: 1; -fx-border-radius: 8; -fx-background-radius: 8; -fx-font-size: 14px;");
        salesStartPicker.setPrefWidth(150);

        Label toLabel = new Label("إلى:"); toLabel.setStyle(Theme.labelStyle());
        salesEndPicker = new DatePicker(LocalDate.now());
        salesEndPicker.setStyle("-fx-background-color: white; -fx-border-color: " + Theme.GRAY_300 + "; -fx-border-width: 1; -fx-border-radius: 8; -fx-background-radius: 8; -fx-font-size: 14px;");
        salesEndPicker.setPrefWidth(150);

        Button refreshBtn = Theme.createPrimaryButton("عرض التقرير");
        refreshBtn.setMinWidth(110);
        refreshBtn.setOnAction(e -> refreshSalesReport());

        Button printBtn = Theme.createSuccessButton("🖨️ طباعة التقرير");
        printBtn.setMinWidth(130);
        printBtn.setOnAction(e -> printCurrentSalesReport());

        filterRow.getChildren().addAll(fromLabel, salesStartPicker, toLabel, salesEndPicker, refreshBtn, printBtn);
        filterCard.getChildren().addAll(filterTitle, filterRow);

        // ── Products Table ──────────────────────────────────────────────────
        VBox productsTableCard = new VBox(12);
        productsTableCard.setStyle(Theme.cardStyle());
        VBox.setVgrow(productsTableCard, Priority.ALWAYS);

        Label tableTitle = new Label("🛍️ المنتجات المباعة في الفترة");
        tableTitle.setStyle(Theme.sectionTitleStyle());

        salesTable = new TableView<>();
        salesTable.setStyle("-fx-background-color: white; -fx-background-radius: 8;");
        salesTable.setFixedCellSize(46);
        salesTable.setPrefHeight(200);

        TableColumn<ReportService.ProductSalesSummary, String> nameCol = new TableColumn<>("اسم المنتج");
        nameCol.setMinWidth(200); nameCol.setPrefWidth(280);
        nameCol.setCellFactory(col -> new TableCell<ReportService.ProductSalesSummary, String>() {
            @Override protected void updateItem(String item, boolean empty) {
                super.updateItem(item, empty);
                if (empty) { setText(null); } else {
                    setText(getTableView().getItems().get(getIndex()).productName);
                    setStyle("-fx-font-size: 14px; -fx-padding: 10px 12px; -fx-font-weight: 600;");
                }
            }
        });

        TableColumn<ReportService.ProductSalesSummary, String> typeCol = new TableColumn<>("النوع");
        typeCol.setMinWidth(90); typeCol.setPrefWidth(100);
        typeCol.setCellFactory(col -> new TableCell<ReportService.ProductSalesSummary, String>() {
            @Override protected void updateItem(String item, boolean empty) {
                super.updateItem(item, empty);
                if (empty) { setText(null); } else {
                    ReportService.ProductSalesSummary r = getTableView().getItems().get(getIndex());
                    setText("GALLON".equals(r.productType) ? "جالون" : "كرتونة");
                    setStyle("-fx-font-size: 13px; -fx-padding: 10px 12px; -fx-text-fill: " + Theme.PRIMARY_COLOR + ";");
                }
            }
        });

        TableColumn<ReportService.ProductSalesSummary, String> qtyCol = new TableColumn<>("الكمية المباعة");
        qtyCol.setMinWidth(130); qtyCol.setPrefWidth(150);
        qtyCol.setCellFactory(col -> new TableCell<ReportService.ProductSalesSummary, String>() {
            @Override protected void updateItem(String item, boolean empty) {
                super.updateItem(item, empty);
                if (empty) { setText(null); } else {
                    ReportService.ProductSalesSummary r = getTableView().getItems().get(getIndex());
                    String qtyStr = (r.totalQtySold == Math.floor(r.totalQtySold))
                            ? String.valueOf((int) r.totalQtySold)
                            : String.format("%.2f", r.totalQtySold);
                    setText(qtyStr + " " + r.unitLabel);
                    setStyle("-fx-font-size: 14px; -fx-padding: 10px 12px; -fx-text-fill: " + Theme.GRAY_700 + ";");
                }
            }
        });

        TableColumn<ReportService.ProductSalesSummary, String> revenueCol = new TableColumn<>("إجمالي الإيراد");
        revenueCol.setMinWidth(130); revenueCol.setPrefWidth(160);
        revenueCol.setCellFactory(col -> new TableCell<ReportService.ProductSalesSummary, String>() {
            @Override protected void updateItem(String item, boolean empty) {
                super.updateItem(item, empty);
                if (empty) { setText(null); } else {
                    ReportService.ProductSalesSummary r = getTableView().getItems().get(getIndex());
                    setText(MoneyFormat.egp(r.totalRevenue));
                    setStyle("-fx-font-size: 14px; -fx-padding: 10px 12px; -fx-text-fill: " + Theme.SUCCESS_COLOR + "; -fx-font-weight: bold;");
                }
            }
        });

        salesTable.getColumns().addAll(nameCol, typeCol, qtyCol, revenueCol);
        salesTable.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);

        HBox productTotalBox = new HBox(16);
        productTotalBox.setAlignment(Pos.CENTER_RIGHT);
        productTotalBox.setPadding(new Insets(8, 4, 4, 4));
        grandTotalLabel = new Label("إجمالي المنتجات: 0.00 جنيه");
        grandTotalLabel.setStyle("-fx-font-size: 15px; -fx-font-weight: bold; -fx-text-fill: " + Theme.SUCCESS_COLOR + ";");
        productTotalBox.getChildren().add(grandTotalLabel);

        productsTableCard.getChildren().addAll(tableTitle, salesTable, productTotalBox);

        // ── Services Table ──────────────────────────────────────────────────
        VBox servicesTableCard = new VBox(12);
        servicesTableCard.setStyle(Theme.cardStyle());
        VBox.setVgrow(servicesTableCard, Priority.ALWAYS);

        Label svcTableTitle = new Label("🧽 الخدمات المقدمة في الفترة");
        svcTableTitle.setStyle(Theme.sectionTitleStyle());

        serviceSalesTable = new TableView<>();
        serviceSalesTable.setStyle("-fx-background-color: white; -fx-background-radius: 8;");
        serviceSalesTable.setFixedCellSize(46);
        serviceSalesTable.setPrefHeight(200);

        TableColumn<ReportService.ServiceSalesSummary, String> svcNameCol = new TableColumn<>("اسم الخدمة");
        svcNameCol.setMinWidth(200); svcNameCol.setPrefWidth(300);
        svcNameCol.setCellFactory(col -> new TableCell<ReportService.ServiceSalesSummary, String>() {
            @Override protected void updateItem(String item, boolean empty) {
                super.updateItem(item, empty);
                if (empty) { setText(null); } else {
                    setText(getTableView().getItems().get(getIndex()).serviceName);
                    setStyle("-fx-font-size: 14px; -fx-padding: 10px 12px; -fx-font-weight: 600;");
                }
            }
        });

        TableColumn<ReportService.ServiceSalesSummary, String> svcCountCol = new TableColumn<>("عدد المرات");
        svcCountCol.setMinWidth(110); svcCountCol.setPrefWidth(130);
        svcCountCol.setCellFactory(col -> new TableCell<ReportService.ServiceSalesSummary, String>() {
            @Override protected void updateItem(String item, boolean empty) {
                super.updateItem(item, empty);
                if (empty) { setText(null); } else {
                    ReportService.ServiceSalesSummary r = getTableView().getItems().get(getIndex());
                    setText(r.totalCount + " مرة");
                    setStyle("-fx-font-size: 14px; -fx-padding: 10px 12px; -fx-text-fill: " + Theme.PRIMARY_COLOR + ";");
                }
            }
        });

        TableColumn<ReportService.ServiceSalesSummary, String> svcRevenueCol = new TableColumn<>("إجمالي الإيراد");
        svcRevenueCol.setMinWidth(130); svcRevenueCol.setPrefWidth(160);
        svcRevenueCol.setCellFactory(col -> new TableCell<ReportService.ServiceSalesSummary, String>() {
            @Override protected void updateItem(String item, boolean empty) {
                super.updateItem(item, empty);
                if (empty) { setText(null); } else {
                    ReportService.ServiceSalesSummary r = getTableView().getItems().get(getIndex());
                    setText(MoneyFormat.egp(r.totalRevenue));
                    setStyle("-fx-font-size: 14px; -fx-padding: 10px 12px; -fx-text-fill: " + Theme.SUCCESS_COLOR + "; -fx-font-weight: bold;");
                }
            }
        });

        serviceSalesTable.getColumns().addAll(svcNameCol, svcCountCol, svcRevenueCol);
        serviceSalesTable.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);

        HBox svcTotalBox = new HBox(16);
        svcTotalBox.setAlignment(Pos.CENTER_RIGHT);
        svcTotalBox.setPadding(new Insets(8, 4, 4, 4));
        serviceGrandTotalLabel = new Label("إجمالي الخدمات: 0.00 جنيه");
        serviceGrandTotalLabel.setStyle("-fx-font-size: 15px; -fx-font-weight: bold; -fx-text-fill: " + Theme.SUCCESS_COLOR + ";");
        svcTotalBox.getChildren().add(serviceGrandTotalLabel);

        servicesTableCard.getChildren().addAll(svcTableTitle, serviceSalesTable, svcTotalBox);

        content.getChildren().addAll(filterCard, productsTableCard, servicesTableCard);
        return content;
    }

    private void refreshSalesReport() {
        LocalDate start = salesStartPicker.getValue();
        LocalDate end = salesEndPicker.getValue();
        if (start == null || end == null) return;

        String startStr = start.format(DateTimeFormatter.ISO_DATE);
        String endStr = end.format(DateTimeFormatter.ISO_DATE);

        List<ReportService.ProductSalesSummary> productRows = ReportService.getProductSalesReport(startStr, endStr);
        salesTable.setItems(FXCollections.observableArrayList(productRows));
        double productTotal = ReportService.getProductsGrandTotal(startStr, endStr);
        grandTotalLabel.setText("إجمالي المنتجات: " + MoneyFormat.egp(productTotal));

        List<ReportService.ServiceSalesSummary> serviceRows = ReportService.getServiceSalesReport(startStr, endStr);
        serviceSalesTable.setItems(FXCollections.observableArrayList(serviceRows));
        double serviceTotal = ReportService.getServicesGrandTotal(startStr, endStr);
        serviceGrandTotalLabel.setText("إجمالي الخدمات: " + MoneyFormat.egp(serviceTotal));
    }

    private void printCurrentSalesReport() {
        LocalDate start = salesStartPicker.getValue();
        LocalDate end = salesEndPicker.getValue();
        if (start == null || end == null) return;

        String startStr = start.format(DateTimeFormatter.ISO_DATE);
        String endStr = end.format(DateTimeFormatter.ISO_DATE);
        List<ReportService.ProductSalesSummary> rows = ReportService.getProductSalesReport(startStr, endStr);
        double grandTotal = ReportService.getProductsGrandTotal(startStr, endStr);
        PrintService.printSalesReport(startStr, endStr, rows, grandTotal);
    }

    // ── Sidebar ──────────────────────────────────────────────────────────

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
        productsBtn.setOnAction(e -> ScreenNavigator.navigateTo(new ProductScreen()));
        Button receiptBtn = createNavButton("فاتورة جديدة", "🧾");
        receiptBtn.setOnAction(e -> ScreenNavigator.navigateTo(new ReceiptScreen()));
        Button paymentsBtn = createNavButton("المدفوعات", "💳");
        paymentsBtn.setOnAction(e -> ScreenNavigator.navigateTo(new PaymentScreen()));
        Button reportsBtn = createNavButton("التقارير", "📈");
        reportsBtn.setStyle(getActiveNavStyle());
        Button usersBtn = createNavButton("المستخدمين", "⚙️");
        usersBtn.setOnAction(e -> ScreenNavigator.navigateTo(new UserManagementScreen()));

        navSection.getChildren().addAll(dashboardBtn, customersBtn, servicesBtn, productsBtn,
                receiptBtn, paymentsBtn, reportsBtn, usersBtn);

        Region navSpacer = new Region();
        VBox.setVgrow(navSpacer, Priority.ALWAYS);

        Button logoutBtn = createNavButton("تسجيل الخروج", "🚪");
        logoutBtn.setOnAction(e -> { oop.carwash.service.AuthService.logout(); ScreenNavigator.navigateTo(new LoginScreen()); });

        sidebar.getChildren().addAll(logoSection, navSection, navSpacer, logoutBtn);
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

    @Override
    public javafx.scene.Parent getRoot() { return root; }
}

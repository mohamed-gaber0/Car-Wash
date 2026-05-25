package oop.carwash.ui;

import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;

/**
 * Modern Theme class for the Car Wash Billing System.
 * Provides consistent styling across all screens with a professional,
 * modern design language.
 * 
 * IMPROVED: Better table styling, larger buttons, clearer text
 */
public class Theme {

    // ── Color Palette ─────────────────────────────────────────────────────
    public static final String PRIMARY_COLOR = "#2563EB";        // Blue
    public static final String PRIMARY_DARK = "#1D4ED8";         // Darker Blue
    public static final String PRIMARY_LIGHT = "#3B82F6";        // Lighter Blue
    
    public static final String SECONDARY_COLOR = "#0EA5E9";      // Sky Blue
    public static final String ACCENT_COLOR = "#06B6D4";         // Cyan
    
    public static final String SUCCESS_COLOR = "#10B981";        // Green
    public static final String SUCCESS_LIGHT = "#34D399";
    public static final String WARNING_COLOR = "#F59E0B";        // Orange
    public static final String WARNING_LIGHT = "#FBBF24";
    public static final String DANGER_COLOR = "#EF4444";         // Red
    public static final String DANGER_LIGHT = "#F87171";
    public static final String INFO_COLOR = "#0EA5E9";           // Sky Blue (for info buttons)
    
    public static final String DARK_COLOR = "#1E293B";           // Slate Dark
    public static final String GRAY_900 = "#0F172A";
    public static final String GRAY_800 = "#1E293B";
    public static final String GRAY_700 = "#334155";
    public static final String GRAY_600 = "#475569";
    public static final String GRAY_500 = "#64748B";
    public static final String GRAY_400 = "#94A3B8";
    public static final String GRAY_300 = "#CBD5E1";
    public static final String GRAY_200 = "#E2E8F0";
    public static final String GRAY_100 = "#F1F5F9";
    public static final String GRAY_50 = "#F8FAFC";
    public static final String WHITE = "#FFFFFF";

    // ── Gradient Backgrounds ───────────────────────────────────────────────
    public static final String GRADIENT_PRIMARY = "linear-gradient(to right, #2563EB, #0EA5E9)";
    public static final String GRADIENT_SUCCESS = "linear-gradient(to right, #10B981, #34D399)";
    public static final String GRADIENT_HEADER = "linear-gradient(to right, #1E293B, #334155)";

    // ── Shadow Styles ──────────────────────────────────────────────────────
    public static final String SHADOW_SM = "-fx-effect: dropshadow(gaussian, rgba(0,0,0,0.05), 4, 0, 0, 1);";
    public static final String SHADOW_MD = "-fx-effect: dropshadow(gaussian, rgba(0,0,0,0.1), 8, 0, 0, 2);";
    public static final String SHADOW_LG = "-fx-effect: dropshadow(gaussian, rgba(0,0,0,0.15), 16, 0, 0, 4);";
    public static final String SHADOW_CARD = "-fx-effect: dropshadow(gaussian, rgba(0,0,0,0.08), 12, 0.2, 0, 2);";

    // ── Border Radius ─────────────────────────────────────────────────────
    public static final String RADIUS_SM = "-fx-background-radius: 4;";
    public static final String RADIUS_MD = "-fx-background-radius: 8;";
    public static final String RADIUS_LG = "-fx-background-radius: 12;";
    public static final String RADIUS_XL = "-fx-background-radius: 16;";
    public static final String RADIUS_FULL = "-fx-background-radius: 9999;";

    // ── Spacing ────────────────────────────────────────────────────────────
    public static final Insets SPACING_XS = new Insets(4);
    public static final Insets SPACING_SM = new Insets(8);
    public static final Insets SPACING_MD = new Insets(12);
    public static final Insets SPACING_LG = new Insets(16);
    public static final Insets SPACING_XL = new Insets(24);
    public static final Insets SPACING_2XL = new Insets(32);

    // ── Font Sizes ─────────────────────────────────────────────────────────
    public static final String FONT_XS = "-fx-font-size: 10px;";
    public static final String FONT_SM = "-fx-font-size: 12px;";
    public static final String FONT_BASE = "-fx-font-size: 14px;";
    public static final String FONT_LG = "-fx-font-size: 16px;";
    public static final String FONT_XL = "-fx-font-size: 18px;";
    public static final String FONT_2XL = "-fx-font-size: 24px;";
    public static final String FONT_3XL = "-fx-font-size: 30px;";
    public static final String FONT_4XL = "-fx-font-size: 36px;";

    // ── Card Styles ────────────────────────────────────────────────────────
    public static String cardStyle() {
        return "-fx-background-color: " + WHITE + "; " +
               RADIUS_LG + " " +
               SHADOW_CARD + " " +
               "-fx-padding: 20px; -fx-background-insets: 0;";
    }

    public static String cardStyleCompact() {
        return "-fx-background-color: " + WHITE + "; " +
               RADIUS_MD + " " +
               SHADOW_SM + " " +
               "-fx-padding: 12px; -fx-background-insets: 0;";
    }

    // ── Button Styles (IMPROVED with larger padding and clearer text) ───────
    public static String primaryButtonStyle() {
        return "-fx-background-color: " + PRIMARY_COLOR + "; " +
               "-fx-text-fill: white; " +
               "-fx-font-size: 15px; " +
               "-fx-font-weight: bold; " +
               "-fx-padding: 12px 28px; " +
               RADIUS_MD + " " +
               "-fx-cursor: hand; " +
               "-fx-border-width: 0; " +
               "-fx-min-height: 40px;";
    }

    public static String primaryButtonHoverStyle() {
        return "-fx-background-color: " + PRIMARY_DARK + "; " +
               "-fx-text-fill: white; " +
               "-fx-font-size: 15px; " +
               "-fx-font-weight: bold; " +
               "-fx-padding: 12px 28px; " +
               RADIUS_MD + " " +
               "-fx-cursor: hand; " +
               "-fx-border-width: 0; " +
               "-fx-min-height: 40px;";
    }

    public static String secondaryButtonStyle() {
        return "-fx-background-color: " + GRAY_100 + "; " +
               "-fx-text-fill: " + GRAY_700 + "; " +
               "-fx-font-size: 15px; " +
               "-fx-font-weight: 600; " +
               "-fx-padding: 12px 28px; " +
               RADIUS_MD + " " +
               "-fx-cursor: hand; " +
               "-fx-border-width: 0; " +
               "-fx-min-height: 40px;";
    }

    public static String secondaryButtonHoverStyle() {
        return "-fx-background-color: " + GRAY_200 + "; " +
               "-fx-text-fill: " + GRAY_800 + "; " +
               "-fx-font-size: 15px; " +
               "-fx-font-weight: 600; " +
               "-fx-padding: 12px 28px; " +
               RADIUS_MD + " " +
               "-fx-cursor: hand; " +
               "-fx-border-width: 0; " +
               "-fx-min-height: 40px;";
    }

    public static String successButtonStyle() {
        return "-fx-background-color: " + SUCCESS_COLOR + "; " +
               "-fx-text-fill: white; " +
               "-fx-font-size: 15px; " +
               "-fx-font-weight: bold; " +
               "-fx-padding: 12px 28px; " +
               RADIUS_MD + " " +
               "-fx-cursor: hand; " +
               "-fx-border-width: 0; " +
               "-fx-min-height: 40px;";
    }

    public static String dangerButtonStyle() {
        return "-fx-background-color: " + DANGER_COLOR + "; " +
               "-fx-text-fill: white; " +
               "-fx-font-size: 15px; " +
               "-fx-font-weight: bold; " +
               "-fx-padding: 12px 28px; " +
               RADIUS_MD + " " +
               "-fx-cursor: hand; " +
               "-fx-border-width: 0; " +
               "-fx-min-height: 40px;";
    }

    public static String warningButtonStyle() {
        return "-fx-background-color: " + WARNING_COLOR + "; " +
               "-fx-text-fill: white; " +
               "-fx-font-size: 15px; " +
               "-fx-font-weight: bold; " +
               "-fx-padding: 12px 28px; " +
               RADIUS_MD + " " +
               "-fx-cursor: hand; " +
               "-fx-border-width: 0; " +
               "-fx-min-height: 40px;";
    }

    public static String outlineButtonStyle() {
        return "-fx-background-color: transparent; " +
               "-fx-text-fill: " + PRIMARY_COLOR + "; " +
               "-fx-font-size: 15px; " +
               "-fx-font-weight: 600; " +
               "-fx-padding: 12px 28px; " +
               RADIUS_MD + " " +
               "-fx-cursor: hand; " +
               "-fx-border-color: " + PRIMARY_COLOR + "; " +
               "-fx-border-width: 2; " +
               "-fx-min-height: 40px;";
    }

    // ── Input Field Styles (IMPROVED) ───────────────────────────────────────
    public static String inputFieldStyle() {
        return "-fx-background-color: " + WHITE + "; " +
               "-fx-border-color: " + GRAY_300 + "; " +
               "-fx-border-width: 1; " +
               "-fx-border-radius: 8; " +
               "-fx-background-radius: 8; " +
               "-fx-padding: 12px 16px; " +
               "-fx-font-size: 15px; " +
               "-fx-text-fill: " + GRAY_900 + "; " +
               "-fx-min-height: 40px;";
    }

    public static String inputFieldFocusStyle() {
        return "-fx-background-color: " + WHITE + "; " +
               "-fx-border-color: " + PRIMARY_COLOR + "; " +
               "-fx-border-width: 2; " +
               "-fx-border-radius: 8; " +
               "-fx-background-radius: 8; " +
               "-fx-padding: 11px 15px; " +
               "-fx-font-size: 15px; " +
               "-fx-text-fill: " + GRAY_900 + "; " +
               "-fx-min-height: 40px;";
    }

    // ── Label Styles ───────────────────────────────────────────────────────
    public static String pageTitleStyle() {
        return "-fx-font-size: 28px; " +
               "-fx-font-weight: bold; " +
               "-fx-text-fill: " + GRAY_900 + ";";
    }

    public static String sectionTitleStyle() {
        return "-fx-font-size: 18px; " +
               "-fx-font-weight: bold; " +
               "-fx-text-fill: " + GRAY_800 + ";";
    }

    public static String labelStyle() {
        return "-fx-font-size: 15px; " +
               "-fx-font-weight: 600; " +
               "-fx-text-fill: " + GRAY_700 + ";";
    }

    public static String hintStyle() {
        return "-fx-font-size: 12px; " +
               "-fx-text-fill: " + GRAY_500 + ";";
    }

    // ── Table Styles (IMPROVED with better readability) ─────────────────────
    public static String tableStyle() {
        return "-fx-background-color: " + WHITE + "; " +
               "-fx-border-color: " + GRAY_200 + "; " +
               "-fx-border-width: 1; " +
               "-fx-border-radius: 8; " +
               "-fx-background-radius: 8;";
    }

    public static String tableHeaderStyle() {
        return "-fx-background-color: " + GRAY_50 + "; " +
               "-fx-text-fill: " + GRAY_700 + "; " +
               "-fx-font-size: 14px; " +
               "-fx-font-weight: bold; " +
               "-fx-padding: 14px 16px;";
    }

    public static String tableCellStyle() {
        return "-fx-text-fill: " + GRAY_800 + "; " +
               "-fx-font-size: 14px; " +
               "-fx-padding: 10px 14px;";
    }

    public static String tableRowStyle() {
        return "-fx-cell-size: 44px; " +
               "-fx-fixed-cell-size: 44px; ";
    }

    // ── Background Styles ──────────────────────────────────────────────────
    public static String pageBackgroundStyle() {
        return "-fx-background-color: " + GRAY_100 + ";";
    }

    public static String sidebarStyle() {
        return "-fx-background-color: " + DARK_COLOR + ";";
    }

    // ── Helper Methods (IMPROVED) ───────────────────────────────────────────

    public static Button createPrimaryButton(String text) {
        Button btn = new Button(text);
        btn.setStyle(primaryButtonStyle());
        btn.setMinWidth(100);
        btn.setMinHeight(40);
        btn.setOnMouseEntered(e -> btn.setStyle(primaryButtonHoverStyle()));
        btn.setOnMouseExited(e -> btn.setStyle(primaryButtonStyle()));
        return btn;
    }

    public static Button createSecondaryButton(String text) {
        Button btn = new Button(text);
        btn.setStyle(secondaryButtonStyle());
        btn.setMinWidth(100);
        btn.setMinHeight(40);
        btn.setOnMouseEntered(e -> btn.setStyle(secondaryButtonHoverStyle()));
        btn.setOnMouseExited(e -> btn.setStyle(secondaryButtonStyle()));
        return btn;
    }

    public static Button createSuccessButton(String text) {
        Button btn = new Button(text);
        btn.setStyle(successButtonStyle());
        btn.setMinWidth(100);
        btn.setMinHeight(40);
        btn.setOnMouseEntered(e -> btn.setStyle("-fx-background-color: " + SUCCESS_LIGHT + "; " +
                "-fx-text-fill: white; -fx-font-size: 15px; -fx-font-weight: bold; " +
                "-fx-padding: 12px 28px; " + RADIUS_MD + " -fx-cursor: hand; -fx-border-width: 0; -fx-min-height: 40px;"));
        btn.setOnMouseExited(e -> btn.setStyle(successButtonStyle()));
        return btn;
    }

    public static Button createDangerButton(String text) {
        Button btn = new Button(text);
        btn.setStyle(dangerButtonStyle());
        btn.setMinWidth(100);
        btn.setMinHeight(40);
        btn.setOnMouseEntered(e -> btn.setStyle("-fx-background-color: " + DANGER_LIGHT + "; " +
                "-fx-text-fill: white; -fx-font-size: 15px; -fx-font-weight: bold; " +
                "-fx-padding: 12px 28px; " + RADIUS_MD + " -fx-cursor: hand; -fx-border-width: 0; -fx-min-height: 40px;"));
        btn.setOnMouseExited(e -> btn.setStyle(dangerButtonStyle()));
        return btn;
    }

    public static Button createWarningButton(String text) {
        Button btn = new Button(text);
        btn.setStyle(warningButtonStyle());
        btn.setMinWidth(100);
        btn.setMinHeight(40);
        return btn;
    }

    public static javafx.scene.control.TextField createTextField(String prompt) {
        javafx.scene.control.TextField field = new javafx.scene.control.TextField();
        field.setPromptText(prompt);
        field.setStyle(inputFieldStyle());
        field.setPrefWidth(250);
        field.setMinHeight(40);
        field.focusedProperty().addListener((obs, oldVal, newVal) -> {
            if (newVal) {
                field.setStyle(inputFieldFocusStyle());
            } else {
                field.setStyle(inputFieldStyle());
            }
        });
        return field;
    }

    public static javafx.scene.control.PasswordField createPasswordField(String prompt) {
        javafx.scene.control.PasswordField field = new javafx.scene.control.PasswordField();
        field.setPromptText(prompt);
        field.setStyle(inputFieldStyle());
        field.setPrefWidth(250);
        field.setMinHeight(40);
        field.focusedProperty().addListener((obs, oldVal, newVal) -> {
            if (newVal) {
                field.setStyle(inputFieldFocusStyle());
            } else {
                field.setStyle(inputFieldStyle());
            }
        });
        return field;
    }

    public static VBox createCard(String title) {
        VBox card = new VBox(12);
        card.setStyle(cardStyle());
        
        if (title != null && !title.isEmpty()) {
            Label titleLabel = new Label(title);
            titleLabel.setStyle(sectionTitleStyle());
            card.getChildren().add(titleLabel);
        }
        
        return card;
    }

    public static VBox createStatCard(String title, String icon, String color) {
        VBox card = new VBox(8);
        card.setStyle("-fx-background-color: " + WHITE + "; " +
                     RADIUS_LG + " " +
                     SHADOW_CARD + " " +
                     "-fx-padding: 20px; " +
                     "-fx-border-color: " + GRAY_100 + "; " +
                     "-fx-border-width: 1; " +
                     "-fx-border-radius: 12;");
        card.setPrefWidth(220);
        
        Label iconLabel = new Label(icon);
        iconLabel.setStyle("-fx-font-size: 32px;");
        
        Label titleLabel = new Label(title);
        titleLabel.setStyle("-fx-font-size: 12px; -fx-text-fill: " + GRAY_500 + ";");
        
        card.getChildren().addAll(iconLabel, titleLabel);
        return card;
    }

    public static HBox createHeader(String title, Button... extraButtons) {
        Label titleLabel = new Label(title);
        titleLabel.setStyle("-fx-font-size: 18px; -fx-font-weight: bold; -fx-text-fill: white;");
        
        HBox header = new HBox(16);
        header.setStyle("-fx-background-color: " + DARK_COLOR + "; " +
                       "-fx-padding: 12px 24px; " +
                       "-fx-background-radius: 0;");
        header.setAlignment(Pos.CENTER_LEFT);
        
        Label brandIcon = new Label("🚿");
        brandIcon.setStyle("-fx-font-size: 24px;");
        
        header.getChildren().add(brandIcon);
        header.getChildren().add(titleLabel);
        
        if (extraButtons != null && extraButtons.length > 0) {
            javafx.scene.layout.Region spacer = new javafx.scene.layout.Region();
            HBox.setHgrow(spacer, javafx.scene.layout.Priority.ALWAYS);
            header.getChildren().add(spacer);
            
            for (Button btn : extraButtons) {
                btn.setStyle("-fx-background-color: transparent; " +
                           "-fx-text-fill: white; " +
                           "-fx-font-size: 13px; " +
                           "-fx-padding: 8px 16px; " +
                           RADIUS_MD +
                           "-fx-cursor: hand;");
                btn.setOnMouseEntered(e -> btn.setStyle("-fx-background-color: " + GRAY_700 + "; " +
                           "-fx-text-fill: white; -fx-font-size: 13px; -fx-padding: 8px 16px; " +
                           RADIUS_MD + "-fx-cursor: hand;"));
                btn.setOnMouseExited(e -> btn.setStyle("-fx-background-color: transparent; " +
                           "-fx-text-fill: white; -fx-font-size: 13px; -fx-padding: 8px 16px; " +
                           RADIUS_MD + "-fx-cursor: hand;"));
                header.getChildren().add(btn);
            }
        }
        
        return header;
    }

    public static Button createBackButton() {
        Button backBtn = new Button("← Back");
        backBtn.setStyle("-fx-background-color: transparent; " +
                        "-fx-text-fill: " + GRAY_600 + "; " +
                        "-fx-font-size: 15px; " +
                        "-fx-padding: 8px 0; " +
                        "-fx-cursor: hand;");
        backBtn.setOnMouseEntered(e -> backBtn.setStyle("-fx-background-color: transparent; " +
                        "-fx-text-fill: " + PRIMARY_COLOR + "; " +
                        "-fx-font-size: 15px; " +
                        "-fx-padding: 8px 0; " +
                        "-fx-cursor: hand;"));
        backBtn.setOnMouseExited(e -> backBtn.setStyle("-fx-background-color: transparent; " +
                        "-fx-text-fill: " + GRAY_600 + "; " +
                        "-fx-font-size: 15px; " +
                        "-fx-padding: 8px 0; " +
                        "-fx-cursor: hand;"));
        return backBtn;
    }

    /**
     * IMPROVED: Style table with better row height, header styling, and overall appearance
     */
    public static void styleTable(javafx.scene.control.TableView<?> table) {
        // Main table styling
        table.setStyle("-fx-background-color: " + WHITE + "; " +
                      "-fx-background-radius: 8; " +
                      "-fx-border-color: " + GRAY_200 + "; " +
                      "-fx-border-width: 1; " +
                      "-fx-border-radius: 8; " +
                      "-fx-fixed-cell-size: 44px;");
        
        // Use constrained resize policy for better column fitting
        table.setColumnResizePolicy(javafx.scene.control.TableView.CONSTRAINED_RESIZE_POLICY);
        
        // Apply CSS for better table appearance
        table.getStylesheets().clear();
    }
    
    /**
     * Get CSS for table styling (can be applied via stylesheet)
     */
    public static String getTableCSS() {
        return ".table-view {\n" +
               "    -fx-background-color: " + WHITE + ";\n" +
               "    -fx-background-radius: 8;\n" +
               "    -fx-border-color: " + GRAY_200 + ";\n" +
               "    -fx-border-width: 1;\n" +
               "    -fx-border-radius: 8;\n" +
               "    -fx-fixed-cell-size: 44px;\n" +
               "}\n" +
               ".table-view .column-header {\n" +
               "    -fx-background-color: " + GRAY_50 + ";\n" +
               "    -fx-text-fill: " + GRAY_700 + ";\n" +
               "    -fx-font-size: 14px;\n" +
               "    -fx-font-weight: bold;\n" +
               "    -fx-padding: 14px 16px;\n" +
               "    -fx-border-color: " + GRAY_200 + ";\n" +
               "    -fx-border-width: 0 0 1 0;\n" +
               "}\n" +
               ".table-view .table-cell {\n" +
               "    -fx-text-fill: " + GRAY_800 + ";\n" +
               "    -fx-font-size: 14px;\n" +
               "    -fx-padding: 10px 14px;\n" +
               "    -fx-cell-size: 44px;\n" +
               "}\n" +
               ".table-view .table-row-cell:selected {\n" +
               "    -fx-background-color: derive(" + PRIMARY_COLOR + ", 90%);\n" +
               "}\n" +
               ".table-view .table-row-cell:hover {\n" +
               "    -fx-background-color: " + GRAY_50 + ";\n" +
               "}";
    }

    /**
     * Create a styled button with custom minimum width
     */
    public static Button createButtonWithMinWidth(String text, String style, double minWidth) {
        Button btn = new Button(text);
        btn.setStyle(style);
        btn.setMinWidth(minWidth);
        btn.setMinHeight(40);
        return btn;
    }
}

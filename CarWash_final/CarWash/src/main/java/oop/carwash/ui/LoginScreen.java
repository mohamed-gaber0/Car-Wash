package oop.carwash.ui;

import oop.carwash.service.AuthService;

import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import javafx.scene.layout.VBox;
import javafx.scene.layout.HBox;

/**
 * Modern Login screen with professional card-based design.
 * Updated with consistent styling matching CustomerScreen.
 */
public class LoginScreen implements AppScreen {

    private VBox root;
    private TextField userField;
    private PasswordField passField;

    public LoginScreen() {
        buildUI();
    }

    private void buildUI() {
        root = new VBox();
        root.setAlignment(Pos.CENTER);
        root.setStyle("-fx-background-color: linear-gradient(135deg, #1E293B 0%, #0F172A 100%);");
        root.setFillWidth(true);
        
        // Login card
        VBox card = new VBox(24);
        card.setStyle("-fx-background-color: " + Theme.WHITE + "; " + Theme.RADIUS_XL + " " + Theme.SHADOW_LG + " -fx-padding: 48px 40px; -fx-background-insets: 0;");
        card.setMaxWidth(420);
        card.setAlignment(Pos.CENTER);
        
        // Logo/Icon
        Label logoIcon = new Label("🚿");
        logoIcon.setStyle("-fx-font-size: 56px;");
        
        // Title section
        Label titleLabel = new Label("نظام مغسلة السيارات");
        titleLabel.setStyle("-fx-font-size: 28px; -fx-font-weight: bold; -fx-text-fill: " + Theme.GRAY_900 + ";");
        
        Label subtitleLabel = new Label("برجاء تسجيل الدخول لحسابك");
        subtitleLabel.setStyle("-fx-font-size: 15px; -fx-text-fill: " + Theme.GRAY_500 + ";");
        
        VBox titleBox = new VBox(8, logoIcon, titleLabel, subtitleLabel);
        titleBox.setAlignment(Pos.CENTER);
        
        // Form section
        VBox formBox = new VBox(20);
        formBox.setAlignment(Pos.CENTER);
        
        // Username field
        VBox userBox = new VBox(8);
        Label userLabel = new Label("اسم المستخدم");
        userLabel.setStyle(Theme.labelStyle());
        userField = Theme.createTextField("اكتب اسم المستخدم");
        userField.setPrefWidth(320);
        userBox.getChildren().addAll(userLabel, userField);
        
        // Password field
        VBox passBox = new VBox(8);
        Label passLabel = new Label("كلمة السر");
        passLabel.setStyle(Theme.labelStyle());
        passField = Theme.createPasswordField("اكتب كلمة السر");
        passField.setPrefWidth(320);
        passBox.getChildren().addAll(passLabel, passField);
        
        // Buttons
        HBox buttonBox = new HBox(12);
        buttonBox.setAlignment(Pos.CENTER);
        
        Button loginBtn = Theme.createPrimaryButton("تسجيل الدخول");
        loginBtn.setPrefWidth(150);
        loginBtn.setOnAction(e -> handleLogin());
        
        Button clearBtn = Theme.createSecondaryButton("مسح");
        clearBtn.setPrefWidth(100);
        clearBtn.setOnAction(e -> clearForm());
        
        buttonBox.getChildren().addAll(loginBtn, clearBtn);
        
        // Hint label
        Label hintLabel = new Label("للتجربة: اسم المستخدم admin وكلمة السر admin");
        hintLabel.setStyle("-fx-font-size: 12px; -fx-text-fill: " + Theme.GRAY_400 + "; -fx-padding: 16px 0 0 0;");
        
        formBox.getChildren().addAll(userBox, passBox, buttonBox, hintLabel);
        
        // Assemble card
        card.getChildren().addAll(titleBox, formBox);
        
        root.getChildren().add(card);
        
        // Allow Enter key to submit
        userField.setOnAction(e -> passField.requestFocus());
        passField.setOnAction(e -> handleLogin());
    }
    
    private void handleLogin() {
        String username = userField.getText();
        String password = passField.getText();

        if (AuthService.login(username, password)) {
            ScreenNavigator.navigateTo(new DashboardScreen());
        } else {
            showError("بيانات الدخول غلط. راجع اسم المستخدم وكلمة السر.");
            passField.clear();
            passField.requestFocus();
        }
    }
    
    private void clearForm() {
        userField.clear();
        passField.clear();
        userField.requestFocus();
    }

    private void showError(String message) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle("خطأ في تسجيل الدخول");
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }

    @Override
    public javafx.scene.Parent getRoot() { return root; }
}

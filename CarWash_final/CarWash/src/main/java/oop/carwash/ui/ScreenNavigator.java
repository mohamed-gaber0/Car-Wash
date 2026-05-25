package oop.carwash.ui;

import javafx.scene.Scene;
import javafx.scene.layout.BorderPane;
import javafx.stage.Stage;

import javafx.geometry.NodeOrientation;

/**
 * Manages navigation between screens.
 *
 * Single Stage, single Scene. The root is a BorderPane.
 * To navigate, call navigateTo(screen) and it swaps the center region.
 *
 * Usage from MainApp:
 *   ScreenNavigator.init(primaryStage);
 *   ScreenNavigator.navigateTo(new LoginScreen());
 */
public class ScreenNavigator {

    private static Stage stage;
    private static Scene scene;
    private static BorderPane root;

    private ScreenNavigator() {
        // Utility class
    }

    // ── Initialization ─────────────────────────────────────────────────────

    /**
     * Initialize the navigator with a stage.
     */
    public static void init(Stage primaryStage) {
        stage = primaryStage;
        root = new BorderPane();
        root.setStyle("-fx-font-family: 'Segoe UI', sans-serif; -fx-font-size: 12px;");
        root.setNodeOrientation(NodeOrientation.RIGHT_TO_LEFT);

        scene = new Scene(root, 1280, 800);
        stage.setScene(scene);
        stage.setTitle("مغسلة مركز ابو جميل لغسيل وزيت السيارات");
        stage.setMinWidth(1024);
        stage.setMinHeight(700);
        // Use maximized mode instead of fullscreen - taskbar stays visible
        stage.setMaximized(true);
    }

    // ── Navigation ─────────────────────────────────────────────────────────

    /**
     * Navigate to a screen (replace center region).
     */
    public static void navigateTo(AppScreen screen) {
        if (screen != null) {
            root.setCenter(screen.getRoot());
            System.out.println("[Nav] Navigated to " + screen.getClass().getSimpleName());
        }
    }

    // ── Accessors ──────────────────────────────────────────────────────────

    public static Stage getStage() {
        return stage;
    }

    public static BorderPane getRoot() {
        return root;
    }

    public static void show() {
        if (stage != null) {
            stage.show();
        }
    }
}
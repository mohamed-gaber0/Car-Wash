package oop.carwash;


import oop.carwash.dao.DatabaseConnection;
import oop.carwash.dao.DatabaseManager;
import oop.carwash.socket.CarWashServer;
import oop.carwash.ui.ScreenNavigator;
import oop.carwash.ui.LoginScreen;

import javafx.application.Application;
import javafx.application.Platform;
import javafx.stage.Stage;

/**
 * Application entry point.
 *
 * Startup sequence:
 *   1. DatabaseConnection singleton opens carwash.db
 *   2. DatabaseManager creates all tables / indexes (idempotent)
 *   3. CarWashServer starts in a daemon background thread
 *   4. ScreenNavigator is initialized
 *   5. LoginScreen is shown
 *
 * Shutdown sequence (Application.stop):
 *   1. CarWashServer is stopped
 *   2. DatabaseConnection is closed
 */
public class MainApp extends Application {

    @Override
    public void init() {
        // init() runs on the JavaFX Launcher thread BEFORE start().
        // Initialise the DB here so the UI never has to wait for it.
        try {
            DatabaseManager.initialise();
        } catch (RuntimeException e) {
            System.err.println("[FATAL] " + e.getMessage());
            Platform.exit();
        }
    }

    @Override
    public void start(Stage primaryStage) {
        // ── Start socket server in background ─────────────────────────
        Thread serverThread = new Thread(
            CarWashServer::start,
            "carwash-socket-server"
        );
        serverThread.setDaemon(true);
        serverThread.start();
        System.out.println("[App] Socket server thread started");

        // ── Initialize screen navigator ───────────────────────────────
        ScreenNavigator.init(primaryStage);
        primaryStage.setOnCloseRequest(e -> Platform.exit());

        // ── Show login screen ─────────────────────────────────────────
        ScreenNavigator.navigateTo(new LoginScreen());
        ScreenNavigator.show();
    }

    @Override
    public void stop() {
        System.out.println("[App] Shutting down...");
        CarWashServer.stop();
        DatabaseConnection.getInstance().close();
        System.out.println("[App] Goodbye.");
    }

    public static void main(String[] args) {
        launch(args);
    }
}
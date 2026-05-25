package oop.carwash.ui;


import oop.carwash.socket.CarWashClient;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.scene.layout.Priority;
import javafx.scene.paint.Color;

import java.util.Map;

/**
 * Demo screen for testing socket client/server communication.
 * 
 * Connect to CarWashServer on localhost:5050 and send commands.
 * Shows real-time responses and parsed data.
 */
public class SocketDemoScreen implements AppScreen {

    private VBox root;
    private TextArea logArea;
    private Label statusLabel;
    private CarWashClient client;
    private boolean connected = false;

    public SocketDemoScreen() {
        buildUI();
    }

    private void buildUI() {
        // Header
        Button backBtn = new Button("← Back");
        backBtn.setOnAction(e -> ScreenNavigator.navigateTo(new DashboardScreen()));

        HBox header = new HBox(10, backBtn);
        header.setPadding(new Insets(10));
        header.setStyle("-fx-border-color: " + Theme.GRAY_200 + "; -fx-border-width: 0 0 1 0;");

        // Title
        Label titleLabel = new Label("Socket Client/Server Demo");
        titleLabel.setStyle("-fx-font-size: 24px; -fx-font-weight: bold; -fx-text-fill: " + Theme.GRAY_900 + ";");

        // Status
        statusLabel = new Label("Status: Disconnected");
        statusLabel.setStyle("-fx-font-size: 12px; -fx-text-fill: " + Theme.DANGER_COLOR + ";");

        HBox statusBox = new HBox(10, new Label("Server: localhost:5050"), statusLabel);
        statusBox.setPadding(new Insets(10));
        statusBox.setStyle("-fx-border-color: " + Theme.GRAY_100 + "; -fx-border-width: 1;");

        // Connection buttons
        HBox connectionBox = buildConnectionBox();

        // Command buttons
        VBox commandsBox = buildCommandsBox();

        // Log area
        Label logLabel = new Label("Server Responses:");
        logLabel.setStyle("-fx-font-weight: bold; -fx-text-fill: " + Theme.GRAY_800 + ";");

        logArea = new TextArea();
        logArea.setWrapText(true);
        logArea.setEditable(false);
        logArea.setPrefHeight(300);
        logArea.setStyle("-fx-font-family: monospace; -fx-font-size: 10px; -fx-control-inner-background: " + Theme.GRAY_50 + ";");

        VBox logBox = new VBox(8, logLabel, logArea);
        VBox.setVgrow(logArea, Priority.ALWAYS);

        // Custom command
        HBox customBox = buildCustomCommandBox();

        // Main content
        VBox content = new VBox(12,
            titleLabel,
            statusBox,
            connectionBox,
            commandsBox,
            logBox,
            customBox
        );
        content.setPadding(new Insets(20));
        content.setStyle("-fx-background-color: " + Theme.GRAY_100 + ";");
        VBox.setVgrow(logBox, Priority.ALWAYS);

        root = new VBox(header, content);
        root.setStyle("-fx-background-color: " + Theme.GRAY_100 + ";");
    }

    private HBox buildConnectionBox() {
        Button connectBtn = new Button("Connect");
        connectBtn.setPrefWidth(100);
        connectBtn.setStyle("-fx-font-size: 12px; -fx-background-color: " + Theme.SUCCESS_COLOR + "; -fx-text-fill: white;");
        connectBtn.setOnAction(e -> connectToServer());

        Button disconnectBtn = new Button("Disconnect");
        disconnectBtn.setPrefWidth(100);
        disconnectBtn.setStyle("-fx-font-size: 12px; -fx-background-color: " + Theme.DANGER_COLOR + "; -fx-text-fill: white;");
        disconnectBtn.setOnAction(e -> disconnectServer());

        Button clearLogBtn = new Button("Clear Log");
        clearLogBtn.setPrefWidth(100);
        clearLogBtn.setStyle("-fx-font-size: 12px; -fx-background-color: " + Theme.GRAY_200 + "; -fx-text-fill: " + Theme.GRAY_700 + ";");
        clearLogBtn.setOnAction(e -> logArea.clear());

        return new HBox(10, connectBtn, disconnectBtn, clearLogBtn);
    }

    private VBox buildCommandsBox() {
        Label label = new Label("Quick Commands:");
        label.setStyle("-fx-font-weight: bold; -fx-text-fill: " + Theme.GRAY_800 + ";");

        Button summaryBtn = new Button("GET_SUMMARY");
        summaryBtn.setStyle("-fx-background-color: " + Theme.PRIMARY_COLOR + "; -fx-text-fill: white;");
        summaryBtn.setOnAction(e -> sendCommand("GET_SUMMARY"));

        Button todayRevBtn = new Button("GET_TODAY_REV");
        todayRevBtn.setStyle("-fx-background-color: " + Theme.PRIMARY_COLOR + "; -fx-text-fill: white;");
        todayRevBtn.setOnAction(e -> sendCommand("GET_TODAY_REV"));

        Button unpaidBtn = new Button("GET_UNPAID_COUNT");
        unpaidBtn.setStyle("-fx-background-color: " + Theme.PRIMARY_COLOR + "; -fx-text-fill: white;");
        unpaidBtn.setOnAction(e -> sendCommand("GET_UNPAID_COUNT"));

        Button totalRevBtn = new Button("GET_TOTAL_REV");
        totalRevBtn.setStyle("-fx-background-color: " + Theme.PRIMARY_COLOR + "; -fx-text-fill: white;");
        totalRevBtn.setOnAction(e -> sendCommand("GET_TOTAL_REV"));

        HBox btnBox = new HBox(10, summaryBtn, todayRevBtn, unpaidBtn, totalRevBtn);
        btnBox.setSpacing(10);

        return new VBox(8, label, btnBox);
    }

    private HBox buildCustomCommandBox() {
        Label label = new Label("Custom Command:");
        label.setStyle("-fx-font-weight: bold; -fx-text-fill: " + Theme.GRAY_800 + ";");

        TextField cmdField = new TextField();
        cmdField.setPromptText("Enter command (e.g., GET_SUMMARY)");
        cmdField.setPrefWidth(300);
        cmdField.setStyle("-fx-border-color: " + Theme.GRAY_300 + "; -fx-background-color: " + Theme.WHITE + ";");

        Button sendBtn = new Button("Send");
        sendBtn.setPrefWidth(80);
        sendBtn.setStyle("-fx-background-color: " + Theme.PRIMARY_COLOR + "; -fx-text-fill: white;");
        sendBtn.setOnAction(e -> {
            String cmd = cmdField.getText().trim();
            if (!cmd.isEmpty()) {
                sendCommand(cmd);
                cmdField.clear();
            }
        });

        return new HBox(10, label, cmdField, sendBtn);
    }

    private void connectToServer() {
        try {
            client = new CarWashClient("localhost", 5050);
            
            // Test connection with a simple command
            String response = client.sendCommand("GET_SUMMARY");
            
            if (response != null && !response.contains("ERROR")) {
                connected = true;
                statusLabel.setText("Status: Connected ✓");
                statusLabel.setStyle("-fx-font-size: 12px; -fx-text-fill: " + Theme.SUCCESS_COLOR + ";");
                log("✓ Connected to CarWashServer on localhost:5050");
                log("Response: " + response);
            } else {
                log("✗ Connection failed or server not responding");
                statusLabel.setText("Status: Failed");
                statusLabel.setStyle("-fx-font-size: 12px; -fx-text-fill: " + Theme.WARNING_COLOR + ";");
            }
        } catch (Exception e) {
            log("✗ Connection Error: " + e.getMessage());
            statusLabel.setText("Status: Error");
            statusLabel.setStyle("-fx-font-size: 12px; -fx-text-fill: " + Theme.DANGER_COLOR + ";");
        }
    }

    private void disconnectServer() {
        connected = false;
        client = null;
        statusLabel.setText("Status: Disconnected");
        statusLabel.setStyle("-fx-font-size: 12px; -fx-text-fill: " + Theme.DANGER_COLOR + ";");
        log("Disconnected from server");
    }

    private void sendCommand(String command) {
        if (!connected || client == null) {
            log("✗ Not connected to server. Click 'Connect' first.");
            return;
        }

        try {
            log("\n>>> Sending: " + command);
            String response = client.sendCommand(command);
            
            if (response == null) {
                log("✗ No response from server");
                return;
            }

            log("<<< Response: " + response);
            
            // Parse and display response
            if (response.contains("|")) {
                Map<String, Object> data = parseDelimitedResponse(response);
                log("\nParsed Data:");
                for (Map.Entry<String, Object> entry : data.entrySet()) {
                    log("  " + entry.getKey() + " = " + entry.getValue());
                }
            }

        } catch (Exception e) {
            log("✗ Error: " + e.getMessage());
        }
    }

    private Map<String, Object> parseDelimitedResponse(String response) {
        Map<String, Object> map = new java.util.LinkedHashMap<>();
        String[] parts = response.split("\\|");
        
        for (int i = 0; i < parts.length - 1; i += 2) {
            String key = parts[i].trim();
            String value = parts[i + 1].trim();
            
            try {
                if (value.contains(".")) {
                    map.put(key, Double.parseDouble(value));
                } else {
                    map.put(key, Long.parseLong(value));
                }
            } catch (NumberFormatException e) {
                map.put(key, value);
            }
        }
        
        return map;
    }

    private void log(String message) {
        logArea.appendText(message + "\n");
    }

    @Override
    public javafx.scene.Parent getRoot() {
        return root;
    }
}
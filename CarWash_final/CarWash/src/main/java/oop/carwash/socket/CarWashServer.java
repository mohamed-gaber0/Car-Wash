package oop.carwash.socket;


import oop.carwash.service.ReportService;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.Map;

/**
 * Socket server for the car wash system.
 *
 * SOCKET PROGRAMMING REQUIREMENT: Listens on port 5050.
 * Plain-text, delimited protocol (no JSON).
 * Sends real data from the database via ReportService.
 *
 * Commands:
 *   GET_SUMMARY    -> returns: DATE|2024-01-15|TODAY_REVENUE|3200.50|UNPAID_COUNT|5|...
 *   GET_TODAY_REV  -> returns: TODAY_REVENUE|3200.50
 *   SHUTDOWN       -> closes server
 *
 * MULTITHREADING REQUIREMENT: Server runs as a daemon thread.
 * Each client connection is handled in its own thread.
 *
 * Usage from MainApp:
 *   Thread serverThread = new Thread(() -> CarWashServer.start(), "carwash-socket-server");
 *   serverThread.setDaemon(true);
 *   serverThread.start();
 */
public class CarWashServer {

    private static final int PORT = 5050;
    private static volatile boolean running = false;
    private static ServerSocket serverSocket;

    private CarWashServer() {
        // Utility class
    }

    // ── Startup/Shutdown ───────────────────────────────────────────────────

    /**
     * Starts the server. Blocks until stop() is called.
     * Call from a background thread.
     */
    public static void start() {
        if (running) {
            System.out.println("[Socket] Server already running");
            return;
        }

        try {
            serverSocket = new ServerSocket(PORT);
            running = true;
            System.out.println("[Socket] Server started on port " + PORT);

            // Accept client connections in a loop
            while (running) {
                try {
                    Socket clientSocket = serverSocket.accept();
                    // Handle each client in a separate thread
                    Thread clientThread = new Thread(
                        () -> handleClient(clientSocket),
                        "carwash-socket-client-" + clientSocket.getPort()
                    );
                    clientThread.setDaemon(true);
                    clientThread.start();

                } catch (IOException e) {
                    if (running) {
                        System.err.println("[Socket] Error accepting client: " + e.getMessage());
                    }
                    // If not running, the exception is expected (socket closed)
                }
            }

        } catch (IOException e) {
            System.err.println("[Socket] Server startup failed: " + e.getMessage());
        } finally {
            running = false;
            if (serverSocket != null && !serverSocket.isClosed()) {
                try {
                    serverSocket.close();
                } catch (IOException e) {
                    System.err.println("[Socket] Error closing server socket: " + e.getMessage());
                }
            }
            System.out.println("[Socket] Server stopped");
        }
    }

    /**
     * Stops the server gracefully.
     */
    public static void stop() {
        running = false;
        if (serverSocket != null && !serverSocket.isClosed()) {
            try {
                serverSocket.close();
            } catch (IOException e) {
                System.err.println("[Socket] Error stopping server: " + e.getMessage());
            }
        }
    }

    // ── Client handling ────────────────────────────────────────────────────

    /**
     * Handles a single client connection.
     * Reads commands, executes them, sends responses.
     */
    private static void handleClient(Socket socket) {
        try (BufferedReader in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
             PrintWriter out = new PrintWriter(socket.getOutputStream(), true)) {

            System.out.println("[Socket] Client connected from " + socket.getInetAddress());

            String command;
            while ((command = in.readLine()) != null && running) {
                String response = processCommand(command.trim());
                out.println(response);
            }

        } catch (IOException e) {
            System.err.println("[Socket] Error handling client: " + e.getMessage());
        } finally {
            try {
                socket.close();
            } catch (IOException e) {
                System.err.println("[Socket] Error closing client socket: " + e.getMessage());
            }
        }
    }

    /**
     * Processes a command and returns a delimited response.
     * Format: KEY|VALUE|KEY|VALUE|...
     */
    private static String processCommand(String command) {
        if (command == null || command.isEmpty()) {
            return "ERROR|Invalid command";
        }

        switch (command.toUpperCase()) {

            case "GET_SUMMARY":
                return buildSummaryResponse();

            case "GET_TODAY_REV":
                double todayRev = ReportService.getTodayRevenue();
                return "TODAY_REVENUE|" + todayRev;

            case "GET_UNPAID_COUNT":
                int unpaidCount = ReportService.getUnpaidReceiptCount();
                return "UNPAID_COUNT|" + unpaidCount;

            case "GET_TOTAL_REV":
                double totalRev = ReportService.getTotalRevenue();
                return "TOTAL_REVENUE|" + totalRev;

            case "GET_TODAY_COUNT":
                int todayCount = ReportService.getTodayReceiptCount();
                return "TODAY_COUNT|" + todayCount;

            case "SHUTDOWN":
                stop();
                return "OK|Server shutting down";

            default:
                return "ERROR|Unknown command: " + command;
        }
    }

    /**
     * Builds a summary response with all key metrics.
     */
    private static String buildSummaryResponse() {
        Map<String, Object> summary = ReportService.getSummary();
        StringBuilder response = new StringBuilder();

        response.append("TIMESTAMP|").append(summary.get("timestamp"));
        response.append("|TODAY_REVENUE|").append(summary.get("todayRevenue"));
        response.append("|TODAY_COUNT|").append(summary.get("todayReceiptCount"));
        response.append("|UNPAID_COUNT|").append(summary.get("unpaidCount"));
        response.append("|TOTAL_REVENUE|").append(summary.get("totalRevenue"));

        return response.toString();
    }
}
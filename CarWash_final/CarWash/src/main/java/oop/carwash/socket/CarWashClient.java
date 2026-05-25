package oop.carwash.socket;


import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * Client for connecting to CarWashServer and sending commands.
 *
 * Plain-text, delimited protocol.
 * Example:
 *   CarWashClient client = new CarWashClient("localhost", 5050);
 *   Map<String, Object> summary = client.getSummary();
 *   System.out.println("Today's revenue: " + summary.get("todayRevenue"));
 */
public class CarWashClient {

    private String host;
    private int port;

    public CarWashClient(String host, int port) {
        this.host = host;
        this.port = port;
    }

    // ── High-level commands ────────────────────────────────────────────────

    /**
     * Fetches the summary of key metrics.
     * Returns a map with: timestamp, todayRevenue, todayCount, unpaidCount, totalRevenue
     */
    public Map<String, Object> getSummary() {
        String response = sendCommand("GET_SUMMARY");
        return parseDelimitedResponse(response);
    }

    /**
     * Fetches today's revenue.
     */
    public double getTodayRevenue() {
        String response = sendCommand("GET_TODAY_REV");
        Map<String, Object> map = parseDelimitedResponse(response);
        Object value = map.get("TODAY_REVENUE");
        return value != null ? Double.parseDouble(value.toString()) : 0.0;
    }

    /**
     * Fetches the count of unpaid receipts.
     */
    public int getUnpaidCount() {
        String response = sendCommand("GET_UNPAID_COUNT");
        Map<String, Object> map = parseDelimitedResponse(response);
        Object value = map.get("UNPAID_COUNT");
        return value != null ? Integer.parseInt(value.toString()) : 0;
    }

    /**
     * Fetches total all-time revenue.
     */
    public double getTotalRevenue() {
        String response = sendCommand("GET_TOTAL_REV");
        Map<String, Object> map = parseDelimitedResponse(response);
        Object value = map.get("TOTAL_REVENUE");
        return value != null ? Double.parseDouble(value.toString()) : 0.0;
    }

    /**
     * Fetches the count of receipts created today.
     */
    public int getTodayReceiptCount() {
        String response = sendCommand("GET_TODAY_COUNT");
        Map<String, Object> map = parseDelimitedResponse(response);
        Object value = map.get("TODAY_COUNT");
        return value != null ? Integer.parseInt(value.toString()) : 0;
    }

    // ── Low-level communication ────────────────────────────────────────────

    /**
     * Sends a raw command to the server and returns the response line.
     * Returns null if connection fails or is closed.
     */
    public String sendCommand(String command) {
        try (Socket socket = new Socket(host, port);
             PrintWriter out = new PrintWriter(socket.getOutputStream(), true);
             BufferedReader in = new BufferedReader(new InputStreamReader(socket.getInputStream()))) {

            out.println(command);
            String response = in.readLine();

            if (response == null) {
                System.err.println("[SocketClient] Server closed connection unexpectedly");
                return "ERROR|No response from server";
            }

            return response;

        } catch (IOException e) {
            System.err.println("[SocketClient] Connection error: " + e.getMessage());
            return "ERROR|Connection failed: " + e.getMessage();
        }
    }

    /**
     * Parses a delimited response (KEY|VALUE|KEY|VALUE|...) into a map.
     */
    private Map<String, Object> parseDelimitedResponse(String response) {
        Map<String, Object> map = new LinkedHashMap<>();

        if (response == null || response.isEmpty()) {
            map.put("ERROR", "No response");
            return map;
        }

        String[] parts = response.split("\\|");
        for (int i = 0; i < parts.length - 1; i += 2) {
            String key = parts[i].trim();
            String value = parts[i + 1].trim();
            // Try to parse as number, fall back to string
            try {
                if (value.contains(".")) {
                    map.put(key, Double.parseDouble(value));
                } else {
                    map.put(key, Integer.parseInt(value));
                }
            } catch (NumberFormatException e) {
                map.put(key, value);
            }
        }

        return map;
    }
}
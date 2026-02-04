import java.io.*;
import java.net.*;
import java.util.*;

public class MMORPGClient {
    private static final String SERVER_ADDRESS = "localhost";
    private static final int SERVER_PORT = 12345;
    
    // Track statistics
    private static int messagesReceived = 0;
    private static long startTime = System.currentTimeMillis();
    private static List<String> activePlayers = new ArrayList<>();
    private static List<String> largeDataStorage = new ArrayList<>();
    
    public static void main(String[] args) {
        try (
            Socket socket = new Socket(SERVER_ADDRESS, SERVER_PORT);
            BufferedReader in = new BufferedReader(
                new InputStreamReader(socket.getInputStream())
            );
            PrintWriter out = new PrintWriter(
                socket.getOutputStream(), true
            );
            BufferedReader consoleInput = new BufferedReader(
                new InputStreamReader(System.in)
            )
        ) {
            System.out.println("=== MMORPG Client Started ===");
            System.out.println("Connected to: " + SERVER_ADDRESS + ":" + SERVER_PORT);
            System.out.println("============================\n");
            
            // Read the name prompt
            System.out.println(in.readLine()); // "Enter your player name:"
            String playerName = consoleInput.readLine();
            out.println(playerName);
            
            System.out.println("\n✓ Joined as: " + playerName);
            System.out.println("Waiting for server messages...\n");
            
            // Start statistics thread
            new Thread(() -> {
                try {
                    while (true) {
                        Thread.sleep(5000); // Print stats every 5 seconds
                        printStats();
                    }
                } catch (InterruptedException e) {
                    // Exit gracefully
                }
            }).start();
            
            // Thread to listen for server messages
            Thread serverListener = new Thread(() -> {
                String serverMessage;
                try {
                    while ((serverMessage = in.readLine()) != null) {
                        messagesReceived++;
                        processMessage(serverMessage);
                    }
                } catch (IOException e) {
                    System.err.println("\n❌ Connection lost!");
                    e.printStackTrace();
                }
            });
            serverListener.start();
            
            // Main thread handles user input
            System.out.println("Type coordinates to move (e.g., '100,200') or 'quit' to exit\n");
            String userInput;
            while ((userInput = consoleInput.readLine()) != null) {
                if (userInput.equalsIgnoreCase("quit")) {
                    break;
                }
                
                if (userInput.equalsIgnoreCase("stats")) {
                    printStats();
                    continue;
                }
                
                if (!userInput.isEmpty()) {
                    out.println("MOVE:" + userInput);
                }
            }
            
        } catch (IOException e) {
            System.err.println("Connection error!");
            e.printStackTrace();
        }
    }
    
    private static void processMessage(String message) {
        // Check for attack indicators
        if (messagesReceived > 50 && (System.currentTimeMillis() - startTime) < 10000) {
            if (messagesReceived % 100 == 0) {
                System.out.println("\n⚠️⚠️⚠️ HIGH MESSAGE RATE - POSSIBLE ATTACK! ⚠️⚠️⚠️");
                System.out.println("Messages/second: " + calculateMessageRate());
            }
        }
        
        // Process different message types
        if (message.startsWith("PLAYER_JOIN:")) {
            String newPlayer = message.substring(12);
            activePlayers.add(newPlayer);
            
            // Only print every 50th join to avoid console spam
            if (activePlayers.size() % 50 == 0) {
                System.out.println("⚠️  Players joined: " + activePlayers.size() + " (Last: " + newPlayer + ")");
            }
            
        } else if (message.startsWith("UPDATE:")) {
            String[] parts = message.split(":", 3);
            if (parts.length >= 3) {
                String playerName = parts[1];
                String positionData = parts[2];
                
                // Only print every 100th update
                if (messagesReceived % 100 == 0) {
                    System.out.println("📍 " + playerName + " moved to " + positionData);
                }
                
                // Simulate processing overhead (consuming CPU)
                try {
                    String[] coords = positionData.split(",");
                    int x = Integer.parseInt(coords[0].trim());
                    int y = Integer.parseInt(coords[1].trim());
                    // Simulate some calculation
                    double distance = Math.sqrt(x * x + y * y);
                } catch (Exception e) {
                    // Invalid position data
                }
            }
            
        } else if (message.startsWith("BROADCAST:")) {
            String broadcastMsg = message.substring(10);
            System.out.println("📢 " + broadcastMsg);
            
        } else if (message.startsWith("DATA:")) {
            // MEMORY BOMB - Store large data (this consumes memory)
            String largeData = message.substring(5);
            largeDataStorage.add(largeData);
            
            if (largeDataStorage.size() % 10 == 0) {
                System.out.println("💣 Large data packets received: " + largeDataStorage.size());
                System.out.println("💾 Estimated memory consumed: " + 
                    (largeData.length() * largeDataStorage.size() / 1024 / 1024) + " MB");
            }
            
        } else {
            // Generic message
            System.out.println("💬 " + message);
        }
    }
    
    private static void printStats() {
        long elapsed = (System.currentTimeMillis() - startTime) / 1000;
        if (elapsed == 0) elapsed = 1;
        
        double msgRate = messagesReceived / (double) elapsed;
        Runtime runtime = Runtime.getRuntime();
        long usedMemory = (runtime.totalMemory() - runtime.freeMemory()) / (1024 * 1024);
        long totalMemory = runtime.totalMemory() / (1024 * 1024);
        
        System.out.println("\n╔════════════════════════════════════════╗");
        System.out.println("║       CLIENT STATISTICS                ║");
        System.out.println("╠════════════════════════════════════════╣");
        System.out.println("║ Time connected:      " + padLeft(elapsed + "s", 15) + " ║");
        System.out.println("║ Messages received:   " + padLeft(String.valueOf(messagesReceived), 15) + " ║");
        System.out.println("║ Messages/second:     " + padLeft(String.format("%.2f", msgRate), 15) + " ║");
        System.out.println("║ Players tracked:     " + padLeft(String.valueOf(activePlayers.size()), 15) + " ║");
        System.out.println("║ Memory used:         " + padLeft(usedMemory + " MB", 15) + " ║");
        System.out.println("║ Total memory:        " + padLeft(totalMemory + " MB", 15) + " ║");
        System.out.println("║ Large data stored:   " + padLeft(String.valueOf(largeDataStorage.size()), 15) + " ║");
        System.out.println("╚════════════════════════════════════════╝\n");
        
        // Warning if under heavy attack
        if (msgRate > 50) {
            System.out.println("🚨 CRITICAL: System under heavy load! 🚨");
        } else if (msgRate > 20) {
            System.out.println("⚠️  WARNING: High message rate detected");
        }
    }
    
    private static double calculateMessageRate() {
        long elapsed = (System.currentTimeMillis() - startTime) / 1000;
        if (elapsed == 0) return messagesReceived;
        return messagesReceived / (double) elapsed;
    }
    
    private static String padLeft(String s, int length) {
        while (s.length() < length) {
            s = " " + s;
        }
        return s;
    }
}

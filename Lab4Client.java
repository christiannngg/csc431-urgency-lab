import java.io.*;
import java.net.*;
import javax.swing.*;
import java.awt.*;

public class VulnerableGameClient extends JFrame {
    private JTextArea gameDisplay;
    private Socket socket;
    private PrintWriter out;
    private BufferedReader in;
    
    public VulnerableGameClient() {
        setTitle("MMORPG Client");
        setSize(600, 400);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        
        gameDisplay = new JTextArea();
        gameDisplay.setEditable(false);
        gameDisplay.setContentType("text/html"); // VULNERABILITY!
        
        add(new JScrollPane(gameDisplay), BorderLayout.CENTER);
        
        connectToServer();
    }
    
    private void connectToServer() {
        try {
            socket = new Socket("localhost", 12345);
            out = new PrintWriter(socket.getOutputStream(), true);
            in = new BufferedReader(
                new InputStreamReader(socket.getInputStream())
            );
            
            String playerName = JOptionPane.showInputDialog("Enter name:");
            out.println(playerName);
            
            // Listen for server messages
            new Thread(() -> {
                try {
                    String message;
                    while ((message = in.readLine()) != null) {
                        // VULNERABILITY: Directly rendering server data
                        displayMessage(message);
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }).start();
            
        } catch (IOException e) {
            JOptionPane.showMessageDialog(this, 
                "Cannot connect to server!");
        }
    }
    
    private void displayMessage(String message) {
        // CRITICAL VULNERABILITY: No sanitization!
        // Server-controlled content rendered directly
        SwingUtilities.invokeLater(() -> {
            gameDisplay.append(message + "\n");
            
            // If using HTML rendering (common in game clients)
            // XSS payload executes here
            if (message.contains("<script>")) {
                System.out.println("COMPROMISED!");
                // In real scenario, malicious code would execute
            }
        });
    }
    
    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            new VulnerableGameClient().setVisible(true);
        });
    }
}
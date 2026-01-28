import java.io.*;
import java.net.*;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

public class MMORPGServer {

    private static final int PORT = 12345;
    private static Map<String, PlayerHandler> players =
            new ConcurrentHashMap<>();

    public static void main(String[] args) {

        try (ServerSocket serverSocket = new ServerSocket(PORT)) {

            System.out.println("MMORPG Server is running on port " + PORT);

            while (true) {
                Socket clientSocket = serverSocket.accept();
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

            if (player != sender) {
                player.sendMessage(message);
            }
        }
    }

    public static synchronized void updatePlayerPosition(
            String playerName,
            String positionData
    ) {

        String message =
                "UPDATE:" + playerName + ":" + positionData;

        broadcast(message, players.get(playerName));
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
}


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
                    new InputStreamReader(
                            socket.getInputStream()
                    )
            );

            out = new PrintWriter(
                    socket.getOutputStream(),
                    true
            );

            out.println("Enter your player name: ");

            playerName = in.readLine();

            MMORPGServer.addPlayer(playerName, this);

            System.out.println(
                    playerName + " has joined the game."
            );

            String message;

            while ((message = in.readLine()) != null) {

                if (message.startsWith("MOVE:")) {

                    // Extract position data
                    String positionData = message.substring(5);

                    System.out.println(
                            playerName + " moved to: "
                                    + positionData
                    );

                    MMORPGServer.updatePlayerPosition(
                            playerName,
                            positionData
                    );
                }
            }

        } catch (IOException e) {

            e.printStackTrace();

        } finally {

            try {

                MMORPGServer.removePlayer(playerName);

                socket.close();

                System.out.println(
                        playerName + " has left the game."
                );

            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    public void sendMessage(String message) {
        out.println(message);
    }
}
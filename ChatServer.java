import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class ChatServer {
    private static final int PORT = 5555;
    private static final ExecutorService executorService = Executors.newFixedThreadPool(10);
    private static final Map<String, Socket> userSocketMap = new ConcurrentHashMap<>();

    public static void main(String[] args) {
        try (ServerSocket serverSocket = new ServerSocket(PORT)) {
            System.out.println("Server is running on port " + PORT);

            while (true) {
                Socket clientSocket = serverSocket.accept();
                executorService.submit(() -> handleClient(clientSocket));
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private static void handleClient(Socket clientSocket) {
        try (BufferedReader reader = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));
             PrintWriter writer = new PrintWriter(clientSocket.getOutputStream(), true)) {

            // Read the username from the client
            String username = reader.readLine();
            addUser(username, clientSocket);
            System.out.println(username + " connected.");

            while (true) {
                // Read message from the client
                String message = reader.readLine();
                if (message == null || message.equalsIgnoreCase("exit")) {
                    break;
                }

                // Check for private message format
                if (message.startsWith("@")) {
                    String[] parts = message.split(" ", 2);
                    if (parts.length == 2) {
                        String recipient = parts[0].substring(1);
                        String privateMessage = parts[1];
                        sendPrivateMessage(username, recipient, privateMessage);
                    } else {
                        System.out.println("Invalid private message format. Correct format: '@recipient message'");
                    }
                } else {

                    broadcastMessage(username, message);
                }
            }


            System.out.println(username + " disconnected.");
            removeUser(username);

        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static void addUser(String username, Socket clientSocket) {
        userSocketMap.put(username, clientSocket);
    }

    public static void removeUser(String username) {
        userSocketMap.remove(username);
    }

    public static void broadcastMessage(String sender, String message) {
        userSocketMap.forEach((username, socket) -> {
            try {
                PrintWriter writer = new PrintWriter(socket.getOutputStream(), true);
                writer.println(sender + ": " + message);
            } catch (IOException e) {
                e.printStackTrace();
            }
        });
    }

    public static void sendPrivateMessage(String sender, String recipient, String message) {
        Socket recipientSocket = userSocketMap.get(recipient);
        if (recipientSocket != null) {
            try {
                PrintWriter writer = new PrintWriter(recipientSocket.getOutputStream(), true);
                writer.println(sender + " (private): " + message);
            } catch (IOException e) {
                e.printStackTrace();
            }
        } else {
            System.out.println("Recipient not found: " + recipient);
        }
    }
}

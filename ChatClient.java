import java.io.*;
import java.net.Socket;

public class ChatClient {
    private static final String SERVER_ADDRESS = "localhost";
    private static final int SERVER_PORT = 5555;

    public static void main(String[] args) {
        try {
            Socket socket = new Socket(SERVER_ADDRESS, SERVER_PORT);


            BufferedReader in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
            PrintWriter out = new PrintWriter(socket.getOutputStream(), true);


            System.out.print("Enter your username: ");
            String username = new BufferedReader(new InputStreamReader(System.in)).readLine();


            out.println(username);

            new Thread(() -> {
                try {
                    while (true) {
                        String message = in.readLine();
                        if (message == null) {
                            break;
                        }
                        System.out.println(message);
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }).start();


            System.out.println("Type your messages (type 'exit' to quit):");
            while (true) {
                String message = new BufferedReader(new InputStreamReader(System.in)).readLine();
                out.println(message);

                if (message.equalsIgnoreCase("exit")) {
                    break;
                }
            }


            socket.close();

        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}

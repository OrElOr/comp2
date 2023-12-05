package one;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.List;

public class Server {
    private static List<ClientHandler> clients;
    private static ServerSocket serverSocket;

    public Server() throws IOException {
        clients = new ArrayList<>();
        serverSocket = new ServerSocket(5000);

        while (true) {
            System.out.println("접속 대기중");
            Socket clientSocket = serverSocket.accept();
            System.out.println("접속 되었음");
            ClientHandler clientHandler = new ClientHandler(clientSocket);
            clients.add(clientHandler);
            new Thread(clientHandler).start();
        }
    }

    public static void main(String[] args) throws IOException {
        new Server();
    }

    private static class ClientHandler implements Runnable {
        private Socket socket;
        private ObjectInputStream inputStream;
        private ObjectOutputStream outputStream;

        public ClientHandler(Socket socket) {
            this.socket = socket;
            try {
                this.outputStream = new ObjectOutputStream(socket.getOutputStream());
                this.inputStream = new ObjectInputStream(socket.getInputStream());
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        @Override
        public void run() {
            while (true) {

            }
        }
    }
}

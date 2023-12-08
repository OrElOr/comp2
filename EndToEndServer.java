package endtoend;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.List;

public class EndToEndServer {
    private static List<ClientHandler> clients = new ArrayList<>();
    private ServerSocket serverSocket;
    public EndToEndServer() {
        try {
            serverSocket = new ServerSocket(12345);
            System.out.println("서버 시작. 클라이언트 연결 대기 중...");

            while (true) {
                Socket clientSocket = serverSocket.accept();
                System.out.println("클라이언트 연결됨: " + clientSocket);

                // 클라이언트 처리를 위한 스레드 생성
                ClientHandler clientHandler = new ClientHandler(clientSocket);
                clients.add(clientHandler);
                new Thread(clientHandler).start();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
    public static void main(String[] args) {
        new EndToEndServer();
    }
}

package endtoend;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;

public class EndToEndServer {
    public static void main(String[] args) {
        try {
            ServerSocket serverSocket = new ServerSocket(12345);
            System.out.println("서버 시작. 클라이언트 연결 대기 중...");

            while (true) {
                Socket clientSocket = serverSocket.accept();
                System.out.println("클라이언트 연결됨: " + clientSocket);

                // 클라이언트 처리를 위한 스레드 생성
                ClientHandler clientHandler = new ClientHandler(clientSocket);
                new Thread(clientHandler).start();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}

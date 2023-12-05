package endtoend;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;

public class ClientHandler implements Runnable {
    private Socket clientSocket;
    private BufferedReader reader;
    private PrintWriter writer;

    public ClientHandler(Socket clientSocket) {
        this.clientSocket = clientSocket;
        try {
            this.reader = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));
            this.writer = new PrintWriter(clientSocket.getOutputStream(), true);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void run() {
        try {
            while (true) {
                String message = reader.readLine();
                if (message == null) {
                    break;
                }
                System.out.println("클라이언트로부터 받은 메시지: " + message);

                // 여기에서 끝말잇기 게임 로직을 처리하고, 결과를 클라이언트에 전송할 수 있습니다.
                // 예: writer.println("서버 응답: " + 결과);

                // 이 예제에서는 받은 메시지를 그대로 다시 클라이언트에게 보내는 코드를 사용합니다.
                writer.println("서버 응답: " + message);
            }
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            try {
                clientSocket.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }
}

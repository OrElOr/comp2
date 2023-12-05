package endtoend;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;

public class EndToEndClient {
    public static void main(String[] args) {
        try {
            Socket socket = new Socket("localhost", 12345);
            System.out.println("서버에 연결됨");

            BufferedReader reader = new BufferedReader(new InputStreamReader(socket.getInputStream()));
            PrintWriter writer = new PrintWriter(socket.getOutputStream(), true);

            // GUI를 생성하고 시작
            GameGUI gameGUI = new GameGUI(reader, writer);
            gameGUI.init();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}

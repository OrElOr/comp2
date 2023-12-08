package endtoend;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;

public class EndToEndClient {
    private Socket socket;
    private BufferedReader reader;
    private PrintWriter writer;
    private String username;

    public EndToEndClient() {
        try {
            socket = new Socket("localhost", 12345);
            System.out.println("서버에 연결됨");

            reader = new BufferedReader(new InputStreamReader(socket.getInputStream()));
            writer = new PrintWriter(socket.getOutputStream(), true);

            username = JOptionPane.showInputDialog("이름을 입력하세요!");
            if(username == null) {
                System.exit(0);
            }
            GameGUI gameGUI = new GameGUI(reader, writer,username);
            gameGUI.init();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static void main(String[] args) {
        new EndToEndClient();
    }
}

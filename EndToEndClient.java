package endtoend;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.*;
import java.net.Socket;

public class EndToEndClient {
    private Socket socket;
    private BufferedReader reader;
    private BufferedWriter writer;
    private String username;

    public EndToEndClient() {
        try {
            socket = new Socket("localhost", 12345);
            System.out.println("서버에 연결됨");

            reader = new BufferedReader(new InputStreamReader(socket.getInputStream()));
            writer = new BufferedWriter(new OutputStreamWriter(socket.getOutputStream()));

            //유저 이름 저장
            username = JOptionPane.showInputDialog("이름을 입력하세요!");
            if(username == null) {
                System.exit(0);
            }



            //게임 GUI생성
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

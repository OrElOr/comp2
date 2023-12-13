package WordChain;

import javax.swing.*;
import java.io.*;
import java.net.Socket;

public class WordChainClient {
    private Socket socket;
    private BufferedReader reader;
    private BufferedWriter writer;
    private ObjectOutputStream objectWriter;
    private String username;
    public WordChainClient() {
        try {
            socket = new Socket("localhost", 12345);

            reader = new BufferedReader(new InputStreamReader(socket.getInputStream()));
            writer = new BufferedWriter(new OutputStreamWriter(socket.getOutputStream()));
            objectWriter = new ObjectOutputStream(socket.getOutputStream());

            setUserName();

            //유저 정보 클래스 생성
            ClientInfo myInfo = new ClientInfo(username);
            objectWriter.writeObject(myInfo);
            objectWriter.flush();

            //GUI생성
            GameGUI gameGUI = new GameGUI(socket, reader, writer, username);
            gameGUI.setGUI();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    // 유저 이름 설정 메소드
    private void setUserName() {
        Boolean setUsername = false;
        while (!setUsername) {
            username = JOptionPane.showInputDialog("이름을 입력하세요!(1~9자, 특수문자 사용 불가)");

            if(username == null) {
                System.exit(0);
            } else if (username.isEmpty() || username.length() >= 10) {
                JOptionPane.showMessageDialog(null,"1~9자 사이의 이름을 입력하세요!");
                continue;
            } else if (username.matches(".*[!@#$%^&*(),./?;':\"\\[\\]{}|<>\\-_=+\\\\].*")) {
                JOptionPane.showMessageDialog(null,"특수문자 사용은 불가합니다!");
                continue;
            }
            setUsername = true;
        }
    }

    public static void main(String[] args) {
        new WordChainClient();
    }
}

package WordChain;

import javax.swing.*;
import java.io.*;
import java.net.Socket;

//*******************************************************************
// # 03-01
//*******************************************************************
// Name : WordChainClient
// Type : Class
// Description : 서버와의 소켓통신에 필요한 socket 정보와 BufferedReader,BufferedWriter
//               ObjectOutputStream,String 을 선언한다.
//*******************************************************************
public class WordChainClient {
    private Socket socket;
    private BufferedReader reader;
    private BufferedWriter writer;
    private ObjectOutputStream objectWriter;
    private String username;


    //*******************************************************************
    // # 03-01-01
    //*******************************************************************
    // Name : WordChainClient
    // Type : 생성자
    // Description :  reader, writer, username, objectWriter를 초기화 하고 서버와의 소켓통신을 담당한다.
    //                입력받은 username으로 ClientInfo를 생성하고 GameGUI를 초기화한다.
    //*******************************************************************
    public WordChainClient() {
        try {
            socket = new Socket("localhost", 5000);

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
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    //*******************************************************************
    // # 03-01-02
    //*******************************************************************
    // Name : setUserName
    // Type : Method
    // Description :  게임을 시작하기전 username을 입력하게하는 GUI를 화면에 띄운다.
    //                조건에 맞는 username을 입력했을때 while문에서 벗어난다.
    //*******************************************************************
    private void setUserName() {
        Boolean setUsername = false;
        while (!setUsername) {
            username = JOptionPane.showInputDialog("이름을 입력하세요!(1~9자 / 특수문자, 공백 사용 불가)");

            if(username == null) {
                System.exit(0);
            } else if (username.isEmpty() || username.length() >= 10) {
                JOptionPane.showMessageDialog(null,"1~9자 사이의 이름을 입력하세요!");
                continue;
            } else if (username.matches(".*[!@#$%^&*(),./?;':\"\\[\\]{}|<>\\-_=+\\\\].*")) {
                JOptionPane.showMessageDialog(null,"특수문자 사용은 불가합니다!");
                continue;
            } else if (username.contains(" ")){
                JOptionPane.showMessageDialog(null,"공백 사용은 불가합니다!");
                continue;
            }
            setUsername = true;
        }
    }

    public static void main(String[] args) {
        new WordChainClient();
    }
}

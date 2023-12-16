package WordChain;

import javax.swing.*;
import java.awt.*;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.net.Socket;

//*******************************************************************
// # 02-01
//*******************************************************************
// Name : GameGUI
// Type : Class
// Description :  client 화면의 GUI 프레임이다.
//*******************************************************************
public class GameGUI extends JFrame{
    private Socket socket;
    private BufferedReader reader;
    private BufferedWriter writer;

    private JTextField inputWord, inputChat;
    private JTextArea preWord, chatArea;
    private JPanel gameP, chatP;
    private JScrollPane preWordscroll, chatScroll;
    private String username;
    private JLabel targetWord;

    //*******************************************************************
    // # 02-01-01
    //*******************************************************************
    // Name : GameGUI
    // Type : 생성자
    // Description :  GameGUI Class의 생성자로서, WordChainClient에서  받은
    //                socket, reader, writer, username으로 GameGUI를 초기화 한다.
    //*******************************************************************
    public GameGUI(Socket socket, BufferedReader reader, BufferedWriter writer, String username){
        this.socket = socket;
        this.reader = reader;
        this.writer = writer;
        this.username = username;
        initGUI();
    }


    //*******************************************************************
    // # 02-01-02
    //*******************************************************************
    // Name : InitGui
    // Type : Method
    // Description :  GameGUI Class의 GUI 컴포넌트를 할당하고 초기화한다.
    //                gameP와 chatP는 각각 게임과 채팅에 해당하는 패널들이다.
    //*******************************************************************
    public void initGUI() {
        setTitle(username +"님의 끝말잇기 게임");
        setSize(400, 600);
        setLayout(new GridLayout(2,1));

        inputWord = new JTextField();
        inputWord.addActionListener(e->{
            String word = inputWord.getText();
            if (!word.isEmpty()) {
                try {
                    writer.write("Game:" + word);
                    writer.newLine();
                    writer.flush();
                } catch (IOException ioe) {
                    ioe.printStackTrace();
                }
            }
            inputWord.setText("");
        });

        inputChat = new JTextField();
        inputChat.addActionListener(e->{
            String chat = inputChat.getText();
            if (!chat.isEmpty()) {
                try {
                    writer.write("Chat:" + username + " : " + chat);
                    writer.newLine();
                    writer.flush();
                } catch (IOException ioe) {
                    ioe.printStackTrace();
                }
            }
            inputChat.setText("");
        });

        preWord = new JTextArea(2,1);
        preWord.setFocusable(false);
        preWord.setSize(400,100);
        preWordscroll = new JScrollPane(preWord);

        targetWord = new JLabel();
        targetWord.setFont(new Font("HYHeadLine",Font.BOLD,20));

        JPanel inputWordP = new JPanel();
        inputWordP.setLayout(new BorderLayout());
        inputWordP.add(inputWord);

        gameP = new JPanel();
        gameP.setSize(400,300);
        gameP.setLayout(new BorderLayout());
        gameP.setBackground(Color.WHITE);
        gameP.add(preWordscroll,BorderLayout.NORTH);
        gameP.add(targetWord,BorderLayout.CENTER);
        gameP.add(inputWordP, BorderLayout.SOUTH);


        chatArea = new JTextArea();
        chatArea.setEditable(false);
        chatArea.setLineWrap(true);
        chatArea.setWrapStyleWord(true);
        chatScroll = new JScrollPane(chatArea);

        JPanel inputChatP = new JPanel();
        inputChatP.setLayout(new BorderLayout());
        inputChatP.add(inputChat);

        chatP = new JPanel();
        chatP.setSize(400,300);
        chatP.setLayout(new BorderLayout());
        chatP.add(chatScroll, BorderLayout.CENTER);
        chatP.add(inputChatP, BorderLayout.SOUTH);


        add(gameP);
        add(chatP);

        setResizable(false);
        setVisible(true);
        //창 종료시 closeClient() 호출
        addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosing(WindowEvent e) {
                closeClient();
            }
        });

        try{ //입장 메세지 출력
            writer.write("Chat:"+ username +" 님이 입장했습니다.");
            writer.newLine();
            writer.flush();}
        catch (IOException e){
            e.printStackTrace();
        }

        new Thread(new Runnable() {
            @Override
            public void run() {
                receiveMessages();
            }
        }).start();

        initTargetword(); // TargetWord 초기화
    }


    //*******************************************************************
    // # 02-01-03
    //*******************************************************************
    // Name : receiveMessages
    // Type : Method
    // Description :  Server로 부터 message를 받아 시작 단어로부터 종류를 구분하고
    //                구분에 따라 작동한다.
    //*******************************************************************
    public void receiveMessages() {
        try {
            String message;
            while ((message = reader.readLine()) != null) {
                if (message.startsWith("Game:")) {
                    String tgw = message.replace("Game:", "");//받은 message에서 Game:를 제거한 String
                    targetWord.setText(tgw);
                    String usedWord = preWord.getText();
                    preWord.setText("");
                    preWord.append(tgw +", "+ usedWord); //위에서 받은 String을 상단 정답 textarea에 추가함
                } else if (message.startsWith("Chat:")) {
                    String chat = message.replace("Chat:","");
                    chatArea.append(chat + "\n"); //받은 message를 chatArea에 추가함
                    scrollToBottom();
                } else if (message.equals("WaitClient:")) {
                    waitInputWord();
                } else if (message.equals("GameEnd:Win:")) {
                    chatArea.append("당신이 이겼습니다!");
                    showResultDialog(true);
                    closeClient();
                } else if(message.equals("GameEnd:Lose:")) {
                    showResultDialog(false);
                    closeClient();
                } else if(message.startsWith("SetInitTargetWord:")){
                    String tgw = message.replace("SetInitTargetWord:","");
                    targetWord.setText(tgw);
                }
            }
        } catch(IOException e){
            if (!socket.isClosed()) {
                    closeClient();
            }
        }

    }

    // *******************************************************************
    // # 02-01-04
    //*******************************************************************
    // Name : initTargetword
    // Type : Method
    // Description :  Server에게 현재 TargetWord를 요청한다.
    //*******************************************************************
    public void initTargetword() {
        try {
            writer.write("SetInitTargetWord:");
            writer.newLine();
            writer.flush();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }


    // *******************************************************************
    // # 02-01-05
    //*******************************************************************
    // Name : scrollToBottom
    // Type : Method
    // Description : 새로운 채팅이 출력되었을때 chatArea의 스크롤을 맨 아래로 내려준다.
    // *******************************************************************
    private void scrollToBottom() {
        JScrollBar verticalScrollBar = chatScroll.getVerticalScrollBar();
        verticalScrollBar.setValue(verticalScrollBar.getMaximum());
    }

    // *******************************************************************
    // # 02-01-06
    //*******************************************************************
    // Name : waitInputWord
    // Type : Method
    // Description :  user가 끝말잇기 단어를 성공 했을 경우 3초간 끝말잇기 게임에
    //                참가하지 못하도록 Thread를 sleep 시킨다.
    // *******************************************************************
    public void waitInputWord() {
        new Thread(new Runnable() {
            @Override
            public void run() {
                inputWord.setEditable(false);
                inputWord.setFocusable(false);
                try {
                    Thread.sleep(3000);
                } catch (InterruptedException e) {
                    throw new RuntimeException(e);
                }
                inputWord.setEditable(true);
                inputWord.setFocusable(true);
                inputWord.requestFocus();
            }
        }).start();
    }


    // *******************************************************************
    // # 02-01-07
    //*******************************************************************
    // Name : showResultDialog
    // Type : Method
    // Description :  승패 결과에 따라서 다른 결과를 출력시켜준다.
    // *******************************************************************
    private void showResultDialog(boolean isWinner) {
        String message = isWinner ? "축하합니다! 승리하셨습니다." : "아쉽게도 패배하셨습니다.";
        JOptionPane.showMessageDialog(this, message, "게임 종료", JOptionPane.INFORMATION_MESSAGE);
    }

    // *******************************************************************
    // # 02-01-08
    //*******************************************************************
    // Name : closeClient
    // Type : Method
    // Description :  다른 user에게 접속 종료 메세지를 보내주고 자신의 reader, writer, socket을 닫는다.
    // *******************************************************************
    private void closeClient() {
        if (!socket.isClosed()) {
            try {
                writer.write("Chat:" + username + "님이 접속을 종료하였습니다.");
                writer.newLine();
                writer.flush();

                writer.write("Disconnect:");
                writer.newLine();
                writer.flush();

                reader.close();
                writer.close();
                socket.close();
                System.exit(0);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }
}

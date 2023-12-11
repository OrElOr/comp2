package endtoend;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.PrintWriter;

public class GameGUI extends JFrame{
    private BufferedReader reader;
    private BufferedWriter writer;

    private JTextField inputField,gameinputField;
    private JTextArea chatArea,preanswer;
    private JPanel chatP,gameP;
    private JScrollPane scroll,scroll2;
    private String username;
    private JLabel targetword;
    private String tgw;

    public GameGUI(BufferedReader reader, BufferedWriter writer, String username){
        this.reader = reader;
        this.writer = writer;
        this.username = username;
    }

    public void init() {
        setTitle(username +"님의 끝말잇기 게임");
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        setSize(400, 600);
        setResizable(false);
        setLayout(new GridLayout(2,1));

        inputField = new JTextField();
        gameinputField = new JTextField();

        inputField.addActionListener(e->{
            String chatword = inputField.getText();
            try {
                writer.write(username+":"+chatword);
                writer.newLine();
                writer.flush();
            } catch (IOException ioe) {
                ioe.printStackTrace();
            }
            inputField.setText(""); // 엔터치고 textfield지우기
        });

        gameinputField.addActionListener(e->{
            String gameword = gameinputField.getText();
            //예외처리
            try {
                writer.write("Game:" + gameword);
                writer.newLine();
                writer.flush();
            } catch (IOException ioe){
                ioe.printStackTrace();
            }
            gameinputField.setText(""); // 엔터치고 textfield지우기
        });

        chatArea = new JTextArea();
        chatArea.setEditable(false);
        scroll = new JScrollPane(chatArea);

        preanswer = new JTextArea(2,1);
        preanswer.setFocusable(false);
        preanswer.setSize(400,100);
        scroll2 = new JScrollPane(preanswer);



        gameP = new JPanel();
        gameP.setSize(400,300);
        gameP.setLayout(new BorderLayout());
        gameP.setBackground(Color.WHITE);

        //위치 가운데로
        targetword = new JLabel(initTargetword());
        gameP.add(targetword,BorderLayout.CENTER);
        gameP.add(scroll2,BorderLayout.NORTH);

        //timecount = new JLabel("timer");
        //timecount.setPreferredSize(new Dimension(50,50));
        //gameP.add(timecount,null);
        //timecount.setLocation(200,200);
        add(gameP);

        JPanel gameChatP = new JPanel();
        gameChatP.setLayout(new BorderLayout());
        gameChatP.add(gameinputField, BorderLayout.CENTER);

        gameP.add(gameChatP, BorderLayout.SOUTH);


        //chatP 영역 아래 채팅 입력 부분.
        JPanel panel = new JPanel();
        panel.setLayout(new BorderLayout());
        panel.add(inputField, BorderLayout.CENTER);

        chatP = new JPanel();
        chatP.setSize(400,300);

        chatP.setLayout(new BorderLayout());
        chatP.add(scroll, BorderLayout.CENTER); //스크롤 기능이 있는 textfiled 추가
        chatP.add(panel, BorderLayout.SOUTH);
        add(chatP);

        /*
        frame.getContentPane().setLayout(new BorderLayout());
        frame.getContentPane().add(chatArea, BorderLayout.CENTER);
        frame.getContentPane().add(panel, BorderLayout.SOUTH);
         */

        setVisible(true);


        try{ //입장 메세지 출력
                writer.write(username+" 님이 입장했습니다.");
                writer.newLine();
                writer.flush();}
            catch (IOException e){
                e.printStackTrace();
        }






        // 서버로부터 메시지 수신을 위한 스레드 시작
        new Thread(new Runnable() {
            @Override
            public void run() {
                receiveMessages();
            }
        }).start();
    }

    public void receiveMessages() {
        try {
            while (true) {
                System.out.println("recevieMessages 메서드 시작");
                String message = reader.readLine();
                if(message.startsWith("Game:")) {
                    //targetword로 setText
                    tgw = message.replace("Game:","");//받은 message에서 Game:를 제거한 String
                    targetword.setText(tgw);
                    preanswer.append(tgw+", "); //위에서 받은 String을 상단 정답 textarea에 추가함

                } else {
                    chatArea.append(message+"\n"); //받은 message를 chatArea에 추가함

                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
    
    public String initTargetword() {
        try {
            writer.write("GetTargetWord");
            writer.newLine();
            writer.flush();
            // 스택에서 현재 타겟 단어를 읽음
            String targetWord = reader.readLine();
            return targetWord;
        } catch (IOException e) {
            e.printStackTrace();
        }
        return null;
    }
}


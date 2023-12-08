package endtoend;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.PrintWriter;

public class GameGUI {
    private BufferedReader reader;
    private PrintWriter writer;

    private JFrame frame;
    private JTextField inputField,gameinputField;
    private JTextArea chatArea,preanswer;
    private JPanel chatP,gameP;
    private JScrollPane scroll,scroll2;
    private JLabel timecount;

    public GameGUI(BufferedReader reader, PrintWriter writer) {
        this.reader = reader;
        this.writer = writer;
    }

    public void init() {
        frame = new JFrame("끝말잇기 게임");
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setSize(400, 600);
        frame.setResizable(false);
        frame.setLayout(new GridLayout(2,1));

        inputField = new JTextField();
        gameinputField = new JTextField();

        inputField.addActionListener(e->{//enter 쳐도 입력가능하게
            String word = inputField.getText();
            sendMessage(word);
                });

        gameinputField.addActionListener(e->{//enter 쳐도 입력가능하게
            String gameword = gameinputField.getText();
            System.out.println(gameword); //여기서 텍스트가 조건을 통과하면 입력되도록
            preanswer.append(", "+gameword);
            gameinputField.setText("");
        });

        chatArea = new JTextArea();
        chatArea.setEditable(false);
        scroll = new JScrollPane(chatArea);

        preanswer = new JTextArea(2,1);
        preanswer.setFocusable(false);
        preanswer.setSize(400,100);
        scroll2 = new JScrollPane(preanswer);





        JButton submitButton = new JButton("제출");
        submitButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                String word = inputField.getText();
                sendMessage(word);
            }
        });

        JButton gamesubmitButton = new JButton("제출");
        submitButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                String gameword = gameinputField.getText();
                System.out.println(gameword);//여기서 텍스트가 조건을 통과하면 입력되도록
                preanswer.append(", "+gameword);
                gameinputField.setText("");
            }
        });

        gameP = new JPanel();
        gameP.setSize(400,300);
        gameP.setLayout(new BorderLayout());
        gameP.setBackground(Color.WHITE);

        JButton targetword = new JButton("targetword");
        targetword.setFocusable(false);

        targetword.setSize(150,100);
        gameP.add(targetword,BorderLayout.CENTER);
        gameP.add(scroll2,BorderLayout.NORTH);

        //timecount = new JLabel("timer");
        //timecount.setPreferredSize(new Dimension(50,50));
        //gameP.add(timecount,null);
        // timecount.setLocation(200,200);
        frame.add(gameP);

        JPanel gameChatP = new JPanel();
        gameChatP.setLayout(new BorderLayout());
        gameChatP.add(gameinputField, BorderLayout.CENTER);
        gameChatP.add(gamesubmitButton, BorderLayout.EAST);


        gameP.add(gameChatP, BorderLayout.SOUTH);





        //chatP 영역 아래 채팅 입력 부분.
        JPanel panel = new JPanel();
        panel.setLayout(new BorderLayout());
        panel.add(inputField, BorderLayout.CENTER);
        panel.add(submitButton, BorderLayout.EAST);

        chatP = new JPanel();
        chatP.setSize(400,300);

        chatP.setLayout(new BorderLayout());
        chatP.add(scroll, BorderLayout.CENTER); //스크롤 기능이 있는 textfiled 추가
        chatP.add(panel, BorderLayout.SOUTH);
        frame.add(chatP);

        /*
        frame.getContentPane().setLayout(new BorderLayout());
        frame.getContentPane().add(chatArea, BorderLayout.CENTER);
        frame.getContentPane().add(panel, BorderLayout.SOUTH);
         */

        frame.setVisible(true);

        // 서버로부터 메시지 수신을 위한 스레드 시작
        new Thread(new Runnable() {
            @Override
            public void run() {
                receiveMessages();
            }
        }).start();
    }

    private void sendMessage(String message) {
        writer.println(message);
        inputField.setText("");
    }

    private void receiveMessages() {
        try {
            while (true) {
                String message = reader.readLine();
                if (message != null) {
                    chatArea.append(message + "\n");
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}

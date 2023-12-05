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
    private JTextField inputField;
    private JTextArea chatArea;

    public GameGUI(BufferedReader reader, PrintWriter writer) {
        this.reader = reader;
        this.writer = writer;
    }

    public void init() {
        frame = new JFrame("끝말잇기 게임");
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setSize(400, 300);

        inputField = new JTextField();
        chatArea = new JTextArea();
        chatArea.setEditable(false);

        JButton submitButton = new JButton("제출");
        submitButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                String word = inputField.getText();
                sendMessage(word);
            }
        });

        JPanel panel = new JPanel();
        panel.setLayout(new BorderLayout());
        panel.add(inputField, BorderLayout.CENTER);
        panel.add(submitButton, BorderLayout.EAST);

        frame.getContentPane().setLayout(new BorderLayout());
        frame.getContentPane().add(chatArea, BorderLayout.CENTER);
        frame.getContentPane().add(panel, BorderLayout.SOUTH);

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

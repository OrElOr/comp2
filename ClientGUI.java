package one;

import javax.swing.*;
import java.awt.*;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;
import java.util.Map;

public class ClientGUI extends JFrame {
    private Socket socket;
    private ObjectOutputStream outputStream;
    private ObjectInputStream inputStream;
    private JPanel panel;
    private JButton player;

    public ClientGUI(Socket socket, ObjectOutputStream outputStream, ObjectInputStream inputStream) {
        this.socket = socket;
        this.outputStream = outputStream;
        this.inputStream = inputStream;

        setSize(500, 500);
        setResizable(false);
        panel = new JPanel();
        panel.setLayout(null);

        player = new JButton();
        player.setBackground(Color.BLACK);
        player.setBorderPainted(false);
        player.setBounds((int) (Math.random() * 480) + 10, (int) (Math.random() * 480) + 10, 10, 10);
        player.setFocusable(true);

        player.addKeyListener(new KeyAdapter() {
            @Override
            public void keyPressed(KeyEvent e) {
                // 키를 눌렀을 때의 동작
                int keyCode = e.getKeyCode();
                movePlayer(keyCode);
            }
        });

        panel.add(player);
        add(panel);
        setVisible(true);
        setDefaultCloseOperation(EXIT_ON_CLOSE);

    }
    private void movePlayer(int keyCode) {
        int x = player.getX();
        int y = player.getY();
        int speed = 5;  // 이동 속도 조절

        switch (keyCode) {
            case KeyEvent.VK_UP:
                y -= speed;
                break;
            case KeyEvent.VK_DOWN:
                y += speed;
                break;
            case KeyEvent.VK_LEFT:
                x -= speed;
                break;
            case KeyEvent.VK_RIGHT:
                x += speed;
                break;
        }

        player.setLocation(x, y);
    }
}

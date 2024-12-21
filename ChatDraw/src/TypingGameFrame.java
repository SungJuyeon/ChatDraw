import java.awt.Color;
import java.awt.Font;
import java.awt.Point;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.Socket;

import javax.swing.*;

public class TypingGameFrame extends JFrame {
    private ChatClient chatClient;
    private Socket socket;
    private DataInputStream dis;
    private DataOutputStream dos;
    private String userName; 
    private TypingGamePanel gamePanel;

    public TypingGameFrame(ChatClient chatClient, String userName) {
        this.chatClient = chatClient;
        this.userName = userName;
        setTitle("Typing Game");
        setSize(373, 675);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

        // 기존 창 위치와 동일하게 열기
        Point chatClientLocation = chatClient.getLocation();
        setLocation(chatClientLocation);

        gamePanel = new TypingGamePanel(this);
        add(gamePanel);

        // 뒤로가기 버튼
        JButton backButton = new JButton("돌아가기");
        backButton.setBounds(10, 10, 100, 30); // 정사각형 크기 유지
        backButton.setBackground(Color.WHITE); // 흰색 배경
        backButton.setFont(new Font("맑은 고딕", Font.BOLD, 12)); // 크기 키움
        gamePanel.add(backButton);

        // 뒤로가기 버튼 동작
        backButton.addActionListener(e -> {
            dispose(); // 게임창 닫기
            chatClient.setVisible(true); // 채팅창 다시 표시
        });

    }

    private class MessageReceiver implements Runnable {
        @Override
        public void run() {
            try {
                while (true) {
                    String message = dis.readUTF();
                    gamePanel.appendMessage(message, false); // 받은 메시지를 게임 패널에 추가
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }
}

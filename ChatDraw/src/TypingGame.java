//import javax.swing.*;
//import java.awt.*;
//import java.awt.event.ActionEvent;
//import java.awt.event.ActionListener;
//import java.io.*;
//import java.util.Random;
//
//public class TypingGame {
//    private JFrame gameFrame;
//    private JTextArea sentenceArea;
//    private JTextField inputField;
//    private JLabel scoreLabel;
//    private JButton backButton;
//    private int score;
//    private String currentSentence;
//    private String userName;
//    private DataOutputStream dos;
//    private ChatClient chatClient;  // ChatClient 객체 추가 (채팅 창을 제어할 수 있도록)
//
//    private JButton startGameButton;
//
//    public TypingGame(String userName, DataOutputStream dos, ChatClient chatClient) {
//        this.userName = userName;
//        this.dos = dos;
//        this.chatClient = chatClient;  // 채팅 창을 제어할 수 있는 객체
//        this.score = 0;
//
//        // 게임 창 생성
//        gameFrame = new JFrame("Typing Game");
//        gameFrame.setSize(400, 300);
//        gameFrame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
//        gameFrame.setLayout(new BorderLayout());
//
//        // 문장, 입력 필드 등
//        sentenceArea = new JTextArea();
//        sentenceArea.setEditable(false);
//        sentenceArea.setFont(new Font("Arial", Font.PLAIN, 20));
//        sentenceArea.setText("Game Starting...");
//
//        scoreLabel = new JLabel("Score: 0");
//        scoreLabel.setFont(new Font("Arial", Font.PLAIN, 16));
//
//        inputField = new JTextField();
//        inputField.setFont(new Font("Arial", Font.PLAIN, 16));
//        inputField.addActionListener(new ActionListener() {
//            @Override
//            public void actionPerformed(ActionEvent e) {
//                checkInput(inputField.getText());
//                inputField.setText(""); // 텍스트 필드를 비움
//            }
//        });
//
//        // 게임 시작 버튼
//        startGameButton = new JButton("Start Game");
//        startGameButton.setFont(new Font("Arial", Font.PLAIN, 16));
//        startGameButton.addActionListener(new ActionListener() {
//            @Override
//            public void actionPerformed(ActionEvent e) {
//                try {
//                    dos.writeUTF("- START_GAME -");  // 서버로 게임 시작 메시지 전송
//                } catch (IOException ex) {
//                    ex.printStackTrace();
//                }
//            }
//        });
//
//        // Back 버튼 설정
//        backButton = new JButton("Back to Chat");
//        backButton.setFont(new Font("Arial", Font.PLAIN, 16));
//        backButton.setEnabled(false);  // 게임이 끝날 때 활성화
//        backButton.addActionListener(new ActionListener() {
//            @Override
//            public void actionPerformed(ActionEvent e) {
//                gameFrame.dispose(); // 게임 창 닫기
//                chatClient.setVisible(true);  // 채팅 창 다시 보이게 하기
//            }
//        });
//
//        // 게임 창에 버튼 추가
//        gameFrame.add(sentenceArea, BorderLayout.NORTH);
//        gameFrame.add(inputField, BorderLayout.CENTER);
//        gameFrame.add(scoreLabel, BorderLayout.SOUTH);
//        gameFrame.add(startGameButton, BorderLayout.WEST);  // Start Game 버튼을 게임 창에 추가
//        gameFrame.add(backButton, BorderLayout.EAST);
//    }
//
//    public void startGame() {
//    	try {
//            dos.writeUTF("- START_GAME -");  // 서버에 게임 시작 요청 보내기
//        } catch (IOException e) {
//            e.printStackTrace();
//        }
//    }
//
//    private void checkInput(String input) {
//        if (input.equals(currentSentence)) {
//            score++;
//            scoreLabel.setText("Score: " + score);
//            if (score == 5) {
//                backButton.setEnabled(true);  // 5점 도달 시 "채팅 창으로 돌아가기" 버튼 활성화
//                sendMessageToChat(userName + " reached 5 points!");
//            }
//            currentSentence = getRandomSentence();
//            sentenceArea.setText(currentSentence);
//        }
//    }
//
//    private String getRandomSentence() {
//        try (BufferedReader br = new BufferedReader(new FileReader("src/words.txt"))) {
//            String line;
//            Random random = new Random();
//            int lineCount = 0;
//            while ((line = br.readLine()) != null) {
//                lineCount++;
//            }
//            int randomLine = random.nextInt(lineCount);
//
//            // 문장 추출
//            try (BufferedReader br2 = new BufferedReader(new FileReader("src/words.txt"))) {
//                int currentLine = 0;
//                while ((line = br2.readLine()) != null) {
//                    if (currentLine == randomLine) {
//                        return line;
//                    }
//                    currentLine++;
//                }
//            }
//        } catch (IOException e) {
//            e.printStackTrace();
//        }
//        return "Error loading sentence.";
//    }
//
//    private void sendMessageToChat(String message) {
//        try {
//            dos.writeUTF(message);  // 서버로 메시지 전송
//        } catch (IOException e) {
//            e.printStackTrace();
//        }
//    }
//}

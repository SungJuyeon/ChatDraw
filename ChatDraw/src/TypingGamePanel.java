import javax.swing.*;
import javax.swing.text.*;
import java.awt.*;
import java.io.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class TypingGamePanel extends JPanel {
    private JLabel wordLabel;
    private JTextField inputField;
    private JTextPane messagePane;
    private List<String> wordList;
    private TypingGameFrame parentFrame;

    public TypingGamePanel(TypingGameFrame parentFrame) {
        this.parentFrame = parentFrame;
        setLayout(null);
        setBackground(Color.WHITE); // 전체 배경 흰색

        // 단어 표시
        wordLabel = new JLabel("준비 중...", SwingConstants.CENTER);
        wordLabel.setFont(new Font("Serif", Font.BOLD, 24));
        wordLabel.setBounds(0, 70, 353, 50); // 뒤로가기 버튼 아래로 이동
        wordLabel.setOpaque(true);
        wordLabel.setBackground(new Color(133, 159, 254)); // 주요 포인트 색
        wordLabel.setForeground(Color.WHITE); // 글자 흰색
        add(wordLabel);

        // 메시지 출력 영역
        messagePane = new JTextPane();
        messagePane.setEditable(false);
        JScrollPane scrollPane = new JScrollPane(messagePane);
        scrollPane.setBounds(10, 130, 343, 390); // 메시지 출력 영역 위치 조정
        add(scrollPane);

        // 입력 필드
        inputField = new JTextField();
        inputField.setBounds(10, 530, 343, 90);
        inputField.setFont(new Font("Serif", Font.PLAIN, 18));
        add(inputField);

        // 단어 리스트 로드
        loadWords();

     // 입력 필드 이벤트
        inputField.addActionListener(e -> {
            String input = inputField.getText();
            if (!input.isEmpty()) {
                appendMessage(input, true); // 항상 오른쪽 정렬
                inputField.setText("");
                showRandomWord();
            }
        });

        showRandomWord();
    }

    private void loadWords() {
        wordList = new ArrayList<>();
        try (BufferedReader reader = new BufferedReader(new FileReader("src/words.txt"))) {
            String line;
            while ((line = reader.readLine()) != null) {
                wordList.add(line);
            }
        } catch (IOException e) {
            wordLabel.setText("단어를 불러올 수 없습니다.");
        }
    }

    private void showRandomWord() {
        if (wordList.isEmpty()) {
            wordLabel.setText("단어가 없습니다.");
            return;
        }
        Random random = new Random();
        String randomWord = wordList.get(random.nextInt(wordList.size()));
        wordLabel.setText(randomWord);
    }
    
    

    public void appendMessage(String msg, boolean isOwnMessage) {
        StyledDocument doc = messagePane.getStyledDocument();
        SimpleAttributeSet attributes = new SimpleAttributeSet();

        // 글꼴 크기와 굵기 설정
        int fontSize = 13; // 글꼴 크기 설정
        boolean isBold = true; // Bold 설정

        // 본인이 보낸 메시지인지 확인
        if (isOwnMessage) {
            StyleConstants.setAlignment(attributes, StyleConstants.ALIGN_RIGHT);
            StyleConstants.setForeground(attributes, Color.white); // 글자 색을 흰색으로
            StyleConstants.setBackground(attributes, new Color(69, 106, 255)); // 배경 색을 (69, 106, 255)로
        } else if (msg.startsWith("-")) { // 접속 메시지일 경우
            StyleConstants.setAlignment(attributes, StyleConstants.ALIGN_CENTER); // 가운데 정렬
            StyleConstants.setForeground(attributes, Color.black); // 글자 색을 검정으로
            StyleConstants.setBackground(attributes, new Color(0, 0, 0, 0)); // 배경을 투명하게 설정
        } else {
            StyleConstants.setAlignment(attributes, StyleConstants.ALIGN_LEFT);
            StyleConstants.setForeground(attributes, Color.black); // 글자 색을 검정으로
            StyleConstants.setBackground(attributes, new Color(223, 229, 255)); // 배경 색을 (223, 229, 255)로
        }

        // 글씨 크기와 굵기 설정
        StyleConstants.setFontSize(attributes, fontSize); // 글꼴 크기 설정
        StyleConstants.setBold(attributes, isBold); // 글꼴 굵기 설정

        try {
            // 메시지 추가
            int start = doc.getLength(); // 현재 문서의 끝 위치
            doc.insertString(doc.getLength(), msg + "\n", attributes); // 메시지 삽입

            // 추가한 텍스트의 스타일을 설정
            int end = doc.getLength();
            doc.setParagraphAttributes(start, end - start, attributes, false);

            // 채팅 창의 끝으로 커서 이동
            messagePane.setCaretPosition(doc.getLength());
        } catch (BadLocationException e) {
            e.printStackTrace();
        }
    }

}

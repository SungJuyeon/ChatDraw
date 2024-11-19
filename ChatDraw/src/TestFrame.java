import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import javax.swing.ImageIcon;

public class TestFrame extends JFrame {
    private final JLabel imageLabel = new JLabel();

    public TestFrame() {
        setTitle("저장된 그림 보기");
        setSize(400, 300);
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        setLocationRelativeTo(null);

        initUI();
    }

    private void initUI() {
        // 버튼 생성
        JButton stickerButton = new JButton("스티커");
        stickerButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                refreshImage();
            }
        });

        // 이미지 표시 영역
        JPanel imagePanel = new JPanel();
        imagePanel.add(imageLabel);

        // 레이아웃 설정
        setLayout(new BorderLayout());
        add(stickerButton, BorderLayout.NORTH);
        add(imagePanel, BorderLayout.CENTER);

        // DrawingPanel을 생성하여 TestFrame을 전달
        DrawingPanel drawingPanel = new DrawingPanel(this);  // TestFrame 전달
        add(drawingPanel, BorderLayout.SOUTH);  // DrawingPanel을 하단에 추가
    }

    public void refreshImage() {
        File file = new File("drawing.png");
        if (file.exists()) {
            ImageIcon imageIcon = new ImageIcon(file.getAbsolutePath());
            imageLabel.setIcon(imageIcon);
        } else {
            imageLabel.setText("저장된 그림이 없습니다.");
        }
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            TestFrame frame = new TestFrame();
            frame.setVisible(true);
        });
    }
}

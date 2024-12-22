import javax.imageio.ImageIO;
import javax.swing.*;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;

public class ImageGallery {
	private ChatClient chatClient;

	public ImageGallery(ChatClient chatClient) {
        this.chatClient = chatClient;
    }
	
    public void show() {
        JFrame imageFrame = new JFrame("저장된 그림들");
        imageFrame.setSize(600, 450);
        imageFrame.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        //imageFrame.getContentPane().setBackground(Color.WHITE);

        JPanel imagePanel = new JPanel();
        imagePanel.setLayout(new GridLayout(0, 3));
        imagePanel.setBackground(Color.WHITE);

        // saved_images 폴더의 이미지들을 로드
        File folder = new File("saved_images");
        if (folder.exists()) {
            File[] imageFiles = folder.listFiles((dir, name) -> name.endsWith(".png"));
            if (imageFiles != null) {
                for (File imageFile : imageFiles) {
                    try {
                        BufferedImage image = ImageIO.read(imageFile);

                        // 이미지 크기 축소 (50%)
                        int width = image.getWidth() / 2;
                        int height = image.getHeight() / 2;
                        Image scaledImage = image.getScaledInstance(width, height, Image.SCALE_SMOOTH);

                        // 이미지 JLabel 생성
                        JLabel imageLabel = new JLabel(new ImageIcon(scaledImage));
                        imageLabel.setBorder(BorderFactory.createLineBorder(Color.GRAY, 1)); // 기본 테두리 설정
                        imageLabel.setCursor(new Cursor(Cursor.HAND_CURSOR)); // 마우스 커서 변경

                        // 클릭 이벤트 추가
                        imageLabel.addMouseListener(new MouseAdapter() {
                            private long lastClickTime = 0;

                            @Override
                            public void mouseClicked(MouseEvent e) {
                                long currentClickTime = System.currentTimeMillis();
                                
                                // 더블 클릭을 처리 (두 클릭 사이의 간격이 짧으면 더블 클릭으로 간주)
                                if (currentClickTime - lastClickTime < 500) {
                                    // 더블 클릭 시, 이미지를 채팅창에 전송
                                    //chatClient.addImageToChat(imageFile.getAbsolutePath());
                                    chatClient.sendFile(imageFile);
                                    imageFrame.dispose(); // 이미지 갤러리 창 닫기
                                } else {
                                    // 한 번 클릭 시, 선택된 이미지를 강조 (노란색 테두리)
                                    for (Component comp : imagePanel.getComponents()) {
                                        if (comp instanceof JLabel) {
                                            ((JLabel) comp).setBorder(BorderFactory.createLineBorder(Color.GRAY, 1));
                                        }
                                    }
                                    imageLabel.setBorder(BorderFactory.createLineBorder(Color.YELLOW, 3));
                                }
                                lastClickTime = currentClickTime;
                            }
                        });

                        // 이미지 패널에 추가
                        imagePanel.add(imageLabel);
                    } catch (IOException ex) {
                        ex.printStackTrace();
                    }
                }
            }
        }

        // 스크롤 패널에 이미지 패널을 추가
        JScrollPane scrollPane = new JScrollPane(imagePanel);
        scrollPane.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
        scrollPane.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED);
        imageFrame.add(scrollPane);

        // 프레임을 보여줌
        imageFrame.setVisible(true);
    }
}

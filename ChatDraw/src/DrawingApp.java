import javax.imageio.ImageIO;
import javax.swing.*;

import java.awt.*;
import java.awt.geom.Ellipse2D;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;

public class DrawingApp extends JFrame {
	private final TestFrame testFrame;
    private final DrawingPanel drawingPanel;
    
    public DrawingApp() {
    	
    	testFrame = new TestFrame();  // 생성자에서 초기화
        drawingPanel = new DrawingPanel(testFrame);
        setTitle("그림판");
        setSize(600, 600);
        setDefaultCloseOperation(DISPOSE_ON_CLOSE);
        setLocationRelativeTo(null);

        initUI();
    }

    private void initUI() {
        // 캔버스 추가
        add(drawingPanel, BorderLayout.CENTER);

        // 컨트롤 패널 추가
        JPanel controlPanel = new JPanel();
        controlPanel.setLayout(new FlowLayout(FlowLayout.LEFT));

     // 색상 선택 버튼
        JButton colorButton = new JButton();
        colorButton.setBackground(drawingPanel.getBrushColor()); // 배경색을 흰색으로 설정
        colorButton.setOpaque(true); // 배경색을 보이도록 설정
        colorButton.setBorder(BorderFactory.createLineBorder(Color.BLACK, 2)); // 테두리 추가
        colorButton.setPreferredSize(new Dimension(40, 40)); // 버튼 크기 설정
        colorButton.addActionListener(e -> {
            new ColorChooserFrame(drawingPanel, colorButton); // DrawingPanel과 colorButton 전달
        });
        controlPanel.add(colorButton);

        
        // 브러시 크기 조절 슬라이더
        JSlider sizeSlider = new JSlider(5, 30, 8);
        sizeSlider.setPreferredSize(new Dimension(100, 30));
        sizeSlider.addChangeListener(e -> drawingPanel.setBrushSize(sizeSlider.getValue()));
        controlPanel.add(new JLabel("크기:"));
        controlPanel.add(sizeSlider);

        
        // 그리기 버튼
        JButton pencilButton = new JButton("");
        try {
            // pencil.png를 아이콘으로 설정
            ImageIcon pencilIcon = new ImageIcon(ChatClient.class.getResource("/images/Pencil.png"));
            Image scaledImage = pencilIcon.getImage().getScaledInstance(30, 30, Image.SCALE_SMOOTH); // 이미지 크기 조정
            pencilButton.setIcon(new ImageIcon(scaledImage));
        } catch (Exception ex) {
            pencilButton.setText("그리기"); // 이미지 로드 실패 시 텍스트로 대체
        }
        pencilButton.setBackground(Color.WHITE);
        pencilButton.addActionListener(e -> {
            if (drawingPanel.isEraserMode()) { // 현재 지우개 모드라면 전환
                drawingPanel.setEraserMode(false);
            }
        });
        controlPanel.add(pencilButton);
        
        
        // 지우개 버튼
        JButton eraserButton = new JButton("");
        
        try {
            ImageIcon eraserIcon = new ImageIcon(ChatClient.class.getResource("/images/Eraser.png"));
            Image scaledImage = eraserIcon.getImage().getScaledInstance(30, 30, Image.SCALE_SMOOTH); // 이미지 크기 조정
            eraserButton.setIcon(new ImageIcon(scaledImage));
        } catch (Exception ex) {
        	eraserButton.setText("지우기"); // 이미지 로드 실패 시 텍스트로 대체
        }
        eraserButton.setBackground(Color.WHITE);
        eraserButton.addActionListener(e -> {
            if (!drawingPanel.isEraserMode()) { // 현재 그리기 모드라면 전환
                drawingPanel.setEraserMode(true);
            }
        });
        controlPanel.add(eraserButton);
        

        // 다시 그리기 버튼
        JButton clearButton = new JButton("");
        
        try {
            ImageIcon resetIcon = new ImageIcon(ChatClient.class.getResource("/images/Reset.png"));
            Image scaledImage = resetIcon.getImage().getScaledInstance(30, 30, Image.SCALE_SMOOTH); // 이미지 크기 조정
            clearButton.setIcon(new ImageIcon(scaledImage));
        } catch (Exception ex) {
        	clearButton.setText("reset"); // 이미지 로드 실패 시 텍스트로 대체
        }
        clearButton.setBackground(Color.WHITE);
        clearButton.addActionListener(e -> {
            drawingPanel.clearCanvas();
            drawingPanel.setEraserMode(false);
        });
        controlPanel.add(clearButton);
        
        
     // 스티커 버튼 (그림 저장)
        JButton stickerButton = new JButton("");
        
        try {
            ImageIcon stickerIcon = new ImageIcon(ChatClient.class.getResource("/images/Save.png"));
            Image scaledImage = stickerIcon.getImage().getScaledInstance(30, 30, Image.SCALE_SMOOTH); // 이미지 크기 조정
            stickerButton.setIcon(new ImageIcon(scaledImage));
        } catch (Exception ex) {
        	stickerButton.setText("저장"); // 이미지 로드 실패 시 텍스트로 대체
        }
        stickerButton.setBackground(Color.WHITE);
        stickerButton.addActionListener(e -> {
            // 이미지 저장
            drawingPanel.saveCanvas(testFrame); // TestFrame을 전달하여 이미지 갱신을 처리
        });
        controlPanel.add(stickerButton);
        
        
     // 저장된 그림을 보기 위한 버튼
        JButton viewSavedButton = new JButton("");
        
//        try {
//            ImageIcon viewSavedIcon = new ImageIcon(ChatClient.class.getResource("/images/ImageGallery.png"));
//            Image scaledImage = viewSavedIcon.getImage().getScaledInstance(30, 30, Image.SCALE_SMOOTH); // 이미지 크기 조정
//            viewSavedButton.setIcon(new ImageIcon(scaledImage));
//        } catch (Exception ex) {
//        	viewSavedButton.setText("저장"); // 이미지 로드 실패 시 텍스트로 대체
//        }
//        viewSavedButton.setBackground(Color.WHITE);
//        viewSavedButton.addActionListener(e -> {
//            ImageGallery gallery = new ImageGallery();
//            gallery.show(); // 이미지 갤러리 표시
//        });
//        controlPanel.add(viewSavedButton);
        
        
        
        add(controlPanel, BorderLayout.NORTH);

    }
    

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            DrawingApp app = new DrawingApp();
            app.setVisible(true);
        });
    }
}

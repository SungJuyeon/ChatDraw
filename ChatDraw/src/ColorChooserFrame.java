import javax.swing.*;
import javax.swing.event.ChangeListener;

import java.awt.*;

public class ColorChooserFrame extends JFrame {
    public ColorChooserFrame(DrawingPanel drawingPanel, JButton colorButton) {
        setTitle("색상 선택");
        setSize(500, 400);
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);

        // JColorChooser 생성
        JColorChooser colorChooser = new JColorChooser(drawingPanel.getBrushColor());

        ChangeListener changeListener = e -> {
            Color selectedColor = colorChooser.getColor();
            drawingPanel.setBrushColor(selectedColor); // DrawingPanel 색상 업데이트
            colorButton.setBackground(selectedColor);  // colorButton 색상 업데이트
        };
        colorChooser.getSelectionModel().addChangeListener(changeListener);

        // "확인" 버튼을 누를 때 창 닫기
        JButton confirmButton = new JButton("확인");
        confirmButton.setBackground(Color.WHITE); // 초록색 배경
        confirmButton.setForeground(Color.BLACK); // 흰색 텍스트
        confirmButton.setFocusPainted(false); // 클릭시 테두리 없애기
        confirmButton.setCursor(new Cursor(Cursor.HAND_CURSOR)); // 손 모양 커서
        confirmButton.setPreferredSize(new Dimension(100, 40)); // 버튼 크기 설정
        confirmButton.addActionListener(e -> {
            colorButton.setBackground(drawingPanel.getBrushColor()); // 색상 변경 후 colorButton 색상 업데이트
            dispose(); // 창 닫기
        });

        // Layout 설정
        setLayout(new BorderLayout());
        add(colorChooser, BorderLayout.CENTER);
        
        JPanel buttonPanel = new JPanel();
        buttonPanel.setLayout(new FlowLayout(FlowLayout.CENTER));
        buttonPanel.add(confirmButton);
        add(buttonPanel, BorderLayout.SOUTH);

        setVisible(true);
    }
}

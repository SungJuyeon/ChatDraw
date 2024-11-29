import javax.swing.*;
import javax.swing.text.*;
import java.awt.*;
import java.awt.geom.Rectangle2D;

public class ChatBubbleTextPane extends JTextPane {
    private static final long serialVersionUID = 1L;

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);

        Graphics2D g2 = (Graphics2D) g;
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

        StyledDocument doc = getStyledDocument();
        Element root = doc.getDefaultRootElement();

        try {
            for (int i = 0; i < root.getElementCount(); i++) {
                Element line = root.getElement(i);
                int startOffset = line.getStartOffset();
                int endOffset = line.getEndOffset();

                String text = doc.getText(startOffset, endOffset - startOffset).trim();
                if (text.isEmpty()) {
                    continue; // 공백 라인은 무시
                }

                // 텍스트 범위 가져오기
                Rectangle2D rect2D = getUI().modelToView2D(this, startOffset, Position.Bias.Forward);
                if (rect2D == null) {
                    continue;
                }

                // 텍스트 크기 계산
                FontMetrics fm = g2.getFontMetrics(getFont());
                int textWidth = fm.stringWidth(text);
                int textHeight = fm.getHeight();

                // 배경 위치와 크기 설정
                int padding = 10; // 여백
                int arc = 20; // 둥근 모서리 크기
                int bubbleX = (int) rect2D.getX() - padding;
                int bubbleY = (int) rect2D.getY() - padding / 2;
                int bubbleWidth = textWidth + 2 * padding;
                int bubbleHeight = textHeight + padding;

                // 스타일 정보 확인
                AttributeSet attributes = doc.getCharacterElement(startOffset).getAttributes();
                Color backgroundColor = new Color(223, 229, 255); // 상대 메시지
                int alignment = StyleConstants.getAlignment(attributes);

                if (alignment == StyleConstants.ALIGN_RIGHT) {
                    backgroundColor = new Color(69, 106, 255); // 본인 메시지
                    bubbleX = getWidth() - bubbleWidth - padding; // 오른쪽 정렬
                } else if (alignment == StyleConstants.ALIGN_CENTER) {
                    backgroundColor = new Color(69, 106, 255, 0); // 중앙 정렬 배경색
                    bubbleX = (getWidth() - bubbleWidth) / 2; // 중앙 정렬
                } else {
                    // 기본 좌측 정렬
                    bubbleX = padding; // 기본 좌측 정렬
                }

                // 둥근 배경 그리기
                g2.setColor(backgroundColor);
                g2.fillRoundRect(bubbleX, bubbleY, bubbleWidth, bubbleHeight, arc, arc);
                
                if (alignment == StyleConstants.ALIGN_RIGHT) {
                    g2.setColor(Color.WHITE); // 본인 메시지 글자색은 흰색
                } else {
                    g2.setColor(Color.BLACK); // 상대방 메시지 글자색은 검정색
                }
                
                g2.drawString(text, bubbleX + padding, bubbleY + fm.getAscent());
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}


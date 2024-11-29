import javax.swing.*;
import javax.swing.text.*;
import java.awt.*;
import java.awt.geom.Rectangle2D;

public class ChatBubbleTextPane extends JTextPane {
    private static final long serialVersionUID = 1L;

    public ChatBubbleTextPane() {
        setEditable(false); // 텍스트 수정 불가
        setBackground(Color.WHITE); // 배경색 설정
        setMargin(new Insets(5, 5, 5, 5)); // 여백 설정
        setOpaque(false); // 투명 배경 설정
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);

        Graphics2D g2 = (Graphics2D) g;
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

        StyledDocument doc = getStyledDocument();
        Element root = doc.getDefaultRootElement();

        try {
        	int verticalOffset = 0;
        	
            for (int i = 0; i < root.getElementCount(); i++) {
                Element line = root.getElement(i);
                int startOffset = line.getStartOffset();
                int endOffset = line.getEndOffset();

                String text = doc.getText(startOffset, endOffset - startOffset).trim();
                if (text.isEmpty()) {
                    continue; // 공백 라인은 무시
                }
                
             // 텍스트에서 timeStamp 분리
                String timeStamp = "";
                int indexOfBracket = text.indexOf("{");
                if (indexOfBracket != -1) {
                    timeStamp = text.substring(indexOfBracket);
                    text = text.substring(0, indexOfBracket).trim(); // timeStamp 제거
                }else {
                    // "{" 문자가 없으면, 메시지를 그대로 사용
                    System.out.println("");
                }

                // 텍스트 스타일 정보 가져오기
                AttributeSet attributes = doc.getCharacterElement(startOffset).getAttributes();
                Color backgroundColor = new Color(223, 229, 255); // 상대 메시지
                int alignment = StyleConstants.getAlignment(attributes);

                if (alignment == StyleConstants.ALIGN_RIGHT) {
                    backgroundColor = new Color(69, 106, 255); // 본인 메시지
                } else if (alignment == StyleConstants.ALIGN_CENTER) {
                    backgroundColor = new Color(69, 106, 255, 0); // 중앙 정렬 배경색
                }

                // 텍스트를 분할
                FontMetrics fm = g2.getFontMetrics(getFont());
                int maxWidth = getWidth() - 100; // 여유 공간을 고려한 최대 너비
                String[] lines = splitTextIntoLines(text, fm, maxWidth);

                int lineHeight = fm.getHeight();
                int padding = 7; // 여백
                int arc = 10; // 둥근 모서리 크기
                int bubbleHeight = (lineHeight + padding) * lines.length + padding;	//text 가운데

                // 첫 번째 줄 위치
                Rectangle2D rect2D = getUI().modelToView2D(this, startOffset, Position.Bias.Forward);
                if (rect2D == null) {
                    continue;
                }
                int bubbleY = (int) rect2D.getY() - padding / 2 + verticalOffset;

                int bubbleX = padding; // 기본 좌측 정렬
                int bubbleWidth = 0;
                for (String lineText : lines) {
                    int textWidth = fm.stringWidth(lineText);
                    bubbleWidth = Math.max(bubbleWidth, textWidth);
                }
                bubbleWidth += 2 * padding;

                if (alignment == StyleConstants.ALIGN_RIGHT) {
                    bubbleX = getWidth() - bubbleWidth - padding;
                } else if (alignment == StyleConstants.ALIGN_CENTER) {
                    bubbleX = (getWidth() - bubbleWidth) / 2;
                }

             // 배경 그리기
                g2.setColor(backgroundColor);
                g2.fillRoundRect(bubbleX, bubbleY, bubbleWidth, bubbleHeight, arc, arc);

                // 텍스트 높이 중앙 정렬 및 출력
                g2.setColor(alignment == StyleConstants.ALIGN_RIGHT ? Color.WHITE : Color.BLACK);
                int textY = bubbleY + padding;
                for (String lineText : lines) {
                    g2.drawString(lineText, bubbleX + padding, textY + fm.getAscent());
                    textY += lineHeight + padding / 2;
                }
                
             // timeStamp 출력
                if (!timeStamp.isEmpty()) {
                    int timeStampY = bubbleY + bubbleHeight; // 둥근 사각형 바로 아래
                    g2.setColor(Color.GRAY);
                    g2.drawString(timeStamp, bubbleX - padding, timeStampY + fm.getAscent());
                }
             // 첫 번째 사각형과 두 번째 사각형 사이에 간격 추가
                verticalOffset += bubbleHeight - 25;
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private String[] splitTextIntoLines(String text, FontMetrics fm, int maxWidth) {
        StringBuilder currentLine = new StringBuilder();
        java.util.List<String> lines = new java.util.ArrayList<>();

        for (int i = 0; i < text.length(); i++) {
            char c = text.charAt(i);
            
            // 현재 라인의 길이가 maxWidth를 초과하면 새로운 줄로 넘김
            if (currentLine.length() >= 35) {
                lines.add(currentLine.toString());
                currentLine = new StringBuilder();
            }

            // 문자 추가
            currentLine.append(c);
        }

        // 마지막 남은 라인도 추가
        if (currentLine.length() > 0) {
            lines.add(currentLine.toString());
        }

        return lines.toArray(new String[0]);
    }

}

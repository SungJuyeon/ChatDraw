package src;

import javax.swing.*;
import java.awt.*;
import java.io.IOException;
import java.net.URI;
import java.net.URL;

public class WebViewerApp {
    // 패널 설정
    private JPanel contentPanel;

    public WebViewerApp() {
        contentPanel = new JPanel();
        contentPanel.setLayout(new BoxLayout(contentPanel, BoxLayout.Y_AXIS)); // 세로로 배치

        // URL을 클릭했을 때 실행되는 버튼
        JButton linkButton = new JButton("기사 보기");
        linkButton.addActionListener(e -> openArticleInPanel("https://www.ajunews.com/view/20241121185539608"));

        contentPanel.add(linkButton);
    }

    private void openArticleInPanel(String url) {
        // 기존 컴포넌트 제거
        contentPanel.removeAll();

        // JEditorPane로 URL 로드
        JEditorPane editorPane = new JEditorPane();
        editorPane.setEditable(false);
        try {
            editorPane.setPage(url); // URL을 로드
        } catch (IOException e) {
            editorPane.setText("페이지를 불러올 수 없습니다.");
        }

        // 스크롤 패널에 추가
        JScrollPane scrollPane = new JScrollPane(editorPane);
        contentPanel.add(scrollPane);

        // 화면 갱신
        contentPanel.revalidate();
        contentPanel.repaint();
    }
}

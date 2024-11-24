import javax.swing.*;
import java.awt.*;
import java.awt.datatransfer.StringSelection;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.*;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;

public class NewsBot {
    private JFrame frame;
    private JPanel contentPanel; // 뉴스 항목을 표시할 JPanel
    private JTextField inputField;
    private JPanel buttonPanel;
    private HashMap<String, String[][]> newsData; // 카테고리별 뉴스 데이터 (제목, URL, 설명)

    public NewsBot() {
        frame = new JFrame("뉴스봇");
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setSize(500, 600);
        frame.setLayout(new BorderLayout());

        // 뉴스 내용 패널
        contentPanel = new JPanel();
        contentPanel.setLayout(new BoxLayout(contentPanel, BoxLayout.Y_AXIS)); // 세로로 배치
        JScrollPane contentScrollPane = new JScrollPane(contentPanel);
        frame.add(contentScrollPane, BorderLayout.CENTER);

        // 입력 영역
        inputField = new JTextField();
        JButton searchButton = new JButton("🔍");
        JPanel inputPanel = new JPanel(new BorderLayout());
        inputPanel.add(inputField, BorderLayout.CENTER);
        inputPanel.add(searchButton, BorderLayout.EAST);
        frame.add(inputPanel, BorderLayout.SOUTH);

        // 버튼 패널 (카테고리 표시용)
        buttonPanel = new JPanel();
        buttonPanel.setLayout(new FlowLayout());
        frame.add(buttonPanel, BorderLayout.NORTH);

        // 뉴스 데이터 초기화
        initNewsData();

        // 돋보기 버튼 클릭 시
        searchButton.addActionListener(e -> displayCategories());

        frame.setVisible(true);
    }

    private void initNewsData() {
        newsData = new HashMap<>();
        String[] categories = {"정치", "경제", "사회", "문화", "IT", "세계"};
        for (String category : categories) {
            newsData.put(category, getNewsData(category));
        }
    }

    private String[][] getNewsData(String category) {
        String clientId = "4dXH7aExv3_aHDA9nvGO"; // 애플리케이션 클라이언트 아이디값
        String clientSecret = "irI0lyffBR"; // 애플리케이션 클라이언트 시크릿값

        try {
            String encodedCategory = URLEncoder.encode(category, StandardCharsets.UTF_8);
            String apiURL = "https://openapi.naver.com/v1/search/news.json?query=" + encodedCategory + "&display=3&sort=date";
            URL url = new URL(apiURL);
            HttpURLConnection con = (HttpURLConnection) url.openConnection();
            con.setRequestMethod("GET");
            con.setRequestProperty("X-Naver-Client-Id", clientId);
            con.setRequestProperty("X-Naver-Client-Secret", clientSecret);

            int responseCode = con.getResponseCode();
            BufferedReader br;
            if (responseCode == 200) {
                br = new BufferedReader(new InputStreamReader(con.getInputStream(), StandardCharsets.UTF_8));
            } else {
                br = new BufferedReader(new InputStreamReader(con.getErrorStream(), StandardCharsets.UTF_8));
            }

            StringBuilder response = new StringBuilder();
            String inputLine;
            while ((inputLine = br.readLine()) != null) {
                response.append(inputLine);
            }
            br.close();

            // JSON 파싱 (여기서는 간단히 정규식으로 처리)
            String json = response.toString();
            String[] titles = json.split("\"title\":\"");
            String[] links = json.split("\"link\":\"");
            String[] descriptions = json.split("\"description\":\"");

            String[][] news = new String[3][3]; // 3개의 뉴스 데이터 저장 (제목, 링크, 설명)
            for (int i = 1; i <= 3 && i < titles.length && i < links.length && i < descriptions.length; i++) {
                news[i - 1][0] = titles[i].split("\",")[0].replaceAll("<.*?>", ""); // 제목
                news[i - 1][1] = links[i].split("\",")[0]; // URL
                news[i - 1][2] = descriptions[i].split("\",")[0].replaceAll("<.*?>", ""); // 설명
            }
            return news;

        } catch (Exception e) {
            e.printStackTrace();
            return new String[][]{{"뉴스를 가져오지 못했습니다.", "", ""}};
        }
    }

    private void displayCategories() {
        buttonPanel.removeAll(); // 기존 버튼 제거
        String[] categories = {"정치", "경제", "사회", "문화", "IT", "세계"};
        for (String category : categories) {
            JButton button = new JButton(category);
            button.addActionListener(new CategoryButtonListener(category));
            buttonPanel.add(button);
        }
        buttonPanel.revalidate();
        buttonPanel.repaint();
    }

    private class CategoryButtonListener implements ActionListener {
        private String category;

        public CategoryButtonListener(String category) {
            this.category = category;
        }

        @Override
        public void actionPerformed(ActionEvent e) {
            // 기존 내용 삭제
            contentPanel.removeAll();
            contentPanel.revalidate();
            contentPanel.repaint();

            // 카테고리별 뉴스 표시
            JLabel headerLabel = new JLabel("뉴스봇: 이 시각 " + category + " 뉴스입니다.");
            headerLabel.setFont(new Font("맑은 고딕", Font.BOLD, 18));
            contentPanel.add(headerLabel);

            String[][] news = newsData.getOrDefault(category, new String[][]{{"뉴스 없음", "", ""}});
            // 뉴스 항목 생성 코드 (기사 보기 + 공유하기 버튼)
            for (String[] item : news) {
                JPanel newsPanel = new JPanel();
                newsPanel.setLayout(new BoxLayout(newsPanel, BoxLayout.Y_AXIS)); // 세로로 배치
                newsPanel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10)); // 단락 간격 설정
                newsPanel.setBackground(Color.WHITE);

                // 제목을 JTextArea로 추가
                JTextArea titleArea = new JTextArea(item[0]);
                titleArea.setFont(new Font("맑은 고딕", Font.BOLD, 16)); // 한글 지원 폰트
                titleArea.setEditable(false); // 수정 불가
                titleArea.setLineWrap(true); // 자동 줄바꿈 활성화
                titleArea.setWrapStyleWord(true); // 단어 단위로 줄바꿈
                titleArea.setBackground(Color.WHITE);
                titleArea.setAlignmentX(Component.LEFT_ALIGNMENT); // 왼쪽 정렬
                newsPanel.add(titleArea);

                // 설명을 JTextArea로 추가
                JTextArea descriptionArea = new JTextArea(item[2]);
                descriptionArea.setFont(new Font("맑은 고딕", Font.PLAIN, 12));
                descriptionArea.setEditable(false); // 수정 불가
                descriptionArea.setLineWrap(true); // 자동 줄바꿈 활성화
                descriptionArea.setWrapStyleWord(true); // 단어 단위로 줄바꿈
                descriptionArea.setBackground(Color.WHITE);
                descriptionArea.setAlignmentX(Component.LEFT_ALIGNMENT); // 왼쪽 정렬
                newsPanel.add(descriptionArea);

                // 버튼들을 담을 수평 버튼 패널 생성
                JPanel buttonPanel = new JPanel();
                buttonPanel.setLayout(new BoxLayout(buttonPanel, BoxLayout.X_AXIS)); // 버튼을 가로로 배치
                buttonPanel.setBackground(Color.WHITE); // 배경색 설정

                // 기사 보기 버튼 추가
                JButton linkButton = new JButton("기사 보기");
                linkButton.setBackground(Color.WHITE);
                linkButton.addActionListener(ev -> {
                    try {
                        String validUrl = item[1].replace("\\/", "/");
                        Desktop.getDesktop().browse(new URI(validUrl));
                    } catch (IOException | URISyntaxException ex) {
                        throw new RuntimeException(ex);
                    }
                });
                buttonPanel.add(linkButton); // 버튼 패널에 추가

                // 공유하기 버튼 추가
                JButton shareButton = new JButton("공유하기");
                shareButton.setBackground(Color.WHITE);
                shareButton.addActionListener(ev -> {
                    try {
                        // "기사 공유" 로직 추가 (URL 복사 또는 다른 로직 구현 가능)
                        String validUrl = item[1].replace("\\/", "/");
                        Toolkit.getDefaultToolkit().getSystemClipboard().setContents(new StringSelection(validUrl), null);
                        JOptionPane.showMessageDialog(frame, "URL이 클립보드에 복사되었습니다: " + validUrl);
                    } catch (Exception ex) {
                        throw new RuntimeException(ex);
                    }
                });
                buttonPanel.add(Box.createHorizontalStrut(10)); // 두 버튼 사이 간격 추가
                buttonPanel.add(shareButton); // 버튼 패널에 추가

                // 버튼 패널을 newsPanel에 추가
                buttonPanel.setAlignmentX(Component.LEFT_ALIGNMENT); // 왼쪽 정렬
                newsPanel.add(buttonPanel);

                // 뉴스 패널을 contentPanel에 추가
                contentPanel.add(newsPanel);
            }




            // "더보기" 버튼 추가
            JButton moreButton = new JButton("더보기");
            moreButton.addActionListener(ev -> openCategoryPage(category));
            contentPanel.add(moreButton);

            // 새로운 뉴스 항목 표시 후 화면 갱신
            contentPanel.revalidate();
            contentPanel.repaint();
        }
    }

    private void goBackToCategoryNews() {
        // 카테고리 뉴스 화면으로 돌아가기
        contentPanel.removeAll(); // 기존 내용 삭제
        contentPanel.revalidate();
        contentPanel.repaint();

        // 다시 카테고리별 뉴스 표시
        displayCategories();
    }

    private void openCategoryPage(String category) {
        try {
            // 카테고리별 URL 설정
            String url;
            switch (category) {
                case "경제":
                    url = "https://news.naver.com/section/101";
                    break;
                case "사회":
                    url = "https://news.naver.com/section/102";
                    break;
                case "문화":
                    url = "https://news.naver.com/section/103";
                    break;
                case "과학":
                    url = "https://news.naver.com/section/105";
                    break;
                case "세계":
                    url = "https://news.naver.com/section/104";
                    break;
                default:
                    url = "https://news.naver.com";
                    break;
            }

            // 해당 URL 브라우저에서 열기
            Desktop.getDesktop().browse(new URI(url));
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(NewsBot::new);
    }
}

import java.awt.Color;
import java.awt.EventQueue;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.Image;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.net.Socket;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

import javax.swing.BorderFactory;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollBar;
import javax.swing.JScrollPane;
import javax.swing.JSeparator;
import javax.swing.JTextPane;
import javax.swing.ScrollPaneConstants;
import javax.swing.SwingConstants;
import javax.swing.SwingUtilities;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.text.BadLocationException;
import javax.swing.text.SimpleAttributeSet;
import javax.swing.text.StyleConstants;
import javax.swing.text.StyledDocument;

public class ChatClient extends JFrame {

	private static final long serialVersionUID = 1L;
	private JPanel contentPane;
	private JScrollPane chatScrollPane;
	private JTextPane chatTextArea;
	private JScrollPane scrollPane;
	private JTextPane textPane;
	private JPanel panel;
	private JPanel topPanel;
	private JButton sendButton;

	private Socket socket;
	private DataInputStream dis;
	private DataOutputStream dos;

	private String roomName; 
	private String userName; 
	
	private String inputId;
    private String loginName;
    private JFrame parentFrame;

	public static void main(String[] args) {
		EventQueue.invokeLater(new Runnable() {
			public void run() {
				try {
					ChatClient frame = new ChatClient("", "");
					frame.setVisible(true);
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
		});
	}

	public ChatClient(String userName, String roomName) 
	{
		setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
		this.userName = userName;
		this.roomName = roomName;
		

		setSize(373, 675);
		contentPane = new JPanel();

		// GUI 
		TopPanel(roomName);
		chatPanel();
		TextPanel();
		sendPanel();

		setContentPane(contentPane);

		// 서버 연결
		try 
		{
			socket = new Socket("localhost", 30000);
			dis = new DataInputStream(socket.getInputStream());
			dos = new DataOutputStream(socket.getOutputStream());

			Thread receiveThread = new Thread(new MessageReceiver());
			receiveThread.start();
		} 
		catch (IOException e) 
		{
			e.printStackTrace();
			appendText("connect error", false);
		}

		loadChatHistory(roomName, userName);
	}

	// 채팅 내역 로드 및 출력
	private void loadChatHistory(String roomName, String username) {
	    List<ChatMessage> chatHistory = DBManager.loadChatHistory(roomName);

	    for (ChatMessage chatMessage : chatHistory) {
	        String sender = chatMessage.getSender();
	        String content = chatMessage.getContent();
	        System.out.println(sender);
	        System.out.println(content);
	        System.out.println(chatMessage.getFormattedTimestamp());

	        if (content.contains(".jpg") || content.contains(".png") || content.contains(".jpeg")) {
	            addImageToChat(content);  // 이미지 경로를 처리하여 이미지를 채팅에 표시
	        } else {
	            boolean isOwnMessage = sender.equals(username);  // 내가 보낸 메시지인지 확인

	            // 메시지를 본인 메시지인지 아닌지에 따라 다르게 포맷
	            if (isOwnMessage) {
	                String formattedMessage = String.format("[%s]\n%s  {%s}\n", sender, content, chatMessage.getFormattedTimestamp());
	                appendText(formattedMessage, isOwnMessage);  // 본인 메시지 -> 오른쪽 정렬
	            } else if (content.startsWith("-")) {
	                String formattedMessage = String.format("[%s]\n%s  {%s}\n", sender, content, chatMessage.getFormattedTimestamp());
	                appendText(formattedMessage, false);  // 접속 메시지 -> 가운데 정렬
	            } else {
	                String formattedMessage = String.format("[%s]\n%s  {%s}\n", sender, content, chatMessage.getFormattedTimestamp());
	                appendText(formattedMessage, false);  // 상대방 메시지 -> 왼쪽 정렬
	            }
	            
	        }
	    }

	    // 접속 알림 메시지 처리
	    String connect = String.format("- %s님이 접속하였습니다. -%n", username);
	    appendText(connect, false);  // 접속 메시지는 가운데 정렬로 출력
	}


	// 채팅 화면 GUI
	private void TopPanel(String roomName) 
	{
		contentPane.setLayout(null);
		topPanel = new JPanel();
		topPanel.setBounds(0, 0, 360, 70);
		topPanel.setBackground(new Color(133, 159, 254));
		topPanel.setLayout(null);
		contentPane.add(topPanel);
		
		// 방 이름
	    JLabel roomNameLabel = new JLabel(roomName);
	    roomNameLabel.setBounds(10, 20, 340, 30);
	    roomNameLabel.setFont(new Font("맑은 고딕", Font.BOLD, 15));
	    //roomNameLabel.setHorizontalAlignment(SwingConstants.CENTER); // 중앙 정렬
	    roomNameLabel.setForeground(Color.WHITE);
	    topPanel.add(roomNameLabel); // 패널에 추가
	}

	private void chatPanel() {
	    chatScrollPane = new JScrollPane();
	    chatScrollPane.setBounds(0, 70, 360, 437);
	    chatScrollPane.setHorizontalScrollBarPolicy(ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER);
	    chatScrollPane.setBorder(BorderFactory.createEmptyBorder());
	    contentPane.add(chatScrollPane);

	    chatTextArea = new JTextPane();
	    chatTextArea.setFocusable(false);
	    chatTextArea.setBackground(new Color(255, 255, 255));
	    chatScrollPane.setViewportView(chatTextArea);

	    // 스크롤을 맨 아래로 내리기 위한 리스너 설정
	    chatTextArea.addCaretListener(e -> {
	        // 텍스트가 변경될 때마다 스크롤을 맨 아래로 내림
	        SwingUtilities.invokeLater(() -> {
	            JScrollBar vertical = chatScrollPane.getVerticalScrollBar();
	            vertical.setValue(vertical.getMaximum());
	        });
	    });

	    contentPane.add(chatScrollPane);
	}


	private void TextPanel() 
	{
		scrollPane = new JScrollPane();
		scrollPane.setBounds(0, 506, 360, 88);
		scrollPane.setBorder(BorderFactory.createEmptyBorder());
		scrollPane.setBorder(BorderFactory.createLineBorder(new Color(230, 230, 230), 2)); // 윤곽선
		contentPane.add(scrollPane);

		textPane = new JTextPane();
		scrollPane.setViewportView(textPane);
		scrollPane.setBackground(new Color(255, 255, 255));

		textPane.addKeyListener(new KeyAdapter() {
			@Override
			public void keyReleased(KeyEvent e) 
			{
				if (isEnter(e)) 
				{
					pressEnter();
				}
			}
		});

		textPane.getDocument().addDocumentListener(new DocumentListener() {
			@Override
			public void insertUpdate(DocumentEvent e) 
			{
				buttonState();
			}

			@Override
			public void removeUpdate(DocumentEvent e) 
			{
				buttonState();
			}

			@Override
			public void changedUpdate(DocumentEvent e) 
			{
				buttonState();
			}
		});
	}

	private void sendPanel() 
	{
		panel = new JPanel();
		panel.setBounds(0, 595, 360, 45);
		panel.setBackground(new Color(255, 255, 255));
		contentPane.add(panel);
		panel.setLayout(null);
		
		// 파일 선택 버튼
		JButton fileButton = new JButton("");
		fileButton.setFocusPainted(false);
		fileButton.setBorderPainted(false);
		fileButton.setBackground(Color.WHITE);
		fileButton.setIcon(new ImageIcon(ChatClient.class.getResource("/images/icon_folder.png")));
		fileButton.setBounds(111, 0, 44, 40);
		panel.add(fileButton);

		fileButton.addActionListener(new ActionListener() {
		    @Override
		    public void actionPerformed(ActionEvent e)
		    {
		        JFileChooser fileChooser = new JFileChooser();
		        int returnValue = fileChooser.showOpenDialog(null);
		        if (returnValue == JFileChooser.APPROVE_OPTION) 
		        {
		            File selectedFile = fileChooser.getSelectedFile();
		            sendFile(selectedFile);
		        }
		    }
		});

		
		// 그림판 열기 버튼
		JButton drawingBoardButton = new JButton(""); 
		ImageIcon originalIcon = new ImageIcon(ChatClient.class.getResource("/images/icon_palette.png"));
		Image img = originalIcon.getImage();
		Image resizedImage = img.getScaledInstance(44, 35, Image.SCALE_SMOOTH); // 버튼 크기에 맞게 조정
		ImageIcon resizedIcon = new ImageIcon(resizedImage);
		drawingBoardButton.setIcon(resizedIcon);

		
		drawingBoardButton.setBackground(Color.white);
		drawingBoardButton.setFont(new Font("Dialog", Font.BOLD, 18));
		drawingBoardButton.setBounds(10, 0, 44, 40);
		panel.add(drawingBoardButton);

		drawingBoardButton.addActionListener(new ActionListener() {
		    @Override
		    public void actionPerformed(ActionEvent e) {
		        SwingUtilities.invokeLater(() -> {
		            DrawingApp drawingApp = new DrawingApp();
		            drawingApp.setVisible(true);
		        });
		    }
		});
		
		// "저장된 그림 보기" 버튼
		JButton viewSavedImagesButton = new JButton("");
		ImageIcon galleryIcon = new ImageIcon(ChatClient.class.getResource("/images/ImageGallery.png"));
		Image galleryImg = galleryIcon.getImage();
		Image resizedGalleryImage = galleryImg.getScaledInstance(44, 35, Image.SCALE_SMOOTH);
		ImageIcon resizedGalleryIcon = new ImageIcon(resizedGalleryImage);
		viewSavedImagesButton.setIcon(resizedGalleryIcon);

		viewSavedImagesButton.setFocusPainted(false);
		viewSavedImagesButton.setBorderPainted(false);
		viewSavedImagesButton.setBackground(Color.white);
		viewSavedImagesButton.setFont(new Font("Dialog", Font.BOLD, 18));
		viewSavedImagesButton.setBounds(60, 0, 44, 40); // 위치를 그림판 버튼 오른쪽으로 설정
		panel.add(viewSavedImagesButton);

		viewSavedImagesButton.addActionListener(new ActionListener() {
		    @Override
		    public void actionPerformed(ActionEvent e) {
		        SwingUtilities.invokeLater(() -> {
		        	ImageGallery imageGallery = new ImageGallery(ChatClient.this); // ChatClient를 전달
                    imageGallery.show();
		        });
		    }
		});
		
		sendButton = new JButton();
		sendButton.setFocusPainted(false);
		sendButton.setBorderPainted(false);
		sendButton.setBackground(new Color(255, 255, 255));

		try {
		    URL imageUrl = getClass().getResource("/images/sendButton.png");
		    if (imageUrl != null) {
		        ImageIcon originalIcon2 = new ImageIcon(imageUrl);
		        Image scaledImage = originalIcon2.getImage().getScaledInstance(30, 25, Image.SCALE_SMOOTH);
		        sendButton.setIcon(new ImageIcon(scaledImage));
		    } else {
		        System.out.println("이미지를 찾을 수 없습니다.");
		    }
		} catch (Exception e) {
		    e.printStackTrace();
		    System.out.println("이미지 로드 중 오류 발생");
		}

		sendButton.setBounds(300, 8, 50, 25);
		panel.add(sendButton);

	}

	// 메시지 전송 및 채팅 기록 db에 저장
	private void pressEnter() {
		String enteredText = textPane.getText().trim();

		if (!enteredText.isEmpty()) 
		{
			String msg = String.format("[%s]\n%s\n", userName, enteredText);
			sendMessage(msg);
			
			appendText(msg, true);
			
			DBManager.saveChatHistory(roomName, userName, enteredText);
			textPane.setText("");
			textPane.requestFocus();
			
			// 종료 커맨드
			if (msg.contains("/exit"))
			{
				System.exit(0);
			}
			buttonState();
		}
	}

	// 텍스트 패널이 비어있으면 버튼 비활성화
	private void buttonState() 
	{
		sendButton.setEnabled(!textPane.getText().trim().isEmpty());
	}
	
	// 채팅창에 메시지 추가
	private void appendText(String msg, boolean isOwnMessage) {
	    StyledDocument doc = chatTextArea.getStyledDocument();
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

	    // 타임스탬프 부분을 본문에서 제거하고
	    String contentWithoutTimestamp = removeTimestamp(msg);

	    try {
	        int maxLineWidth = 200; // 최대 줄 너비
	        String[] wrappedLines = wrapMessage(contentWithoutTimestamp, maxLineWidth); // 메시지를 나눔

	        int start = doc.getLength();
	        for (String line : wrappedLines) {
	            doc.insertString(doc.getLength(), line + "\n", attributes); // 한 줄씩 삽입
	        }

	        // 타임스탬프만 별도로 처리하여 삽입
	        String timestamp = extractTimestamp(msg); // 타임스탬프 부분 추출
	        if (timestamp != null) {
	            SimpleAttributeSet timestampAttributes = new SimpleAttributeSet();
	            StyleConstants.setFontSize(timestampAttributes, fontSize); // 글꼴 크기 설정
	            StyleConstants.setBold(timestampAttributes, isBold); // 글꼴 굵기 설정
	            StyleConstants.setForeground(timestampAttributes, Color.GRAY); // 회색 글씨
	            StyleConstants.setBackground(timestampAttributes, Color.WHITE); // 배경 없이

	            // 타임스탬프 삽입
	            doc.insertString(doc.getLength(), timestamp + "\n\n", timestampAttributes);
	        }

	        int end = doc.getLength();
	        doc.setParagraphAttributes(start, end - start, attributes, false);
	        chatTextArea.setCaretPosition(doc.getLength()); // 채팅 창의 끝으로 커서 이동
	    } catch (BadLocationException e) {
	        e.printStackTrace();
	    }
	}

	// 메시지를 최대 너비를 기준으로 줄바꿈 처리
	private String[] wrapMessage(String message, int maxLineWidth) {
	    FontMetrics metrics = chatTextArea.getFontMetrics(chatTextArea.getFont());
	    
	    List<String> lines = new ArrayList<>();
	    StringBuilder currentLine = new StringBuilder();
	    int currentWidth = 0;

	    for (char c : message.toCharArray()) {
	        int charWidth = metrics.charWidth(c); // 현재 문자의 너비 계산

	        if (currentWidth + charWidth > maxLineWidth) {
	            // 현재 줄의 너비가 최대 줄 너비를 초과하면 줄바꿈
	            lines.add(currentLine.toString());
	            currentLine.setLength(0);
	            currentWidth = 0;
	        }

	        currentLine.append(c);
	        currentWidth += charWidth;
	    }

	    // 마지막 줄 추가
	    if (currentLine.length() > 0) {
	        lines.add(currentLine.toString());
	    }

	    return lines.toArray(new String[0]);
	}


	// 타임스탬프를 추출하는 메소드
	private String extractTimestamp(String message) {
	    // 메시지에서 "{"와 "}" 사이의 텍스트를 타임스탬프로 추출
	    int start = message.indexOf("{");
	    int end = message.indexOf("}");

	    if (start != -1 && end != -1 && start < end) {
	        return message.substring(start + 1, end);
	    }
	    return null;  // 타임스탬프가 없다면 null 반환
	}

	// 본문 메시지에서 타임스탬프 부분을 제거하는 메소드
	private String removeTimestamp(String message) {
	    int start = message.indexOf("{");
	    int end = message.indexOf("}");

	    if (start != -1 && end != -1 && start < end) {
	        return message.substring(0, start).trim() + message.substring(end + 1).trim();  // 타임스탬프 제거 후 반환
	    }
	    return message;  // 타임스탬프가 없으면 그대로 반환
	}
	

	private void sendMessage(String msg) 
	{
		try 
		{
			dos.writeUTF(msg);
		} 
		catch (IOException e) 
		{
			appendText("Error sending message", true);
			try 
			{
				dos.close();
				dis.close();
				socket.close();
			} catch (IOException e1) 
			{
				e1.printStackTrace();
				System.exit(0);
			}
		}
	}

	private boolean isEnter(KeyEvent e) 
	{
		return e.getKeyCode() == KeyEvent.VK_ENTER;
	}

	// 서버로부터 메시지 수신
	class MessageReceiver extends Thread {
	    public void run() {
	        while (true) {
	            try {
	                String msg = dis.readUTF();
	                boolean isOwnMessage = msg.contains("[" + userName + "]");

	                if (msg.contains("[이미지]")) {
	                	// 발신자명 및 확장자 추출
	                    String sender = msg.substring(1, msg.indexOf("]")).trim();
	                    String originalFileName = msg.substring(msg.indexOf("[이미지]") + 7).trim();
	                    String fileExtension = originalFileName.substring(originalFileName.lastIndexOf('.'));	                  
	                    
	                    // c:\received_image 디렉토리에 저장
	                    File directory = new File("c:\\received_image");
	                    if (!directory.exists()) {
	                        directory.mkdir();
	                    }
	                    File tempFile = new File(directory, "received_image_" + System.currentTimeMillis() + fileExtension);

	                    // 파일 데이터 수신
	                    try (FileOutputStream fos = new FileOutputStream(tempFile)) {
	                        byte[] buffer = new byte[8192];
	                        int bytesRead;
	                        while (dis.available() > 0 && (bytesRead = dis.read(buffer)) > 0) {
	                            fos.write(buffer, 0, bytesRead);
	                        }
	                    }

	                    // 이미지 표시
	                    addImage(tempFile.getAbsolutePath(), sender);
	                } else {
	                    if (!isOwnMessage) {
	                        appendText(msg, false);
	                    }
	                }
	            } catch (IOException e) {
	                appendText("서버로부터 메시지 읽기 오류", false);
	                try {
	                    dis.close();
	                    socket.close();
	                    break;
	                } catch (Exception ee) {
	                    break;
	                }
	            }
	        }
	    }
	}

	// 파일을 전송하는 메소드
	public void sendFile(File file)
	{
	    try 
	    {
	        // 사용자 이름과 파일 시작 메시지를 전송
	        String msg = String.format("[%s]\n[이미지]%s", userName, file.getName());
	        dos.writeUTF(msg);

	        // 파일 데이터를 전송
	        FileInputStream fis = new FileInputStream(file);
	        byte[] buffer = new byte[8192];
	        int bytesRead;
	        while ((bytesRead = fis.read(buffer)) > 0) 
	        {
	            dos.write(buffer, 0, bytesRead);
	        }
	        fis.close();
	        dos.flush();
	    } 
	    catch (IOException e)
	    {
	        appendText("파일 전송 오류", true);
	    }
	}
	
	// 채팅창에 이미지 표시
	public void addImage(String imagePath, String sender) 
	{
	    try {
	        ImageIcon imageIcon = new ImageIcon(imagePath);
	        Image img = imageIcon.getImage();
	        Image resizedImg = img.getScaledInstance(100, 100, Image.SCALE_SMOOTH);
	        ImageIcon resizedIcon = new ImageIcon(resizedImg);

	        StyledDocument doc = chatTextArea.getStyledDocument();
	        SimpleAttributeSet nameAttributes = new SimpleAttributeSet();
	        SimpleAttributeSet ImageAttributes = new SimpleAttributeSet();
	        SimpleAttributeSet imageAlignment = new SimpleAttributeSet();
	        
	        boolean isOwnMsg = sender.equals(this.userName);    
	        doc.insertString(doc.getLength(), "\n", null);
	        // 이름 속성 설정
	        String msg = String.format("[%s]", sender);
	        if (isOwnMsg)
	        {
	            StyleConstants.setAlignment(nameAttributes, StyleConstants.ALIGN_RIGHT);
	            StyleConstants.setForeground(nameAttributes, Color.white);
	            StyleConstants.setBackground(nameAttributes, new Color(69, 106, 255));
	        } 
	        else 
	        {
	            StyleConstants.setAlignment(nameAttributes, StyleConstants.ALIGN_LEFT);
	            StyleConstants.setForeground(nameAttributes, Color.black);
	            StyleConstants.setBackground(nameAttributes, new Color(223, 229, 255));
	        }
	        StyleConstants.setFontSize(nameAttributes, 13);
	        StyleConstants.setBold(nameAttributes, true);
	        
	        // 이름 표시
	        int nameStart = doc.getLength();
	        doc.insertString(nameStart, msg, nameAttributes);
	        doc.setParagraphAttributes(nameStart, doc.getLength() - nameStart, nameAttributes, false);
	        
	        doc.insertString(doc.getLength(), "\n", null);
	        appendText("\n", false);
	        // 이미지 속성 설정
	        StyleConstants.setIcon(ImageAttributes, resizedIcon);
	        
	        if (isOwnMsg) 
	        {
	            StyleConstants.setAlignment(imageAlignment, StyleConstants.ALIGN_RIGHT);
	        } 
	        else
	        {
	            StyleConstants.setAlignment(imageAlignment, StyleConstants.ALIGN_LEFT);
	        }
	        // 이미지 표시
	        int start = doc.getLength();
	        doc.insertString(start, " ", ImageAttributes);
	        doc.setParagraphAttributes(start, doc.getLength() - start, imageAlignment, false);
	        
	        doc.insertString(doc.getLength(), "\n", null);
	        appendText("\n", false);
	        
	        SwingUtilities.invokeLater(() -> {
	        	chatTextArea.setCaretPosition(doc.getLength());
	        });

	    } catch (BadLocationException e) {
	        e.printStackTrace();
	    }
	}
	
	public void addImageToChat(String imagePath) {
	    try {
	        ImageIcon imageIcon = new ImageIcon(imagePath);
	        Image img = imageIcon.getImage();
	        Image resizedImg = img.getScaledInstance(200, 200, Image.SCALE_SMOOTH);
	        ImageIcon resizedIcon = new ImageIcon(resizedImg);

	        JLabel imageLabel = new JLabel(resizedIcon);
	        // 채팅 영역에 추가
	        StyledDocument doc = chatTextArea.getStyledDocument();
	        SimpleAttributeSet attributes = new SimpleAttributeSet();

	        
	        StyleConstants.setIcon(attributes, resizedIcon);
	        doc.insertString(doc.getLength(), " ", attributes);

	        // 줄바꿈 추가
	        doc.insertString(doc.getLength(), "\n", null);

	     // DB에 이미지 경로 저장
	        //String message = String.format("[%s]\n이미지: %s\n", userName, imagePath);
	        //DBManager.saveChatHistory(roomName, message, imagePath);
	        
	        appendText("\n", false); // 레이아웃 조정을 위해 공백 추가
	    } catch (BadLocationException e) {
	        e.printStackTrace();
	    }
	}


}
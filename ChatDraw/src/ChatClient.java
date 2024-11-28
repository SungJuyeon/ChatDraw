import java.awt.Color;
import java.awt.EventQueue;
import java.awt.Font;
import java.awt.Image;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.Socket;
import java.util.List;

import javax.swing.BorderFactory;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextPane;
import javax.swing.ScrollPaneConstants;
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

		setBounds(100, 100, 373, 675);
		contentPane = new JPanel();

		// GUI 
		topPanel();
		chatPanel();
		textPanel();
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
	private void loadChatHistory(String roomName, String username) 
	{
		List<ChatMessage> chatHistory = DBManager.loadChatHistory(roomName);

		for (ChatMessage chatMessage : chatHistory) 
		{
			String sender = chatMessage.getSender();
			String content = chatMessage.getContent();
			System.out.println(sender);
			System.out.println(content);
			System.out.println(chatMessage.getFormattedTimestamp());

			if (content.contains(".jpg") || content.contains(".png") || content.contains(".jpeg")) {
	            addImageToChat(content);  // 이미지 경로를 처리하여 이미지를 채팅에 표시
	        }
			else {

			boolean isOwnMessage = sender.equals(username);
			
			// 내가 보낸 메시지인지 판별하여 표시
			if (isOwnMessage)
			{
				String formattedMessage = String.format("[%s]\n(%s)  %s\n", sender, chatMessage.getFormattedTimestamp(), content);
				appendText(formattedMessage, isOwnMessage);
			}
			else
			{
				String formattedMessage = String.format("[%s]\n%s  (%s)\n", sender, content, chatMessage.getFormattedTimestamp());
				appendText(formattedMessage, isOwnMessage);
			}

			}
		}

		String connect = String.format("                                 - %s님이 접속하였습니다. -%n", userName);
		appendText(connect, false);
	}

	// 채팅 화면 GUI
	private void topPanel() 
	{
		contentPane.setLayout(null);
		topPanel = new JPanel();
		topPanel.setBounds(0, 0, 360, 70);
		topPanel.setBackground(new Color(255, 255, 255));
		contentPane.add(topPanel);
	}

	private void chatPanel() 
	{
		chatScrollPane = new JScrollPane();
		chatScrollPane.setBounds(0, 70, 360, 437);
		chatScrollPane.setHorizontalScrollBarPolicy(ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER);
		chatScrollPane.setBorder(BorderFactory.createEmptyBorder());
		contentPane.add(chatScrollPane);

		chatTextArea = new JTextPane();
		chatTextArea.setFocusable(false);
		chatScrollPane.setViewportView(chatTextArea);
		chatTextArea.setBackground(new Color(245, 245, 245));

		chatScrollPane.setViewportView(chatTextArea);
		contentPane.add(chatScrollPane);
	}

	private void textPanel() 
	{
		scrollPane = new JScrollPane();
		scrollPane.setBounds(0, 506, 360, 88);
		scrollPane.setBorder(BorderFactory.createEmptyBorder());
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
		
		// 그림판 열기 버튼
		JButton drawingBoardButton = new JButton(""); 
		ImageIcon originalIcon = new ImageIcon(ChatClient.class.getResource("/images/icon_palette.png"));
		Image img = originalIcon.getImage();
		Image resizedImage = img.getScaledInstance(44, 35, Image.SCALE_SMOOTH); // 버튼 크기에 맞게 조정
		ImageIcon resizedIcon = new ImageIcon(resizedImage);
		drawingBoardButton.setIcon(resizedIcon);

		drawingBoardButton.setFocusPainted(false);
		drawingBoardButton.setBorderPainted(false);
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
		
		sendButton = new JButton("전송");
		sendButton.setFocusPainted(false);
		sendButton.setBorderPainted(false);
		sendButton.setBackground(new Color(243, 239, 180));
		sendButton.setFont(new Font("맑은 고딕", Font.BOLD, 11));
		sendButton.setBounds(284, 8, 61, 25);
		panel.add(sendButton);

		sendButton.setEnabled(false);

		sendButton.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent actionEvent) 
			{
				pressEnter();
			}
		});
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
	private void appendText(String msg, boolean isOwnMessage) 
	{
		StyledDocument doc = chatTextArea.getStyledDocument();
		SimpleAttributeSet attributes = new SimpleAttributeSet();

		// 본인이 보낸 메시지인지 확인
		if (isOwnMessage)
		{
			StyleConstants.setAlignment(attributes, StyleConstants.ALIGN_RIGHT);
			StyleConstants.setForeground(attributes, Color.blue);
		}
		else
		{
			StyleConstants.setAlignment(attributes, StyleConstants.ALIGN_LEFT);
			StyleConstants.setForeground(attributes, Color.black);
		}
		
		try
		{
			int start = doc.getLength();
			doc.insertString(doc.getLength(), msg, attributes);
			int end = doc.getLength();
			doc.setParagraphAttributes(start, end - start, attributes, false);
			chatTextArea.setCaretPosition(doc.getLength());
		}
		catch (BadLocationException e)
		{
			e.printStackTrace();
		}
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
		public void run() 
		{
			while (true) 
			{
				try 
				{
					String msg = dis.readUTF();
					// pressEnter에서 이미 출력했으므로 다른 사용자의 메시지만 표시
					boolean isOwnMessage = msg.contains("[" + userName + "]");
					if (!isOwnMessage)
					{
						appendText(msg, false);
					}

				} 
				catch (IOException e) 
				{
					appendText("Error reading from server", false);
					try
					{
						dis.close();
						socket.close();
						break;
					} 
					catch (Exception ee) {
						break;
					}
				}
			}
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
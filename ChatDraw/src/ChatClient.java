import java.awt.Color;
import java.awt.EventQueue;
import java.awt.Font;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.Socket;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.List;

import javax.swing.BorderFactory;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextPane;
import javax.swing.ScrollPaneConstants;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.filechooser.FileNameExtensionFilter;
import javax.swing.text.BadLocationException;
import javax.swing.text.SimpleAttributeSet;
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
		TopPanel();
		chatPanel();
		TextPanel();
		sendButton();

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
			AppendText("connect error");
		}

		loadChatHistory(roomName, userName);
	}

	// 채팅 내역 로드 및 출력
	private void loadChatHistory(String roomName, String username) 
	{
		List<ChatMessage> chatHistory = DBManager.loadChatHistory(roomName);

		for (ChatMessage chatMessage : chatHistory) 
		{
			//String sender = chatMessage.getSender();
			String content = chatMessage.getContent();

			// 메시지 내용에서 발신자 정보를 제외한 텍스트 추출
			Integer closingBracketIndex = content.indexOf("]");
			String extractedText = content.substring(closingBracketIndex + 2);

			// 메시지 출력
			String formattedMessage = String.format("(%s)\n[%s] - %s\n", chatMessage.getFormattedTimestamp(), 
					chatMessage.getSender(), extractedText);
			AppendText(formattedMessage);
		}

		String connect = String.format("                               - %s님이 접속하였습니다. -%n", userName);
		AppendText(connect);
	}

	// db에 채팅방 정보 저장
	public void saveChatRoom(String chatRoomName, String loginName) 
	{
		String sql = "INSERT INTO ChatRooms (chat_name, user_name) VALUES (?, ?)";
		
		try (Connection connection = DBConnector.getInstance().getConnection();
				PreparedStatement pstmt = connection.prepareStatement(sql)) 
		{
			pstmt.setString(1, chatRoomName);
			pstmt.setString(2, loginName);
			pstmt.executeUpdate();
		} 
		catch (SQLException e)
		{
			e.printStackTrace();
		}
	}

	// 채팅 화면 GUI
	private void TopPanel() 
	{
		contentPane.setLayout(null);
		topPanel = new JPanel();
		topPanel.setBounds(0, 0, 360, 70);
		topPanel.setBackground(Color.LIGHT_GRAY);
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
		chatTextArea.setBackground(new Color(240, 240, 240));

		chatScrollPane.setViewportView(chatTextArea);
		contentPane.add(chatScrollPane);
	}

	private void TextPanel() 
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

	private void sendButton() 
	{
		panel = new JPanel();
		panel.setBounds(0, 595, 360, 45);
		panel.setBackground(new Color(255, 255, 255));
		contentPane.add(panel);
		panel.setLayout(null);
		
		// 그림판 열기 버튼
		JButton drawingBoardButton = new JButton(""); 
		drawingBoardButton.setIcon(new ImageIcon(ChatClient.class.getResource("/images/icon_palette.png")));
		drawingBoardButton.setFocusPainted(false);
		drawingBoardButton.setBorderPainted(false);
		drawingBoardButton.setBackground(new Color(243, 239, 180));
		drawingBoardButton.setFont(new Font("Dialog", Font.BOLD, 18));
		drawingBoardButton.setBounds(10, 8, 44, 25);
		panel.add(drawingBoardButton);

		drawingBoardButton.addActionListener(new ActionListener() {
			
			@Override
			public void actionPerformed(ActionEvent e) {
				// TODO Auto-generated method stub
				
			}
		});
	/*	
		JButton sendImageButton = new JButton("");
		
		sendImageButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) 
			{
				JFileChooser fileChooser = new JFileChooser();
				fileChooser.setFileFilter(new FileNameExtensionFilter("Image Files", "jpg", "png", "gif", "jpeg"));
				int result = fileChooser.showOpenDialog(null);
			}
		});
	*/
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
			SendMessage(msg);
			
			DBManager.saveChatHistory(roomName, msg, enteredText);
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
	private void buttonState() {
		sendButton.setEnabled(!textPane.getText().trim().isEmpty());
	}

	// 채팅 텍스트 영역에 메시지 추가 메서드
	private void AppendText(String msg) 
	{
		StyledDocument doc = chatTextArea.getStyledDocument();
		SimpleAttributeSet attributes = new SimpleAttributeSet();

		try
		{
			doc.insertString(doc.getLength(), msg, attributes);
			chatTextArea.setCaretPosition(doc.getLength());
		}
		catch (BadLocationException e)
		{
			e.printStackTrace();
		}
	}

	private void SendMessage(String msg) 
	{
		try 
		{
			dos.writeUTF(msg);
		} 
		catch (IOException e) 
		{
			AppendText("Error sending message");
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
					//서버로부터 메시지 읽기
					String msg = dis.readUTF();
					AppendText(msg);

				} 
				catch (IOException e) 
				{
					AppendText("Error reading from server");
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

}
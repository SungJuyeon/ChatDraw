import java.awt.Color;
import java.awt.EventQueue;
import java.awt.Font;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import javax.swing.BorderFactory;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextPane;

public class ChatList extends JFrame {
	private static final long serialVersionUID = 1L;
	private JPanel contentPane;
	private String loggedinId;
	private String loggedinUserName;

	public static void main(String[] args) 
	{
		EventQueue.invokeLater(new Runnable() {
			public void run() 
			{
				try 
				{
					ChatList frame = new ChatList("", "");
					frame.setVisible(true);
				} 
				catch (Exception e) 
				{
					e.printStackTrace();
				}
			}
		});
	}

	public ChatList(String inputId, String loginName) 
	{
		this.loggedinId = inputId;
		this.loggedinUserName = loginName;
		
		setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
		setBounds(100, 100, 373, 675);
		contentPane = new JPanel();
		contentPane.setLayout(null);
		contentPane.setBackground(new Color(255, 255, 255));

		// GUI
		setSideMenu(inputId);
		setTopMenu(loginName);
		loadChatRooms(loginName);
		setContentPane(contentPane);
	}

	// 현재 사용자를 포함한 모든 사용자 닉네임을 불러오기
	private List<String> loadFriendList(String currentUserEmail) 
	{
	    // 사용자의 닉네임을 담을 리스트 생성
	    List<String> names = new ArrayList<>();
	    String sql = "SELECT name FROM users WHERE loginId != ? OR loginId = ?";
	    try (Connection conn = DBConnector.getInstance().getConnection();
	         PreparedStatement pstmt = conn.prepareStatement(sql)) 
	    {
	        pstmt.setString(1, currentUserEmail);
	        pstmt.setString(2, currentUserEmail);
	        try (ResultSet rs = pstmt.executeQuery()) 
	        {
	            while (rs.next()) 
	            {
	                names.add(rs.getString("name"));
	            }
	        }
	    } 
	    catch (SQLException e) 
	    {
	        e.printStackTrace();
	    }
	    return names;
	}


	// 좌측 메뉴 버튼
	private void setSideMenu(String inputId) 
	{
		JPanel sidePanel = new JPanel();
		sidePanel.setBackground(Color.LIGHT_GRAY);
		sidePanel.setBounds(0, 0, 60, 640);
		contentPane.add(sidePanel);
		sidePanel.setLayout(null);

		JButton viewUserButton = new JButton();
		viewUserButton.setIcon(new ImageIcon(ChatList.class.getResource("/images/icon_users.png")));
		viewUserButton.setFocusPainted(false);
		viewUserButton.setBorderPainted(false);
		viewUserButton.setBackground(Color.LIGHT_GRAY);
		viewUserButton.setBounds(10, 23, 40, 40);
		sidePanel.add(viewUserButton);

		viewUserButton.addActionListener(new ActionListener() 
		{
			public void actionPerformed(ActionEvent e) 
			{
				UserList n = new UserList(inputId);
				n.setVisible(true);
				ChatList.this.dispose();
			}
		});

		JButton chatListButton = new JButton();
		chatListButton.setIcon(new ImageIcon(ChatList.class.getResource("/images/icon_chat.png")));
		chatListButton.setBorderPainted(false);
		chatListButton.setFocusPainted(false);
		chatListButton.setBackground(Color.LIGHT_GRAY);
		chatListButton.setBounds(10, 73, 40, 40);
		sidePanel.add(chatListButton);
	}

	// 상단 채팅+ 버튼
	private void setTopMenu(String loginName) {
		JPanel topPanel = new JPanel();
		topPanel.setBounds(60, 0, 300, 70);
		topPanel.setBackground(new Color(255, 255, 255));
		contentPane.add(topPanel);
		topPanel.setLayout(null);

		JLabel label = new JLabel();
		label.setBounds(12, 23, 73, 33);
		label.setFont(new Font("맑은 고딕", Font.BOLD, 21));
		label.setText("채팅");
		topPanel.add(label);

		JButton addChatButton = new JButton("");
		addChatButton.setIcon(new ImageIcon(ChatList.class.getResource("/images/icon_add_chat.png")));
		addChatButton.setFont(new Font("Dialog", Font.PLAIN, 12));
		addChatButton.setFocusPainted(false);
		addChatButton.setBorder(null);
		addChatButton.setBackground(new Color(255, 255, 255));
		addChatButton.setBounds(248, 23, 40, 40);
		topPanel.add(addChatButton);

		addChatButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) 
			{
				addChatRoom(loginName); // 새로운 채팅방 생성
			}
		});
	}

	// db에 저장된 채팅방 목록 표시하기
	private void loadChatRooms(String loginName)
	{
		int currentY = 74;
		
		// db에서 모든 채팅방 목록 불러오기
		List<String> chatRoomNames = getAllChatRoomNames();
		
		for (String chatRoomName : chatRoomNames) 
		{
			
			String[] names = chatRoomName.split(",");

			System.out.println(Arrays.asList(names));

			// 현재 로그인한 사용자의 이름이 배열에 포함되어 있다면 채팅 패널을 로드하여 화면에 추가
			if (Arrays.asList(names).contains(loginName))
			{
				JPanel loadChatPanel2 = loadChatPanel(chatRoomName, loginName);
				loadChatPanel2.setBounds(62, currentY, 295, 46);
				loadChatPanel2.setVisible(true);
				contentPane.add(loadChatPanel2);
				currentY += 50;
			}
		}
	}
	
	// db에서 모든 채팅방의 이름을 불러오기
	private List<String> getAllChatRoomNames() 
	{
		List<String> chatRoomNames = new ArrayList<>();
		String sql = "SELECT chat_name FROM ChatRooms";
		
		try (Connection conn = DBConnector.getInstance().getConnection();
				PreparedStatement pstmt = conn.prepareStatement(sql);
				ResultSet resultSet = pstmt.executeQuery()) 
		{
			while (resultSet.next()) 
			{
				chatRoomNames.add(resultSet.getString("chat_name"));
			}
		} 
		catch (SQLException e) 
		{
			e.printStackTrace();
		}
		
		return chatRoomNames;
	}
	
	// 채팅방 패널 생성 및 리턴
	private JPanel loadChatPanel(String chatRoomName, String loginName)
	{
		System.out.println(loginName);
		JPanel chatPanel = new JPanel();
		chatPanel.setBackground(new Color(240, 240, 240));
		chatPanel.setLayout(null);

		JTextPane textPane = new JTextPane();
		textPane.setBounds(12, 10, 160, 27);
		textPane.setBackground(new Color(240, 240, 240));
		textPane.setEditable(false);
		textPane.setText(chatRoomName);

		chatPanel.add(textPane);

		JButton openChatButton = new JButton("채팅하기");
		openChatButton.setFont(new Font("맑은 고딕", Font.PLAIN, 10));
		openChatButton.setBackground(new Color(240, 240, 240));
		openChatButton.setBorder(BorderFactory.createLineBorder(new Color(192, 192, 192)));
		openChatButton.setBounds(200, 12, 55, 23);

		chatPanel.add(openChatButton);

		// 버튼 클릭 시 지정된 정보로 ChatClient 열기
		openChatButton.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) 
			{
				openChatClient(chatRoomName, loginName);
			}
		});

		contentPane.revalidate();
		contentPane.repaint();

		return chatPanel;
	}

	// 새로운 채팅방 생성
	private void addChatRoom(String loginName) 
	{
		List<String> users = loadFriendList(loginName);

		// 친구 선택을 위한 체크박스 배열 생성
		JCheckBox[] checkBoxes = new JCheckBox[users.size()];
		for (int i = 0; i < users.size(); i++) 
		{
			checkBoxes[i] = new JCheckBox(users.get(i));
		}

		// 패널에 체크박스 추가
		JPanel panel = new JPanel(new GridLayout(0, 1));
		for (JCheckBox checkBox : checkBoxes) 
		{
			panel.add(checkBox);
		}

		// 스크롤 가능한 패널 생성
		JScrollPane scrollPane = new JScrollPane(panel);

		// 다이얼로그 표시
		int option = JOptionPane.showConfirmDialog(contentPane, scrollPane, "초대하기",
				JOptionPane.OK_CANCEL_OPTION, JOptionPane.PLAIN_MESSAGE);

		if (option == JOptionPane.OK_OPTION) 
		{
			// 선택된 유저의 이름을 저장할 리스트
			List<String> selectedUsers = new ArrayList<>();

			// 선택된 유저 리스트에 추가
			System.out.println("Selected friends:");
			for (JCheckBox checkBox : checkBoxes) 
			{
				if (checkBox.isSelected()) 
				{
					String userName = checkBox.getText();
					selectedUsers.add(userName);
				}
			}
			// 선택된 유저들에게 채팅 패널 추가
			createChatPanel(selectedUsers, loginName);
		}
	}

	// 선택된 유저들에게 채팅 패널 생성 및 표시
	private void createChatPanel(List<String> selectedUsers, String loginName) {
		JPanel userPanel = new JPanel();
		userPanel.setBackground(new Color(240, 240, 240));
		userPanel.setLayout(null);

		StringBuilder textContent = new StringBuilder();
		for (String name : selectedUsers) 
		{
			textContent.append(name).append(",");
		}

		JTextPane textPane = new JTextPane();
		textPane.setBounds(12, 10, 160, 27);
		textPane.setBackground(new Color(240, 240, 240));
		textPane.setEditable(false);
		textPane.setText(textContent.toString());

		userPanel.add(textPane);

		JButton openChatButton = new JButton("채팅하기");
		openChatButton.setFont(new Font("맑은 고딕", Font.PLAIN, 10));
		openChatButton.setBackground(new Color(240, 240, 240));
		openChatButton.setBorder(BorderFactory.createLineBorder(new Color(192, 192, 192)));
		openChatButton.setBounds(200, 12, 55, 23);

		userPanel.add(openChatButton);

		openChatButton.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) 
			{
				openChatClient(textContent.toString(), loginName);
			}
		});

		// db에 채팅방 저장
		saveChatRoom(textContent.toString(), loginName);

		JFrame chatList = new ChatList(loggedinId, this.loggedinUserName);
		chatList.setVisible(true);
		dispose();
		contentPane.revalidate();
		contentPane.repaint();
	}
	
	// 새로운 채팅방을 db에 저장
	private void saveChatRoom(String chatRoomName, String loginName) 
	{
		String sql = "INSERT INTO ChatRooms (chat_name, user_name) VALUES (?, ?)";
		
		try (Connection conn = DBConnector.getInstance().getConnection();
				PreparedStatement pstmt = conn.prepareStatement(sql)) 
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

	// 채팅창 열기
	private void openChatClient(String RoomName, String loginName) 
	{
		// ChatClient 실행 및 필요한 정보 전달
		ChatClient chatClient = new ChatClient(loginName, RoomName);
		chatClient.setVisible(true);
	}
}

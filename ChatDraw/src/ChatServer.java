import java.awt.Color;
import java.awt.EventQueue;
import java.awt.Font;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.Vector;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextPane;
import javax.swing.UIManager;

public class ChatServer extends JFrame {
	private JPanel contentPane;
	private JButton startButton;
	private JScrollPane scrollPane;
	private JTextPane textPane;

	private static final long serialVersionUID = 1L;

	private ServerSocket serverSocket;
	private Socket clientSocket;
	private Vector<UserThread> UserVec = new Vector<UserThread>();

	/**
	 * Launch the application.
	 */
	public static void main(String[] args) {
		EventQueue.invokeLater(new Runnable() {
			public void run() {
				try {
					ChatServer frame = new ChatServer();
					frame.setVisible(true);
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
		});
	}

	/**
	 * Create the frame.
	 */
	public ChatServer() 
	{
		setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		setBounds(100, 100, 380, 498);
		contentPane = new JPanel();

		serverUI();

		startButton.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent actionEvent)
			{
				try 
				{
					serverSocket = new ServerSocket(30000);
				} catch (IOException e) 
				{
					e.printStackTrace();
				}
				//채팅 서버 실행 메시지 출력
				appendText("Chat Server Running..." + "\n");
				//버튼 텍스트 및 활성화 상태 설정
				startButton.setText("Server Running.." + "\n");
				startButton.setEnabled(false);
				
				// AcceptServer 스레드 시작
				AcceptServer acceptServer = new AcceptServer();
				acceptServer.start();
			}
		});
	}

	//스레드 생성
	class AcceptServer extends Thread 
	{
		public void run() 
		{
			appendText("Waiting clients ..." + "\n");
			
			while (true) 
			{
				try 
				{
					clientSocket = serverSocket.accept();
					ChatServer.UserThread newUser = new ChatServer.UserThread(clientSocket);
					UserVec.add(newUser);
					newUser.start();
				} 
				catch (IOException e) 
				{
					appendText("accept error" + "\n");
				}
			}
		}
	}

	class UserThread extends Thread 
	{
		private Socket clientSocket;
		private DataInputStream dis;
		private DataOutputStream dos;
		private Vector<UserThread> user_vc;

		public UserThread(Socket clientSocket) 
		{
			this.clientSocket = clientSocket;
			this.user_vc = UserVec;
			try 
			{
				dis = new DataInputStream(clientSocket.getInputStream());
				dos = new DataOutputStream(clientSocket.getOutputStream());
			} 
			catch (IOException e) 
			{
				appendText("userService error" + "\n");
			}
		}

		//특정 클라이언트에게 메시지 전송 메소드
		public void WriteOne(String msg) 
		{
			try 
			{
				dos.writeUTF(msg);
			} 
			catch (IOException e) 
			{
				appendText("dos.write() error" + "\n");
				try {
					dos.close();
					dis.close();
					clientSocket.close();
				} catch (IOException e1) 
				{
					e1.printStackTrace();
				}
			}
		}
		// 모든 클라이언트에게 메시지 전송
		public void WriteAll(String str)
		{
			for (int i = 0; i < user_vc.size(); i++)
			{
				ChatServer.UserThread user = user_vc.get(i);
				user.WriteOne(str);
			}
		}

		public void run() 
		{
		    while (true) 
		    {
		        try {
		            String msg = dis.readUTF();
		            
		            if (msg.contains("[이미지]")) 
		            {
		                WriteAll(msg + "\n");

		                // 파일 데이터를 수신 및 전송
		                byte[] buffer = new byte[8192]; 
		                int bytesRead;
		                while ((bytesRead = dis.read(buffer)) > 0) 
		                {
		                    for (UserThread user : UserVec) 
		                    {
		                        user.dos.write(buffer, 0, bytesRead);
		                    }
		                }
		            } 
		            else 
		            {
		                WriteAll(msg + "\n");
		            }
		        } 
		        catch (Exception e) 
		        {
		            UserVec.removeElement(this);
		            appendText("UserName " + "퇴장. 현재 참가자 수 " + UserVec.size() + "\n");
		            try 
		            {
		                dos.close();
		                dis.close();
		                clientSocket.close();
		                break;
		            } 
		            catch (Exception ee)
		            {
		                break;
		            }
		        }
		    }
		}
	}

	// gui
	private void serverUI() {
		setContentPane(contentPane);
		contentPane.setLayout(null);
		JPanel panel = new JPanel();
		panel.setBackground(new Color(192, 192, 192));
		panel.setBounds(0, 0, 366, 70);
		contentPane.add(panel);
		panel.setLayout(null);
		
		JLabel lblNewLabel = new JLabel("Server Log");
		lblNewLabel.setFont(new Font("맑은 고딕", Font.BOLD, 16));
		lblNewLabel.setBounds(12, 10, 97, 50);
		panel.add(lblNewLabel);

		scrollPane = new JScrollPane();
		scrollPane.setBorder(BorderFactory.createEmptyBorder());
		scrollPane.setBounds(12, 88, 342, 265);
		contentPane.add(scrollPane);

		textPane = new JTextPane();
		textPane.setFont(new Font("굴림", Font.PLAIN, 16));
		textPane.setEditable(false);
		textPane.setBackground(new Color(255, 255, 255));
		textPane.setBorder(null);
		scrollPane.setViewportView(textPane);
		
		startButton = new JButton("Server Start");
		startButton.setBounds(12, 404, 342, 35);
		contentPane.add(startButton);
		startButton.setFont(new Font("맑은 고딕", Font.BOLD, 13));
		startButton.setFocusPainted(false);
		startButton.setBorderPainted(false);
		startButton.setBackground(UIManager.getColor("Button.highlight"));
	}

	public void appendText(String str) 
	{
		textPane.setText(textPane.getText() + str);
		textPane.setCaretPosition(textPane.getText().length());
	}
}
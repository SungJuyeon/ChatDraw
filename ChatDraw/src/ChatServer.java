import java.awt.Color;
import java.awt.EventQueue;
import java.awt.Font;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.BufferedReader;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.List;
import java.util.Random;
import java.util.Vector;
import java.util.stream.Collectors;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextPane;
import javax.swing.UIManager;
import java.util.HashMap;
import java.util.Map;

public class ChatServer extends JFrame {
	private JPanel contentPane;
	private JButton startButton;
	private JScrollPane scrollPane;
	private JTextPane textPane;
	private static boolean isGameStarted = false;	//게임 상태
	private String currentAnswer = ""; // 현재 정답

	private int questionCount = 0;	//퀴즈 개수
	
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
		private String userName;
		private DataInputStream dis;
		private DataOutputStream dos;
		private Vector<UserThread> user_vc;
		private int score = 0;
		private boolean isAnswerCorrect = false; 
		
		private String correctUserName = ""; // 정답을 맞춘 사용자
		private Map<String, Player> players = new HashMap<>(); // 사용자 상태 관리

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
            // 클라이언트로부터 사용자 이름을 먼저 받음
            try {
				userName = dis.readUTF();
			} catch (IOException e) {
				e.printStackTrace();
			}  // 클라이언트가 전송한 사용자 이름 받기
            String connectMessage = "-" + userName + "님이 접속하였습니다. -"; // 접속 메시지
            WriteAll(connectMessage); // 모든 클라이언트에게 메시지 전송
            appendText("현재 참가자 수: " + UserVec.size() + "\n");
            
            players.putIfAbsent(userName, new Player(false));
		    while (true) 
		    {
		        try {
		            String msg = dis.readUTF();
		            System.out.println("서버에서 받은 msg: " +msg);
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
		                    dos.flush();
		                }
		            } else if (msg.equals("-START_GAME-")) {
		            	if (!isGameStarted) { // 게임이 시작되지 않은 경우에만 시작
		            		isGameStarted = true;
		            		for (UserThread user : UserVec) {
		                        if (!user.userName.equals(userName)) {
		                            user.WriteOne("-START_GAME-");  // 현재 userName 제외한 사용자에게만 전송
		                        }
		                    }
                            startQuiz();
                        } else {
                            WriteOne("- " + userName + "님, 이미 게임이 시작되었습니다. -");
                        }
                        
                    } else if (msg.startsWith("-ANSWER-")) {
                        String userAnswer = msg.substring(8).trim();	//사용자 답안에서 "-ANSWER-" 제외

                        if (userAnswer.contains("]")) {	//사용자 답안에서 "["와"]" 안에 있는 유저 이름 제외
                            int startIndex = userAnswer.indexOf("]") + 1;
                            userAnswer = userAnswer.substring(startIndex).trim();
                        }
                        if (!userAnswer.isEmpty()) {
                            checkAnswer(userName, userAnswer);  // 답을 체크하는 함수 호출
                        } else {
                            WriteOne("- " + userName + "님, 답이 비어 있습니다. 다시 시도하세요. -");
                        }
                    } else {
                        WriteAll(msg);
                    }
		        } 
		        catch (IOException e) 
		        {
		            UserVec.remove(this);
		            appendText(userName + "퇴장. 현재 참가자 수 " + UserVec.size() + "\n");
		            WriteAll("- " + userName + "님이 퇴장하였습니다. -");
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
		//게임 시작
		private synchronized void startQuiz() {
		    isGameStarted = true;
		    questionCount = 0;

		    for (UserThread user : UserVec) {
		        players.putIfAbsent(user.userName, new Player(true)); // Player 객체 초기화
		        Player player = players.get(user.userName);
		        player.score = 0; // 점수 초기화
		    }
		    nextQuestion();	//문제 출제
		}

		private void nextQuestion() {
			correctUserName = null;
			
		    if (questionCount < 3) {
		        questionCount++;
		        currentAnswer = getRandomSentence();
		        WriteAll("- 문제 " + questionCount + " -\n" + currentAnswer + " \n");
		    } else {
		        WriteAll("- 퀴즈 종료! -\n");
		        showRanking();
		        isGameStarted = false;
		    }
		}

        private String getRandomSentence() {
            try {
                BufferedReader reader = new BufferedReader(new FileReader("src/words.txt"));
                List<String> sentences = reader.lines().collect(Collectors.toList());
                reader.close();
                String sentence = sentences.get(new Random().nextInt(sentences.size()));
                // 해당 문장을 출력해 확인
                System.out.println("정답 문장: " + sentence); // 디버그용 출력
                return sentence;
            } catch (IOException e) {
                e.printStackTrace();
                return "ERROR";
            }
        }

        private synchronized void checkAnswer(String userName, String userAnswer) {
            Player player = players.get(userName);
            correctUserName = null;
            // 게임이 시작되었는지 체크
            if (!isGameStarted) {
                // 게임이 시작되지 않았으면, 정답을 맞춰도 아무 일도 일어나지 않음
                WriteOne(userName + "님, 게임이 시작되지 않았습니다.");
                return;
            }
            
            if (userAnswer.trim().equalsIgnoreCase(currentAnswer.trim())) {
                // 첫 번째로 정답을 맞춘 사용자
                if (correctUserName == null) {
                    // 첫 번째 정답자 처리
                    correctUserName = userName;  // 첫 번째 정답자 저장
                    player.score++;  // 첫 번째 정답자 점수 증가
                    WriteAll("- " +userName + "님이 정답을 맞췄습니다! -");
                } 
                // 모든 사용자에게 다음 문제로 넘어가기
                nextQuestion();
            } else {
                // 틀린 사용자에게만 개별적으로 알림
                WriteOne("- " + userName + "님, 틀렸습니다. 다시 시도하세요! -");
            }
        }


        private void showRanking() {
            // 랭킹 계산
            StringBuilder rankingMessage = new StringBuilder("----- 랭킹 ------\n");
            
            // UserVec를 점수 내림차순으로 정렬
            UserVec.sort((u1, u2) -> Integer.compare(u2.players.get(u2.userName).score, u1.players.get(u1.userName).score)); 
            
            // 랭킹 출력
            for (int i = 0; i < UserVec.size(); i++) {
                UserThread user = UserVec.get(i);
                int rank = i + 1;
                int userScore = user.players.get(user.userName).score;
                rankingMessage.append(rank + ". " + user.userName + ": " + userScore + "점\n");
            }

            WriteAll(rankingMessage.toString());
            WriteAll("- 채팅모드로 돌아갑니다. -");
            // 게임이 종료된 후, 모든 사용자의 상태를 초기화
            for (UserThread user : UserVec) {
                user.isAnswerCorrect = false;
                user.players.get(user.userName).score = 0;  
            }
        }


        private static class Player {
            boolean hasStartedGame;
            int score;

            Player(boolean hasStartedGame) {
                this.hasStartedGame = hasStartedGame;
                this.score = 0;
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
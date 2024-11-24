import java.awt.Color;
import java.awt.EventQueue;
import java.awt.Graphics;
import java.awt.Image;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JPasswordField;
import javax.swing.JTextField;
import javax.swing.SwingConstants;
import javax.swing.UIManager;

public class RegisterUI extends JFrame {

    private JTextField idField;
    private JPasswordField passwordField;
    private JTextField nameField;
    private JLabel idLabel;
    private JLabel passwordLabel;
    private JLabel nameLabel;
    private JLabel alertLabel;
    
    private static final long serialVersionUID = 1L;
    private JPanel contentPane;

    public static void main(String[] args) {
        EventQueue.invokeLater(new Runnable() {
            public void run() 
            {
                try 
                {
                    RegisterUI frame = new RegisterUI();
                    frame.setVisible(true);
                } 
                catch (Exception e) 
                {
                    e.printStackTrace();
                }
            }
        });
    }

    public RegisterUI() {
        setTitle("회원가입");
        setSize(373, 675);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setBounds(100, 100, 373, 675);
        contentPane = new JPanel() {
			private static final long serialVersionUID = 1L;

			@Override
            protected void paintComponent(Graphics g) 
			{
                super.paintComponent(g);
                ImageIcon icon = new ImageIcon("src/images/logo.png");
                Image image = icon.getImage();

                int imageWidth = 200;
                int imageHeight = 200;
                Image scaledImage = image.getScaledInstance(imageWidth, imageHeight, Image.SCALE_SMOOTH);
                ImageIcon scaledIcon = new ImageIcon(scaledImage);

                int x = (getWidth() - imageWidth) / 2;
                int y = 50;
                g.drawImage(scaledIcon.getImage(), x, y, this);
            }
        };
        contentPane.setBackground(UIManager.getColor("window"));
        setContentPane(contentPane);
        contentPane.setLayout(null);

        idField = new JTextField();
        idField.setBounds(114, 402, 132, 28);
        contentPane.add(idField);
        idField.setColumns(10);

        nameField = new JTextField();
        nameField.setBounds(114, 478, 132, 28);
        contentPane.add(nameField);
        nameField.setColumns(10);

        passwordField = new JPasswordField();
        passwordField.setBounds(114, 440, 132, 28);
        contentPane.add(passwordField);

        JButton joinButton = new JButton("회원가입");
        joinButton.setBackground(new Color(255, 255, 255));
        joinButton.setFocusPainted(false);
        joinButton.setBounds(114, 538, 132, 28);
        contentPane.add(joinButton);
        
        idLabel = new JLabel("아이디");
        idLabel.setBounds(50, 405, 51, 21);
        idLabel.setForeground(new Color(0, 0, 0));
        contentPane.add(idLabel);
        
        nameLabel = new JLabel("이름");
        nameLabel.setBounds(50, 481, 52, 21);
        nameLabel.setForeground(new Color(0, 0, 0));
        contentPane.add(nameLabel);
        
        passwordLabel = new JLabel("비밀번호");
        passwordLabel.setBounds(50, 443, 52, 21);
        passwordLabel.setForeground(new Color(0, 0, 0));
        contentPane.add(passwordLabel);

        joinButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) 
            {
            	if(register()) // 회원가입 성공 시
            	{
            		JFrame loginUI = new LoginUI();
                    loginUI.setVisible(true);
                    dispose();
            	}
            }
        });
        
        alertLabel = new JLabel("");
        alertLabel.setHorizontalAlignment(SwingConstants.CENTER);
        alertLabel.setForeground(Color.RED);
        alertLabel.setBounds(50, 516, 261, 15);
        contentPane.add(alertLabel);
    }

    private boolean register() 
    {
        String inputId = idField.getText();
        char[] inputPassword = passwordField.getPassword();
        String password = new String(inputPassword);
        String inputName = nameField.getText();
        
        if (!userExists(inputId)) // 중복된 id가 없으면
        { 
            saveUserToDatabase(inputId, password, inputName);
            System.out.println("register successful : " + inputId);
            return true;
        } 
        else 
        {
            System.out.println("Email already exists");
            alertLabel.setText("이미 가입된 아이디입니다.");
            return false;
        }
    }

    private boolean userExists(String loginId) 
    {
        try (Connection conn = DBConnector.getInstance().getConnection()) 
        {
            String sql = "SELECT * FROM users WHERE loginId = ?";
            try (PreparedStatement pstmt = conn.prepareStatement(sql)) 
            {
                pstmt.setString(1, loginId);
                try (ResultSet rs = pstmt.executeQuery()) 
                {
                    return rs.next();
                }
            }
        } 
        catch (SQLException e) 
        {
            e.printStackTrace();
        }
        return false;
    }

    // 데이터베이스에 가입된 정보를 저장하는 함수
    private void saveUserToDatabase(String email, String password, String name) 
    {
        try (Connection conn = DBConnector.getInstance().getConnection()) 
        {
            String sql = "INSERT INTO users (loginId, password, name) VALUES (?, ?, ?)";
            try (PreparedStatement pstmt = conn.prepareStatement(sql)) 
            {
                pstmt.setString(1, email);
                pstmt.setString(2, password);
                pstmt.setString(3, name);
                pstmt.executeUpdate();
            }
        } 
        catch (SQLException e) 
        {
            e.printStackTrace();
        }
    }
}

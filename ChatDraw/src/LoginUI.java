import java.awt.Color;
import java.awt.EventQueue;
import java.awt.Graphics;
import java.awt.Image;
import java.awt.SystemColor;
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

public class LoginUI extends JFrame { 
	private static final long serialVersionUID = 1L;
	private JTextField idField;
    private JPasswordField passwordField;

    public static void main(String[] args) {
        EventQueue.invokeLater(new Runnable() 
        {
            public void run() 
            {
                try 
                {
                    LoginUI frame = new LoginUI();
                    frame.setVisible(true);
                } 
                catch (Exception e) 
                {
                    e.printStackTrace();
                }
            }
        });
    }

    public LoginUI() 
    {
        setTitle("");
        setSize(373, 675);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(373, 675);
        JPanel contentPane = new JPanel() {
			private static final long serialVersionUID = 1L;

			@Override
            protected void paintComponent(Graphics g) 
			{
                super.paintComponent(g);
                ImageIcon icon = new ImageIcon("src/images/logo.png");
                Image logo = icon.getImage();

                // 이미지 크기 조절
                int imageWidth = 200;
                int imageHeight = 200;
                
                Image scaledLogo = logo.getScaledInstance(imageWidth, imageHeight, Image.SCALE_SMOOTH);
                ImageIcon scaledIcon = new ImageIcon(scaledLogo);

                int x = (getWidth() - imageWidth) / 2;
                int y = 50;
                g.drawImage(scaledIcon.getImage(), x, y, this);
            }
        };
        contentPane.setBackground(SystemColor.window);
        setContentPane(contentPane);
        contentPane.setLayout(null);

        idField = new JTextField();
        idField.setBounds(114, 400, 133, 28); 
        contentPane.add(idField);
        idField.setColumns(10);

        passwordField = new JPasswordField();
        passwordField.setBounds(114, 440, 132, 28); 
        contentPane.add(passwordField);

        JButton loginButton = new JButton("로그인");
        loginButton.setBackground(new Color(255, 255, 255));
        loginButton.setFocusPainted(false);
        loginButton.setBounds(258, 440, 87, 28); 
        contentPane.add(loginButton);

        JButton registerButton = new JButton("회원가입");
        registerButton.setBackground(new Color(255, 255, 255));
        registerButton.setFocusPainted(false);
        registerButton.setBounds(114, 515, 132, 28); 
        contentPane.add(registerButton);
        
        JLabel idLabel = new JLabel("아이디");
        idLabel.setForeground(Color.BLACK);
        idLabel.setBounds(58, 403, 44, 21);
        contentPane.add(idLabel);
        
        JLabel passwordLabel = new JLabel("비밀번호");
        passwordLabel.setForeground(Color.BLACK);
        passwordLabel.setBounds(57, 443, 50, 21);
        contentPane.add(passwordLabel);

        // 로그인 버튼 클릭
        loginButton.addActionListener(new ActionListener() { 
            @Override
            public void actionPerformed(ActionEvent e)
            {
                login();
            }
        });
        
        // 회원가입 버튼 클릭
        registerButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) 
            {
                openRegisterUI();
            }
        });
    }

    private void login() 
    { 
        String inputId = idField.getText();
        char[] inputPassword = passwordField.getPassword();
        String password = new String(inputPassword);

        if (isUserValid(inputId, password))
        {
            System.out.println("login successful : " + inputId);
            EventQueue.invokeLater(new Runnable() 
            {
                public void run() 
                {
                    try 
                    {
                        UserList frame = new UserList(inputId); 
                        frame.setVisible(true);
                        dispose();
                    } 
                    catch (Exception e) 
                    {
                        e.printStackTrace();
                    }
                }
            });
        } 
        else 
        {
            System.out.println("Invalid id or password");
        }
    }

    private void openRegisterUI() 
    {
        EventQueue.invokeLater(new Runnable() {
            public void run() 
            {
                try 
                {
                    RegisterUI joinFrame = new RegisterUI();
                    dispose();
                    joinFrame.setVisible(true);
                } 
                catch (Exception e) 
                {
                    e.printStackTrace();
                }
            }
        });
    }

    // 데이터베이스와 비교
    private boolean isUserValid(String loginId, String password) 
    { 
        try (Connection conn = DBConnector.getInstance().getConnection())
        {
            String sql = "SELECT * FROM users WHERE loginId = ? AND password = ?";
            
            try (PreparedStatement pstmt = conn.prepareStatement(sql)) 
            {
                pstmt.setString(1, loginId);
                pstmt.setString(2, password);

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
}

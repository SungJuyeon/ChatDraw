import java.awt.Color;
import java.awt.Font;
import java.awt.Image;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;

public class UserList extends JFrame {

    private static final long serialVersionUID = 1L;
    private JPanel contentPane;
    private String loggedInId;

    public UserList(String inputId) 
    {
        this.loggedInId = inputId;
        
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        setBounds(100, 100, 373, 675);
        contentPane = new JPanel();
        contentPane.setBackground(new Color(255, 255, 255));
        contentPane.setLayout(null);
        
        // GUI
        setSideMenu(inputId);
        loadUserList(inputId);
        setLabel();
        setContentPane(contentPane);
    }
    
    // 유저, 채팅 목록 이동 버튼
    private void setSideMenu(String inputId)
    {
        JPanel sidePanel = new JPanel();
        sidePanel.setBackground(Color.LIGHT_GRAY);
        sidePanel.setLayout(null);
        sidePanel.setBounds(0, 0, 60, 640);
        contentPane.add(sidePanel);
        
        JButton viewUserButton = new JButton();
        viewUserButton.setIcon(new ImageIcon(UserList.class.getResource("/images/icon_users.png")));
        viewUserButton.setFocusPainted(false);
        viewUserButton.setBorderPainted(false);
        viewUserButton.setBackground(Color.LIGHT_GRAY);
        viewUserButton.setBounds(10, 23, 40, 40);
        sidePanel.add(viewUserButton);
        
        JButton chatListButton = new JButton(); 
        chatListButton.setIcon(new ImageIcon(UserList.class.getResource("/images/icon_chat.png")));
        chatListButton.setFocusPainted(false);
        chatListButton.setBorderPainted(false);
        chatListButton.setBackground(Color.LIGHT_GRAY);
        chatListButton.setBounds(10, 73, 40, 40);
        sidePanel.add(chatListButton);
        
        // 채팅방 아이콘 클릭 시 채팅방 목록 오픈
        chatListButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) 
            {
            	ChatList list = new ChatList(inputId, loadMyName(inputId));
            	list.setVisible(true);
            	dispose(); 
            }
        });
    }
    
    private void setLabel()
    {
        JLabel me = new JLabel();
        me.setText("나");
        me.setFont(new Font("맑은 고딕", Font.BOLD, 21));
        me.setBounds(72, 25, 28, 33);
        contentPane.add(me);
        
        JLabel other = new JLabel();
        other.setText("유저 목록");
        other.setFont(new Font("맑은 고딕", Font.BOLD, 21));
        other.setBounds(71, 125, 97, 33);
        contentPane.add(other);
    }
    
    // db에 저장된 유저 목록 표시하기
    private void loadUserList(String inputId)
    {
        int currentY = 167;
        
        JPanel myPanel = createUserPanel(loadImagePath(loadMyName(inputId)), loadMyName(inputId));
        myPanel.setBounds(61, 63, 296, 46);
        contentPane.add(myPanel);

        List<String> userNames = loadUserNames();
        // db에 저장된 유저 패널 생성
        for (String userName : userNames) 
        { 
            JPanel userPanel = createUserPanel(loadImagePath(userName), userName);
            userPanel.setBounds(61, currentY, 296, 46);
            contentPane.add(userPanel);
            currentY += 50;
        }
    }
    
    private JPanel createUserPanel(String imagePath, String name) 
    {
        JPanel dividePanel = new JPanel();
        dividePanel.setBackground(new Color(240, 240, 240));

        JLabel myName = new JLabel();
        myName.setBounds(56, 10, 72, 27);
        myName.setBackground(new Color(240, 240, 240));
        myName.setText(name);

        JLabel iconLabel = new JLabel(new ImageIcon(new ImageIcon(imagePath).getImage().getScaledInstance(41, 30, Image.SCALE_DEFAULT)));
        iconLabel.setBounds(3, 7, 41, 30);
        
        dividePanel.setLayout(null);
        dividePanel.add(myName);
        dividePanel.add(iconLabel);

        return dividePanel;
    }

    private String loadImagePath(String name)
    {
        String sql = "SELECT image_path FROM users WHERE name = ?";
        
        try (Connection conn = DBConnector.getInstance().getConnection();
        		PreparedStatement pstmt = conn.prepareStatement(sql)) 
        {
            pstmt.setString(1, name);
            try (ResultSet rs = pstmt.executeQuery()) 
            {
                if (rs.next()) 
                {
                    return rs.getString("image_path");
                }
            }
        } 
        catch (SQLException e) 
        {
            e.printStackTrace();
        }
        return "";
    }


    private String loadMyName(String inputId) 
    { 
        String sql = "SELECT name FROM users WHERE loginId = ?";
        
        try (Connection conn = DBConnector.getInstance().getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) 
        {
            pstmt.setString(1, inputId);
            
            try (ResultSet rs = pstmt.executeQuery()) 
            {
                if (rs.next()) 
                {
                    return rs.getString("name");
                }
            }
        }
        catch (SQLException e) 
        {
            e.printStackTrace();
        }
        return "";
    }

    // 본인 제외 데이터베이스에 저장된 유저 이름 불러오기
    private List<String> loadUserNames() 
    { 
        List<String> names = new ArrayList<>();
        String sql = "SELECT name FROM users WHERE loginId != ?";
        
        try (Connection conn = DBConnector.getInstance().getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) 
        {
            pstmt.setString(1, loggedInId);
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


}

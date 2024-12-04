import javax.swing.*;
import java.awt.Color;
import java.awt.Image;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

public class SideMenu extends JPanel {
    private String inputId;
    private String loginName;
    private JFrame parentFrame;

    public SideMenu(JFrame parentFrame, String inputId, String loginName) {
        this.parentFrame = parentFrame; 
        this.inputId = inputId;
        this.loginName = loginName;

        // SideMenu 구성
        setLayout(null);
        setBackground(new Color(110, 144, 255));

        // 사용자 버튼
        JButton viewUserButton = createButton("/images/icon_users.png", 10, 23);
        add(viewUserButton);

        // 채팅방 버튼
        JButton chatListButton = createButton("/images/icon_chat.png", 10, 73);
        add(chatListButton);

        // 뉴스봇 버튼
        JButton newsbotButton = createButton("/images/newsbot.png", 10, 123);
        add(newsbotButton);

        // ActionListener 연결
        viewUserButton.addActionListener(new ViewUserActionListener(inputId));
        chatListButton.addActionListener(new ChatListActionListener(inputId, loginName));
        newsbotButton.addActionListener(new NewsBotActionListener(inputId, loginName));
    }

    private JButton createButton(String iconPath, int x, int y) {
        JButton button = new JButton();
        ImageIcon originalIcon = new ImageIcon(getClass().getResource(iconPath));
        Image scaledImage = originalIcon.getImage().getScaledInstance(40, 40, Image.SCALE_SMOOTH);
        ImageIcon scaledIcon = new ImageIcon(scaledImage);

        button.setIcon(scaledIcon);
        button.setFocusPainted(false);
        button.setBorderPainted(false);
        button.setBackground(new Color(110, 144, 255));
        button.setBounds(x, y, 40, 40);

        return button;
    }

    // ViewUser 버튼 클릭 시 동작하는 리스너
    private class ViewUserActionListener implements ActionListener {
        private String inputId;

        public ViewUserActionListener(String inputId) {
            this.inputId = inputId;
        }

        @Override
        public void actionPerformed(ActionEvent e) {
            UserList userList = new UserList(inputId);
            userList.setVisible(true);
            parentFrame.dispose();  
        }
    }

    // ChatList 버튼 클릭 시 동작하는 리스너
    private class ChatListActionListener implements ActionListener {
        private String inputId;
        private String loginName;

        public ChatListActionListener(String inputId, String loginName) {
            this.inputId = inputId;
            this.loginName = loginName;
        }

        @Override
        public void actionPerformed(ActionEvent e) {
            ChatList chatList = new ChatList(inputId, loginName);
            chatList.setVisible(true);
            parentFrame.dispose();  
        }
    }

    // NewsBot 버튼 클릭 시 동작하는 리스너
    private class NewsBotActionListener implements ActionListener {
        private String inputId;
        private String loginName;

        public NewsBotActionListener(String inputId, String loginName) {
            this.inputId = inputId;
            this.loginName = loginName;
        }

        @Override
        public void actionPerformed(ActionEvent e) {
            NewsBot newsBot = new NewsBot(inputId, loginName);
            newsBot.setVisible(true);
        }
    }
}

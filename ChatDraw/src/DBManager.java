import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class DBManager {

    // 메시지를 데이터베이스에 저장
    public static void saveChatHistory(String roomID, String sender, String message) 
    {
    	
        try (Connection conn = DBConnector.getInstance().getConnection()) 
        {
        	PreparedStatement pstmt = conn.prepareStatement("INSERT INTO chatmessages (roomID, sender, content, timestamp) VALUES (?, ?, ?, ?)"); 

            pstmt.setString(1, roomID);
            pstmt.setString(2, sender);
            pstmt.setString(3, message);
            pstmt.setString(4, getCurrentTimestamp());

            pstmt.executeUpdate();

        } 
        catch (SQLException e) 
        {
            e.printStackTrace();
        }
    }

    private static String getCurrentTimestamp() 
    {
        SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        return formatter.format(new Date());
    }
    
    // 채팅 기록을 데이터베이스에서 불러오는 메소드
    public static List<ChatMessage> loadChatHistory(String roomName)
    {
        List<ChatMessage> chatHistory = new ArrayList<>();
        // 지정된 방의 채팅 기록을 시간 순으로
        String sql = "SELECT sender, content, timestamp FROM ChatMessages WHERE RoomId = ? ORDER BY timestamp ASC";

        try (Connection conn = DBConnector.getInstance().getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) 
        {
            pstmt.setString(1, roomName);

            try (ResultSet rs = pstmt.executeQuery())
            {
                while (rs.next()) 
                {
                    String sender = rs.getString("Sender");
                    String content = rs.getString("content");
                    long timestamp = rs.getTimestamp("timestamp").getTime();

                    ChatMessage chatMessage = new ChatMessage(sender, content, timestamp);
                    chatHistory.add(chatMessage);
                }
            }
        } 
        catch (SQLException e) 
        {
            e.printStackTrace();
        }

        return chatHistory;
    }
}


import java.text.SimpleDateFormat;
import java.util.Date;

// 메시지의 정보를 담는 클래스
public class ChatMessage {
    private String sender;
    private String content;
    private long timestamp;

    public ChatMessage(String sender, String messageText, long timestamp) 
    {
        this.sender = sender;
        this.content = messageText;
        this.timestamp = timestamp;
    }

    public String getSender() 
    {
        return sender;
    }

    public String getContent()
    {
        return content;
    }

    public long getTimestamp() 
    {
        return timestamp;
    }

    public String getFormattedTimestamp() 
    {
        SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        return formatter.format(new Date(timestamp));
    }
}

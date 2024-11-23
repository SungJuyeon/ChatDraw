import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

// 드라이버 초기화 및 커넥션 반환
public class DBConnector {
    private static DBConnector instance;
    private Connection connection;
    private String url = "jdbc:mysql://localhost:3306/Chat";
    private String username = "root";
    private String password = "1234";

    private DBConnector() throws SQLException
    {
        try 
        {
            Class.forName("com.mysql.cj.jdbc.Driver");
            this.connection = DriverManager.getConnection(url, username, password);
        } 
        catch (ClassNotFoundException ex) 
        {
            ex.printStackTrace();
        }
    }

    public Connection getConnection() 
    {
        return connection;
    }
    
    // instance가 null이거나 connection이 닫혀있으면 새로운 인스턴스 생성 후 리턴 (싱글톤)
    public static DBConnector getInstance() throws SQLException 
    {
        if (instance == null) 
        {
            instance = new DBConnector();
        } 
        else if (instance.getConnection().isClosed()) 
        {
            instance = new DBConnector();
        }

        return instance;
    }

}

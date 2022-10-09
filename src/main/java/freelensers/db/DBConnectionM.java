package freelensers.db;
import java.sql.*;


public class DBConnectionM {
    public static Connection getConnection() throws SQLException{
        Connection conex = null;
        try {
            Class.forName("com.mysql.cj.jdbc.Driver");
            String serverName = "freelenser.mysql.database.azure.com";    
            String user = "freelenserapp";
            String password = "strongpassword!";
            String dbname = "freelenser";
            System.out.println("Connecting to MySQL Server: " + serverName);
            System.out.println("Connecting to MySQL Database: " + dbname);
            System.out.println("Connecting with user: " + user);
            
            conex = DriverManager.getConnection("jdbc:mysql://" + serverName + ":3306/" + dbname, user, password);
            
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        }
        return conex;
    }
    
    public static void closeConnection(Connection conex) throws SQLException{
        conex.close();
    }
}
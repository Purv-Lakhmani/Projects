import java.sql.*;

public class ConnectionDB {
    static Connection con;
    public static void connect() throws Exception {
        Class.forName("com.mysql.cj.jdbc.Driver");
        con = DriverManager.getConnection("jdbc:mysql://localhost:3306/StockManagementSystem", "root", "");
        System.out.println((con!=null)?"Successfully connected to the database!":"Failed to connect to the database!");
    }
}
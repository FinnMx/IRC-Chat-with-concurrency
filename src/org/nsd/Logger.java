package org.nsd;
import java.sql.*;

public class Logger {
    private Connection connection;
    private Statement statement;

    // Constructor for our Logger object, initializes the connection to db and our statement obj that has multiple calls
    public Logger(String db) {
        try {
            this.connection = DriverManager.getConnection("jdbc:sqlite:src\\org\\nsd\\" + db);
            this.statement = connection.createStatement();
            statement.setQueryTimeout(15);
        } catch (SQLException e) {
            System.out.println("ERROR: Database connection cannot be established!");
        }
        System.out.println("Database connection established!");
    }

    public void close() {
        try {
            connection.close();
        } catch (SQLException e) {
            System.out.println("This language makes so little sense that closing a connection can return an error!");
        }
    }

    public void write(String channel, String message) {
        try {
            statement.executeUpdate("UPDATE Channels SET Messagelog = '" + message + "' WHERE Channel = '" + channel + "'");
        } catch (SQLException e) {
            System.out.println(e.getMessage());
        }
    }

    public String load(String channel){
        try{
            ResultSet rs = statement.executeQuery("SELECT Messagelog FROM Channels WHERE Channel = '" + channel + "'");
            return rs.getString("Messagelog");
        }catch (SQLException e){
            System.out.println(e.getMessage());
        }
        return null;
    }
}
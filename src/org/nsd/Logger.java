package org.nsd;
import java.sql.*;
import java.util.ArrayList;

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
            statement.executeUpdate("UPDATE Channels SET Messagelog = Messagelog || char(10) ||'" + message + "' WHERE Channel = '" + channel + "'");
        } catch (SQLException e) {
            System.out.println(e.getMessage());
        }
    }

    public String load(String channel){
        try{
            ResultSet rs = statement.executeQuery("SELECT Messagelog FROM Channels WHERE Channel = '" + channel + "'");
            return rs.getString("Messagelog").trim();
        }catch (SQLException e){
            System.out.println(e.getMessage());
        }
        return null;
    }

    public ArrayList<String> loadAllChannels(){
        ArrayList<String> channels = new ArrayList<>();
        try{
            ResultSet rs = statement.executeQuery("SELECT Channel FROM Channels");
            while(rs.next()){
                channels.add(rs.getString("Channel"));
            }
        }catch (SQLException e){
            System.out.println(e.getMessage());
        }
        return channels;
    }

    public void deleteChannel(String channel){
        try {
            statement.executeUpdate("DELETE FROM Channels WHERE Channel = '" + channel + "'");
        } catch (SQLException e) {
            System.out.println(e.getMessage());
        }
    }

    public void writeChannel(String channel) {
        {
            try {
                statement.executeUpdate("INSERT INTO Channels VALUES('" + channel + "',' ')");
            } catch (SQLException e) {
                System.out.println(e.getMessage());
            }
        }
    }

}
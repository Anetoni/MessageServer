package com.server;

import java.io.File;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

import org.json.JSONObject;

public class MessageDatabase {

    private Connection dbConnection = null;
    private static MessageDatabase msgDb = null;

    private MessageDatabase() {
        
    }

    public static synchronized MessageDatabase getInstance() {
        if(null == msgDb) {
            msgDb = new MessageDatabase();
        }
        return msgDb;
    }   

    public void open(String dbName) throws SQLException {
        String filePath = "C:\\Documents\\database\\db\\"+dbName+".db";
        File path = new File(filePath);
        if(path.exists() && !path.isDirectory()) {
            String dbPath = ("jbdc:sqlite:"+filePath);
            dbConnection = DriverManager.getConnection(dbPath);
        }else {
            initializeDatabase(dbConnection);
        }
    }

    private boolean initializeDatabase(Connection dbConnection) throws SQLException {
        System.out.println(dbConnection);
        if(null != dbConnection) {
            String createUserTable = "create table users (username varchar(50) NOT NULL, password varchar(50) NOT NULL, email varchar(50), primary key(username))";
            String createMessageTable = "create table messages (nickname varchar(50) NOT NULL, dangertype varchar(50) NOT NULL, longitude double(2, 15) NOT NULL, latitude double(2, 15) NOT NULL, sent int)";
            Statement createStatement = dbConnection.createStatement();
            createStatement.executeUpdate(createUserTable);
            createStatement.executeUpdate(createMessageTable);
            createStatement.close();
            System.out.println("Database created successfully");
            return true;
        }
        System.out.println("Database creation failed");
        return false;
    }

    public void closeDB() throws SQLException {
        if(null != dbConnection) {
            dbConnection.close();
            System.out.println("Closing connection to database");
            dbConnection = null;
        }
    }

    public boolean setUser(JSONObject user) throws SQLException {
        if(checkIfUserExists(user.getString("username"))) {
            return false;
        }
        String setUser = "insert into users " + "VALUES('"+user.getString("username") + "','" + user.getString("password") + "','" + user.getString("email") + "')";
        Statement createStatement = dbConnection.createStatement();
        createStatement.executeUpdate(setUser);
        createStatement.close();

        return true;
    }

    public boolean checkIfUserExists(String user) throws SQLException {
        Statement query = null;
        ResultSet rs;

        String checkUser = "select username from users where username = '" + user + "'";
        System.out.println("Checking if user exists");

        query = dbConnection.createStatement();
        rs = query.executeQuery(checkUser);

        if(rs.next()) {
            System.out.println("User already exists in the database");
            return true;
        }else {
            return false;
        }
    }

    public boolean authenticateUser(String username, String password) throws SQLException {
        Statement query = null;
        ResultSet rs;

        String getMessage = "select username, password from users where username = '" + username + "'";
        System.out.println(username);

        query = dbConnection.createStatement();
        rs = query.executeQuery(getMessage);

        if(rs.next() == false) {
            System.out.println("Cannot find user");
            return false;
        }else {
            String pass = rs.getString("password");
            if(pass.equals(password)) {
                return true;
            } else {
                return false;
            }
        }
    }

    public void setMessage(WarningMessage msg) throws SQLException {
        String setMsg = "Insert into messages " + "VALUES('"+msg.getNickname() + "','" + msg.getLongitude() + "','" + msg.getLatitude() + "','" + msg.dateAsInt() + "','" + msg.getDangertype() + "')";
        Statement createStatement = dbConnection.createStatement();
        createStatement.executeUpdate(setMsg);
        createStatement.close();
    }

    public JSONObject getMessages() throws SQLException {
        Statement queryStatement = null;
        JSONObject obj = new JSONObject();
        String getMessagesStr = "select * from messages";
        
        queryStatement = dbConnection.createStatement();
        ResultSet rs = queryStatement.executeQuery(getMessagesStr);

        return obj;
    }

}
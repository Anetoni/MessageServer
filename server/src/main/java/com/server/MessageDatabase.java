package com.server;

import java.io.File;
import java.security.SecureRandom;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.time.ZonedDateTime;
import java.util.Base64;

import org.apache.commons.codec.digest.Crypt;
import org.json.JSONObject;

public class MessageDatabase {

    private Connection dbConnection = null;
    private static MessageDatabase msgDb = null;
    private SecureRandom secureRandom = new SecureRandom();

    private MessageDatabase() {
        
    }

    public static synchronized MessageDatabase getInstance() {
        if(null == msgDb) {
            msgDb = new MessageDatabase();
        }
        return msgDb;
    }   

    public void open(String dbName) throws SQLException {
        File path = new File(dbName);
        boolean existed = true;
        if(!path.exists() && !path.isDirectory()) {
            existed = false;
        }
        String dbPath = ("jdbc:sqlite:"+dbName);
        dbConnection = DriverManager.getConnection(dbPath);
        if(existed == false) {
            initializeDatabase();
        }
    }

    private boolean initializeDatabase() throws SQLException {
        System.out.println(dbConnection);
        if(null != dbConnection) {
            String createUserTable = "create table users (username varchar(50) NOT NULL, password varchar(50) NOT NULL, email varchar(50), primary key(username))";
            String createMessageTable = "create table messages (nickname varchar(50) NOT NULL, longitude double(2, 15) NOT NULL, latitude double(2, 15) NOT NULL, sent int NOT NULL, dangertype varchar(50) NOT NULL, phonenumber varchar(50), areacode varchar(50))";
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
        byte bytes[] = new byte[13];
        secureRandom.nextBytes(bytes);
        String saltBytes = new String(Base64.getEncoder().encode(bytes));
        String salt = "$6$" + saltBytes;
        String hashedPassword = Crypt.crypt(user.getString("password"), salt);
        String setUser = "insert into users " + "VALUES('"+user.getString("username") + "','" + hashedPassword + "','" + user.getString("email") + "')";
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

        query = dbConnection.createStatement();
        rs = query.executeQuery(getMessage);

        if(rs.next() == false) {
            System.out.println("Cannot find user");
            return false;
        }else {
            String hashedPassword = rs.getString("password");
            password = Crypt.crypt(password, hashedPassword);
            if(hashedPassword.equals(password)) {
                return true;
            } else {
                return false;
            }
        }
    }

    public void setMessage(WarningMessage msg) throws SQLException {
        String setMsg = "Insert into messages " + "VALUES('"+msg.getNickname() + "','" + msg.getLongitude() + "','" + msg.getLatitude() + "','" + msg.dateAsInt() + "','" + msg.getDangertype() + "','" + msg.getPhonenumber() + "','" + msg.getAreacode() + "')";
        Statement createStatement = dbConnection.createStatement();
        createStatement.executeUpdate(setMsg);
        createStatement.close();
    }

    public JSONObject getMessages() throws SQLException {
        Statement queryStatement = null;
        JSONObject obj = new JSONObject();
        String getMessagesStr = "select * from messages";
        WarningMessage msg = new WarningMessage();
        
        queryStatement = dbConnection.createStatement();
        ResultSet rs = queryStatement.executeQuery(getMessagesStr);

        while(rs.next()) {
            obj.put("nickname", rs.getString("nickname"));
            obj.put("longitude", rs.getDouble("longitude"));
            obj.put("latitude", rs.getDouble("latitude"));
            msg.setSent(rs.getLong("sent"));
            ZonedDateTime zdt = msg.getSent();
            obj.put("sent", zdt);
            obj.put("dangertype", rs.getString("dangertype"));
            if(!rs.getString("phonenumber").equals("null")) {
                obj.put("phonenumber", rs.getString("phonenumber"));
            }
            if(!rs.getString("areacode").equals("null")) {
                obj.put("areacode", rs.getString("areacode"));
            }
        }
        System.out.println(obj);
        return obj;
    }

}
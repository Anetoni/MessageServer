package com.server;

import java.sql.SQLException;

import org.json.JSONException;
import org.json.JSONObject;

import com.sun.net.httpserver.BasicAuthenticator;

public class UserAuthenticator extends BasicAuthenticator{
    private MessageDatabase msgDb = null;

    public UserAuthenticator() {
        super("warning");
        msgDb = MessageDatabase.getInstance();
    }

    /**
     * Checks whether input credentials are valid and correspond with a user stored in the database
     * @param username
     * @param password
     * @return boolean 
     */
    @Override
    public boolean checkCredentials(String username, String password) {
        System.out.println("Checking credentials: " + username + " " + password);

        boolean valid;
        try {
            valid = msgDb.authenticateUser(username, password);
        }catch(SQLException e){
            e.printStackTrace();
            return false;
        }
        return valid;
    }

    /***
     * Adds user in to the database 
     * @param username
     * @param password
     * @param email
     * @return boolean
     * @throws JSONException
     * @throws SQLException
     */
    public boolean addUser(String username, String password, String email) throws JSONException, SQLException {

        boolean result = msgDb.setUser(new JSONObject().put("username", username).put("password", password).put("email", email));
        if(!result) {
            System.out.println("Cannot register user");
            return false;
        }
        System.out.println(username + " registered successfully");

        return true;
    }

} 
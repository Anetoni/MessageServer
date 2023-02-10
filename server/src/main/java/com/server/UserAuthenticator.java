package com.server;

import java.util.Hashtable;
import java.util.Map;
import com.sun.net.httpserver.BasicAuthenticator;

public class UserAuthenticator extends BasicAuthenticator{
    private Map<String, String> users = null;

    public UserAuthenticator() {
        super("warning");
        users = new Hashtable<String, String>();
        users.put("dummy", "passwd");
    }

    @Override
    public boolean checkCredentials(String username, String password) {
        boolean valid = false;
        if(users.containsKey(username)) {
            if(users.get(username).equals(password)) {
                valid = true;
            }
        }
        return valid; 
    }

    public boolean addUser(String username, String password) {
        boolean added = false;
        if(!users.containsKey(username)) {
            users.put(username, password);
            added = true;
        }
        return added;
    }
} 
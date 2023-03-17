package com.server;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.nio.charset.StandardCharsets;
import java.util.stream.Collectors;

import org.json.JSONException;
import org.json.JSONObject;

import com.sun.net.httpserver.*;

public class RegistrationHandler implements HttpHandler {
    private UserAuthenticator userAuthenticator;
    
    public RegistrationHandler(UserAuthenticator userAuthenticator) {
        this.userAuthenticator = userAuthenticator;
    }

    /***
     * Handles registration to the server
     * @param exhange
     * @throws IOException
     */
    @Override
    public void handle(HttpExchange exchange) throws IOException {

        Headers headers = exchange.getRequestHeaders();
        String contentType = "";
        String response = "";
        int code = 200;
        JSONObject obj = null;

        try {
            if(exchange.getRequestMethod().equalsIgnoreCase("POST")) {
                if(headers.containsKey("Content-Type")) {
                    contentType = headers.get("Content-Type").get(0);
                } else {
                    code = 411;
                    response = "No content type in request";
                }
                if(contentType.equalsIgnoreCase("application/json")) {
                    InputStream inputStream = exchange.getRequestBody();
                    String info = new BufferedReader(new InputStreamReader(inputStream, StandardCharsets.UTF_8))
                    .lines().collect(Collectors.joining("\n"));

                    inputStream.close();
                    if(info == null || info.length() == 0) {
                        code = 412;
                        response = "No user credentials";
                    } else {
                        try {
                            obj = new JSONObject(info);
                        }catch(JSONException e) {
                            System.out.println("JSON parse error");
                        }

                        if(obj.getString("username").length() == 0 || obj.getString("password").length() == 0) {
                            code = 413;
                            response = "Invalid user credentials";
                        } else {
                            Boolean result = userAuthenticator.addUser(obj.getString("username"), obj.getString("password"), obj.getString("email"));
                            if(result) {
                                code = 200;
                                response = "User successfully registered";
                            } else {
                                code = 405;
                                response = "User already exists";
                            }
                        }
                    }
                    byte[] bytes = response.getBytes("UTF-8");
                    exchange.sendResponseHeaders(code, bytes.length);
                    OutputStream responseStream = exchange.getResponseBody();
                    responseStream.write(bytes);
                    responseStream.close();
                }
    
            } else {
                response = "Function not supported";
                code = 400;
            }
        } catch(Exception e) {
            System.out.println(e.getStackTrace().toString());
            code = 500;
            response = "Internal server error";
        }
        if(code >= 400) {
            byte[] bytes = response.getBytes("UTF-8");
            exchange.sendResponseHeaders(code, bytes.length);
            OutputStream outputStream = exchange.getResponseBody();
            outputStream.write(bytes);
            outputStream.close();
        }
        
    }
}
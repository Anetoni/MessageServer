package com.server;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.nio.charset.StandardCharsets;
import java.sql.SQLException;
import java.time.DateTimeException;

import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.stream.Collectors;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;


import com.sun.net.httpserver.*;


public class MessageHandler implements HttpHandler  {
    private static ArrayList<WarningMessage> messages = new ArrayList<WarningMessage>();
    private MessageDatabase msgDb = null;

    public MessageHandler() {
        msgDb = MessageDatabase.getInstance();
    }

    /***
     * Handles POST or GET requests to the server regarding sending messages
     * @param exchange POST or GET Request, others are disregarded
     * @throws IOException
     */
    @Override
    public void handle(HttpExchange exchange) throws IOException {
        System.out.println("Request handled in thread " + Thread.currentThread().getId()); 
        int code = 200;
        String response = "";
        WarningMessage warning = null;
        if(exchange.getRequestMethod().equalsIgnoreCase("POST")) {
            InputStream inputStream = exchange.getRequestBody();
            String text = new BufferedReader(new InputStreamReader(inputStream, StandardCharsets.UTF_8))
            .lines().collect(Collectors.joining("\n"));
            inputStream.close();
            
            if(text == null || text.length() == 0) {
                code =412;
                response = "No messages posted";
            } else {
                JSONObject msg = null;
                try {
                    msg = new JSONObject(text);
                }catch (JSONException e) {
                    response = "Json parse error";
                    code = 403;
                }
                if(code == 200) {
                    DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss.SSSX");
                    ZonedDateTime sent = null;
                    try{
                    sent = ZonedDateTime.parse(msg.getString("sent"), formatter);
                    }catch (DateTimeException e) {
                        response = "Invalid time format";
                        code = 413;
                    }
                    Object checkLatitude = msg.get("latitude");
                    Object checkLongitude = msg.get("longitude");
                    if(msg.getString("nickname").length() == 0 || !(checkLatitude instanceof Number) || !(checkLongitude instanceof Number) || msg.getString("dangertype").length() == 0 || sent == null) {
                        code = 413;
                        response = "Invalid warning message";
                    } else {
                        if(!msg.getString("dangertype").equals("Reindeer") && !msg.getString("dangertype").equals("Moose") && !msg.getString("dangertype").equals("Deer") && !msg.getString("dangertype").equals("Other")) {
                            code = 413;
                            response = "Invalid dangertype";
                        } else {
                            if(msg.has("phonenumber") && msg.has("areacode")) {
                                warning = new WarningMessage(msg.getString("nickname"), msg.getDouble("latitude"), msg.getDouble("longitude"), sent, msg.getString("dangertype"), msg.getString("phonenumber"), msg.getString("areacode"));
                            } else if(msg.has("phonenumber") && !msg.has("areacode")) {
                                warning = new WarningMessage(msg.getString("nickname"), msg.getDouble("latitude"), msg.getDouble("longitude"), sent, msg.getString("dangertype"), msg.getString("phonenumber"), null);
                            } else if(msg.has("areacode") && !msg.has("phonenumber")) {
                                warning = new WarningMessage(msg.getString("nickname"), msg.getDouble("latitude"), msg.getDouble("longitude"), sent, msg.getString("dangertype"), null, msg.getString("areacode"));
                            } else {
                                warning = new WarningMessage(msg.getString("nickname"), msg.getDouble("latitude"), msg.getDouble("longitude"), sent, msg.getString("dangertype"), null, null);
                            }
                            messages.add(warning);
                            try {
                                msgDb.setMessage(warning);
                            } catch (SQLException e) {
                                // TODO Auto-generated catch block
                                e.printStackTrace();
                            }
                        }
                    }
                }
            }
        } else if(exchange.getRequestMethod().equalsIgnoreCase("GET")) {
            if(messages.isEmpty()) {
                code = 204;
                exchange.sendResponseHeaders(code, -1);
            } else {
                JSONArray retrievedMessages = new JSONArray();
                try {
                    retrievedMessages.put(msgDb.getMessages());
                } catch (SQLException e) {
                    // TODO Auto-generated catch block
                    e.printStackTrace();
                }
                response = retrievedMessages.toString();
                code = 200;
            }
        } else {
            response = "Function not supported";
            code = 400;
        }
        byte[] bytes = response.getBytes("UTF-8");
        exchange.sendResponseHeaders(code, bytes.length);
        OutputStream outputStream = exchange.getResponseBody();
        outputStream.write(bytes);
        outputStream.close();
    }
}

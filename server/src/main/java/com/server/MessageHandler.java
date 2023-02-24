package com.server;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.nio.charset.StandardCharsets;
import java.sql.SQLException;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.stream.Collectors;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.InputStream;

import org.json.JSONObject;

import com.sun.net.httpserver.*;


public class MessageHandler implements HttpHandler  {
    private static ArrayList<WarningMessage> messages = new ArrayList<WarningMessage>();
    private MessageDatabase msgDb = null;

    public MessageHandler() {
        msgDb = MessageDatabase.getInstance();
    }

    @Override
    public void handle(HttpExchange exchange) throws IOException {
        int code = 200;
        String response = "";
        JSONObject msg = null;
        if(exchange.getRequestMethod().equalsIgnoreCase("POST")) {
            InputStream inputStream = exchange.getRequestBody();
            String text = new BufferedReader(new InputStreamReader(inputStream, StandardCharsets.UTF_8))
            .lines().collect(Collectors.joining("\n"));
            inputStream.close();
            ZonedDateTime now = ZonedDateTime.now(ZoneId.of("UTC"));
            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MMdd'T'HH:mm:ss.SSSX");
            String dateText = now.format(formatter);
            LocalDateTime sent = LocalDateTime.parse(dateText, formatter);
            if(text == null || text.length() == 0) {
                code =412;
                response = "No messages posted";
            } else {
                try {
                    msg = new JSONObject(text);
                }catch (JSONException e) {
                    System.out.println("Json parse error");
                }
                if(msg.getString("nickname").length() == 0 || msg.getString("latitude").length() == 0 || msg.getString("longitude").length() == 0 || msg.getString("dangertype").length() == 0 || msg.getString("sent").length() == 0) {
                    code = 413;
                    response = "Invalid warning message";
                } else {
                    WarningMessage warning = new WarningMessage(msg.getString("nickname"), msg.getDouble("latitude"), msg.getDouble("longitude"), sent, msg.getString("dangertype"));
                    messages.add(warning);
                    try {
                        msgDb.setMessage(warning);
                    } catch (SQLException e) {
                        // TODO Auto-generated catch block
                        e.printStackTrace();
                    }
                }
            }
        } else if(exchange.getRequestMethod().equalsIgnoreCase("GET")) {
            if(messages.isEmpty()) {
                code = 204;
                exchange.sendResponseHeaders(code, -1);
            } else {
                JSONArray retrievedMessages = new JSONArray();
                for(int i = 0; i < messages.size(); i++) {
                    JSONObject message = new JSONObject(messages.get(i));
                    retrievedMessages.put(message);
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

package com.server;

import com.sun.net.httpserver.*;

import java.io.IOException;
import java.io.InputStream;
import java.net.InetSocketAddress;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.stream.Collectors;
import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.OutputStream;


public class Server implements HttpHandler {
    private static ArrayList<String> msgList = new ArrayList<String>();
    
    private Server() {

    }

    @Override
    public void handle(HttpExchange exchange) throws IOException {
        if(exchange.getRequestMethod().equalsIgnoreCase("POST")) {
            InputStream inputStream = exchange.getRequestBody();
            String text = new BufferedReader(new InputStreamReader(inputStream, StandardCharsets.UTF_8))
            .lines().collect(Collectors.joining("\n"));

            msgList.add(text);
            exchange.sendResponseHeaders(200, -1);
            inputStream.close();
        } else if(exchange.getRequestMethod().equalsIgnoreCase("GET")) {
            String responseString = null;
            for(int i = 0; i < msgList.size(); i++) {
                responseString = msgList.get(i);
            }
            byte [] bytes = responseString.getBytes("UTF-8");
            exchange.sendResponseHeaders(200, bytes.length);

            OutputStream outputStream = exchange.getResponseBody();
            outputStream.write(responseString.getBytes());

            outputStream.flush();
            outputStream.close();
        } else {
            String response = "Function not supported";
            byte[] bytes = response.getBytes("UTF-8");
            exchange.sendResponseHeaders(400, bytes.length);
            OutputStream outputStream = exchange.getResponseBody();
            outputStream.write(bytes);
            outputStream.close();
        }

    }

    public static void main(String[] args) throws Exception {
        //create the http server to port 8001 with default logger
        HttpServer server = HttpServer.create(new InetSocketAddress(8001),0);
        //create context that defines path for the resource, in this case a "warning"
        server.createContext("/warning", new Server());
        // creates a default executor
        server.setExecutor(null); 
        server.start(); 
    }
}
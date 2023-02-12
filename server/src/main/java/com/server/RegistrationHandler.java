package com.server;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.nio.charset.StandardCharsets;
import java.util.stream.Collectors;

import com.sun.net.httpserver.*;

public class RegistrationHandler implements HttpHandler {
    private UserAuthenticator userAuthenticator;
    
    public RegistrationHandler(UserAuthenticator userAuthenticator) {
        this.userAuthenticator = userAuthenticator;
    }

    @Override
    public void handle(HttpExchange exchange) throws IOException {
        if(exchange.getRequestMethod().equalsIgnoreCase("POST")) {
            InputStream inputStream = exchange.getRequestBody();
            String info = new BufferedReader(new InputStreamReader(inputStream, StandardCharsets.UTF_8))
            .lines().collect(Collectors.joining("\n"));

            String[] infoArray = info.split(":");
            System.out.println(infoArray.length);
            if(infoArray.length != 2) {
                exchange.sendResponseHeaders(400, 0);
                OutputStream errorOutput = exchange.getResponseBody();
                errorOutput.write("Invalid output".getBytes(StandardCharsets.UTF_8));

                errorOutput.close();
                inputStream.close();
            } else {
                boolean added = userAuthenticator.addUser(infoArray[0], infoArray[1]);
                if(!added) {
                    String msg = "User already registered";
                    byte[] bytes = msg.getBytes("UTF-8"); 
                    exchange.sendResponseHeaders(403, bytes.length);
                    OutputStream errorOutput = exchange.getResponseBody();
                    errorOutput.write(bytes);

                    errorOutput.close();
                } else {
                    String confirmation = "Registration complete";
                    byte[] bytes = confirmation.getBytes("UTF-8");
                    exchange.sendResponseHeaders(200, bytes.length);
                    OutputStream confirmationOutput = exchange.getResponseBody();
                    confirmationOutput.write(bytes);
                    inputStream.close(); 
                    confirmationOutput.close();
                }
            }
        } else {
            String response = "Function not supported";
            byte[] bytes = response.getBytes("UTF-8");
            exchange.sendResponseHeaders(400, bytes.length);
            OutputStream outputStream = exchange.getResponseBody();
            outputStream.write(bytes);
            outputStream.close();
        }
    }
}
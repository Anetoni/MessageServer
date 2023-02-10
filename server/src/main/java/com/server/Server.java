package com.server;

import com.sun.net.httpserver.*;

import java.io.IOException;
import java.io.InputStream;
import java.net.InetSocketAddress;
import java.nio.charset.StandardCharsets;
import java.security.KeyStore;
import java.util.ArrayList;
import java.util.stream.Collectors;

import javax.net.ssl.KeyManagerFactory;
import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLParameters;
import javax.net.ssl.TrustManagerFactory;

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;


public class Server implements HttpHandler {
    private static ArrayList<String> msgList = new ArrayList<String>();
    
    private Server() {

    }

    public static void main(String[] args) throws Exception {
        try {
            //create the http server to port 8001 with default logger
            HttpsServer server = HttpsServer.create(new InetSocketAddress(8001),0);
            SSLContext sslContext = serverSSLContext(args[0], args[1]);
            server.setHttpsConfigurator (new HttpsConfigurator(sslContext) {
                public void configure (HttpsParameters params) {
                InetSocketAddress remote = params.getClientAddress();
                SSLContext c = getSSLContext();
                SSLParameters sslparams = c.getDefaultSSLParameters();
                params.setSSLParameters(sslparams);
                }
            });
            UserAuthenticator userAuthenticator = new UserAuthenticator();
            //create context that defines path for the resource, in this case a "warning"
            HttpContext context = server.createContext("/warning", new Server());
            HttpContext regContext = server.createContext("/registration", new RegistrationHandler(userAuthenticator));
            context.setAuthenticator(userAuthenticator);
            // creates a default executor
            server.setExecutor(null); 
            server.start(); 
        } catch (Exception e) {
            e.printStackTrace();
        }
        
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
            //If there are no messages
            if(responseString == null) {
                responseString = "No messages";
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

    private static SSLContext serverSSLContext(String file, String passw) throws Exception {
        char[] passphrase = passw.toCharArray();
        KeyStore ks = KeyStore.getInstance("JKS");
        ks.load(new FileInputStream(file), passphrase);

        KeyManagerFactory kmf = KeyManagerFactory.getInstance("SunX509");
        kmf.init(ks, passphrase);

        TrustManagerFactory tmf = TrustManagerFactory.getInstance("SunX509");
        tmf.init(ks);

        SSLContext ssl = SSLContext.getInstance("TLS");
        ssl.init(kmf.getKeyManagers(), tmf.getTrustManagers(), null);

        return ssl;
    }

}
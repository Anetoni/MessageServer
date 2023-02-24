package com.server;

import com.sun.net.httpserver.*;

import java.net.InetSocketAddress;
import java.security.KeyStore;
import javax.net.ssl.KeyManagerFactory;
import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLParameters;
import javax.net.ssl.TrustManagerFactory;

import java.io.FileInputStream;


public class Server {

    private Server() {

    }

    public static void main(String[] args) throws Exception {
        try {
            //create the http server to port 8001 with default logger
            HttpsServer server = HttpsServer.create(new InetSocketAddress(8001),0);
            SSLContext sslContext = serverSSLContext();
            server.setHttpsConfigurator (new HttpsConfigurator(sslContext) {
                public void configure (HttpsParameters params) {
                InetSocketAddress remote = params.getClientAddress();
                SSLContext c = getSSLContext();
                SSLParameters sslparams = c.getDefaultSSLParameters();
                params.setSSLParameters(sslparams);
                }
            });
            //Create instances of database and userAuthenticator
            MessageDatabase msgDb = MessageDatabase.getInstance();
            UserAuthenticator userAuthenticator = new UserAuthenticator();
            //create or open connection to database
            msgDb.open("MessageDB");
            //create context that defines path for the resource, in this case a "warning"
            HttpContext context = server.createContext("/warning", new MessageHandler());
            context.setAuthenticator(userAuthenticator);
            HttpContext regContext = server.createContext("/registration", new RegistrationHandler(userAuthenticator));
            // creates a default executor
            server.setExecutor(null); 
            server.start(); 
        } catch (Exception e) {
            e.printStackTrace();
        }
        
    }

    private static SSLContext serverSSLContext() throws Exception {
        char[] passphrase = "tirppis123".toCharArray();
        KeyStore ks = KeyStore.getInstance("JKS");
        ks.load(new FileInputStream("keystore.jks"), passphrase);

        KeyManagerFactory kmf = KeyManagerFactory.getInstance("SunX509");
        kmf.init(ks, passphrase);

        TrustManagerFactory tmf = TrustManagerFactory.getInstance("SunX509");
        tmf.init(ks);

        SSLContext ssl = SSLContext.getInstance("TLS");
        ssl.init(kmf.getKeyManagers(), tmf.getTrustManagers(), null);

        return ssl;
    }

}
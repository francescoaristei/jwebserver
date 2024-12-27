package org.example;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

// HTTP/1.1 protocol: https://www.w3.org/Protocols/rfc2616/rfc2616.txt
public class HttpServer {
    private static final Logger logger = LogManager.getLogger(HttpServer.class);
    public static void main(String[] args) {
        try (ServerSocket serverSocket = new ServerSocket(80)) {
            serveRequest(serverSocket);
        } catch (IOException e) {
            logger.error("Error creating the server: {}", e.getMessage());
        }
    }

    private static void serveRequest (ServerSocket serverSocket){
        while (true) {
            try {
                // listen continuously for connections
                Socket server = serverSocket.accept();
                Thread thread = new Thread(new SocketHandler(server));
                thread.start();
            } catch (IOException e) {
                logger.error("Error accepting connection: {}", e.getMessage());
            }
        }
    }
}
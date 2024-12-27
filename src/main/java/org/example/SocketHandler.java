package org.example;

import org.apache.logging.log4j.Level;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.example.http.HttpRequestHandler;
import org.example.http.HttpRequestParser;
import java.io.IOException;
import java.net.Socket;

public class SocketHandler implements Runnable{
    private final Socket socket;
    private static final Logger logger = LogManager.getLogger(SocketHandler.class);

    public SocketHandler(Socket socket) {
        this.socket = socket;
    }

    @Override
    public void run() {
        try {
            logger.log(Level.INFO,"Thread number: {} running", Thread.currentThread().getId());
            handleRequest();
            // closes associated input/output streams as well
            socket.close();
        } catch (IOException e) {
            logger.error("Error in input socket stream: {}", e.getMessage());
        }
    }

    private void handleRequest() throws IOException {
        new HttpRequestHandler(socket, new HttpRequestParser(socket.getInputStream())).generateHttpResponse();
    }
}

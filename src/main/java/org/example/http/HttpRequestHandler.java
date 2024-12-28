package org.example.http;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.example.http.errors.InvalidHttpHeaderException;
import org.example.http.errors.InvalidHttpRequestException;
import org.example.http.errors.InvalidHttpRequestLineException;
import org.example.http.util.HttpResponseBuilder;
import org.example.http.util.HttpValidator;
import org.example.http.util.ResourceManager;
import java.io.*;
import java.net.Socket;
import java.util.Map;

import static org.example.http.HttpConfiguration.ALLOWED_CONTENT_TYPES;

public class HttpRequestHandler {
    private final Socket socket;
    private StringBuilder httpResponse;
    private final HttpRequestParser httpRequestParser;
    private static final Logger logger = LogManager.getLogger(HttpRequestHandler.class);

    public HttpRequestHandler(Socket socket, HttpRequestParser httpRequestParser) {
        this.socket = socket;
        this.httpRequestParser = httpRequestParser;
        this.httpResponse = new StringBuilder();
    }

    public void generateHttpResponse(){
        try(
            BufferedWriter bufferedWriter = new BufferedWriter(new OutputStreamWriter(socket.getOutputStream()))
        ) {
            HttpRequestLine httpRequestLine = this.httpRequestParser.parseHttpRequestLine();
            Map<String, String> httpRequestHeaders = this.httpRequestParser.parseHttpRequestHeaders();
            try {
                // handling just GET and POST requests for simplicity
                switch(httpRequestLine.getHttpMethod()) {
                    case "GET" -> handleGetRequest(httpRequestLine, httpRequestHeaders);
                    case "POST" -> handlePostRequest(httpRequestLine, httpRequestHeaders);
                    default -> this.httpResponse = HttpResponseBuilder
                            .generateHttpErrorResponse(
                                    httpRequestLine.getHttpVersion(),
                                    HttpResponseStatus.METHOD_NOT_ALLOWED
                                );
                }
            } catch (Exception e) {
                logger.error("Error generating http response: {}", e.getMessage());
                this.httpResponse = HttpResponseBuilder
                        .generateHttpErrorResponse(
                                httpRequestLine.getHttpVersion(),
                                HttpResponseStatus.INTERNAL_SERVER_ERROR
                        );
            }
            bufferedWriter.write(String.valueOf(this.httpResponse));
            bufferedWriter.flush();
        } catch (IOException e) {
            logger.error("Error in output socket stream: {}", e.getMessage());
        } catch (InvalidHttpRequestException e) {
            logger.error("Error in HTTP request line or header: {}", e.getMessage());
        }
    }

    private void handlePostRequest(HttpRequestLine httpRequestLine, Map<String, String> httpRequestHeaders) throws IOException, InvalidHttpRequestException {
        if (HttpValidator.isNotValidHost(httpRequestHeaders.get("host"))) {
            throw new InvalidHttpHeaderException("Host provided not valid or missing.");
        }
        try {
            StringBuilder body = this.httpRequestParser.parseHttpRequestBody();
            String resourcePath = httpRequestLine.getUriPath();
            if (HttpValidator.isValidContentType(httpRequestHeaders.get("content-type"))) {
                ResourceManager.createResource(resourcePath, body);
                this.httpResponse = HttpResponseBuilder
                        .generateHttpPostResponse(
                                httpRequestLine.getHttpVersion(),
                                HttpResponseStatus.CREATED,
                                body
                        );
            } else {
                this.httpResponse = HttpResponseBuilder
                        .generateHttpErrorResponse(
                                httpRequestLine.getHttpVersion(),
                                HttpResponseStatus.UNSUPPORTED_MEDIA_TYPE
                        );
            }
        } catch (InvalidHttpRequestException e) {
            this.httpResponse = HttpResponseBuilder
                    .generateHttpErrorResponse(
                            httpRequestLine.getHttpVersion(),
                            HttpResponseStatus.BAD_REQUEST
                    );
        }
    }

    private void handleGetRequest(HttpRequestLine httpRequestLine, Map<String, String> httpRequestHeaders) throws InvalidHttpRequestException {
        if (HttpValidator.isNotValidHost(httpRequestHeaders.get("host"))) {
            throw new InvalidHttpRequestLineException("Host provided not valid or missing.");
        }
        String httpPath = httpRequestLine.getUriPath();
        StringBuilder fileContent = new StringBuilder();
        try(BufferedReader bufferedReader = ResourceManager.getResourceContent(httpPath)) {
            String line;
            while((line = bufferedReader.readLine()) != null) {
                fileContent.append(line);
                fileContent.append("\n");
            }
            httpPath = httpPath.equals("/") ? "index.html": httpPath;
            this.httpResponse = HttpResponseBuilder
                    .generateHttpGetResponse(
                            httpRequestLine.getHttpVersion(),
                            HttpResponseStatus.OK,
                            fileContent,
                            ALLOWED_CONTENT_TYPES.get(ResourceManager.getFileExtensions(httpPath))
                    );
        } catch(IOException e) {
            logger.error("URI path not valid: no such file or directory.");
            this.httpResponse = HttpResponseBuilder
                    .generateHttpErrorResponse(
                            httpRequestLine.getHttpVersion(),
                            HttpResponseStatus.NOT_FOUND
                    );
        } catch(InvalidHttpRequestLineException e) {
            logger.error("Tried to access a non authorized resource.");
            this.httpResponse = HttpResponseBuilder
                    .generateHttpErrorResponse(
                            httpRequestLine.getHttpVersion(),
                            HttpResponseStatus.FORBIDDEN
                    );
        }
    }
}
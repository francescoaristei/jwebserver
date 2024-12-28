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
import static org.example.http.HttpConfiguration.DEFAULT_HTTP_VERSION;

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
            try {
                HttpRequestLine httpRequestLine = this.httpRequestParser.parseHttpRequestLine();
                Map<String, String> httpRequestHeaders = this.httpRequestParser.parseHttpRequestHeaders();
                handleRequests(httpRequestLine, httpRequestHeaders);
            } catch (InvalidHttpRequestException e) {
                generateErrorResponse(e.getMessage(), DEFAULT_HTTP_VERSION, HttpResponseStatus.BAD_REQUEST);
            } catch (Exception e) {
                generateErrorResponse(e.getMessage(), DEFAULT_HTTP_VERSION, HttpResponseStatus.INTERNAL_SERVER_ERROR);
            }
            bufferedWriter.write(String.valueOf(this.httpResponse));
            bufferedWriter.flush();
        } catch (IOException e) {
            logger.error("Error in output socket stream: {}", e.getMessage());
        }
    }

    private void handleRequests(HttpRequestLine httpRequestLine, Map<String, String> httpRequestHeaders) throws InvalidHttpRequestException, IOException {
        checkHttpVersionAndHost(httpRequestLine, httpRequestHeaders);
        // handling just GET and POST requests for simplicity
        switch(httpRequestLine.getHttpMethod()) {
            case "GET" -> handleGetRequest(httpRequestLine);
            case "POST" -> handlePostRequest(httpRequestLine, httpRequestHeaders);
            default -> generateErrorResponse("Method not allowed", httpRequestLine.getHttpVersion(), HttpResponseStatus.METHOD_NOT_ALLOWED);
        }
    }

    private void handlePostRequest(HttpRequestLine httpRequestLine, Map<String, String> httpRequestHeaders) throws IOException, InvalidHttpRequestException {
        StringBuilder body = this.httpRequestParser.parseHttpRequestBody();
        if (body == null) {
            throw new InvalidHttpHeaderException("Both content-length or transfer-encoding missing.");
        }
        String resourcePath = httpRequestLine.getUriPath();
        if (HttpValidator.isValidContentType(httpRequestHeaders.get("content-type"))) {
            try {
                ResourceManager.createResource(resourcePath, body);
                generatePostResponse(httpRequestLine.getHttpVersion(), body);
            } catch (IOException e) {
                generateErrorResponse(e.getMessage(), httpRequestLine.getHttpVersion(), HttpResponseStatus.NOT_FOUND);
            } catch (InvalidHttpRequestLineException e) {
                generateErrorResponse(e.getMessage(), httpRequestLine.getHttpVersion(), HttpResponseStatus.FORBIDDEN);
            }
        } else {
            generateErrorResponse("Unsupported media type", httpRequestLine.getHttpVersion(), HttpResponseStatus.UNSUPPORTED_MEDIA_TYPE);
        }
    }

    private void handleGetRequest(HttpRequestLine httpRequestLine) {
        String httpPath = httpRequestLine.getUriPath();
        StringBuilder fileContent = new StringBuilder();
        try (BufferedReader bufferedReader = ResourceManager.getResourceContent(httpPath)) {
            String line;
            while ((line = bufferedReader.readLine()) != null) {
                fileContent.append(line);
                fileContent.append("\n");
            }
            httpPath = httpPath.equals("/") ? "index.html" : httpPath;
            generateGetResponse(httpRequestLine.getHttpVersion(), fileContent, ALLOWED_CONTENT_TYPES.get(ResourceManager.getFileExtensions(httpPath)));
        } catch (IOException e) {
            generateErrorResponse(e.getMessage(), httpRequestLine.getHttpVersion(), HttpResponseStatus.NOT_FOUND);
        } catch (InvalidHttpRequestLineException e) {
            generateErrorResponse(e.getMessage(), httpRequestLine.getHttpVersion(), HttpResponseStatus.FORBIDDEN);
        }
    }

    private void generateErrorResponse(String log, String httpVersion, HttpResponseStatus status) {
        logger.error("Error generating http response: {}", log);
        this.httpResponse = HttpResponseBuilder
                .generateHttpErrorResponse(
                        httpVersion,
                        status
                );
    }

    private void generatePostResponse(String httpVersion, StringBuilder body) {
        this.httpResponse = HttpResponseBuilder
                .generateHttpPostResponse(
                        httpVersion,
                        HttpResponseStatus.CREATED,
                        body
                );
    }

    private void generateGetResponse(String httpVersion, StringBuilder fileContent, String contentType) {
        this.httpResponse = HttpResponseBuilder
                .generateHttpGetResponse(
                        httpVersion,
                        HttpResponseStatus.OK,
                        fileContent,
                        contentType
                );
    }

    private void checkHttpVersionAndHost(HttpRequestLine httpRequestLine, Map<String, String> httpRequestHeaders) throws InvalidHttpRequestException{
        if (!(HttpValidator.isValidHttpVersion(httpRequestLine.getHttpVersion()))) {
            throw new InvalidHttpRequestLineException("HTTP version is not correct.");
        }
        if (HttpValidator.isNotValidHost(httpRequestHeaders.get("host"))) {
            throw new InvalidHttpHeaderException("Host provided not valid or missing.");
        }
    }
}
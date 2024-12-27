package org.example.http;

import org.example.http.errors.InvalidHttpBodyException;
import org.example.http.errors.InvalidHttpHeaderException;
import org.example.http.errors.InvalidHttpRequestException;
import org.example.http.errors.InvalidHttpRequestLineException;
import org.example.http.util.HttpValidator;

import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

public class HttpRequestParser {
    private final HttpRequestLine httpRequestLine;
    private final Map<String, String> httpRequestHeaders;
    private final StringBuilder httpRequestBody;
    private final InputStream inputStream;
    private final StringBuilder httpRequestLineStrings;
    private final ArrayList<String> httpRequestHeadersStrings;

    public HttpRequestParser(InputStream inputStream) {
        this.inputStream = inputStream;
        this.httpRequestBody = new StringBuilder();
        this.httpRequestLine = new HttpRequestLine();
        this.httpRequestHeaders = new HashMap<>();
        this.httpRequestLineStrings = new StringBuilder();
        this.httpRequestHeadersStrings = new ArrayList<>();
    }

    // to check if content-length is correct and if transfer-encoding = chunked (only type allowed)
    public StringBuilder parseHttpRequestBody() throws InvalidHttpRequestException, IOException {
        if (this.httpRequestHeaders.containsKey("content-length")) {
            this.readHttpBodyWithLength(this.httpRequestHeaders.get("content-length"));
        } else if(this.httpRequestHeaders.containsKey("transfer-encoding")) {
            this.readHttpChunkedBody();
        } else {
            throw new InvalidHttpHeaderException("Both content-length or transfer-encoding missing.");
        }
        return this.httpRequestBody;
    }

    public HttpRequestLine parseHttpRequestLine() throws InvalidHttpRequestLineException, IOException {
        this.readHttpRequestLine();
        String requestLine = String.valueOf(this.httpRequestLineStrings);
        String[] requestLineComponents = requestLine.split("\\s+");
        if (!(requestLineComponents.length == 3)) {
            throw new InvalidHttpRequestLineException("HTTP method, URI and HTTP version must be provided.");
        }
        if (!(HttpValidator.isValidHttpVersion(requestLineComponents[2]))) {
            throw new InvalidHttpRequestLineException("HTTP version is not correct.");
        }
        String httpMethod = requestLineComponents[0];
        String uriPath = requestLineComponents[1];
        String httpVersion = requestLineComponents[2];
        this.httpRequestLine.setHttpMethod(httpMethod);
        this.httpRequestLine.setUriPath(uriPath);
        this.httpRequestLine.setHttpVersion(httpVersion);
        return this.httpRequestLine;
    }

    public Map<String, String> parseHttpRequestHeaders() throws InvalidHttpRequestException, IOException {
        this.readHttpHeaders();
        if (!this.httpRequestHeadersStrings.isEmpty()) {
            for (String header : this.httpRequestHeadersStrings) {
                String[] headerComponent = header.split(":\\s?", 2);
                this.httpRequestHeaders.put(headerComponent[0].toLowerCase(), headerComponent[1]);
            }
        }
        return this.httpRequestHeaders;
    }

    private void readHttpBodyWithLength(String contentLength) throws IOException {
        int intContentLength = Integer.parseInt(contentLength);
        byte[] bytes = new byte[intContentLength];
        int offset = 0;
        while (offset < intContentLength) {
            int actuallyRead = this.inputStream.read(bytes, offset, intContentLength - offset);
            if (actuallyRead < 0) {
                byte[] shortResult = new byte[offset];
                System.arraycopy(bytes, 0, shortResult, 0, offset);
                bytes = shortResult;
                break;
            }
            offset += actuallyRead;
        }
        this.httpRequestBody.append(new String(bytes, StandardCharsets.UTF_8));
    }

    // first chunk size, then chunk body finished by CRLF (with size matching chunk size), then again...
    private void readHttpChunkedBody() throws IOException, InvalidHttpBodyException {
        int chunkSize = 1;
        while (chunkSize > 0) {
            chunkSize = readChunkSize();
            if (chunkSize < 0) {
                throw new InvalidHttpBodyException("unexpected EOF, could not read chunked body.");
            }
            this.httpRequestBody.append(readChunk(chunkSize));
        }
    }

    private int readChunkSize() throws IOException, InvalidHttpBodyException {
        char[] chars = new char[4];
        int byteValue;
        int size = 0;
        while((byteValue = this.inputStream.read()) >= 0 && size < 4) {
            if (byteValue == '\r') {
                int next = this.inputStream.read();
                if (next == '\n') {
                    break;
                } else {
                    throw new InvalidHttpBodyException("Illegal character after return (chunk-size).");
                }
            } if (byteValue == '\n') {
                throw new InvalidHttpBodyException("Illegal character after chunk-size: new line without return.");
            }
            chars[size++] = (char) byteValue;
        }
        if (size == 4) {
            if (byteValue == '\r') {
                int next = this.inputStream.read();
                if (next != '\n') {
                    throw new InvalidHttpBodyException("Illegal character after return (chunk-size).");
                }
            } else if(byteValue == '\n') {
                throw new InvalidHttpBodyException("Illegal character after chunk-size: new line without return.");
            } else {
                throw new InvalidHttpBodyException("Invalid chunk-size (too big, more than 4 hex-digits.)");
            }
        }
        return Integer.parseInt(new String(chars, 0, size), 16);
    }

    private String readChunk(int chunkSize) throws IOException, InvalidHttpBodyException {
        byte[] data = new byte[chunkSize];

        if(chunkSize > 0) {
            int bytesRead;
            int totalBytesRead = 0;
            while((bytesRead = this.inputStream.read(data, totalBytesRead, chunkSize - totalBytesRead)) >= 0) {
                totalBytesRead += bytesRead;
                if (totalBytesRead == chunkSize) {
                    break;
                }
            }
            if (totalBytesRead < chunkSize) {
                throw new InvalidHttpBodyException("Unexpected EOF while reading chunk data.");
            }

            // consume \r\n
            int byteValue = this.inputStream.read();
            if (byteValue == '\r') {
                int next = this.inputStream.read();
                if (next != '\n') {
                    throw new InvalidHttpBodyException("Illegal character after return (chunk-size).");
                }
            } else if (byteValue == '\n') {
                throw new InvalidHttpBodyException("Illegal character after chunk-size: new line without return.");
            } else {
                throw new InvalidHttpBodyException("Illegal character after chunk data (missing CRLF)");
            }
        }
        return new String(data, StandardCharsets.UTF_8);
    }

    private void readHttpRequestLine() throws IOException, InvalidHttpRequestLineException {
        int byteValue;
        while ((byteValue = this.inputStream.read()) >= 0) {
            if (byteValue == '\r') {
                int next = this.inputStream.read();
                if (next < 0 || next == '\n') {
                    break;
                } else {
                    throw new InvalidHttpRequestLineException("Illegal character after return");
                }
            } else if (byteValue == '\n'){
                throw new InvalidHttpRequestLineException("Illegal character after return");
            } else {
                this.httpRequestLineStrings.append((char) byteValue);
            }
        }
    }

    // do not allow new line without preceding '\r'
    private void readHttpHeaders() throws IOException, InvalidHttpRequestException {
        StringBuilder headersBuilder = new StringBuilder();
        boolean wasNewLine = true;
        int byteValue;
        while ((byteValue = this.inputStream.read()) >= 0) {
            if (byteValue == '\r') {
                int next = this.inputStream.read();
                if (next < 0 || next == '\n') {
                    if (wasNewLine) break;
                    this.httpRequestHeadersStrings.add(headersBuilder.toString());
                    if (next < 0) break;
                    headersBuilder = new StringBuilder();
                    wasNewLine = true;
                } else {
                    throw new InvalidHttpRequestException("Illegal character after return.");
                }
            } else if (byteValue == '\n') { // new line without preceding '\r'
                throw new InvalidHttpRequestException("Illegal character after return.");
            } else {
                headersBuilder.append((char) byteValue);
                wasNewLine = false;
            }
        }
        if (!headersBuilder.isEmpty()) {
            this.httpRequestHeadersStrings.add(headersBuilder.toString());
        }
    }
}

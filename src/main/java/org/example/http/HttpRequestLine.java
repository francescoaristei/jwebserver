package org.example.http;

public class HttpRequestLine {
    private String httpVersion;
    private String httpMethod;
    private String uriPath;

    public HttpRequestLine() {}

    public HttpRequestLine(
            String httpVersion,
            String httpMethod,
            String uriPath) {
        this.httpVersion = httpVersion;
        this.httpMethod = httpMethod;
        this.uriPath = uriPath;
    }

    public void setHttpVersion(String httpVersion) {
        this.httpVersion = httpVersion;
    }

    public void setHttpMethod(String httpMethod) {
        this.httpMethod = httpMethod;
    }

    public void setUriPath(String uriPath) {
        this.uriPath = uriPath;
    }

    public String getHttpVersion() {
        return this.httpVersion;
    }

    public String getHttpMethod() {
        return this.httpMethod;
    }

    public String getUriPath() {
        return this.uriPath;
    }
}

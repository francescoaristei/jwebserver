package org.example.http;

import java.util.List;

public class HttpConfiguration {
    public static final List<String> ALLOWED_HTTP_VERSIONS = List.of(
            "HTTP/0.9", "HTTP/1.0", "HTTP/1.1"
    );
    public static final List<String> ALLOWED_HOSTS = List.of(
            "localhost", "127.0.0.1"
    );
    public static final List<String> ALLOWED_CONTENT_TYPES = List.of(
            "application/json", "text/html"
    );
    public static final String DEFAULT_HTTP_VERSION = "HTTP/1.1";

    private HttpConfiguration(){};
}
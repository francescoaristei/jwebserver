package org.example.http.util;

import static org.example.http.HttpConfiguration.*;

public class HttpValidator {
    public static boolean isNotValidHost(String host) {
        return host == null || !ALLOWED_HOSTS.contains(host);
    }

    public static boolean isValidContentType(String contentType) {
        return ALLOWED_CONTENT_TYPES.contains(contentType);
    }

    public static boolean isValidHttpVersion(String httpVersion) {
        return ALLOWED_HTTP_VERSIONS.contains(httpVersion);
    }
}

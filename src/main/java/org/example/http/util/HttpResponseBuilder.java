package org.example.http.util;

import org.example.http.HttpResponseStatus;
import static org.example.http.HttpConfiguration.ALLOWED_HTTP_VERSIONS;
import static org.example.http.HttpConfiguration.DEFAULT_HTTP_VERSION;

public class HttpResponseBuilder {
    public static StringBuilder generateHttpErrorResponse(
            String httpVersion,
            HttpResponseStatus status
    ) {
        String validatedHttpVersion = validateHttpVersion(httpVersion);
        return new StringBuilder()
                .append(validatedHttpVersion)
                .append(" ")
                .append(status.getStatus())
                .append(" ")
                .append(status.getMessage())
                .append("\r\n\r\n");
    }

    public static StringBuilder generateHttpGetResponse(
            String httpVersion,
            HttpResponseStatus status,
            StringBuilder fileContent,
            String fileExtension
    ){
        String validatedHttpVersion = validateHttpVersion(httpVersion);
        return new StringBuilder()
                .append(validatedHttpVersion)
                .append(" ")
                .append(status.getStatus())
                .append(" ")
                .append(status.getMessage())
                .append("\r\n")
                .append("Content-type: ")
                .append(fileExtension)
                .append("\r\n\r\n")
                .append(fileContent);
    }

    public static StringBuilder generateHttpPostResponse(
            String httpVersion,
            HttpResponseStatus status,
            StringBuilder fileContent
    ){
        String validatedHttpVersion = validateHttpVersion(httpVersion);
        return new StringBuilder()
                .append(validatedHttpVersion)
                .append(" ")
                .append(status.getStatus())
                .append(" ")
                .append(status.getMessage())
                .append("\r\n\r\n")
                .append(fileContent);
    }

    private static String validateHttpVersion(String httpVersion) {
        return ALLOWED_HTTP_VERSIONS.contains(httpVersion) ? httpVersion: DEFAULT_HTTP_VERSION;
    }
}

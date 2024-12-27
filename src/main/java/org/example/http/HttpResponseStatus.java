package org.example.http;

public enum HttpResponseStatus {
    OK(200, "OK"),
    NOT_FOUND(404, "Not Found"),
    INTERNAL_SERVER_ERROR(500, "Internal Server Error"),
    METHOD_NOT_ALLOWED(402, "Method Not Allowed"),
    FORBIDDEN(403, "Forbidden"),
    BAD_REQUEST(400, "Bad Request"),
    CREATED(201, "Created"),
    UNSUPPORTED_MEDIA_TYPE(415, "Unsupported Media Type: Content-Type not supported")
    ;

    private final int status;
    private final String message;

    HttpResponseStatus(int status, String message) {
        this.status = status;
        this.message = message;
    }

    public int getStatus() {
        return this.status;
    }

    public String getMessage() {
        return this.message;
    }
}

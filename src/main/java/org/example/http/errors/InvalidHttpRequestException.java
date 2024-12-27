package org.example.http.errors;

public class InvalidHttpRequestException extends Exception {
    public InvalidHttpRequestException(String errorMessage) {
        super(errorMessage);
    }
}

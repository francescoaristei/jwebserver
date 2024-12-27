package org.example.http.errors;

public class InvalidHttpRequestLineException extends InvalidHttpRequestException{
    public InvalidHttpRequestLineException(String errorMessage) {
        super(errorMessage);
    }
}

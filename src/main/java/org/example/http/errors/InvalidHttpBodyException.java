package org.example.http.errors;

public class InvalidHttpBodyException extends InvalidHttpRequestException{
    public InvalidHttpBodyException(String errorMessage) {
        super(errorMessage);
    }
}

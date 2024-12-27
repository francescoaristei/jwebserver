package org.example.http.errors;

public class InvalidHttpHeaderException extends InvalidHttpRequestException{
    public InvalidHttpHeaderException(String errorMessage) {
        super(errorMessage);
    }
}

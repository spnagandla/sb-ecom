package com.buyology.backend.exception;

public class APIException extends RuntimeException{
    public APIException() {
    }

    public APIException(String message) {
        super(message);
    }
}

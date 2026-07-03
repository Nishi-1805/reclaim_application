package com.cdac.exception;

public class InvalidClaimException extends RuntimeException {

    public InvalidClaimException(String message) {
        super(message);
    }
}
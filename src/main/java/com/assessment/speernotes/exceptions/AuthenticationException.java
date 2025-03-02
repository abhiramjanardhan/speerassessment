package com.assessment.speernotes.exceptions;

public class AuthenticationException extends RuntimeException {
    public AuthenticationException() {
        super("Authentication Failure! Please try again!");
    }
}

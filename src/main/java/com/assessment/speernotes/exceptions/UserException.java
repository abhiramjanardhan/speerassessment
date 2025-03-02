package com.assessment.speernotes.exceptions;

public class UserException extends RuntimeException {
    public UserException(String email) {
        super("Invalid user for the email " + email + ". Please try again!");
    }

    public UserException(String email, String message) {
        super("Invalid user for the email " + email + ". Please try again! Reason: " + message);
    }
}

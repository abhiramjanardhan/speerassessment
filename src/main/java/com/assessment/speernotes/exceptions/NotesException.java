package com.assessment.speernotes.exceptions;

public class NotesException extends RuntimeException {
    public NotesException(String message) {
        super("Invalid note. Please try again! Reason: " + message);
    }
}

package com.assessment.speernotes.controller;

import com.assessment.speernotes.exceptions.AuthenticationException;
import com.assessment.speernotes.exceptions.NotesException;
import com.assessment.speernotes.exceptions.UserException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ProblemDetail;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.context.request.WebRequest;
import org.springframework.web.servlet.mvc.method.annotation.ResponseEntityExceptionHandler;

@RestControllerAdvice
@Slf4j
public class GlobalExceptionHandler extends ResponseEntityExceptionHandler {

    /**
     * This method is used to handle the generic exception thrown from the application
     *
     * @param ex
     * @param request
     * @return ResponseEntity<Object>
     */
    @ExceptionHandler(Exception.class)
    public final ResponseEntity<Object> handleNoSuchElementException(Exception ex, WebRequest request) {
        log.error("Caught Exception", ex);
        ProblemDetail pd = ProblemDetail.forStatusAndDetail(HttpStatus.INTERNAL_SERVER_ERROR, ex.getMessage());
        return createResponseEntity(pd, null, HttpStatus.INTERNAL_SERVER_ERROR, request);
    }

    /**
     * This method is used to handle the user exception thrown from the application
     *
     * @param ex
     * @param request
     * @return ResponseEntity<Object>
     */
    @ExceptionHandler(UserException.class)
    public ResponseEntity<Object> handleUserNotFoundException(UserException ex, WebRequest request) {
        log.error("Caught User Exception", ex);
        ProblemDetail pd = ProblemDetail.forStatusAndDetail(HttpStatus.BAD_REQUEST, ex.getMessage());
        return createResponseEntity(pd, null, HttpStatus.BAD_REQUEST, request);
    }

    /**
     * This method is used to handle the notes exception thrown from the application
     *
     * @param ex
     * @param request
     * @return ResponseEntity<Object>
     */
    @ExceptionHandler(NotesException.class)
    public ResponseEntity<Object> handleNotesException(NotesException ex, WebRequest request) {
        log.error("Caught Notes Exception", ex);
        ProblemDetail pd = ProblemDetail.forStatusAndDetail(HttpStatus.BAD_REQUEST, ex.getMessage());
        return createResponseEntity(pd, null, HttpStatus.BAD_REQUEST, request);
    }

    /**
     * This method is used to handle the authentication exception thrown from the application
     *
     * @param ex
     * @param request
     * @return ResponseEntity<Object>
     */
    @ExceptionHandler(AuthenticationException.class)
    public ResponseEntity<Object> handleAuthenticationException(AuthenticationException ex, WebRequest request) {
        log.error("Caught Authentication Exception", ex);
        ProblemDetail pd = ProblemDetail.forStatusAndDetail(HttpStatus.NOT_FOUND, ex.getMessage());
        return createResponseEntity(pd, null, HttpStatus.NOT_FOUND, request);
    }
}

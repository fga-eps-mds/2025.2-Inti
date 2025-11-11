package br.mds.inti.controller.exception;

import java.time.Instant;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;

import br.mds.inti.service.exceptions.UsernameAlreadyExistsException;
import br.mds.inti.service.exceptions.ProfileNotFoundException;
import jakarta.servlet.http.HttpServletRequest;

@ControllerAdvice
public class ResourceExceptionHandler {

    @ExceptionHandler(ProfileNotFoundException.class)
    public ResponseEntity<StandardError> profileNotFound(ProfileNotFoundException e, HttpServletRequest request) {

        String error = "profile not found";
        HttpStatus status = HttpStatus.NOT_FOUND;
        StandardError err = new StandardError(Instant.now(), status.value(), error, e.getMessage(),
                request.getRequestURI());

        return ResponseEntity.status(status).body(err);
    }

    @ExceptionHandler(UsernameAlreadyExistsException.class)
    public ResponseEntity<StandardError> usernameAlreadyExists(UsernameAlreadyExistsException e, HttpServletRequest request) {

        String error = "username already exists";
        HttpStatus status = HttpStatus.CONFLICT;
        StandardError err = new StandardError(Instant.now(), status.value(), error, e.getMessage(),
                request.getRequestURI());

        return ResponseEntity.status(status).body(err);
    }
}